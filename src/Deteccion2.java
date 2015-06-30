import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 6/24/15.
 */
public class Deteccion2 {

    private static double[] arrayVel = new double[] {0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95, 1, 1.5, 2, 2.5, 3};   // Array de velocidades de las rectas, es decir de las inclinaciones
    private static double[] arrayThreshold = new double[] {7, 9.8, 14.4};  // Array con los posibles valores umbral para los experimentos
    private static int[] arrayLambda = new int[] {5, 10, 20};  // Array con las posibles lambdas a tomar en los experimentos

    private static int lon = 100;  // Cantidad de números antes de introducir en cambio
    private static int lon2 = 50;  // Cantidad de números después de introducir el cambio
    private static int exp = 10000;  // Cantidad de experimentos a realizar TODO incremenar hasta los 10000
    private static int nven = 15;  // Cantidad de ventanas a utilizar
    private static int errorVelocidad = 0;
    private static int errorArl = 0;

    private static double threshold, l0, l, b0, b1, lv, lfin, vel3;
    private static double[][] mp;
    private static double[] data, e, arl, velocidades, velocidades2, time, time2, timeTeorica, timeDatos, p, g, v;
    private static int j, alarmi, first, last;
    private static Poisson poisson = new Poisson();
    private static boolean alarma;

    // Factorial
    static double Factorial(double n) {
        if (n == 0)
            return 1;
        else
            return n * Factorial(n-1);
    }

    // f(x) de Poisson ( dpois(data,lambda) en R )
    static double poissonFunction(double x, double lambda){
        return Math.pow(lambda, x) / Factorial(x) * Math.pow(Math.E,-lambda);
    }

    // Media
    static double mean(double[] x){
        double sum = 0;
        for (int i = 0; i < x.length; i++){
            sum += x[i];
        }
        double mean = sum / x.length;
        return mean;
    }

    // Covarianza (x e y tienen la misma length)
    static double cov(double[] y, double[] x){
        double cov = 0;
        for (int i = 0; i < x.length; i++){
            cov += x[i]*y[i];
        }
        cov = cov / x.length;
        cov -= mean(x)*mean(y);
        return cov;
    }

    // Varianza
    static double var(double[] x){
        double var = 0;
        for (int i = 0; i < x.length; i++){
            var += Math.pow(x[i],2);
        }
        var = var / x.length;
        var -= Math.pow(mean(x), 2);
        return var;
    }

    static double sd (double[] x)
    {
        return Math.sqrt(var(x));
    }

    // Regresion lineal Y = b0 + b1 * X
    static List<Double> regLineal(double[] y, double[] x){
        double b1 = cov(y,x)/var(x);
        double b0 = mean(y) - b1 * mean(x);
        List<Double> res = new ArrayList<>();
        res.add(b0);
        res.add(b1);
        return res;
    }

    // DETECTAMOS DONDE SE PRODUCE EL CAMBIO
    static double myPos(double data, double l1, double l2){
        return (l1 - l2 + Math.log(l2 / l1)*data);
    }


    static double v3(double[] data){
        double[] x = new double[data.length];
        for (int i=0; i<data.length; i++){
            x[i] = i+1;
        }
        List<Double> regresion = regLineal(data, x);
        return (regresion.get(1)*data.length);
    }

    static double vv3(double[] data){
        double[] x = new double[data.length];
        for (int i=0; i<data.length; i++){
            x[i] = i+1;
        }
        List<Double> regresion = regLineal(data, x);
        return regresion.get(1);
    }

    static double estimacionVelocidad(double l0, double l1, double l2, double k){
        return ((l2-l1-l0*Math.log(l2 / l1))/(k*Math.log(l2 / l1)));
    }


    //CALCULA EL PUNTO DONDE SE PRODUCE EL CAMBIO
    static double[] calculaPuntoCambio(int lon, int lon2, double l0, double b0, double velocidad, double[] data){
        int last = lon + lon2;
        int lont = last - first;
        double[] S = new double[lont];
        for (int i = 1+first; i<last; i++){
            int i1 = i+1;
            S[i-first] = 0;
            for (int k = i1; k<last; k++){
                double lfirst = l0 +b0*(k+1);
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

    static void detectaCambio() {
        // Atributos

//         for (int i = 0; i < 3; i++) {
        for (int n = 0; n < arrayLambda.length; n++) {
//            n = 3;
            threshold = arrayThreshold[n];
            l0 = arrayLambda[n]; // Lambda inicial

//            for (int threshold : arrayThreshold)
//            for(m in 1:(length(arrayVel)-1)) {
//            b0 = 0; // TODO PREGUNTAR SI DEBE SER 0 Ó 2.5
            for (int mm = 0; mm < arrayVel.length; mm++) {
                b1 = arrayVel[mm];
//                for (double b1 : arrayVel) {

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

//                for(j in 1:exp) {

                j = 0;
                while (j < exp){
                    //System.out.println("Experimento: " + j);
//                    # Inicio las constantes
//                    #       l0 <- 5.0 #Lambda inicial
//                    #   l0 <- 5
//                    #       b0 <- 0.09 #Velocidad antes del cambio
//                    #       b0 <- 0.0
//                    #     b1 <- 0.4 #Velocidad despues del cambio
//                    #     b1 <- 0.35 #Solo llega hasta 2
                    alarmi = lon + lon2;
//                    #       alarmi <- -1

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
                    v = new double[lon+lon2];

                    p[0] = 0; //TODO ¿¿p[0] y g[0] o p[1] y g[1]??
//                    v[0] = 0;
                    g[0] = 0;
                    alarma = false;


                    // Detecta que ha ocurrido un cambio
                    for (int i = 1; i < (lon + lon2); i++) {
                        double lbefore = l0 + i * b0; // Lambda si no hay cambio
                        double lafter = lbefore + l0 / 2; //Un poco despues de lbefore. Lo que suma deb ser constante
                        if (poissonFunction(data[i], lbefore) != 0) {
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

                    if (alarmi > lon) {
                        // DESPUES ESTIMO LA VELOCIDAD DESPUES DEL CAMBIO
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
                            //         mp[k,1] <- log(dpois(data[lon+1], lk)/dpois(data[lon+1], lv))
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
                        List<Double> regresion = regLineal(d, x);
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
                                // mp[k,1] <- log(dpois(data[lon+1], lk)/dpois(data[lon+1], lv))
                                mp[k][0] = myPos(data[lon], lv, lk);
                            }
                            for (int i = 1; i < lon2; i++) {
                                for (int k = 0; k < nven; k++) {
                                    double lk = lv + (k + 1) * (lfin - lv) / nven;
                                    double lk0 = lv + k * (lfin - lv) / nven;
                                    // mp[k,i] <- mp[k,i-1] + log(dpois(data[i+lon], lk)/dpois(data[i+lon], lk0))
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
                            regresion = regLineal(d, x);
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
                            time[j] = maxindex + first -1;
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
                        + " " + mean(velocidades) + " " + mean(velocidades2) + " " + sd(velocidades) + " " + mean(arlMayoresCero) + " " + sd(arlMayoresCero)
                        + " " + mean(time) + " " + sd(time) + " " + mean(time2) + " " + sd(time2)
                        + " " + mean(timeTeorica) + " " + sd(timeTeorica)
                        + " " + mean(timeDatos) + " " + sd(timeDatos)
                        + " " + errorVelocidad + " " + errorArl;

                System.out.println(output);
            }
        }

    }

    public static void main(String[] args){
        detectaCambio();
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
