package es.uji.cusumSpark;


import org.apache.commons.math3.distribution.PoissonDistribution;

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

        PoissonDistribution pd = new PoissonDistribution(5);
        Poisson p = new Poisson();

        for (int i = 0; i < 100; i++) {
            System.out.println(pd.sample());
            System.out.println( p.nextPoisson(5));
            System.out.println("");
        }
    }
}
