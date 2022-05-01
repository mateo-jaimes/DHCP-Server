package puj.redes.Registros;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class ControladorRegistros {
    private static final String pathArchivo = System.getProperty("user.dir") + "/src/main/java/puj/redes/registro.txt";
    public static ArrayList<Registro> registros = new ArrayList<>();

    public static void cargarRegistros() throws FileNotFoundException, ParseException, UnknownHostException {
        actualizarRegistros();
        System.out.println("\t-> " + registros.size() + " cargados desde el archivo registro.txt\n");
    }

    public static void actualizarRegistros() throws FileNotFoundException, UnknownHostException, ParseException {
        InputStream ins = new FileInputStream(pathArchivo);
        Scanner sc = new Scanner(ins);

        while (sc.hasNextLine()) {
            String linea = sc.nextLine();
            String[] lineas = linea.split(", ");
            Registro registro = new Registro();
            byte[] buffer = new byte[6];

            String[] macStr = lineas[0].split(":");

            for (int i = 0; i < macStr.length; i++) {
                int value = Integer.parseInt(macStr[i], 16);
                buffer[i] = (byte) value;
            }

            registro.setChaddr(buffer);
            registro.setIP(InetAddress.getByName(lineas[1]));
            registro.setTiempoAcuse(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(lineas[2]));
            registro.setTiempoAsignado(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(lineas[3]));

            try {
                registro.setHostname(lineas[4]);
            } catch (Exception e) {
                registro.setHostname("");
            }

            if (!registros.contains(registro))
                registros.add(registro);
        }
    }

    public static Registro buscarRegistro(byte[] chaddr, int Hlen) throws FileNotFoundException, ParseException, UnknownHostException {
        byte[] chaddrSinPadding = new byte [Hlen];
        System.arraycopy(chaddr, 0, chaddrSinPadding, 0, Hlen);

        for (Registro registro : registros)
            if (Arrays.equals(registro.getChaddr(), chaddrSinPadding))
                return registro;

        return null;
    }

    public static void anadirRegistro(Registro registro) throws IOException {
        registros.add(registro);
        escribirRegistros();
    }

    public static void eliminarRegistro(Registro registro) throws IOException {
        registros.remove(registro);
        escribirRegistros();
    }

    public static void escribirRegistros () throws IOException {
        Writer fw = new FileWriter(pathArchivo, false);
        for (Registro registro : registros)
            fw.write(registro.toString());
        fw.close();
    }

    public static ArrayList<Registro> getRegistros() {
        return registros;
    }

    public static void setRegistros(ArrayList<Registro> registros) {
        ControladorRegistros.registros = registros;
    }
}
