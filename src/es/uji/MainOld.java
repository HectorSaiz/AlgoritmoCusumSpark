package es.uji;

import es.uji.cusumSpark.CusumSpark;
import es.uji.fuentesDatos.FuenteDatosTwitter;
import es.uji.fuentesDatos.ZonaIntercambioEventos;

public class MainOld {

    public static void main(String[] args) {

        long time_start, time_end;
        Thread cusum, twitter;
        CusumSpark cusumSpark;
        FuenteDatosTwitter conectorTwitter;
        ZonaIntercambioEventos zonaIntercambio;

        System.out.println("Inicia tarea principal\n");

        time_start = System.currentTimeMillis();
        zonaIntercambio = new ZonaIntercambioEventos();

        System.out.println("Arrancan los experimentos\n");
//        CusumSpark.realizaExperimentos(); // llamamos a la tarea

        if (args.length > 1) {
            System.out.println("ERROR: solo se permite introducir una cadena");
            System.exit(-1);
        }

        if (args.length == 1){

            conectorTwitter = new FuenteDatosTwitter( args[0], zonaIntercambio );
            twitter = new Thread( conectorTwitter );
            twitter.start();
            cusumSpark = new CusumSpark( true, zonaIntercambio );
        } else {
            cusumSpark = new CusumSpark( false, zonaIntercambio );
        }
        cusum = new Thread(cusumSpark);
        cusum.start();
        try {
            cusum.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        time_end = System.currentTimeMillis();

        System.out.println("\nTiempo empleado " + (time_end - time_start) / 1000 + " segundos -> " + (time_end - time_start) / 1000 / 60 + " minutos");
    }
}