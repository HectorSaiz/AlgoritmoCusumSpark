package es.uji.fuentesDatos;

/**
 * Created by hector on 20/07/15.
 */
public class Tarea {

    private double cantidadEventos;
    private boolean esVeneno;

    public Tarea(double cantidadEventos, boolean esVeneno) {
        this.cantidadEventos = cantidadEventos;
        this.esVeneno = esVeneno;
    }

    public double getCantidadEventos() {
        return cantidadEventos;
    }

    public boolean isEsVeneno() {
        return esVeneno;
        // TODO en principio solo se utilizará para poisson, si hacemos una GUI se puede utilizar para que al pulsar un botón finalice la ejecución del programa.
    }
}
