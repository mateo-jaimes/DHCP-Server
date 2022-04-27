package puj.redes;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;

public class ServidorOpciones {

    private ArrayList<Opcion> opciones = new ArrayList<>();

    public ServidorOpciones () throws Exception {
            FileInputStream fstream = new FileInputStream(System.getProperty("user.dir") + "/src/main/java/puj/redes/config");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

            Opcion nuevaOpcion = new Opcion();

            do {
                nuevaOpcion.setRangoInicial(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setRangoFinal(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setIP(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setMascaraSubnet(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setPuertaEnlace(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setServidorDNS(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setTiempo(Integer.parseInt(obtenerValorConfig(bufferedReader)));
                this.opciones.add(nuevaOpcion);
            } while (bufferedReader.readLine() != null);
            
            in.close();
            System.out.println("[!] Configuración cargada.");

    }

    static String obtenerValorConfig(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.indexOf('=') > 0)
                return line.substring(line.indexOf('=') + 1).trim();
        }
        return null;
    }

    private class Opcion {
        private byte[] rangoInicial;
        private byte[] rangoFinal;
        private byte[] IP;
        private byte[] mascaraSubnet;
        private byte[] puertaEnlace;
        private byte[] servidorDNS;
        private Integer tiempo;

        public Integer getTiempo() {
            return tiempo;
        }

        public void setTiempo(Integer tiempo) {
            this.tiempo = tiempo;
        }

        public byte[] getRangoInicial() {
            return rangoInicial;
        }

        public void setRangoInicial(byte[] rangoInicial) {
            this.rangoInicial = rangoInicial;
        }

        public byte[] getRangoFinal() {
            return rangoFinal;
        }

        public void setRangoFinal(byte[] rangoFinal) {
            this.rangoFinal = rangoFinal;
        }

        public byte[] getIP() {
            return IP;
        }

        public void setIP(byte[] IP) {
            this.IP = IP;
        }

        public byte[] getMascaraSubnet() {
            return mascaraSubnet;
        }

        public void setMascaraSubnet(byte[] mascaraSubnet) {
            this.mascaraSubnet = mascaraSubnet;
        }

        public byte[] getPuertaEnlace() {
            return puertaEnlace;
        }

        public void setPuertaEnlace(byte[] puertaEnlace) {
            this.puertaEnlace = puertaEnlace;
        }

        public byte[] getServidorDNS() {
            return servidorDNS;
        }
        public void setServidorDNS(byte[] servidorDNS) {
            this.servidorDNS = servidorDNS;
        }

    }

    public ArrayList<Opcion> getOpciones() {
        return opciones;
    }

    public void setOpciones(ArrayList<Opcion> opciones) {
        this.opciones = opciones;
    }
}
