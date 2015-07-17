package es.uji.cusumSpark;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by root on 6/24/15.
 */
public class CusumSpark {


    // TODO preguntar ¿dejamos estos atributos y que las funciones los puedan utilizar libremente o mejor los hacemos locales y que las funciones se los pasen entre sí?

    private static double[] arrayVelA = new double[] {-1.0, 0, 0.05 ,0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95, 1, 1.5, 2, 2.5, 3 }; // Array de velocidades de las rectas, es decir de las pendientes
    private static double[] arrayVel = new double[] {-1.0, 3, 2, 1.5, 1, 0.95, 0.85, 0.75, 0.65, 0.55, 0.45, 0.35, 0.25, 0.15, 0.05, 0.0}; // Array de velocidades de las rectas, es decir de las pendientes
    private static double[] arrayThreshold = new double[] {-1.0, 7, 9.8, 14.4};  // Array con los posibles valores umbral para los experimentos
    private static int[] arrayLambda = new int[] {-1, 5, 10, 20};  // Array con las posibles lambdas a tomar en los experimentos

//    private static int lon = 100;  // Cantidad de números antes de introducir en cambio
//    private static int lon2 = 50;  // Cantidad de números después de introducir el cambio
//    private static int exp = 10000;  // Cantidad de experimentos a realizar
//    private static int nven = 15;  // Cantidad de ventanas a utilizar
//    private static Poisson poisson = new Poisson();  Clase encargada de proporcionar números aleatoriamente siguiento una distribución de Poisson

    public CusumSpark() {
        super();
    }
    /**
     * Detecta donde se produce el cambio
     * @param data
     * @param l1
     * @param l2
     * @return mypos
     */
    static double myPos(double data, double l1, double l2){
        if (l1 == 0 && l2 == 0){
            return 0;
        } else if (l1 == 0 || l2 == 0){
            return Math.log(FuncionesAuxiliares.poissonFunction(data, l2)/FuncionesAuxiliares.poissonFunction(data,l1));
        }
        return (l1 - l2 + Math.log(l2 / l1)*data);
    }

    static double v3(double[] data){
        double[] x = new double[data.length];
        for (int i=1; i<data.length; i++){
            x[i] = i;
        }
        List<Double> regresion = FuncionesAuxiliares.regLineal(data, x);
        return (regresion.get(1)*(data.length-1));
    }

    private static double[] generaDatosLambda(double l0, double b0, double b1, int lon, int lon2){
        RandomDataGenerator rdg = new RandomDataGenerator();
        double[] data = new double[lon+lon2+1];
        double l;
        for (int i = 1; i <= lon; i++) {
            l = l0 + i * b0;
            data[i] = rdg.nextPoisson(l);
        }

        int first = lon+1;
        int last = lon + lon2;
        for (int i = first; i <= last; i++) {
            l = l0 + b0 * lon + b1 * (i - lon);
            data[i] = rdg.nextPoisson(l);
        }
        return data;
    }

    private static int detectaCambio(int lont, double l0, double b0, double threshold, double[] data) {
        // Detecta que ha ocurrido un cambio
        double lbefore, la, lb, s;
        double[] pa = new double[lont+1];
        double[] ga = new double[lont+1];
        double[] pb = new double[lont+1];
        double[] gb = new double[lont+1];

        double threshAux = 3;
        pa[1] = 0; // p after
        ga[1] = 0; // g after
        pb[1] = 0; // p before
        gb[1] = 0; // g before
        boolean alarma = false;
        int inicio = -1;
        int alarmi = 150;
        List<Integer> res = new ArrayList<>();
        for (int i = 2; i <= lont; i++) {
            lbefore = l0 + i * b0; // Lambda si no hay cambio
            if ( lbefore < 0 ) lbefore = 0.0;
            la = lbefore + l0/2 + b0*2; //Un poco despues de lbefore. Lo que suma debe ser constante
            if (FuncionesAuxiliares.poissonFunction(data[i], lbefore) != 0) { //FIXME esto no está en R
                // s <- log(dpois(data[i], lambda=lafter)/dpois(data[i], lambda=lbefore))
                s = myPos(data[i], lbefore, la);
                // p[i] <- p[i-1] + log(dpois(data[i], lambda=lafter)/dpois(data[i], lambda=lbefore))
                pa[i] = pa[i - 1] + s;
                if ((ga[i - 1] + s) < 0) {
                    ga[i] = 0;
                } else {
                    ga[i] = ga[i - 1] + s;
                }
                // FIXME NUEVO
                lb = lbefore - l0/2 -b0*2;
                if (lb < 0) {
                    lb = 0;
                }
                s = myPos(data[i], lbefore, lb);
                pb[i] = pb[i - 1] + s;
                if ((gb[i - 1] + s) < 0) {
                    gb[i] = 0;
                } else {
                    gb[i] = gb[i - 1] + s;
                }
                if (ga[i] > threshAux || gb[i] > threshAux){
                    if (inicio == -1){
                        inicio = i;
                    } else if (ga[i-1] < threshAux && gb[i-1] < threshAux){
                        inicio = i;
                    }
                }
                if (ga[i] > threshold || gb[i] > threshold & !alarma) {
                    alarmi = i;
                    alarma = true;
                    break;
                }
            }
        }
        res.add(inicio);
        res.add(alarmi);
        return alarmi;
    }

    private static List<Double> calculaVelocidad(double lv, int lon, double lfin, int lon2, int nven, double[] data){
        double[][] mp = new double[nven+1][lon2+1];
        for (int k = 1; k <= nven; k++) {
            double lk = lv + (lfin - lv) / nven;
            mp[k][1] = myPos(data[lon+1], lv, lk);
        }
        for (int i = 2; i <= lon2; i++) {
            for (int k = 1; k <= nven; k++) {
                double lk = lv + k * (lfin - lv) / nven; //k+1
                double lk0 = lv + (k - 1) * (lfin - lv) / nven; //k
                mp[k][i] = mp[k][i - 1] + myPos(data[i + lon], lk0, lk);
            }
        }
        // Regresion de los datos
        double[] d = new double[nven+1];
        double[] x = new double[nven+1];
        for (int i = 1; i <= nven; i++) {
            x[i] = i;
            double auxmin = Double.POSITIVE_INFINITY;
            double minindex = 1;
            for (int j = 1; j <= lon2; j++) {
                if (auxmin > mp[i][j]) {
                    auxmin = mp[i][j];
                    minindex = j;
                }
            }
            d[i] = minindex;
        }
        // FIXME en el de Gauss compueba minimos y la regresion la hace con mus/lambdas ~ minimos
        return FuncionesAuxiliares.regLineal(d, x);
    }

    /**
     * Calcula el punto donde se produce el cambio
     * @param lon
     * @param lon2
     * @param l0
     * @param b0
     * @param velocidad
     * @param data
     * @return S
     */
    static int calculaPuntoCambio(int lon, int lon2, double l0, double b0, double velocidad, double[] data){
        int first = 1;
        int last = lon + lon2;
        int lont = last - first;
        double[] S = new double[lont+1];
        for (int i = 1 + first; i <= last-1; i++){ // FIXME Modificado
            int i1 = i+1;
            S[i-first] = 0;
            for (int k = i1; k <= last; k++){
                double lfirst = l0 + b0*k;
                //if (velocidad<0){
                //    System.out.println("fallo");
                //}
                double lsecond = l0 +b0*i + velocidad*(k-i);
                if (lsecond < 0){
                    lsecond = 0;
                }
                double s = myPos(data[k], lfirst, lsecond);
                S[i-first] = S[i-first] + s;
            }
        }
        double auxmax = Double.NEGATIVE_INFINITY;
        int maxindex = 1;
        for (int i=1; i< S.length-1; i++){
            //System.out.println("S"+i+" "+S[i]);
            if ( auxmax < S[i]){
                auxmax = S[i];
                maxindex = i;
            }
        }
        return maxindex + first;
    }

    private static void muestraResultadosExperimentos(double threshold, double l0, double b0, double b1, double[] time, double[] velocidades, int errorArl, int errorVelocidad) {
        String output;

//        List arlMC = new ArrayList<Double>();
//        for (double v1 : arl) {
//            if (v1 > 0) arlMC.add(v1);
//        }
//        double[] arlMayoresCero = new double[arlMC.size()];
//        for (int i =0; i < arlMC.size(); i++){
//            arlMayoresCero[i] = (double) arlMC.get(i);
//        }

        output = "" + threshold + " " + l0 + " " + b0 + " " + b1
                + " " + FuncionesAuxiliares.mean(velocidades) + " " + " " + FuncionesAuxiliares.sdError(velocidades)
                + " " + FuncionesAuxiliares.mean(time) + " " + FuncionesAuxiliares.sdError(time)
                + " " + errorVelocidad + " " + errorArl;

        System.out.println(output);
    }

    private static List<Double> estimaPuntoCambio(double l0, double b0, int lon, double b1, int lon2, int nven, double threshold, double[] data){
        int lont = lon + lon2;
        int cambio = detectaCambio(lont, l0, b0, threshold, data);
        List<Double> res = new ArrayList<>();
        if (cambio >= lon){
            // FIXME En el de Gaus calcula la velocidad de forma distinta
            double lv = l0 + b0 * lon;
            double[] datav3 = new double[lon2+1];
            for (int i = 1; i <= lon2; i++) {
                datav3[i] = data[lon + i];
            }
            double lfin = lv + v3(datav3);
            List<Double> regresion = calculaVelocidad(lv, lon, lfin, lon2, nven, data);
            double velocidad = (lfin - lv) / nven / regresion.get(1); //Tamaño una ventana / y de la regresion
            // Fin del cálculo con la velocidad estimada a partir de los datos

            // Doy una nueva pasada con la velocidad calculada
            if ( velocidad > 0 && Math.abs(velocidad-b1) < b1){
                lv = l0 + b0 * lon;
                lfin = lv + velocidad * lon2;
                regresion = calculaVelocidad(lv, lon, lfin, lon2, nven, data);
                velocidad = (lfin - lv) / nven / regresion.get(1); //Tamaño una ventana / y de la regresion
            }
            // Fin de la segunda pasada

            // FINALMENTE CALCULO EL PUNTO DE CAMBIO
            if ( velocidad > 0 && Math.abs(velocidad-b1) < b1 ){
                double puntoCambio = calculaPuntoCambio(lon, lon2, l0, b0, velocidad, data);
//                if ( inverse ){
//                    time[j] += (lon2-lon);
//                }
//                if ( puntoCambio == 2){
//                    System.out.println("Velocidad: "+velocidad);
//                }
                res.add(puntoCambio);
                res.add(velocidad);
            } else {
                res.add(-2d);
                res.add(-2d);
            }
        } else {
            res.add(-1d);
            res.add(-1d);
        }
        return res;
    }

    private static void unExperimento(double l0, double b0, double b1, int lon, int lon2, int nven, double threshold, int exp){
        int i = 1;
        double[] data;
        double[] velocidades = new double[exp+1];
        double[] time = new double[exp+1];
        List<Double> res;
        int errorVel = 0;
        int errorArl = 0;
        while (i <= exp) {
            data = generaDatosLambda(l0, b0, b1, lon, lon2);
            res = estimaPuntoCambio(l0, b0, lon, b1, lon2, nven, threshold, data);
            if (res.get(0) == -1){
                errorArl++;
            }else if (res.get(0) == -2){
                errorVel++;
            }else{
                time[i] = res.get(0);
                velocidades[i] = res.get(1);
                i++;
            }
        }
        muestraResultadosExperimentos(threshold, l0, b0, b1, time, velocidades, errorArl, errorVel);
    }

    /**
     * Detecta cuando se ha producido un cambio en la tendencia
     * y calcula en punto en el que se ha producido dicho cambio
     */
    public static void realizaExperimentos() {
        double threshold, l0, b0, b1;
        for (int n = 1; n < arrayLambda.length; n++) {
            threshold = arrayThreshold[n]; // Establece el umbral
            l0 = arrayLambda[n]; // Establece la lambda inicial

            for (int indexb0 = 1; indexb0 < arrayVelA.length - 1; indexb0++) {
                b0 = arrayVelA[indexb0];

//            for (int threshold : arrayThreshold)
                for (int indexb1 = indexb0 + 1; indexb1 < arrayVelA.length; indexb1++) {

                    b1 = arrayVelA[indexb1];
                    unExperimento(l0, b0, b1, 100, 50, 15, threshold, 10000);
                }

            }
        }
    }
}