package es.uji.cusumSpark;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 6/24/15.
 */
public class Deteccion2 {


    // TODO preguntar ¿dejamos estos atributos y que las funciones los puedan utilizar libremente o mejor los hacemos locales y que las funciones se los pasen entre sí?
    private static double[] arrayVel = new double[] {0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95, 1, 1.5, 2, 2.5, 3}; // Array de velocidades de las rectas, es decir de las pendientes
    private static double[] arrayThreshold = new double[] {7, 9.8, 14.4};  // Array con los posibles valores umbral para los experimentos
    private static int[] arrayLambda = new int[] {5, 10, 20};  // Array con las posibles lambdas a tomar en los experimentos

    private static int lon = 100;  // Cantidad de números antes de introducir en cambio
    private static int lon2 = 100;  // Cantidad de números después de introducir el cambio
    private static int exp = 10000;  // Cantidad de experimentos a realizar
    private static int nven = 15;  // Cantidad de ventanas a utilizar
    private static int errorVelocidad = 0;
    private static int errorArl = 0;

    private static double threshold, l0, l, b0, b1, lv, lfin, vel3;
    private static double[][] mp;
    private static double[] data, e, arl, velocidades, velocidades2, time, time2, timeTeorica, timeDatos, p, g;
    private static int j, alarmi, first, last;
    private static Poisson poisson = new Poisson(); // Clase encargada de proporcionar números aleatoriamente siguiento una distribución de Poisson
    private static boolean alarma;

    public Deteccion2() {
        super();
    }

    // Getters and setters

    public static double[] getArrayVel() {
        return arrayVel;
    }

    public static void setArrayVel(double[] arrayVel) {
        Deteccion2.arrayVel = arrayVel;
    }

    public static double[] getArrayThreshold() {
        return arrayThreshold;
    }

    public static void setArrayThreshold(double[] arrayThreshold) {
        Deteccion2.arrayThreshold = arrayThreshold;
    }

    public static int[] getArrayLambda() {
        return arrayLambda;
    }

    public static void setArrayLambda(int[] arrayLambda) {
        Deteccion2.arrayLambda = arrayLambda;
    }

    public static int getLon() {
        return lon;
    }

    public static void setLon(int lon) {
        Deteccion2.lon = lon;
    }

    public static int getLon2() {
        return lon2;
    }

    public static void setLon2(int lon2) {
        Deteccion2.lon2 = lon2;
    }

    public static int getExp() {
        return exp;
    }

    public static void setExp(int exp) {
        Deteccion2.exp = exp;
    }

    public static int getNven() {
        return nven;
    }

    public static void setNven(int nven) {
        Deteccion2.nven = nven;
    }

    // Fin getters and setters


    /**
     * Detecta donde se produce el cambio
     * @param data
     * @param l1
     * @param l2
     * @return mypos
     */
    static double myPos(double data, double l1, double l2){
        return (l1 - l2 + Math.log(l2 / l1)*data);
    }

    static double v3(double[] data){
        double[] x = new double[data.length];
        for (int i=0; i<data.length; i++){
            x[i] = i+1;
        }
        List<Double> regresion = FuncionesAuxiliares.regLineal(data, x);
        return (regresion.get(1)*data.length);
    }

    static double vv3(double[] data){
        double[] x = new double[data.length];
        for (int i=0; i<data.length; i++){
            x[i] = i+1;
        }
        List<Double> regresion = FuncionesAuxiliares.regLineal(data, x);
        return regresion.get(1);
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
    static double[] calculaPuntoCambio(int lon, int lon2, double l0, double b0, double velocidad, double[] data){
        int first = 0; //TODO preguntar qué es este first y si debe o no ser 1
        int last = lon + lon2 - 1;
        int lont = last - first;
        double[] S = new double[lont];
        for (int i = 2 + first; i <= last-1; i++){ // FIXME Modificado
            int i1 = i+1;
            S[i-first] = 0;
            for (int k = i1; k <= last; k++){
                double lfirst = l0 + b0*k;
                if (velocidad<0){
                    System.out.println("fallo");
                }
                double lsecond = l0 +b0*i + velocidad*(k-i);
                double s = myPos(data[k], lfirst, lsecond);
                S[i-first] = S[i-first] + s;
            }
        }
        return S;
    }

    /**
     * Detecta cuando se ha producido un cambio en la tendencia
     * y calcula en punto en el que se ha producido dicho cambio
     */
    public static void realizaExperimentos() {

        for (int n = 0; n < arrayLambda.length; n++) {
            threshold = arrayThreshold[n]; // Establece el umbral
            l0 = arrayLambda[n]; // Establece la lambda inicial

//            for (int threshold : arrayThreshold)
            for (double b1 : arrayVel) {

                e = new double[exp];
                arl = new double[exp];

                data = new double[lon+lon2];

                mp = new double [nven][lon2];
                velocidades = new double[exp];
                velocidades2 = new double[exp];

                time = new double[exp];
                time2 = new double[exp];
                timeTeorica = new double[exp];
                timeDatos = new double[exp];


                j = 0;
                while (j < exp){
//                    System.out.println("Experimento: " + j);
                    alarmi = lon + lon2;
//                    #       alarmi <- -1

//                    TODO PREGUNTAR  b0 no se inicializa por lo que siempre es 0 ¿creamos un bucle que inicialice la b0 y los experimentos se realicen de b0 a b1?
                    for (int i = 0; i < lon; i++) {
                        l = l0 + i*b0;
                        data[i] = poisson.nextPoisson(l);
                    }

                    first = lon;
                    last = lon + lon2;
                    for (int i = first; i < last; i++) {
                        l = l0 + b0*lon + b1*(i-lon);
                        data[i] = poisson.nextPoisson(l);
                    }

                    p = new double[lon+lon2];
                    g = new double[lon+lon2];

                    p[0] = 0;
                    g[0] = 0;
                    alarma = false;

//                    Se comprueba si ha habido algún cambio en la tendencia.
                    detectaCambio();


                    if (alarmi > lon) {
                        // Si se cumple ha habido un cambio
                        // Se estima la velocidad después de cambio
                        // lv <- l0 + b0*1000
                        lv = l0 + b0 * lon;
                        // La siguiente línea es buena idea para datos de twitter
                        //       lfin <- lv + v2d(data[lon+1:lon2], l0, lv, lv+b1*lon2)*lon2
                        //       lfin <- lv + b0*lon2
                        //                 lfin <- lv + b1 * lon2
                        //           vel3 <- 0
                        double[] datav3 = new double[lon2];
                        for (int i = 0; i < lon2; i++) {
                            datav3[i] = data[i + lon];
                        }
                        vel3 = vv3(datav3);
                        lfin = lv + v3(datav3);
                        //       lfin <- lv + v2(data[(lon+1):(lon+lon2)])*lon2
                        //       print(v2(data[(lon+1):(lon+lon2]))
                        //       lfin <- 3*lv
                        //       vv <- velocidad(data[lon:(lon+lon2)])
                        //       vv <- velocidad(data[lon:alarmi])
                        //       lfin <- lv + vv * lon2
                        //         lfin <- data[lon+lon2] # Si tomo el último dato es bastante aceptable
                        //         lfin <- data[alarmi]
                        for (int k = 0; k < nven; k++) {
                            double lk = lv + (lfin - lv) / nven;
                            mp[k][0] = myPos(data[lon], lv, lk);
                        }
                        for (int i = 1; i < lon2; i++) {
                            for (int k = 0; k < nven; k++) {
                                double lk = lv + k * (lfin - lv) / nven;
                                double lk0 = lv + (k - 1) * (lfin - lv) / nven;
                                //           mp[k,i] <- mp[k,i-1] + log(dpois(data[i+lon], lk)/dpois(data[i+lon], lk0))
                                mp[k][i] = mp[k][i - 1] + myPos(data[i + lon], lk0, lk);
                            }
                        }

                        // Regresion de los datos
                        double[] d = new double[nven];
                        double[] x = new double[nven];
                        for (int i = 0; i < nven; i++) {
                            x[i] = i + 1;
                            double auxmin = Double.POSITIVE_INFINITY;
                            double minindex = 0;
                            for (int j = 0; j < lon2; j++) {
                                if (auxmin > mp[i][j]) {
                                    auxmin = mp[i][j];
                                    minindex = j;
                                }
                            }
                            d[i] = minindex;
                        }
                        List<Double> regresion = FuncionesAuxiliares.regLineal(d, x);
                        //Fin de la regresion

                        velocidades[j] = (lfin - lv) / nven / regresion.get(1); //Tamaño una ventana / y de la regresion
                        velocidades2[j] = velocidades[j];

                        //if(velocidades2[j]>0 && abs(velocidades2[j] - b1) < b1) {
                        //  time2[j] <- calculaPuntoCambio(lon, lon2, l0, b0, velocidades2[j], data)
                        //}

                        // Calculo el punto de cambio con la velocidad teórica
                        //timeTeorica[j] <- calculaPuntoCambio(lon, lon2, l0, b0, b1, data)
                        // Fin del cálculo con la velocidad teórica

                        // Calculo el punto de cambio con la velocidad estimada a partir de los datos
                        //if(vel3>0 && abs(vel3 - b1) < b1) {
                        //  timeDatos[j] <- calculaPuntoCambio(lon, lon2, l0, b0, vel3, data)
                        //}
                        // Fin del cálculo con la velocidad estimada a partir de los datos

                        //           Doy una nueva pasada con la velocidad calculada
                        if (velocidades[j] > 0 && Math.abs(velocidades[j] - b1) < b1) {
                            lv = l0 + b0 * lon;

                            lfin = lv + velocidades[j] * lon2;

                            for (int k = 0; k < nven; k++) {
                                double lk = lv + (lfin - lv) / nven;
                                mp[k][0] = myPos(data[lon], lv, lk);
                            }
                            for (int i = 1; i < lon2; i++) {
                                for (int k = 0; k < nven; k++) {
                                    double lk = lv + (k + 1) * (lfin - lv) / nven;
                                    double lk0 = lv + k * (lfin - lv) / nven;
                                    mp[k][i] = mp[k][i - 1] + myPos(data[i + lon], lk0, lk);
                                }
                            }
                            // Regresion de los datos
                            d = new double[nven];
                            x = new double[nven];
                            for (int i = 0; i < nven; i++) {
                                x[i] = i + 1;
                                double auxmin = Double.POSITIVE_INFINITY;
                                double minindex = 0;
                                for (int j = 0; j < lon2; j++) {
                                    if (auxmin > mp[i][j]) {
                                        auxmin = mp[i][j];
                                        minindex = j;
                                    }
                                }
                                d[i] = minindex;
                            }
                            regresion = FuncionesAuxiliares.regLineal(d, x);
                            // Fin de la regresion
                            velocidades[j] = (lfin - lv) / nven / regresion.get(1); //Tamaño una ventana / y de la regresion
                        }
                        // Fin de la segunda pasada

                        // FINALMENTE CALCULO EL PUNTO DE CAMBIO
                        // Esta condición es para eliminar velocidades erroneas
                        if (velocidades[j] > 0 && Math.abs(velocidades[j] - b1) < b1) {
                            double[] SS = calculaPuntoCambio(lon, lon2, l0, b0, velocidades[j], data);
                            double auxmax = 0;
                            int maxindex = 0;
                            for (int i=0; i<SS.length; i++){
                                if ( auxmax < SS[i]){
                                    auxmax = SS[i];
                                    maxindex = i;
                                }
                            }
                            time[j] = maxindex;
                            // TODO ¿incorporar la busqueda del índice en la función calculaPuntoCambio()?
                            // time[j] <- calculaPuntoCambio(lon, lon2, l0, b0, velocidades[j], data)
                            j = j + 1;
                        } else {
                            velocidades[j] = -2;
                            errorVelocidad = errorVelocidad + 1;
                        }
                    } else {
                        time[j] = -1;
                        velocidades[j] = -1;
                        errorArl = errorArl + 1;
                    }
                }
                muestraResultadosExperimentos(b1);
            }
        }
    }

    private static void muestraResultadosExperimentos(double b1) {
        String output;

        List arlMC = new ArrayList<Double>();
        for (double v1 : arl) {
            if (v1 > 0) arlMC.add(v1);
        }
        double[] arlMayoresCero = new double[arlMC.size()];
        for (int i =0; i < arlMC.size(); i++){
            arlMayoresCero[i] = (double) arlMC.get(i);
        }

        output = "" + threshold + " " + l0 + " " + b0 + " " + b1 + " " + (lv + b1 * lon2) + " " + lfin + " " + vel3
                + " " + FuncionesAuxiliares.mean(velocidades) + " " + FuncionesAuxiliares.mean(velocidades2) + " " + FuncionesAuxiliares.sd(velocidades) + " " + FuncionesAuxiliares.mean(arlMayoresCero) + " " + FuncionesAuxiliares.sd(arlMayoresCero)
                + " " + FuncionesAuxiliares.mean(time) + " " + FuncionesAuxiliares.sdError(time) + " " + FuncionesAuxiliares.mean(time2) + " " + FuncionesAuxiliares.sdError(time2)
                + " " + FuncionesAuxiliares.mean(timeTeorica) + " " + FuncionesAuxiliares.sdError(timeTeorica)
                + " " + FuncionesAuxiliares.mean(timeDatos) + " " + FuncionesAuxiliares.sdError(timeDatos)
                + " " + errorVelocidad + " " + errorArl;

        System.out.println(output);
    }

    private static void detectaCambio() {
        // Detecta que ha ocurrido un cambio
        for (int i = 1; i < (lon + lon2); i++) {
            double lbefore = l0 + i * b0; // Lambda si no hay cambio
            double lafter = lbefore + l0 / 2; //Un poco despues de lbefore. Lo que suma deb ser constante
            if (FuncionesAuxiliares.poissonFunction(data[i], lbefore) != 0) {
                // s <- log(dpois(data[i], lambda=lafter)/dpois(data[i], lambda=lbefore))
                double s = myPos(data[i], lbefore, lafter);
                // p[i] <- p[i-1] + log(dpois(data[i], lambda=lafter)/dpois(data[i], lambda=lbefore))
                p[i] = p[i - 1] + s;
                if ((g[i - 1] + s) < 0) {
                    g[i] = 0;
                } else {
                    g[i] = g[i - 1] + s;
                }
                if (g[i] > threshold & !alarma) {
                    alarmi = i;
                    alarma = true;
                    arl[j] = alarmi - lon;
                }
            }
        }
    }
/*
      write.table(output, file="resultados-5-7-250.csv", row.names=FALSE, append=TRUE, sep="&", col.names=F)
      p3i <- length(time[time==97])/length(time[time>0])
      p3d <- length(time[time==103])/length(time[time>0])
      p3 <- p3i + p3d
      p2i <- length(time[time==98])/length(time[time>0])
      p2d <- length(time[time==102])/length(time[time>0])
      p2 <- p2i + p2d
      p1i <- length(time[time==99])/length(time[time>0])
      p1d <- length(time[time==101])/length(time[time>0])
      p1 <- p1i + p1d
      pc <- length(time[time==100])/length(time[time>0])
      #     output <- gettextf("%f %f %f %f %f %f %f %f", threshold, l0, b0, b1, pc, pc+p1, pc+p1+p2, pc+p1+p2+p3)
      output <- paste(threshold, l0, b0, b1, pc, pc+p1, pc+p1+p2, pc+p1+p2+p3, sep=" ")
      write.table(output, file="histogramas-5-7-250.csv", row.names=FALSE, append=TRUE, col.names=F)
    }
  #}
}
     */
}