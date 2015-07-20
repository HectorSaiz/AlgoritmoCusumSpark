package es.uji.cusumSpark;

/**
 * Created by hector on 20/07/15.
 */

public interface IObservable {

    public void agregarObservador(IObservador o);

    public void eliminarObservador(IObservador o);

    public void notificarObservadores(double dato);

}