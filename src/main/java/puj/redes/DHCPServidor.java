package puj.redes;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DHCPServidor {
    private static final int MAX_BUFFER_SIZE = 65535;
    private static final int serverPort = 67;

    public DHCPServidor() {
        DatagramSocket datagramSocket = null;
        try {
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

    private static void procesarMensaje(DHCPMensaje mensaje) {
        mensaje.toString();
        return;
    }
}
