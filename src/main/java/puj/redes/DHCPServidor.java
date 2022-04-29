package puj.redes;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DHCPServidor {

    private static final int MAX_BUFFER_SIZE = 65535;
    private static final int serverPort = 67;
    private static ServidorOpciones servidorOpciones;

    public DHCPServidor() {
        try {
            DatagramSocket datagramSocket = null;
            servidorOpciones = new ServidorOpciones();
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

        TipoMensajeDHCP tipoMensajeDHCP = null;
        byte[] IPsolicitada, IPservidor, subnet, nombreCliente;

        for (DHCPOpciones opcion : mensaje.getOpcionesDHCP()) {
            switch (opcion.getType()) {
                case 1:
                    subnet = opcion.getValue();
                    break;

                case 12:
                    nombreCliente = opcion.getValue();
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

                default:
                    tipoMensajeDHCP = TipoMensajeDHCP.INVALID;
            }
        }

        switch (tipoMensajeDHCP) {
            case DHCPDISCOVER:

                break;
        }

        // Asignar IPS
        // Responder
        mensaje.toString();
        return;
    }
}
