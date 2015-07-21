package es.uji.fuentesDatos;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by hector on 20/07/15.
 */
public class ZonaIntercambioEventos extends Thread {

    private BlockingQueue<Tarea> colaTareas;

    public ZonaIntercambioEventos() {
        this.colaTareas = new LinkedBlockingQueue<Tarea>();
    }

    public void insertaTarea(Tarea tarea) {
        try {
            colaTareas.put(tarea);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Tarea dameTarea() {
        try {
            return colaTareas.take();

        } catch (InterruptedException e) {
            e.printStackTrace();
            // si existe algún problema en el take el catch no devolvería nada
            // y se debe devolver una tarea, de ahí el null, cualquiera que use esta función
            // deberá comprobar que no le ha devuelto un null

            return null;
        }

    }
}
