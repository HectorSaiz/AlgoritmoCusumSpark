package es.uji.cusumSpark;

import java.io.FileWriter;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args) {

        long time_start, time_end;

        System.out.println("Inicia tarea principal\n");

        time_start = System.currentTimeMillis();
        System.out.println("Arrancan los experimentos\n");
        CusumSpark.realizaExperimentos(); // llamamos a la tarea
        time_end = System.currentTimeMillis();

        System.out.println("\nTiempo empleado " + (time_end - time_start) / 1000 + " segundos -> " + (time_end - time_start) / 1000 / 60 + " minutos" );
        Poisson poisson = new Poisson();






        /* Generar archivos con datos de Pois
        for (int k = 1; k <= 100; k++) {
            double[] data = new double[150];
            double l;
            for (int i = 0; i < 100; i++) {
                l = 5 + i * 0.45;
                data[i] = poisson.nextPoisson(l);
            }

            for (int i = 100; i < 150; i++) {
                l = 5 + 0.45 * 100 + 0.55 * (i - 100);
                data[i] = poisson.nextPoisson(l);
            }
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(System.getProperty("user.dir") + "/Pois R/Pois(0.45-0.55)Java " + k + " .txt"));
                for (int i = 0; i<data.length; i++){
                    writer.println(data[i]);
                }
                writer.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }*/
    }
}
