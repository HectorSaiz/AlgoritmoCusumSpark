package es.uji.cusumSpark;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.*;

/**
 * Created by root on 6/24/15.
 */
public class CusumGauss {

    private static double[] arrayVelA = new double[] {-1.0, 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.2, 1.4, 1.6, 2.0, 2.5, 3.0}; // Array de velocidades de las rectas, es decir de las pendientes
    private static double[] arrayVel = new double[] {-1.0, 3.0, 2.5, 2.0, 1.5, 1.0, 0.95, 0.85, 0.75, 0.65, 0.55, 0.45, 0.35, 0.25, 0.15, 0.05, 0.0}; // Array de velocidades de las rectas, es decir de las pendientes
    private static double[] arrayThreshold = new double[] {-1.0, 10.0, 9.8, 14.4};  // Array con los posibles valores umbral para los experimentos
    private static int[] arrayLambda = new int[] {-1, 5, 10, 20};  // Array con las posibles lambdas a tomar en los experimentos

    private static double threshold, beta1, beta2;
    private static int mu1;
    private static Poisson poisson = new Poisson();  // Clase encargada de proporcionar números aleatoriamente siguiento una distribución de Poisson

    public CusumGauss() {
        super();
    }

    // Getters and setters

    public static double[] getArrayVel() {
        return arrayVel;
    }

    public static void setArrayVel(double[] arrayVel) {
        CusumGauss.arrayVel = arrayVel;
    }

    public static double[] getArrayThreshold() {
        return arrayThreshold;
    }

    public static void setArrayThreshold(double[] arrayThreshold) {
        CusumGauss.arrayThreshold = arrayThreshold;
    }

    public static int[] getArrayLambda() {
        return arrayLambda;
    }

    public static void setArrayLambda(int[] arrayLambda) {
        CusumGauss.arrayLambda = arrayLambda;
    }


    static double mygauss(double data, double mu1, double mu2, double sigma){  //Antes myPos TODO -> HECHO
        double num = 2d*data*(mu2-mu1) + mu1*mu1 - mu2*mu2;
        double den = 2d*sigma*sigma;
        return (num/den);
    }

    private static Map<String, double[]> generaDatosLambda(double mu1, double sigma, double beta1, double beta2, int lon, int lon2) { // TODO -> HECHO
        RandomDataGenerator rdg = new RandomDataGenerator();
        double[] data = new double[lon+lon2+1], sdData = new double[lon+lon2+1], tmp = new double[5];
        double mean;
        Map<String, double[]> res = new HashMap<>();

        data[0] = 0d;
        sdData[0] = 0d;
        tmp[0] = 0d;

        for (int i = 1; i <= lon; i++) {
            mean = mu1 + i*beta1;
            for (int j = 1; j < 5; j++) {
                tmp[j] = rdg.nextGaussian(mean, 1.0);
            }
            data[i] = FuncionesAuxiliares.mean(tmp);
            sdData[i] = FuncionesAuxiliares.sd(tmp);
        }

        for (int i = (lon+1); i <= (lon+lon2); i++) {
            mean = mu1 + lon*beta1 + beta2*(i-lon);
            for (int j = 1; j < 5; j++) {
                tmp[j] = rdg.nextGaussian(mean, 1.0);
            }
            data[i] = FuncionesAuxiliares.mean(tmp);
            sdData[i] = FuncionesAuxiliares.sd(tmp);
        }

        res.put("data", data);
        res.put("sdData", sdData);
        return res;
    }

    // mu1 siempre será 0 si el proceso inicial no tiene velocidad.
    // lont es la longitud total de los datos.
    private static Map<String, Object> detectaCambio(int lont, double mu1, double mu2, double sigma, double threshold, Map<String, double[]> data) { // TODO -> HECHO
        double s, mu22, mu12;
        double[] pa = new double[lont + 1], ga = new double[lont + 1];
        double[] pb = new double[lont + 1], gb = new double[lont + 1];
        Map<String, Object> res = new HashMap<>();

        pa[0] = 0; // p after
        ga[0] = 0; // g after
        pb[0] = 0; // p before
        gb[0] = 0; // g before
        pa[1] = 0; // p after
        ga[1] = 0; // g after
        pb[1] = 0; // p before
        gb[1] = 0; // g before
        boolean alarma = false;
        int alarmi = -1;
        for (int i = 2; i <= lont; i++) {
//            mu12 = mu1 + i * beta1;
//            mu22 = mu12 + mu1 / 2.0;
            s = mygauss(data.get("data")[i], /*mu12*/mu1, /*mu22*/mu2, data.get("sdData")[i]);
            pa[i] = pa[i - 1] + s;
            if ((ga[i - 1] + s) < 0) {
                ga[i] = 0;
            } else {
                ga[i] = ga[i - 1] + s;
            }

//            if (beta1 > 0.1) {
//                System.out.println("beta1: " + beta1 + ", beta2: " + beta2 + ", mu1: " + mu1 + ", mu2: " + mu2);
//                System.out.println("s: " + s +", ga[i]: " + ga[i] + ", i: " + i + ", data[i]: " + data.get("data")[i] + ", data[i-1]: " + data.get("data")[i-1] + ", ---- sdData: " + data.get("sdData")[i]);
//                System.out.println("");
//            }

            if (ga[i] > threshold && !alarma) {
                alarmi = i;
                alarma = true;
                break;
            }
            // FIXME si no se cumple el if, devuelve alarmi = -1 y en las funciones que utilizan alarmi esto no se gestiona
        }

        res.put("alarmi", alarmi);
        res.put("ga", ga);
        res.put("alarma", alarma);

        return res;
    }

    static int calculaPuntoCambio(int lon, int lon2, double mu1, double sigma, double beta1, double beta2, Map<String, double[]> data) { // TODO -> HECHO
        int first = 1;
        int last = lon + lon2;
        int lont = last - first;
        int i1;
        double[] S = new double[lont+1];
        double mufirst, musecond, s;

//        System.out.println(beta1 + " ------- " + beta2);

        for (int i = 2 + first; i <= last-1; i++){
            i1 = i+1;
            S[i-first] = 0;
            for (int k = i1; k <= last; k++){
                mufirst = mu1 + beta1*k;
                if (beta2 < 0 ) System.out.println("Fallo en calculaPuntoCambio");
                musecond = mu1 + beta1*i + beta2*(k-i);
                s = mygauss(data.get("data")[k], mufirst, musecond, data.get("sdData")[k]);
                S[i-first] = S[i-first] + s;
            }
        }

//        if(length(which.max(S)) == 0) return (0) FIXME que significa esta comprobación
//        else return (which.max(S) + first)
        double auxmax = Double.NEGATIVE_INFINITY;
        int maxindex = 1;
        for (int i=1; i< S.length-1; i++){
            if ( auxmax < S[i]){
                auxmax = S[i];
                maxindex = i;
            }
        }
        return maxindex + first;
    }

    private static double estimaBeta2 (double mu1, double sigma, double muv, int lon, double mufin, int lon2, int nven, Map<String, double[]> data) { // TODO -> HECHO
        double[][] mp = new double[nven+1][lon2+1];
        double muk, muk0;

        for (int k = 1; k <= nven; k++) {
            muk = muv + (mufin - muv) / nven;
            mp[k][1] = mygauss(data.get("data")[lon + 1], muv, muk, data.get("sdData")[lon+1]);
        }

        for (int i = 2; i <= lon2; i++) {
            for (int k = 1; k <= nven; k++) {
                muk =  muv + k*(mufin-muv)/nven;
                muk0 = muv + (k-1)*(mufin-muv)/nven;
                mp[k][i] = mp[k][i - 1] + mygauss(data.get("data")[i + lon], muk0, muk, data.get("sdData")[i + lon]);
            }
        }

        //Regresión de los datos gauss
        List<Integer> minimos = new ArrayList<>();
        List<Double> mus = new ArrayList<>();
        double auxmin;
        int j = 1, minindex, min;

        minimos.add(0, 0); //FIXME OJO
        mus.add(0, 0d); //FIXME OJO

        for (int i = 1; i <= nven; i++) {
            auxmin = Double.POSITIVE_INFINITY;
            minindex = 1; // FIXME OJO
            for (int k = 1; k <= lon2; k++) {
                if (auxmin > mp[i][k]) {
                    auxmin = mp[i][k];
                    minindex = k;
                }
            }

            min = minindex;
            if (min > 1 && min < lon2) {
                minimos.add(j, min); // min = which.min(mp[i, ])
                mus.add(j, (mufin-muv)/nven*i );
                j++;
            }
        }

        if (minimos.size() < 0) {
            System.out.println("Error en la regresión");
            return -1d;
        }

        // FIXME EN R DEVUELVE MINIMOS Y MUS PERO NO PARECE QUE LOS UTILICE

//        Map<String, Object> Resultados = new HashMap<>();
//        Resultados.put("mus", mus);
//        Resultados.put("minimos", minimos);
        double[] mus2 = new double[mus.size()], minimos2 = new double[minimos.size()];

        for (int i = 0; i < mus.size(); i++) {
            mus2[i] = mus.get(i);
            minimos2[i] = minimos.get(i);
        }

        List<Double> regresion = FuncionesAuxiliares.regLineal(mus2, minimos2);
        return regresion.get(1);

        /**
         * TODO PREGUNTAR
         * regresion <- lm(mus ~ minimos);
         # Fin de la regresion
         #velocidad <- (mufin-muv)/nven/regresion$coefficients[2] #Tamanyo una ventana / y de la regresion
         velocidad <- regresion$coefficients[2]
         #return (velocidad)
         list(minimos=minimos, mus=mus) #Devuelva estas listas para hacer las gráficas del artículo.
         */
    }


    static double estimaBeta3(int lon, int lonT, double[] data) { // TODO -> HECHO
        double[] x = new double[lonT-lon+2], data2 = new double[lonT-lon+2];
        data2[0] = 0d;
        x[0] = 0d;
        int index = 1;

        for (int i=lon; i<=lonT; i++){
            x[index] = i;
            data2[index] = data[i];
            index++;
        }

        List<Double> regresion = FuncionesAuxiliares.regLineal(data2, x);
        return regresion.get(1);//*(data.length-1));
    }


    private static Map<String, Object> estimaPuntoCambio(double mu1, double sigma, double beta1, int lon, double beta2, int lon2, int nven, Map<String, double[]> data) { // TODO -> HECHO

        int lont = lon + lon2;
        // mu2 es la mu de la ventana que corre delante de la original y a cierta distancia
        double mu2 = 1.0; // FIXME OJO ESTÁ FIJO
        int threshold = 10; // FIXME OJO ESTÁ FIJO
        double betaEstimada3, beta2Estimada, mufinal;

        Map<String, Object> cambio = detectaCambio(lont, mu1, mu2, sigma, threshold, data);
        int copiaLon = (int) cambio.get("alarmi");
        int copiaLon2 = lont - copiaLon;

        betaEstimada3 = estimaBeta3((int) cambio.get("alarmi"), lont, data.get("data"));
        mufinal = ( (lont - (int) cambio.get("alarmi")) * betaEstimada3 ) + mu1;
        beta2Estimada = estimaBeta2(mu1, sigma, mu1+0.1, (int) cambio.get("alarmi"), mufinal, copiaLon2, nven, data);

        Map<String, Object> res = new HashMap<>();

        if (beta2Estimada < 0 /*|| Double.isInfinite(beta2Estimada) || Double.isNaN(beta2Estimada)*/) {
            res.put("puntoCambio", 0);
            res.put("betaEstimada", -1d);
            res.put("alarmi", 0);
        } else {
            int calculaCambio = calculaPuntoCambio(copiaLon, copiaLon2, mu1, sigma, beta1, beta2Estimada, data);
//            if (beta1 > 0.1)
//                System.out.println("******* " + calculaCambio);

            res.put("puntoCambio", calculaCambio);
            res.put("betaEstimada", beta2Estimada);
            res.put("alarmi", copiaLon);
        }
        return res;
    }

    private static double calculaBeta (double mu0, int t, int T, double tam, double[] data) { // TODO -> HECHO
        double[] dataTmp = new double[T-(t+1)+1];
        int index = 1;
        dataTmp[0] = 0d;
        for (int i = t+1; i <= T; i++) {
            dataTmp[index] = data[i];
            index++;
        }

        int[] time = new int[T-t+1];
        time[0] = 0;
        for (int i = 1; i <= T-t; i++) {
            time[i] = i;
        }

        double num, den, aux = 0;
        int maxLength;
        if (dataTmp.length > time.length) {
            maxLength = dataTmp.length;
        } else {
            maxLength = time.length;
        }

        for (int i = 0; i < maxLength; i++) {
            aux += dataTmp[i]*time[i];
        }
        num = 6*(aux-(mu0*(T-t)*(T-t+1)/2));
        den = (T-t)*(T-t+1)*(2*T-2*t+1);

        return num/den;
    }

    private static Map<String, double[]> estimadorTau (double mu0, int tau, int T, double tam, Map<String, double[]> data) { // TODO -> HECHO
        double[] taus = new double[T], betas = new double[T];
        double betaCalculada, a, b, c;
        double[] dataTmp;
        int index;
        int[] peso;
        taus[0] = 0d;
        betas[0] = 0d;

        for (int t = 0; t <= T-1; t++) {
            betaCalculada = calculaBeta(mu0, t,T, tam, data.get("data"));
            a = 1/2*betaCalculada*(T-t)*(T-t+1);
            b = mu0 + 1/6*betaCalculada*(2*T-2*t+1);

            dataTmp = new double[T-(t+1)+1];
            index = 1;
            dataTmp[0] = 0d;
            for (int i = t+1; i <= T; i++) {
                dataTmp[index] = data.get("data")[i];
                index++;
            }

            peso = new int[T-t];
            peso[0] = 0;
            for (int i = 1; i < T-t; i++) {
                peso[i] = i;
            }

            double aux = 0;
            int maxLength;
            if (dataTmp.length > peso.length) {
                maxLength = dataTmp.length;
            } else {
                maxLength = peso.length;
            }

            for (int i = 0; i < maxLength; i++) {
                aux += dataTmp[i]*peso[i];
            }
            c = betaCalculada*aux;

            taus[t+1] = -1/(data.get("sdData")[t+1]*data.get("sdData")[t+1]) * (a*b-c);
            betas[t+1] = betaCalculada;
        }

        Map<String, double[]> res = new HashMap<>();
        res.put("taus", taus);
        res.put("betas", betas);
        return res;
    }

    private static void unExperimento(double mu1, double sigma, double beta1, double beta2, int lon, int lon2, int nven, int exp){
        int i = 1;
        Map<String, double[]> data;
        double[] tausMio = new double[exp+1];
        double[] betasMio = new double[exp+1];
        Map<String, Object> res;
        int errorVel = 0;
        int errorArl = 0;
        tausMio[0] = 0d;
        betasMio[0] = 0d;

        System.out.println("Experimento, beta1: " + beta1 + ", beta2: " + beta2);

        while (i <= exp){
//            System.out.println("experimento: " + i);
            data = generaDatosLambda(mu1, sigma, beta1, beta2, lon, lon2);
            res = estimaPuntoCambio(mu1, sigma, beta1, lon, beta2, lon2, nven, data);

            if ((int)res.get("alarmi") > lon && !Double.isNaN((double)res.get("betaEstimada")) && !Double.isInfinite((double)res.get("betaEstimada")) ) {
//                if ((int)res.get("alarmi") > (int) res.get("puntoCambio") && (int) res.get("puntoCambio") > lon) {
                tausMio[i] = (int) res.get("puntoCambio");
                betasMio[i] = (double) res.get("betaEstimada");
//                if(beta1 > 0) System.out.println("alarmi: " + res.get("alarmi") + ", puntoCambio: " + res.get("puntoCambio") + ", betaEstimada: " + res.get("betaEstimada"));

                i++;

            } else {
//                System.out.println("Falsa alarma");
                errorArl++;
            }
        }
        muestraResultadosExperimentos(threshold, mu1, beta1, beta2, tausMio, betasMio, errorArl, errorVel);
    }

    private static void muestraResultadosExperimentos(double threshold, double mu1, double beta1, double beta2, double[] tausMio, double[] betasMio, int errorArl, int errorVelocidad) {
        String output;

//        List arlMC = new ArrayList<Double>();
//        for (double v1 : arl) {
//            if (v1 > 0) arlMC.add(v1);
//        }
//        double[] arlMayoresCero = new double[arlMC.size()];
//        for (int i =0; i < arlMC.size(); i++){
//            arlMayoresCero[i] = (double) arlMC.get(i);
//        }

        output = "" + threshold + " " + mu1 + " " + beta1 + " " + beta2
                + " TausMio mean: " + FuncionesAuxiliares.mean(tausMio) + " " + " TausMio sd: " + FuncionesAuxiliares.sdError(tausMio)
                + " betasMio mean: " + FuncionesAuxiliares.mean(betasMio) + " betasMio sd: " + FuncionesAuxiliares.sdError(betasMio)
                + " " + errorVelocidad + " " + errorArl + " ";

        System.out.println(output);
    }

    /**
     * Detecta cuando se ha producido un cambio en la tendencia
     * y calcula en punto en el que se ha producido dicho cambio
     */
    public static void realizaExperimentos() {

        for (int n = 1; n < arrayLambda.length; n++) {
            threshold = arrayThreshold[n]; // Establece el umbral
            mu1 = arrayLambda[n]; // Establece la lambda inicial

            for (int indexb0 = 1; indexb0 < arrayVelA.length - 1; indexb0++) {
                beta1 = arrayVelA[indexb0];

//            for (int threshold : arrayThreshold)
                for (int indexb1 = indexb0 + 1; indexb1 < arrayVelA.length; indexb1++) {

                    beta2 = arrayVelA[indexb1];
                    unExperimento(0, 1, beta1, beta2, 50, 50, 10, 10000);
//                    unExperimento(mu1, beta1, beta2, 100, 50, 15, threshold, 10000);
//                    boolean inverse = false;
//                    if (b1 < b0){
//                        inverse = true;
//                        int aux = lon;
//                        lon = lon2;
//                        lon2 = aux;
//                        double baux = b0;
//                        b0 = b1;
//                        b1 = baux;
//                    }
//                    e = new double[exp+1];
//                    arl = new double[exp+1];
//
//                    data = new double[lon + lon2+1];
//
//                    mp = new double[nven+1][lon2+1];
//
//                    velocidades2 = new double[exp+1];
//
//                    time = new double[exp+1];
//                    time2 = new double[exp+1];
//                    timeTeorica = new double[exp+1];
//                    timeDatos = new double[exp+1];
//                    errorVelocidad = 0;
//                    errorArl = 0;
//
//                    j = 1;
//
//                    while (j <= exp) {
////                    System.out.println("Experimento: " + j);
//                        alarmi = lon + lon2;
////                    #       alarmi <- -1
//
////                    FIXME Realizar las calculos para los diferentes valores de b0
//
//
//
//                        /*BufferedReader br = null;
//
//                        try {
//
//                            String sCurrentLine;
//
//                            br = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/Pois R/Pois(0.45-0.55)R "+j+" .txt"));
//
//                            while ((sCurrentLine = br.readLine()) != null) {
//                                data[z] = Double.parseDouble(sCurrentLine);
//                                z++;
//                            }
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        } finally {
//                            try {
//                                if (br != null)br.close();
//                            } catch (IOException ex) {
//                                ex.printStackTrace();
//                            }
//                        }*/
//
//
//
//
//                        alarma = false;
//
////                    Se comprueba si ha habido algún cambio en la tendencia.
//                        detectaCambio();
//                        //System.out.println("Cambio detectado en :" + alarmi); // 38 y 83
//
//                        if (alarmi >= lon) {
//                            // Primera estimacion de la velocidad
//                            lv = l0 + b0 * lon;
//                            double[] datav3 = new double[lon2+1];
//                            for (int i = 1; i <= lon2; i++) {
//                                datav3[i] = data[lon + i];
//                            }
//                            lfin = lv + v3(datav3);
//                            List<Double> regresion = calculaVelocidad(lv, lfin);
//                            velocidades[j] = (lfin - lv) / nven / regresion.get(1); //Tamaño una ventana / y de la regresion
//                            velocidades2[j] = velocidades[j];
//                            // Fin del cálculo con la velocidad estimada a partir de los datos
//
//                            // Doy una nueva pasada con la velocidad calculada
//                            //if (velocidades[j] > 0 && Math.abs(velocidades[j] - b1) < b1) {
//                            if ( velocidades[j] > 0 && Math.abs(velocidades[j]-b1) < b1){
//                                lv = l0 + b0 * lon;
//                                lfin = lv + velocidades[j] * lon2;
//                                regresion = calculaVelocidad(lv, lfin);
//                                velocidades[j] = (lfin - lv) / nven / regresion.get(1); //Tamaño una ventana / y de la regresion
//                            }
//                            // Fin de la segunda pasada
//
//                            // FINALMENTE CALCULO EL PUNTO DE CAMBIO
//                            // Esta condición es para eliminar velocidades erroneas
//                            //if (velocidades[j] > 0 && Math.abs(velocidades[j] - b1) < b1) {
//                            if ( velocidades[j] > 0 && Math.abs(velocidades[j]-b1) < b1 ){
//                                time[j] = calculaPuntoCambio(lon, lon2, l0, b0, velocidades[j], data);
//                                if ( inverse ){
//                                    time[j] += (lon2-lon);
//                                }
//                                if ( time[j] == 2){
//                                    //System.out.println("Velocidad: "+velocidades[j]+"  alarma: "+alarmi);
//                                }
//                                //System.out.println("Punto de cambio: "+ time[j]);
////                            time[j] = maxindex;
//                                // time[j] <- calculaPuntoCambio(lon, lon2, l0, b0, velocidades[j], data)
//                                j = j + 1;
//
//                            } else {
//                                velocidades[j] = -2;
//                                errorVelocidad = errorVelocidad + 1;
//                            }
//                        } else {
//                            time[j] = -1;
//                            velocidades[j] = -1;
//                            errorArl = errorArl + 1;
//                        }
//                    }
//                    if (inverse){
//                        int aux = lon;
//                        lon = lon2;
//                        lon2 = aux;
//                        double baux = b0;
//                        b0 = b1;
//                        b1 = baux;
//                    }
//                    muestraResultadosExperimentos(b1);
//                }
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