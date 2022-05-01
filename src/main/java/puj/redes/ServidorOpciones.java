package puj.redes;

import puj.redes.guia.Opcion;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;

public class ServidorOpciones {

    private static ArrayList<Opcion> opciones = new ArrayList<>();

    public ServidorOpciones () throws Exception {
            FileInputStream fstream = new FileInputStream(System.getProperty("user.dir") + "/src/main/java/puj/redes/config");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

            Opcion nuevaOpcion = new Opcion();

            do {
                nuevaOpcion.setRangoInicial(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setRangoFinal(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setMascaraSubnet(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setPuertaEnlace(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setServidorDNS(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setTiempo(Integer.parseInt(obtenerValorConfig(bufferedReader)));
                opciones.add(nuevaOpcion);
            } while (bufferedReader.readLine() != null);

            in.close();
            System.out.println("[!] Configuración cargada (" + opciones.size() + " subredes).");
    }

    static String obtenerValorConfig(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.indexOf('=') > 0)
                return line.substring(line.indexOf('=') + 1).trim();
        }
        return null;
    }

    public static ArrayList<Opcion> getOpciones() {
        return opciones;
    }

    public void setOpciones(ArrayList<Opcion> opciones) {
        this.opciones = opciones;
    }
}
