package es.uji.cusumSpark;

public class Main {

    public static void main(String[] args) {

        long time_start, time_end;

        System.out.println("Inicia tarea principal");

        time_start = System.currentTimeMillis();
        System.out.println("Arrancan los experimentos");
        CusumSpark.realizaExperimentos(); // llamamos a la tarea
        time_end = System.currentTimeMillis();

        System.out.println("Tiempo empleado " + (time_end - time_start) / 1000 + " segundos || " + (time_end - time_start) / 1000 / 60 + " minutos" );



    }
}
