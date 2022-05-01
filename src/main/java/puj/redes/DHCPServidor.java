package puj.redes;

import puj.redes.Registros.ManejadorRegistros;
import puj.redes.Registros.Registro;
import puj.redes.Subredes.ManejadorSubredes;
import puj.redes.Subredes.Subred;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class DHCPServidor {

    private static final int MAX_BUFFER_SIZE = 65535;

    private static final int serverPort = 67;
    private static InetAddress IPServidor;
    private static final int clientPort = 68;

    private static byte[] respuestaClienteBytes = new byte[1000];
    private static Integer indexRespuestaMensaje = 0;
    private static DatagramSocket datagramSocket = null;

    public DHCPServidor() {

        try {
            IPServidor = InetAddress.getLocalHost();
            ManejadorSubredes servidorOpciones = new ManejadorSubredes();
            ManejadorRegistros.cargarRegistros();
            datagramSocket = new DatagramSocket(serverPort);

            byte[] buffer = new byte[MAX_BUFFER_SIZE];

            DatagramPacket datagramPacket = null;
            System.out.println("[!] Servidor DHCP escuchando en el puerto [" + serverPort + "].");

            while (true) {
                datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);

                System.out.println("[!] Mensaje recibido desde " + datagramPacket.getAddress());
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

    private static void procesarMensaje(DHCPMensaje mensaje) throws Exception {
        // Guardar en el archivo la solicitud.

        TipoMensajeDHCP tipoMensajeDHCP = TipoMensajeDHCP.INVALID, tipoRespuestaDHCP = TipoMensajeDHCP.INVALID;
        byte[] IPsolicitada, IPservidor, subnet, hostname = new byte[0];

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

        switch (tipoMensajeDHCP) {
            case DHCPDISCOVER:
                tipoRespuestaDHCP = TipoMensajeDHCP.DHCPOFFER;
                break;
            default:
                return;
        }

        responderCliente(tipoRespuestaDHCP, mensaje, new String(hostname));
    }
// FILTRO WIRESHARK udp.port == 68
    public static void responderCliente (TipoMensajeDHCP tipoRespuestaDHCP, DHCPMensaje mensajeCliente, String hostname) throws IOException, ParseException {
        InetAddress IPsolicitada;
        Registro registro;
        DHCPMensaje respuestaCliente = null;
        indexRespuestaMensaje = 0;
        ArrayList <DHCPOpciones> opcionesDHCP = new ArrayList<>();

        switch (tipoRespuestaDHCP) {
            case DHCPOFFER:
                registro = ManejadorRegistros.buscarRegistro(mensajeCliente.getChaddr(), mensajeCliente.getHlen());

                if (registro == null) {
                    registro = new Registro(mensajeCliente.getChaddr(), obtenerPrimeraIPLibre(mensajeCliente.getYiaddr()), new Date(), new Date(), hostname); // Revisar dates.
                    ManejadorRegistros.anadirRegistro(registro);
                }

                opcionesDHCP.add(new DHCPOpciones());
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

            case DHCPACK:
                break;
        }
        enviarRespuestaCliente(respuestaCliente, InetAddress.getByName("255.255.255.255")); // poner el destination address...
    }

    private static void enviarRespuestaCliente (DHCPMensaje respuestaCliente, InetAddress IPCliente) throws IOException {
        mensajeToBytes(respuestaCliente);

        System.out.println("[!] Servidor DHCP enviando respuesta al cliente [puerto: " + clientPort + "].");
        DatagramPacket datagramPacket = new DatagramPacket(respuestaClienteBytes, respuestaClienteBytes.length, InetAddress.getByName("255.255.255.255"), clientPort);
        //DatagramPacket datagramPacket = new DatagramPacket(prueba, prueba.length, IPCliente, clientPort);

        datagramSocket.send(datagramPacket);

        System.out.println("[!] Respuesta enviada al cliente " + datagramPacket.getAddress() + " [puerto: " + datagramPacket.getPort() + "]");
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

        //agregarBytesMensaje(mensajeDHCP.getOpciones());
    }

    private static InetAddress obtenerPrimeraIPLibre(byte[] clienteIP) throws UnknownHostException {
        byte[] primera = obtenerSubred(clienteIP).getRangoInicial();
        byte[] ultima = obtenerSubred(clienteIP).getRangoFinal();
        byte[] actual = primera;

        while (!Arrays.equals(actual, ultima)) {
            if (!IPdisponible(actual)) {
                aumentarIP(actual);
            } else return InetAddress.getByAddress(actual);
        }

        return null;
    }

    private static Subred obtenerSubred (byte[] clienteIP) {
        ArrayList <Subred> subredes = ManejadorSubredes.getSubredes();
        for (Subred subred : ManejadorSubredes.getSubredes()) {
            if (Arrays.equals(subred.getPuertaEnlace(), clienteIP))
                return subred;
        }
        return null;
    }

    private static boolean IPdisponible (byte[] IP) {
        for (Registro registro : ManejadorRegistros.getRegistros()) {
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
        for (Subred subred : ManejadorSubredes.getSubredes()) {
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
