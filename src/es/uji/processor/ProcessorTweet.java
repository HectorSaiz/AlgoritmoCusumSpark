package es.uji.processor;

import es.uji.filter.Filter;
import es.uji.fuentesDatos.Tarea;
import es.uji.fuentesDatos.ZonaIntercambioEventos;
import twitter4j.Status;

import java.util.TreeMap;


/**
 * Created by hector on 22/07/15.
 */
public class ProcessorTweet implements Processor {

    private ZonaIntercambioEventos zonaIntercambio;
    private Filter filter;
    private TreeMap<Long, Integer> map;
    private long totalesProcesados;

    public ProcessorTweet ( ZonaIntercambioEventos zonaIntercambio, Filter filter ) {
        this.zonaIntercambio = zonaIntercambio;
        this.filter = filter;
        this.map = new TreeMap<>();
        this.totalesProcesados = 0l;
    }

    public boolean inicializaBuffer (Status status) {
        if (filter.filter(status)) {
            Integer cantidadEventos = map.get(status.getCreatedAt().getTime());
            cantidadEventos = (cantidadEventos == null) ? 1 : cantidadEventos + 1;
            map.put(status.getCreatedAt().getTime(), cantidadEventos);
            totalesProcesados++;
//            System.out.println(status.getCreatedAt().getTime() + " -------- " + map.size());

            if (map.size() > 10){
                return true;
            } else {
                return false;
            }

        }
        return false;
    }

    @Override
    public boolean processTweet(Status status) {
        if (filter.filter(status)) {
            Integer cantidadEventos;

//            System.out.println(status.getCreatedAt().getTime() + " -------- " + map.size());

            cantidadEventos = map.get(status.getCreatedAt().getTime());
            cantidadEventos = (cantidadEventos == null) ? 1 : cantidadEventos + 1;
            map.put(status.getCreatedAt().getTime(), cantidadEventos);
            totalesProcesados++;

            if (map.size() > 10) {
                Tarea t;

//                System.out.println("Primera clave: " + map.firstKey() + " ------ Primer valor: " + map.get(map.firstKey()));

                cantidadEventos = map.remove(map.firstKey());
//                System.out.println("CantidadEventos: " + cantidadEventos);
                t = new Tarea(cantidadEventos, false);
                zonaIntercambio.insertaTarea(t);
            }

            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        // OJO Inserta una tarea que es veneno y el algorimo no recoge más datos.
        Tarea fin = new Tarea(0d, true);
        zonaIntercambio.insertaTarea(fin);

    }

    public long getTotalesProcesados() {
        return totalesProcesados;
    }
}
