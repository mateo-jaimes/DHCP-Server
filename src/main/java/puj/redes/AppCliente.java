package puj.redes;

import puj.redes.guia.DHCPClient;
import puj.redes.guia.DHCPMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class AppCliente {
    private static final int MAX_BUFFER_SIZE = 1024;
    private static int listenPort =  68;
    private static String serverIP = "127.0.0.1";
    private static final int serverPort = 67;

    public static int index;
    public static byte[] mensaje = new byte[5000];

    public static void main(String[] args) {
        System.out.println("Connecting to DHCPServer at " + serverIP + " on port " + serverPort + "...");
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket(listenPort);

            byte[] IPprueba = {5, 5, 5, 5};

            DHCPOpciones op = new DHCPOpciones();

            DHCPMensaje mensajeDHCP = new DHCPMensaje(
                    (byte)1,
                    (byte)1,
                    (byte)15,
                    (byte)15,
                    (byte)5,
                    (short)5,
                    (short)2,
                    IPprueba,
                    IPprueba,
                    IPprueba,
                    IPprueba,
                    IPprueba,
                    IPprueba,
                    IPprueba,
                    op);

            mensajeToBytes(mensajeDHCP);

            DatagramPacket datagramPacket = new DatagramPacket(mensaje, mensaje.length, InetAddress.getByName(serverIP), serverPort);

            socket.send(datagramPacket);
            System.out.println(mensajeDHCP.toString());

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void agregarBytesMensaje(byte[] bytes) {
        System.arraycopy(bytes, 0, mensaje, index, bytes.length);
        index += bytes.length;
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
        agregarBytesMensaje(mensajeDHCP.getChaddr());
        agregarBytesMensaje(mensajeDHCP.getSname());
        agregarBytesMensaje(mensajeDHCP.getFile());
    }
}
