package puj.redes.Registros;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class ThreadRevocaciones extends Thread {
    public void run () {
        try {
            Date date = null;
            while (true) {
                date = new Date ();
                ArrayList <Registro> registrosEliminar;

                if (ControladorRegistros.getRegistros().size() == 0) {
                    sleep(1000);
                    continue;
                }

                registrosEliminar = ControladorRegistros.obtenerFechaReciente();
                sleep(1500);

                for (Registro registro : registrosEliminar)
                    ControladorRegistros.eliminarRegistro(registro);
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("\t-> Error en el thread de revocaciones: " + e.getMessage());
        }
    }
}
