package es.uji.cusumSpark;

/**
 * Created by hector on 20/07/15.
 */
public interface IObservador extends Runnable {
    public void actualizar( double dato );
}
