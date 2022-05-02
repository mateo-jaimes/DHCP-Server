package puj.redes.Registros;

import java.io.IOException;
import java.util.Date;

public class ThreadRevocaciones extends Thread {
    public void run () {
        try {
            Date date = null;
            while (true) {
                if (ControladorRegistros.getRegistros().size() == 0) {
                    sleep(1000);
                    continue;
                }

                date = new Date ();

                Registro reg = ControladorRegistros.obtenerFechaReciente();
                sleep(Math.abs(date.getTime() - reg.getTiempoRetirar().getTime()));

                ControladorRegistros.eliminarRegistro(reg);
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("\t-> Error en el thread de revocaciones: " + e.getMessage());
        }
    }
}
