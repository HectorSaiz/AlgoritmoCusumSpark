package es.uji.cusumSpark;


import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.ArrayList;
import java.util.List;

public class Main2 {

    public static void main(String[] args) {

//        long time_start, time_end;
//
//        System.out.println("Inicia tarea principal\n");
//
//        time_start = System.currentTimeMillis();
//        System.out.println("Arrancan los experimentos\n");
//        copiaMala.realizaExperimentos(); // llamamos a la tarea
//        time_end = System.currentTimeMillis();
//
//        System.out.println("\nTiempo empleado " + (time_end - time_start) / 1000 + " segundos -> " + (time_end - time_start) / 1000 / 60 + " minutos" );

//        PoissonDistribution pd = new PoissonDistribution(5);
//        Poisson p = new Poisson();
//
//        for (int i = 0; i < 100; i++) {
//            System.out.println(pd.sample());
//            System.out.println( p.nextPoisson(5));
//            System.out.println("");
//        }

        int[] arrayA = new int[] {1, 2, 3, 4, 5};
        int[] arrayB = new int[] {6, 7, 8, 9, 10};

        System.arraycopy(arrayA, 0, arrayB, 0, 4);
//        System.arrayCopy(aOrigen,inicioArrayOrigen,aDestino, inicioArrayDestino,numeroElementosACopiar);
//        System.out.println(arrayA[4]);
//        System.out.println(arrayB[4]);

        List<Integer> l = new ArrayList<>();
        l.add(0, 0);l.add(1, 5); l.add(2, 8);
        for (int i = 1; i < l.size(); i++) {
            System.out.println(l.get(i));
        }

        System.out.println(l.size());


    }
}
