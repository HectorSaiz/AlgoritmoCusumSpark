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
    static List<Object> detectaCambio (int init, int lon, int vLon, double[] data) {
        double[] data2 = new double[vLon];
        double[] x = new double[vLon];
        for (int i=0; i<vLon; i++){
            data2[i] = data[i+init];
            x[i] = i+1;
        }

        List<Double> regresion = regLineal(data2, x);
        double l0 = regresion.get(0);
        double b0 = regresion.get(1);
        int threshold = 10;

        for (int i = 0; i<lon; i++){
            data2 = wc.getArrival(i+init); //Pendiente de ver que será wc
        }

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
            double s = myPos(data2[i], lbefore, la);
            pa[i] = pa[i-1] + s;
            if((ga[i-1] + s) < 0)
                ga[i] = 0;
            else
                ga[i] = ga[i-1] + s;
            if(ga[i] > threshold) {
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

    //Main (pruebas de los métodos)
    public static void main(String args[]){

    }



    // DETECCIÓN DEL CAMBIO
    // TODO: Traducir a java
    /*
    # init: índice del primer dato
    # vEnd: índice del último dato usado para estimar velocidad inicial
    # end: índice del último dato para observar el cambio
    detectaCambio <- function(init=1, lon=2000, vLon=600, data) {
        vEnd <- init + vLon -1
        data <- data[init:vEnd]

        regresion <- lm(data ~ seq(1, vLon))

        l0 <- regresion$coefficients[1]
        b0 <- regresion$coefficients[2]
        threshold <- 10

        end <- init + lon - 1
        data <- wc$arrivals[init:end]
        pb <- vector(length=lon) # p before
        gb <- vector(length=lon) # g before
        pa <- vector(length=lon) # p after
        ga <- vector(length=lon) # g after

        pb[1] <- 0
        gb[1] <- 0
        #   alarmab <- FALSE # alarma before
        #   ialarmb <- -1 # Índice de la alarma para la ventana por delante
        #   ialarma <- -1 # Índice de la alarma para la ventana por detrás
        #   alarmaa <- F # alarma after
        ialarm <- -1
        for(i in 2:lon) {
            lbefore <- l0 + i*b0 #Lambda si no hay cambio
            if(lbefore < 0) lbefore <- 0
            la <- lbefore + 0.2 #Un poco despues de lbefore. Lo que suma debe ser constante
            s <- mypos(data[i], lbefore, la)
            pa[i] <- pa[i-1] + s
            if((ga[i-1] + s) < 0) ga[i] <- 0
            else ga[i] <- ga[i-1] + s
            if(ga[i] > threshold) {
                #     if(ga[i] > threshold & !alarmaa) {
                    #       ialarma <- i
                    #       alarmaa <- TRUE
                    ialarm <- i
                    break
                }

                lb <- lbefore - 0.2 # Un poco antes de lbefore.
                if(lb < 0) lb <- 0
                s <- mypos(data[i], lbefore, lb)
                pb[i] <- pb[i-1] + s
                if((gb[i-1] + s) < 0) gb[i] <- 0
                else gb[i] <- gb[i-1] + s
                if(gb[i] > threshold) {
                    #     if(gb[i] > threshold & !alarmab) {
                        #       ialarmb <- i
                        #       alarmab <- TRUE
                        ialarm <- i
                        break
                    }
                }
                result <- list(l0=l0, b0=b0, ialarm=init+ialarm-1, pa=pa, pb=pb,
                        ga=ga, gb=gb)
                return(result)
            }
            */




    // CALCULA LA VELOCIDAD PARA UN INTERVALO DE DATOS
    // TODO: Traducir a java
            /*
            # ini: índice del primer dato
            # lon: longitud de los datos
            # nven: número de ventanas
            calculaVelocidad <- function(init=1, lon=1200, nven=15, data) {
                # Calculo una velocidad aproximada a partir de los datos
                data <- data[init:(init+lon-1)]
                regresion.datos <- lm(data ~ seq(1:lon))
                lv <- regresion.datos$coefficients[1]
                lfin <- lv + regresion.datos$coefficients[2]*lon
                mp <- matrix(nrow=nven, ncol=lon)
                for(k in 1:nven) {
                    lk <- lv + (lfin-lv)/nven
                    mp[k,1] <- mypos(data[1], lv, lk)
                }
                for(i in 2:lon) {
                    for(k in 1:nven) {
                        lk <- lv + k * (lfin-lv)/nven
                        lk0 <- lv + (k-1) * (lfin-lv)/nven
                        mp[k,i] <- mp[k,i-1] + mypos(data[i], lk0, lk)
                    }
                }

                #Regresion de los datos
                d <- vector(length=nven)
                x <- seq(from=1, to=nven)
                for(i in 1:nven) {
                    d[i] <- which.min(mp[i,])
                }
                regresion.velocidad <- lm(d ~ x)
                #Fin de la regresion
                velocidad <- (lfin-lv)/(nven*regresion.velocidad$coefficients[2]) #Tamanyo una ventana / y de la regresion

                result <- list(velocidad=velocidad, l0=lv)
                return(result)
            }*/

    // PARA CALCULAR EL PUNTO DE CAMBIO
    // TODO: Traducir a java
            /*
            calculaPuntoCambio <- function(init=1, lon, l0, b0, velocidad, data) {
                last <- lon
                S <- vector(length=lon)
                for(i in 2+init:last-1) {
                    i1 <- i+1
                    S[i-init] <- 0
                    for(k in i1:last) {
                        lfirst <- l0 + b0*k
                        if(velocidad<0) print("fallo")
                        lsecond <- l0 + b0*i + velocidad*(k-i)
                        s <- mypos(data[k], lfirst, lsecond)
                        S[i-init] <- S[i-init] + s
                    }
                }
                return (which.max(S) + init)
            }*/
}
