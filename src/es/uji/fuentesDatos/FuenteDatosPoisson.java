package es.uji.fuentesDatos;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * Created by hector on 18/07/15.
 */
public class FuenteDatosPoisson implements Runnable {

    private RandomDataGenerator rdg;
    private int lon, lon2;
    private long tiempoEspera;
    private double l0, b0, b1;
    //Este atributo privado mantiene el vector con los observadores
    private ZonaIntercambioEventos zonaIntercambioEventos;

    public FuenteDatosPoisson ( long tiempoEspera, double l0, double b0, double b1, int lon, int lon2, ZonaIntercambioEventos zonaIntercambioEventos ) {
        this.tiempoEspera = tiempoEspera;
        this.l0 = l0;
        this.b0 = b0;
        this.b1 = b1;
        this.lon = lon;
        this.lon2 = lon2;
        this.rdg = new RandomDataGenerator();
        this.zonaIntercambioEventos = zonaIntercambioEventos;
    }

    @Override
    public void run() {
        double l, dato;

        for (int i = 1; i <= lon; i++) {
            l = l0 + i * b0;
            dato = rdg.nextPoisson(l);
            zonaIntercambioEventos.insertaTarea( new Tarea(dato, false) );
            try {
                Thread.sleep(tiempoEspera);
            } catch (InterruptedException e) { }

        }

        int first = lon+1;
        int last = lon + lon2;
        for (int i = first; i <= last; i++) {
            l = l0 + b0 * lon + b1 * (i - lon);
            dato = rdg.nextPoisson(l);
            zonaIntercambioEventos.insertaTarea( new Tarea(dato, false) );
            try {
                Thread.sleep(tiempoEspera);
            } catch (InterruptedException e) { }
        }

        for (int i = 151; i <= 240; i++){
            l = l0 +b0 * lon + b1 * lon2 + 4 * (i-150);
            dato = rdg.nextPoisson(l);
            zonaIntercambioEventos.insertaTarea( new Tarea(dato, false));
            try {
                Thread.sleep(tiempoEspera);
            } catch (InterruptedException e) { }
        }
        zonaIntercambioEventos.insertaTarea( new Tarea(0.0, true) );
    }

}
