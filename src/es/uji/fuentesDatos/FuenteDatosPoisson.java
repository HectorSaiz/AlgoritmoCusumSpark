package es.uji.fuentesDatos;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * Created by hector on 18/07/15.
 */
public class FuenteDatosPoisson implements Runnable {

    private RandomDataGenerator rdg;
    private int lon, lon2;
    private long tiempoEspera;
    private double l0;
    private double[] betas;
    //Este atributo privado mantiene el vector con los observadores
    private ZonaIntercambioEventos zonaIntercambioEventos;

    public FuenteDatosPoisson ( long tiempoEspera, double l0, double[] betas, int lon, ZonaIntercambioEventos zonaIntercambioEventos ) {
        this.tiempoEspera = tiempoEspera;
        this.l0 = l0;
        this.betas = betas;
        this.lon = lon;
        this.rdg = new RandomDataGenerator();
        this.zonaIntercambioEventos = zonaIntercambioEventos;
    }

    @Override
    public void run() {
        double l, dato;
        for (int k = 0; k<betas.length;k++){
            if (k > 0)
                l0 = l0 + lon*betas[k-1];
            for (int i = 1; i <= lon; i++) {
                l = l0 + i * betas[k];
                dato = rdg.nextPoisson(l);
                zonaIntercambioEventos.insertaTarea( new Tarea(dato, false) );
                try {
                    Thread.sleep(tiempoEspera);
                } catch (InterruptedException e) { }

            }
        }
        zonaIntercambioEventos.insertaTarea( new Tarea(0.0, true) );
    }

}
