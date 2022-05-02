package puj.redes;

import puj.redes.Registros.ControladorLog;
import puj.redes.Registros.ControladorRegistros;
import puj.redes.Registros.Registro;
import puj.redes.Registros.ThreadRevocaciones;
import puj.redes.Subredes.ControladorSubredes;
import puj.redes.Subredes.Subred;

import javax.naming.ldap.Control;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;

public class DHCPServidor {

    private static final int MAX_BUFFER_SIZE = 65535;

    private static final int serverPort = 67;
    private static final int clientPort = 68;
    private static final int routePort = 67;

    private static InetAddress IPServidor;
    private static InetAddress serverGateway = null;

    private static byte[] respuestaClienteBytes = new byte[1000];
    private static Integer indexRespuestaMensaje = 0;
    private static DatagramSocket datagramSocket = null;
    private static Thread thread = new ThreadRevocaciones();

    public DHCPServidor() {

        try {
            thread.start();
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

                DHCPMensaje mensaje = new DHCPMensaje(datagramPacket);
                System.out.print("[!] Mensaje recibido desde " + datagramPacket.getAddress() + " [" + DHCPMensaje.printByteArray(mensaje.getChaddr(),2) + "] ");
                //System.out.println(mensaje.toString());
                procesarMensaje(mensaje);
            }

        } catch (Exception e) {
            System.out.println("[!] Ocurrió un error durante el procesamiento del mensaje.");
        }
    }

    private static InetAddress obtenerGateway() throws Exception {
        String gateway;
        Process result = Runtime.getRuntime().exec("netstat -rn");

        BufferedReader output = new BufferedReader(new InputStreamReader(result.getInputStream()));

        String line = output.readLine();
        while (line != null){
            if (line.trim().startsWith("default") || line.trim().startsWith("0.0.0.0"))
                break;
            line = output.readLine();
        }

        if (line == null)
            throw new Exception ("No se encontró el gateway.");

        StringTokenizer st = new StringTokenizer(line);
        st.nextToken();
        st.nextToken();
        gateway = st.nextToken();
        return InetAddress.getByName(gateway);
    }

    private static void procesarMensaje (DHCPMensaje mensaje) throws Exception {
        // Guardar en el archivo la solicitud.
        Registro registro = null;
        TipoMensajeDHCP tipoMensajeDHCP = TipoMensajeDHCP.INVALID, tipoRespuestaDHCP = TipoMensajeDHCP.INVALID;
        try {
            byte[] IPsolicitada = new byte[4], IPservidor = new byte[4], subnet, hostname = new byte[0];

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

            registro = ControladorRegistros.buscarRegistro(mensaje.getChaddr(), mensaje.getHlen());

            switch (tipoMensajeDHCP) {
                case DHCPDISCOVER:
                    tipoRespuestaDHCP = TipoMensajeDHCP.DHCPOFFER;
                    break;

                case DHCPREQUEST:
                    if (!Arrays.equals(mensaje.getCiaddr(), new byte[]{0, 0, 0, 0})) { // renew.
                        Registro reg = ControladorRegistros.buscarRegistro(mensaje.getChaddr(), mensaje.getHlen());
                        if (reg != null)
                            if (!Arrays.equals(reg.getIP().getAddress(), IPsolicitada) && !Arrays.equals(IPsolicitada, new byte[]{0, 0, 0, 0}))
                                break;

                       ControladorRegistros.eliminarRegistro(reg);

                        registro = new Registro(mensaje.getChaddr(), InetAddress.getByAddress(mensaje.getCiaddr()), new Date(), new Date(), new String(hostname));
                        registro.setTiempoRetirar(new Date (System.currentTimeMillis() + obtenerSubredDesdeIP(mensaje.getCiaddr()).getTiempo() * 1000)); // secs to millis
                       ControladorRegistros.anadirRegistro(registro);
                        ControladorLog.escribir(registro.getChaddr(), tipoMensajeDHCP, tipoRespuestaDHCP);
                        tipoRespuestaDHCP = TipoMensajeDHCP.DHCPACK;
                    }
                    if (registro != null) {
                        if (InetAddress.getByAddress(IPservidor).equals(IPServidor) && Arrays.equals(registro.getIP().getAddress(), IPsolicitada)) {
                            tipoRespuestaDHCP = TipoMensajeDHCP.DHCPACK;
                        }
                    }
                    else {
                        tipoRespuestaDHCP = TipoMensajeDHCP.DHCPNAK;
                    }

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

            }

            if (registro != null)
                ControladorLog.escribir(registro.getChaddr(), tipoMensajeDHCP, tipoRespuestaDHCP);

            if (tipoRespuestaDHCP == TipoMensajeDHCP.INVALID)
                return;

            responderCliente(tipoMensajeDHCP, tipoRespuestaDHCP, mensaje, new String(hostname), registro);
        } catch (Exception e) {
            System.out.println("\t-> Error al procesar el mensaje " + tipoMensajeDHCP + ": "+ e.getMessage() + "\n");
            ControladorLog.escribir(mensaje.getChaddr(), tipoMensajeDHCP, e.getMessage());
        }
    }

    public static void responderCliente (TipoMensajeDHCP tipoMensajeDHCP, TipoMensajeDHCP tipoRespuestaDHCP, DHCPMensaje mensajeCliente, String hostname, Registro registro) throws Exception {
        InetAddress IPsolicitada;
        DHCPMensaje respuestaCliente = null;
        indexRespuestaMensaje = 0;
        ArrayList <DHCPOpciones> opcionesDHCP = new ArrayList<>();

        if (registro == null) {
            registro = new Registro(mensajeCliente.getChaddr(), obtenerPrimeraIPLibre(mensajeCliente.getIpsource().getAddress()), new Date(), new Date(), hostname);
            registro.setTiempoRetirar(new Date (System.currentTimeMillis() + obtenerSubred(mensajeCliente.getIpsource().getAddress()).getTiempo())); // secs to millis
            ControladorRegistros.anadirRegistro(registro);
            ControladorLog.escribir(registro.getChaddr(), tipoMensajeDHCP, tipoRespuestaDHCP);
        }

        Subred subred = null;

        try {
            subred = obtenerSubred(mensajeCliente.getIpsource().getAddress());
        } catch (Exception e) {
            subred = obtenerSubredDesdeIP(mensajeCliente.getIpsource().getAddress());
        }

        // Opciones dentro de la respuesta DHCP

        opcionesDHCP.add(new DHCPOpciones((byte) 53, new byte[]{(byte) tipoRespuestaDHCP.ordinal()})); // message type
        opcionesDHCP.add(new DHCPOpciones((byte) 54, IPServidor.getAddress())); // DHCP ip server
        opcionesDHCP.add(new DHCPOpciones((byte) 1, subred.getMascaraSubnet())); // mask subnet
        opcionesDHCP.add(new DHCPOpciones((byte) 3, subred.getPuertaEnlace())); // gateway
        opcionesDHCP.add(new DHCPOpciones((byte) 6, subred.getServidorDNS())); // IP servidor DNS
        opcionesDHCP.add(new DHCPOpciones((byte) 51, intToByte(subred.getTiempo()))); // revisar lease time.
        opcionesDHCP.add(new DHCPOpciones((byte) 59, intToByte((int) (subred.getTiempo() * 0.75)))); // revisar lease time.
        opcionesDHCP.add(new DHCPOpciones((byte) 58, intToByte((int) (subred.getTiempo() * 0.5)))); // revisar lease time.

        respuestaCliente = new DHCPMensaje(
                (byte) 2, // op
                (byte) 1, // htype
                (byte) 6, // hlen
                (byte) 0, // hops
                mensajeCliente.getXid(), // xid
                (short) 0, // secs
                ByteBuffer.wrap(new byte[]{(byte) 128, 0}).getShort(), // flags
                new byte[]{0, 0, 0, 0}, // ciaddr
                registro.getIP().getAddress(), // ip
                IPServidor.getAddress(), // ip sv
                mensajeCliente.getIpsource().getAddress(), // giaddr
                mensajeCliente.getChaddr(), // chaddr
                new byte[]{0}, // sname
                new byte[]{0}, // sfile
                opcionesDHCP
        );

        InetAddress IPrespuesta = null;

        if (mensajeCliente.getIpsource().equals(InetAddress.getByName("0.0.0.0")))
            IPrespuesta = InetAddress.getByName("255.255.255.255");
        else
            IPrespuesta = InetAddress.getByAddress(obtenerSubredDesdeIP(mensajeCliente.getIpsource().getAddress()).getPuertaEnlace());

        System.out.println("IP: " + registro.getIP() + " | MAC: " + DHCPMensaje.printByteArray(registro.getChaddr(), 2) + " | " + registro.getTiempoACK() + " -> " + registro.getTiempoRetirar() + " (" + subred.getTiempo() + " segs).");
        enviarRespuestaCliente(respuestaCliente, IPrespuesta, tipoRespuestaDHCP);
    }

    private static void enviarRespuestaCliente (DHCPMensaje respuestaCliente, InetAddress IPCliente, TipoMensajeDHCP tipoMensajeDHCP) throws IOException {
        mensajeToBytes(respuestaCliente);

        DatagramPacket datagramPacket = null;
        if (Arrays.equals(IPCliente.getAddress(), new byte[] {0, 0, 0, 0}))
            datagramPacket = new DatagramPacket(respuestaClienteBytes, respuestaClienteBytes.length, IPCliente, clientPort);
        else
            datagramPacket = new DatagramPacket(respuestaClienteBytes, respuestaClienteBytes.length, IPCliente, routePort);

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

    private static InetAddress obtenerPrimeraIPLibre(byte[] clienteIP) throws Exception {
        Subred subred = obtenerSubred(clienteIP);
        byte[] actual = subred.getRangoInicial();
        byte[] ultima = subred.getRangoFinal();

        while (!Arrays.equals(actual, ultima))
            if (!IPdisponible(actual) || Arrays.equals(actual, subred.getPuertaEnlace()) || Arrays.equals(actual, subred.getServidorDNS()))
                aumentarIP(actual);
            else
                return InetAddress.getByAddress(actual);

        throw new Exception ("No hay IP's disponibles para esta subred (gateway " + InetAddress.getByAddress(clienteIP) + ").");
    }

    private static Subred obtenerSubredDesdeIP(byte[] IPcliente) throws Exception {
        for (Subred subred : ControladorSubredes.getSubredes()) {
            byte[] actual = subred.getRangoInicial();
            byte[] ultima = subred.getRangoFinal();

            if (Arrays.equals(IPcliente, subred.getPuertaEnlace()))
                return subred;

            while (!Arrays.equals(actual, ultima))
                if (Arrays.equals(actual, IPcliente))
                    return subred;
                else
                    aumentarIP(actual);
        }
        throw new Exception ("La IP " + DHCPMensaje.printByteArray(IPcliente, 1) + " no fue encontrada dentro de ningún pool de direcciones.");
    }

    private static Subred obtenerSubred (byte[] clienteIP) throws Exception {
        if (Arrays.equals(clienteIP, new byte[] {0, 0, 0, 0}))
            clienteIP = serverGateway.getAddress();

        for (Subred subred : ControladorSubredes.getSubredes()) {
            if (Arrays.equals(subred.getPuertaEnlace(), clienteIP))
                return subred;
        }

        throw new Exception ("No existe la subred dentro del archivo de configuraciones (gateway " + InetAddress.getByAddress(clienteIP) + ").");
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
}
