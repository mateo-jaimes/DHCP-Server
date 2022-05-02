package puj.redes.Registros;

import puj.redes.DHCPMensaje;
import puj.redes.TipoMensajeDHCP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

public class ControladorLog {

    private static final String pathLog =  System.getProperty("user.dir") + "/src/main/java/puj/redes/log.txt";
    private static Writer fw;

    public static void escribir(byte[] chaddr, TipoMensajeDHCP tipoMensajeDHCP, String error) throws IOException {
        nuevoRegistroLog(chaddr, tipoMensajeDHCP);
        fw.write("Error: " + error);
        cerrarLog();
    }

    public static void escribir(byte[] chaddr, TipoMensajeDHCP tipoMensajeDHCP, TipoMensajeDHCP tipoRespuestaDHCP) throws IOException {
        if (tipoRespuestaDHCP == TipoMensajeDHCP.INVALID && tipoMensajeDHCP != TipoMensajeDHCP.DHCPRELEASE)
            return;
        nuevoRegistroLog(chaddr, tipoMensajeDHCP);
        fw.write("Respuesta: " + tipoRespuestaDHCP);
        cerrarLog();
    }

    public static void nuevoRegistroLog(byte[] chaddr, TipoMensajeDHCP tipoMensajeDHCP) throws IOException {
        fw = new FileWriter(pathLog, true);
        Date fechaLog = new Date();
        fw.write("[" + fechaLog.toString() + "] " + DHCPMensaje.printByteArray(chaddr, 2) + " | " + tipoMensajeDHCP.name() + " -> ");
    }

    public static void cerrarLog() throws IOException {
        fw.write("\n");
        fw.close();
    }
}
