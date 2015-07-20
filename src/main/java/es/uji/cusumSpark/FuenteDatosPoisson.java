package es.uji.cusumSpark;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hector on 18/07/15.
 */
public class FuenteDatosPoisson implements IFuenteDatos {

    private RandomDataGenerator rdg;
    private int lon, lon2;
    private long tiempoEspera;
    private double l0, b0, b1;
    //Este atributo privado mantiene el vector con los observadores
    private List<IObservador> _observadores;

    public FuenteDatosPoisson ( long tiempoEspera, double l0, double b0, double b1, int lon, int lon2 ) {
        this.tiempoEspera = tiempoEspera;
        this.l0 = l0;
        this.b0 = b0;
        this.b1 = b1;
        this.lon = lon;
        this.lon2 = lon2;
        this.rdg = new RandomDataGenerator();
        this._observadores = new ArrayList<>();;
    }

    @Override
    public void agregarObservador(IObservador o) {
        _observadores.add(o);
    }

    @Override
    public void eliminarObservador(IObservador o) {
        _observadores.remove(o);
    }

    @Override
    public void notificarObservadores(double dato) {
        for (IObservador o:_observadores) {
            o.actualizar( dato );
        }
    }

    @Override
    public void run() {

        double l, dato;

        for (int i = 1; i <= lon; i++) {
            l = l0 + i * b0;
            dato = rdg.nextPoisson(l);
            notificarObservadores( dato );
//            synchronized(this) {
//                try {
//                    Thread.sleep(tiempoEspera);
//                } catch (InterruptedException e) { }
//            }
        }

        int first = lon+1;
        int last = lon + lon2;
        for (int i = first; i <= last; i++) {
            l = l0 + b0 * lon + b1 * (i - lon);
            dato = rdg.nextPoisson(l);
            notificarObservadores( dato );
//            synchronized(this) {
//                try {
//                    Thread.sleep(tiempoEspera);
//                } catch (InterruptedException e) { }
//            }
        }

    }

//    private void generaEspera(int i) {
//        double l;
//        double dato;
//        l = (i+1) * 0.2;
//        dato = rdg.nextPoisson(l);
//        notificarObservadores( dato );
//        synchronized(this) {
//            try {
//                Thread.sleep(tiempoEspera);
//            } catch (InterruptedException e) { }
//        }
//    }
}
