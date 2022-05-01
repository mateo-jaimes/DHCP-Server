package puj.redes.Subredes;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;

public class ControladorSubredes {

    private static ArrayList<Subred> subredes = new ArrayList<>();

    public ControladorSubredes() throws Exception {
            FileInputStream fstream = new FileInputStream(System.getProperty("user.dir") + "/src/main/java/puj/redes/config");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

            do {
                Subred nuevaOpcion = new Subred();
                nuevaOpcion.setRangoInicial(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setRangoFinal(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setMascaraSubnet(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setPuertaEnlace(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setServidorDNS(InetAddress.getByName(obtenerValorConfig(bufferedReader)).getAddress());
                nuevaOpcion.setTiempo(Integer.parseInt(obtenerValorConfig(bufferedReader)));
                subredes.add(nuevaOpcion);
            } while (bufferedReader.readLine() != null);

            in.close();
            System.out.println("\t-> Configuración cargada (" + subredes.size() + " subredes).");
    }

    static String obtenerValorConfig(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.indexOf('=') > 0)
                return line.substring(line.indexOf('=') + 1).trim();
        }
        return null;
    }

    public static ArrayList<Subred> getSubredes() {
        return subredes;
    }

    public void setSubredes(ArrayList<Subred> subredes) {
        ControladorSubredes.subredes = subredes;
    }
}
