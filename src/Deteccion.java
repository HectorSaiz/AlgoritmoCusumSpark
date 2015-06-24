import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 6/22/15.
 */
public class Deteccion {
    // Carga los datos
    // TODO: Traducir a java
    /* wc <- read.csv("/home/oscar/Oscar/Investigacion/Twitter/TwitterStream/output/worldcupTimeFiltered2.csv"
            , sep=" ")
    */

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
        if ( l1 == 0 && l2 == 0 )
            return (0);
        if ( l1 == 0 || l2 == 0 ) {
            return (Math.log(poissonFunction(data, l2) / poissonFunction(data, l1)));
        } else
            return (l1 - l2 + Math.log(l2 / l1)*data);
    }

    //DETECCIÓN DEL CAMBIO (Habŕa que sobrecargar si queremos que tenga valores por defecto)
    // init: indice del primer dato
    // vEnd: indice del ultimo dato usado para estimar velocidad inicial
    // end: indice del ultimo dato para observar el cambio
    static List<Object> detectaCambio (int init, int lon, int vLon, int[] data) {
        double[] data2 = new double[vLon];
        double[] data3 = new double[lon];
        double[] x = new double[vLon];
        for (int i=0; i<vLon; i++){
            data2[i] = data[i+init];
            x[i] = i+1;
        }

        for (int i=0; i<lon; i++){
            data3[i] = data[i+init];
        }

        List<Double> regresion = regLineal(data2, x);
        double l0 = regresion.get(0);
        double b0 = regresion.get(1);
        double threshold = 10;

//        for (int i = 0; i<lon; i++){
//            data2 = wc.getArrival(i+init); //Pendiente de ver que será wc
//        }

        double[] pb = new double[lon];
        double[] gb = new double[lon];
        double[] pa = new double[lon];
        double[] ga = new double[lon];

        pb[0] = 0;
        gb[0] = 0;
        //   alarmab <- FALSE # alarma before
        //   ialarmb <- -1 # Índice de la alarma para la ventana por delante
        //   ialarma <- -1 # Índice de la alarma para la ventana por detrás
        //   alarmaa <- F # alarma after
        double ialarm = -1;
        for(int i=1; i<lon; i++) {
            double lbefore = l0 + i*b0; //Lambda si no hay cambio
            if(lbefore < 0)
                lbefore = 0;
            double la = lbefore + 0.2; //Un poco despues de lbefore. Lo que suma debe ser constante
            double s = myPos(data3[i], lbefore, la);
            pa[i] = pa[i-1] + s;
            if((ga[i-1] + s) < 0)
                ga[i] = 0;
            else
                ga[i] = ga[i-1] + s;
            if(ga[i] > threshold) {
                System.out.println("ga[i]" + i + " > threshold   " + ga[i]);
                //     if(ga[i] > threshold & !alarmaa) {
                //       ialarma <- i
                //       alarmaa <- TRUE
                ialarm = i;
                break;
            }

            double lb = lbefore - 0.2; // Un poco antes de lbefore.
            if(lb < 0)
                lb = 0;
            s = myPos(data[i], lbefore, lb);
            pb[i] = pb[i-1] + s;
            if((gb[i-1] + s) < 0)
                gb[i] = 0;
            else
                gb[i] = gb[i-1] + s;
            if(gb[i] > threshold) {
                System.out.println("gb[i]" + i + " > threshold   " + gb[i]);
                //     if(gb[i] > threshold & !alarmab) {
                //       ialarmb <- i
                //       alarmab <- TRUE
                ialarm = i;
                break;
            }
        }
        List<Object> result = new ArrayList<>(); // Habrá que mejorar esto... tal vez crear un objeto java para devolver el resultado bien estructurado
        result.add(l0);
        result.add(b0);
        result.add(init+ialarm-1);// -1 tal vez haya que quitarlo (tema diferencia de indices java y R)
        result.add(pa);
        result.add(pb);
        result.add(ga);
        result.add(gb);
        return result;
    }

    // CALCULA LA VELOCIDAD PARA UN INTERVALO DE DATOS
    // init: indice del primer dato
    // lon: longitud de los datos
    // nven: numero de ventanas
    static List<Double> calculaVelocidad(int init, int lon, int nven, int[] data){
        // Calculo una velocidad aproximada a partir de los datos
        double[] data2 = new double[lon];
        double[] aux = new double[lon];
        for (int i=0; i<lon; i++){
            data2[i] = data[i+init];
            aux[i] = i+1;
        }
        List<Double> regresion = regLineal(data2, aux);
        double lv = regresion.get(0);
        double lfin = lv + regresion.get(1)*lon;
        double[][] mp = new double[nven][lon];
        for(int k = 0; k<nven; k++) {
            double lk = lv + (lfin-lv)/nven;
            mp[k][0] = myPos(data[0], lv, lk);
        }
        for(int i=1; i<lon; i++) {
            for(int k=0; k<nven; k++) {
                double lk = lv + (k+1) * (lfin-lv)/nven;
                double lk0 = lv + k * (lfin-lv)/nven;
                mp[k][i] = mp[k][i-1] + myPos(data[i], lk0, lk);
            }
        }

        //Regresion de los datos
        double[] d = new double[nven];
        double[] x = new double[nven];
        for (int i = 0; i<nven;i++){
            x[i] = i+1;
            double auxmin = Double.POSITIVE_INFINITY;
            double minindex = 0;
            for (int j = 0; j<lon; j++){
                if ( auxmin > mp[i][j] ){
                    auxmin = mp[i][j];
                    minindex = j;
                }
            }
            d[i] = minindex;
        }
        List<Double> regresionVelocidad = regLineal(d, x);
        // Fin de la regresion
        double velocidad = (lfin-lv)/(nven*regresionVelocidad.get(1)); //Tamanyo una ventana / y de la regresion

        List<Double> result = new ArrayList<>();
        result.add(velocidad);
        result.add(lv);
        return(result);
    }

    // PARA CALCULAR EL PUNTO DE CAMBIO
    static int calculaPuntoCambio(int init, int lon, double l0, double b0, double velocidad, int[] data){
        int last = lon;
        double[] S = new double[lon];
        for (int i = 1; i<last-1; i++){
            int i1 = i+1;
            S[i-init] = 0;
            for (int k = i1; k<last; k++){
                double lfirst = l0 +b0*(k+1);
                if (velocidad<0){
                    System.out.println("fallo");
                }
                double lsecond = l0 +b0*i + velocidad*(k-i);
                double s = myPos(data[k], lfirst, lsecond);
                S[i-init] = S[i-init] + s;
            }
        }
        double auxmax = 0;
        int maxindex = 0;
        for (int i=0; i<S.length; i++){
            if ( auxmax < S[i]){
                auxmax = S[i];
                maxindex = i;
            }
        }
        return (maxindex+init);
    }

    //Main (Poisson de los métodos)
    public static void main(String args[]){
        Poisson p = new Poisson();

        int[] lista = new int[2001];
        double incremento = 0.1;

        for (int i = 0; i < 1000; i++ ){
            lista[i] =  p.nextPoisson( ((double) i) * incremento);
            System.out.println(lista[i]);
        }

        incremento = 0.25;


        for (int j = 1001; j < 2001; j++) {
            lista[j] =  p.nextPoisson( ((double) j) * incremento);
            System.out.println(lista[j]);
        }


        List<Object> resultDetectaCambio = detectaCambio(1, 2000, 600, lista);
        List<Double> resultCalculaVelocidad = calculaVelocidad(1, 1200, 80, lista);
        int puntoCambio = calculaPuntoCambio(1, 2000, ((double) resultDetectaCambio.get(0)), ((double) resultDetectaCambio.get(1)), resultCalculaVelocidad.get(0), lista );

        System.out.println(puntoCambio);

    }
}
