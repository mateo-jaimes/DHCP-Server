package puj.redes;

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
    private static final int clientPort = 68;

    private static byte[] respuestaClienteBytes = new byte[1000];
    private static Integer indexRespuestaMensaje = 0;
    private static DatagramSocket datagramSocket = null;

    public DHCPServidor() {

        try {
            ServidorOpciones servidorOpciones = new ServidorOpciones();
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
                System.out.println(mensaje.toString());
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
        byte[] IPsolicitada, IPservidor, subnet, hostname;

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

        responderCliente(tipoRespuestaDHCP, mensaje);
    }
// FILTRO WIRESHARK udp.port == 68
    public static void responderCliente (TipoMensajeDHCP tipoRespuestaDHCP, DHCPMensaje mensajeCliente) throws IOException, ParseException {
        InetAddress IPsolicitada;
        Registro registro;
        DHCPMensaje respuestaCliente = null;
        indexRespuestaMensaje = 0;
        ArrayList <DHCPOpciones> opcionesDHCP = new ArrayList<>();

        switch (tipoRespuestaDHCP) {
            case DHCPOFFER:
                registro = ManejadorRegistros.buscarRegistro(mensajeCliente.getChaddr());

                if (registro == null) {
                    registro = new Registro(mensajeCliente.getChaddr(), generarIP(), new Date(), new Date(), new String(mensajeCliente.getSname(), StandardCharsets.UTF_8)); // Revisar dates.
                    registro.setIP(obtenerPrimeraIPLibre());
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
                        registro.getIP().getAddress(), // ip sv, arreglar.
                        new byte[] {0, 0, 0, 0}, // giaddr
                        mensajeCliente.getChaddr(), // chaddr
                        new byte[] {0}, // sname
                        new byte[] {0}, // sfile
                        opcionesDHCP
                        );
                break;

            case DHCPACK:
                break;
        }
        enviarRespuestaCliente(respuestaCliente, InetAddress.getByName("255.255.255.255"));
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

    private static InetAddress generarIP() {
        InetAddress IP = null;

        return IP;
    }

    private static InetAddress obtenerPrimeraIPLibre() throws UnknownHostException {
        byte[] primera = ServidorOpciones.getOpciones().get(0).getRangoInicial();
        byte[] ultima = ServidorOpciones.getOpciones().get(0).getRangoFinal();
        byte[] actual = primera;

        while (!Arrays.equals(actual, ultima)) {
            if (!IPdisponible(actual)) {
                actual = aumentarIP(actual);
            } else return InetAddress.getByAddress(actual);
        }

        return null;
    }

    private static boolean IPdisponible(byte[] IP) {
        for (Registro registro : ManejadorRegistros.getRegistros()) {
            if (Arrays.equals(IP, registro.getIP().getAddress())) return false;
        }
        return true;
    }

    private static byte[] aumentarIP(byte[] IP) {
        IP[3]++;
        if (IP[3] > (byte)255) {
            IP[3] = 0;
            IP[2]++;
            if (IP[2] > (byte)255) {
                IP[2] = 0;
                IP[1]++;
                if (IP[1] > (byte)255) {
                    IP[1] = 0;
                    IP[0]++;
                }
            }
        }
        return IP;
    }
}
