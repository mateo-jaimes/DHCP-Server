package puj.redes;

import puj.redes.Registros.ControladorRegistros;
import puj.redes.Registros.Registro;
import puj.redes.Subredes.ControladorSubredes;
import puj.redes.Subredes.Subred;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;

public class DHCPServidor {

    private static final int MAX_BUFFER_SIZE = 65535;

    private static final int serverPort = 67;
    private static InetAddress IPServidor;
    private static final int clientPort = 68;
    private static InetAddress serverGateway = null;

    private static byte[] respuestaClienteBytes = new byte[1000];
    private static Integer indexRespuestaMensaje = 0;
    private static DatagramSocket datagramSocket = null;

    public DHCPServidor() {
        try {
            IPServidor = InetAddress.getLocalHost();
            serverGateway = obtenerGateway();
            System.out.println(serverPort + " - gateway: " + serverGateway);

            ControladorSubredes servidorOpciones = new ControladorSubredes();
            ControladorRegistros.cargarRegistros();
            datagramSocket = new DatagramSocket(serverPort);

            byte[] buffer = new byte[MAX_BUFFER_SIZE];

            DatagramPacket datagramPacket = null;

            while (true) {
                datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);

                System.out.print("[!] Mensaje recibido desde " + datagramPacket.getAddress() + "\t");
                DHCPMensaje mensaje = new DHCPMensaje(datagramPacket);
                //System.out.println(mensaje.toString());
                procesarMensaje(mensaje);

                // Se recibe un mensaje DHCP...
            }
        } catch (Exception e) {
            // Hacer manejo de excepcion...
            e.printStackTrace();
        }
    }

    private static InetAddress obtenerGateway() throws IOException {
        String gateway;
        Process result = Runtime.getRuntime().exec("netstat -rn");

        BufferedReader output = new BufferedReader(new InputStreamReader(result.getInputStream()));

        String line = output.readLine();
        while (line != null){
            if (line.trim().startsWith("default") || line.trim().startsWith("0.0.0.0"))
                break;
            line = output.readLine();
        }

        if (line==null) // gateway not found;
            return null;

        StringTokenizer st = new StringTokenizer( line );
        st.nextToken();
        st.nextToken();
        gateway = st.nextToken();
        return InetAddress.getByName(gateway);
    }

    private static void procesarMensaje(DHCPMensaje mensaje) throws Exception {
        // Guardar en el archivo la solicitud.

        TipoMensajeDHCP tipoMensajeDHCP = TipoMensajeDHCP.INVALID, tipoRespuestaDHCP = TipoMensajeDHCP.INVALID;
        byte[] IPsolicitada, IPservidor = new byte[4], subnet, hostname = new byte[0];

        for (DHCPOpciones opcion : mensaje.getOpcionesDHCP()) {
            switch (opcion.getType()) {
                case 1:
                    subnet = opcion.getValue();
                    break;

                case 12:
                    hostname = opcion.getValue();
                    break;

                case 50:
                    IPsolicitada = opcion.getValue();
                    break;

                case 53:
                    tipoMensajeDHCP = TipoMensajeDHCP.values()[opcion.getValue()[0]];
                    System.out.println("-> " + tipoMensajeDHCP);
                    break;

                case 54:
                    IPservidor = opcion.getValue();
                    break;
            }
        }

        Registro registro = ControladorRegistros.buscarRegistro(mensaje.getChaddr(), mensaje.getHlen());

        switch (tipoMensajeDHCP) {
            case DHCPDISCOVER:
                tipoRespuestaDHCP = TipoMensajeDHCP.DHCPOFFER;
                break;

            case DHCPREQUEST:
                //System.out.println(Arrays.toString(IPservidor) + "-" + Arrays.toString(IPServidor.getAddress()));
                if (InetAddress.getByAddress(IPservidor).equals(IPServidor))
                    tipoRespuestaDHCP = TipoMensajeDHCP.DHCPACK;
                else
                    tipoRespuestaDHCP = TipoMensajeDHCP.DHCPNAK;

                break;

            case DHCPDECLINE:
                break;

            case DHCPRELEASE:
                if (registro != null)
                    ControladorRegistros.eliminarRegistro(registro);
                break;

            case DHCPINFORM:
                if (registro != null)
                    tipoRespuestaDHCP = TipoMensajeDHCP.DHCPACK;
                break;

            default:
                return;
        }

        responderCliente(tipoRespuestaDHCP, mensaje, new String(hostname), registro);
    }
// FILTRO WIRESHARK udp.port == 68
    public static void responderCliente (TipoMensajeDHCP tipoRespuestaDHCP, DHCPMensaje mensajeCliente, String hostname, Registro registro) throws IOException, ParseException {
        InetAddress IPsolicitada;
        DHCPMensaje respuestaCliente = null;
        indexRespuestaMensaje = 0;
        ArrayList <DHCPOpciones> opcionesDHCP = new ArrayList<>();

        switch (tipoRespuestaDHCP) { // verificar si se puede quitar el switch, por el ordinal del enum.
            case DHCPNAK:
                return;
            case DHCPOFFER:
                if (registro == null) {
                    registro = new Registro(mensajeCliente.getChaddr(), obtenerPrimeraIPLibre(mensajeCliente.getYiaddr()), new Date(), new Date(), hostname); // Revisar dates.
                    ControladorRegistros.anadirRegistro(registro);
                }

                Subred subred = obtenerSubred(mensajeCliente.getYiaddr());

                // Opciones dentro del DHCP OFFER

                opcionesDHCP.add(new DHCPOpciones((byte)53, new byte[]{(byte) tipoRespuestaDHCP.ordinal()})); // message type
                opcionesDHCP.add(new DHCPOpciones((byte)54, IPServidor.getAddress())); // DHCP ip server
                opcionesDHCP.add(new DHCPOpciones((byte)1, subred.getMascaraSubnet())); // mask subnet
                opcionesDHCP.add(new DHCPOpciones((byte)3, subred.getPuertaEnlace())); // gateway
                opcionesDHCP.add(new DHCPOpciones((byte)6, subred.getServidorDNS())); // IP servidor DNS
                opcionesDHCP.add(new DHCPOpciones((byte)51, intToByte(subred.getTiempo()))); // revisar lease time.
                opcionesDHCP.add(new DHCPOpciones((byte)59, intToByte((int) (subred.getTiempo() * 0.75)))); // revisar lease time.
                opcionesDHCP.add(new DHCPOpciones((byte)58, intToByte((int) (subred.getTiempo() * 0.5)))); // revisar lease time.

                respuestaCliente = new DHCPMensaje (
                        (byte)2, // op
                        (byte)1, // htype
                        (byte)6, // hlen
                        (byte)0, // hops
                        mensajeCliente.getXid(), // xid
                        (short)0, // secs
                        ByteBuffer.wrap(new byte[] {(byte) 128, 0}).getShort(), // flags
                        new byte[] {0, 0, 0, 0}, // ciaddr
                        registro.getIP().getAddress(), // ip
                        IPServidor.getAddress(), // ip sv
                        new byte[] {0, 0, 0, 0}, // giaddr
                        mensajeCliente.getChaddr(), // chaddr
                        new byte[] {0}, // sname
                        new byte[] {0}, // sfile
                        opcionesDHCP
                        );

                System.out.println("IP " + registro.getIP() + " asignada a la MAC " + DHCPMensaje.printByteArray(registro.getChaddr(),2));
                break;
        }
        enviarRespuestaCliente(respuestaCliente, InetAddress.getByName("255.255.255.255"), tipoRespuestaDHCP); // poner el destination address...
    }

    private static void enviarRespuestaCliente (DHCPMensaje respuestaCliente, InetAddress IPCliente, TipoMensajeDHCP tipoMensajeDHCP) throws IOException {
        mensajeToBytes(respuestaCliente);

        DatagramPacket datagramPacket = new DatagramPacket(respuestaClienteBytes, respuestaClienteBytes.length, IPCliente, clientPort);

        datagramSocket.send(datagramPacket);

        System.out.println("\t-> " + tipoMensajeDHCP.name() + " enviada a " + datagramPacket.getAddress() + "\n");
    }

    public static void agregarBytesMensaje(byte[] bytes) {
        System.arraycopy(bytes, 0, respuestaClienteBytes, indexRespuestaMensaje, bytes.length);
        indexRespuestaMensaje += bytes.length;
    }

    public static byte[] intToByte (Integer valor) {
        return new byte[]{ (byte)(valor >>> 24), (byte)(valor >> 16 & 0xff), (byte)(valor >> 8 & 0xff), (byte)(valor & 0xff)};
    }

    public static byte[] shortToByte (Short valor) {
        return new byte[] { (byte)(valor >> 8 & 0xff), (byte)(valor & 0xff)};
    }

    public static void mensajeToBytes(DHCPMensaje mensajeDHCP) {
        agregarBytesMensaje(new byte[] {mensajeDHCP.getOp()});
        agregarBytesMensaje(new byte[] {mensajeDHCP.getHtype()});
        agregarBytesMensaje(new byte[] {mensajeDHCP.getHlen()});
        agregarBytesMensaje(new byte[] {mensajeDHCP.getHops()});
        agregarBytesMensaje(intToByte(mensajeDHCP.getXid()));
        agregarBytesMensaje(shortToByte(mensajeDHCP.getSecs()));
        agregarBytesMensaje(shortToByte(mensajeDHCP.getFlags()));
        agregarBytesMensaje(mensajeDHCP.getCiaddr());
        agregarBytesMensaje(mensajeDHCP.getYiaddr());
        agregarBytesMensaje(mensajeDHCP.getSiaddr());
        agregarBytesMensaje(mensajeDHCP.getGiaddr());

        agregarBytesMensaje(mensajeDHCP.getChaddr()); // padding chaddr (16 - 10bytes MAC addr)
        for (int i = mensajeDHCP.getChaddr().length; i < 16; i++)
            agregarBytesMensaje(new byte[]{0});

        agregarBytesMensaje(mensajeDHCP.getSname()); // padding sname
        for (int i = mensajeDHCP.getSname().length; i < 64; i++)
            agregarBytesMensaje(new byte[]{0});

        agregarBytesMensaje(mensajeDHCP.getFile()); // padding file
        for (int i = mensajeDHCP.getFile().length; i < 128; i++)
            agregarBytesMensaje(new byte[]{0});

        agregarBytesMensaje(intToByte(0x63825363)); // magic cookie

        for (DHCPOpciones opcion : mensajeDHCP.getOpcionesDHCP()) {
            agregarBytesMensaje(new byte[] {opcion.getType()});
            agregarBytesMensaje(new byte[] {opcion.getLength()});
            agregarBytesMensaje(opcion.getValue());
        }

        agregarBytesMensaje(new byte[] {(byte)255});
    }

    private static InetAddress obtenerPrimeraIPLibre(byte[] clienteIP) throws UnknownHostException {
        byte[] actual = obtenerSubred(clienteIP).getRangoInicial();
        byte[] ultima = obtenerSubred(clienteIP).getRangoFinal();

        while (!Arrays.equals(actual, ultima))
            if (!IPdisponible(actual))
                aumentarIP(actual);
            else
                return InetAddress.getByAddress(actual);


        return null;
    }

    private static Subred obtenerSubred (byte[] clienteIP) {
        if (Arrays.equals(clienteIP, new byte[] {0, 0, 0, 0}))
            clienteIP = serverGateway.getAddress();

        for (Subred subred : ControladorSubredes.getSubredes()) {
            if (Arrays.equals(subred.getPuertaEnlace(), clienteIP))
                return subred;
        }
        return null;
    }

    private static boolean IPdisponible (byte[] IP) {
        for (Registro registro : ControladorRegistros.getRegistros()) {
            if (Arrays.equals(IP, registro.getIP().getAddress())) return false;
        }
        return true;
    }

    private static void aumentarIP(byte[] IP) {
        IP[3]++;
        if (IP[3] == 0) {
            IP[2]++;
            if (IP[2] == 0) {
                IP[1]++;
                if (IP[1] == 0) {
                    IP[0]++;
                }
            }
        }
    }

    private static Subred obtenerSubredDesdeIP (byte[] IP) {
        byte[] actual;
        for (Subred subred : ControladorSubredes.getSubredes()) {
            actual = subred.getRangoInicial();

            while (!Arrays.equals(actual, subred.getRangoFinal())) {
                if (!IPdisponible(actual)) {
                    aumentarIP(actual);
                } else return new Subred(); // arreglar.
            }
        }
        return null;
    }

    /*
    private static int compararIP(byte[] IP1, byte[] IP2)
    {
        for (int i = 0; i < 4; i++)
            if (byteToInt(ip1[i]) > byteToInt(ip2[i]))
                return 1;
            else if (ByteBuffer.wrap(IP1[i]).getInt() < byteToInt(ip2[i]))
                return -1;
        return 0;
    }
     */
}
