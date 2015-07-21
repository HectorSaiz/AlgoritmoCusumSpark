package es.uji.main;

import es.uji.cusumSpark.CusumSpark;
import es.uji.fuentesDatos.ZonaIntercambioEventos;

public class Main {

    public static void main(String[] args) {

        long time_start, time_end;
        Thread t;
        CusumSpark cusumSpark;
        ZonaIntercambioEventos zonaIntercambio;

        System.out.println("Inicia tarea principal\n");

        time_start = System.currentTimeMillis();
        zonaIntercambio = new ZonaIntercambioEventos();
        System.out.println("Arrancan los experimentos\n");
//        CusumSpark.realizaExperimentos(); // llamamos a la tarea
        cusumSpark = new CusumSpark( zonaIntercambio );
        t = new Thread(cusumSpark);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        time_end = System.currentTimeMillis();

        System.out.println("\nTiempo empleado " + (time_end - time_start) / 1000 + " segundos -> " + (time_end - time_start) / 1000 / 60 + " minutos");
    }
}