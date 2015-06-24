import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 6/24/15.
 */
public class Deteccion2 {

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
        //if ( l1 == 0 && l2 == 0 )
        //    return (0);
        //if ( l1 == 0 || l2 == 0 ) {
        //    return (Math.log(poissonFunction(data, l2) / poissonFunction(data, l1)));
        //} else
        return (l1 - l2 + Math.log(l2 / l1)*data);
    }

    static double velocidad(int[] data){
        int tam = data.length -1;
        double[] resta = new double[tam];
        for (int i=0; i<tam; i++)
            resta[i] = data[i+1] - data[i];
        return (mean(resta));
    }

    static double v2(int[] data){
        double sum = 0;
        for (int i = 0; i < data.length; i++){
            sum += data[i];
        }
        return (sum/data.length);
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

    static double v2d(double[] data, double l0, double lk0, double lk){
        double[] mm = new double[lon];
        mm[1] = 0;
        for(int i = 1; i<lon; i++) {
            mm[i] = mm[i-1] + myPos(data[i], lk0, lk);
        }
        double auxmin = Double.POSITIVE_INFINITY;
        double minindex = 0;
        for (int j = 0; j<lon; j++){
            if ( auxmin > mm[j] ){
                auxmin = mm[j];
                minindex = j;
            }
        }
        return (estimacionVelocidad(l0, lk0, lk, minindex));
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

    //CALCULA EL PUNTO DONDE SE PRODUCE EL CAMBIO
    static int calculaPuntoCambio(int lon, int lon2, double l0, double b0, double velocidad, int[] data){
        int last = lon + lon2;
        int lont = last - 1;
        double[] S = new double[lont];
        for (int i = 2; i<lont; i++){
            int i1 = i+1;
            S[i-1] = 0;
            for (int k = i1; k<last; k++){
                double lfirst = l0 +b0*(k+1);
                if (velocidad<0){
                    System.out.println("fallo");
                }
                double lsecond = l0 +b0*i + velocidad*(k-i);
                double s = myPos(data[k], lfirst, lsecond);
                S[i-1] = S[i-1] + s;
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
        return (maxindex+1);
    }
    /*

*/


    static void detectaCambio() {
        // Atributos

        double[] arrayVel = new double[] {0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95, 1, 1.5, 2, 2.5, 3};   // Array de velocidades de las rectas, es decir de las inclinaciones
        double[] arrayThreshold = new double[] {7, 9.8, 14.4};  // Array con los posibles valores umbral para los experimentos
        int[] arrayLambda = new int[] {5, 10, 20};  // Array con las posibles lambdas a tomar en los experimentos

        int lon = 100;  // Cantidad de números antes de introducir en cambio
        int lon2 = 50;  // Cantidad de números después de introducir el cambio
        int exp = 100;  // Cantidad de experimentos a realizar TODO incremenar hasta los 10000
        int nven = 15;  // Cantidad de ventanas a utilizar
        int errorVelocidad = 0;
        int errorArl = 0;

        double threshold, l0, b0, b1;
//         for (int i = 0; i < 3; i++) {
        for (int n = 0; n < arrayLambda.length; n++) {
//            n = 3;
            threshold = arrayThreshold[n];
            l0 = arrayLambda[n]; // Lambda inicial

//            for (int threshold : arrayThreshold)
//            for(m in 1:(length(arrayVel)-1)) {
            b0 = 0;
            for (int mm = 0; mm < arrayVel.length; mm++) {
                b1 = arrayVel[mm];
//                for (double b1 : arrayVel) {



                }
            }

        }

/*
# for(n in seq(1:3)) {
for(n in 1:length(arrayLambda)) {
  #   n <- 3
  threshold = arrayThreshold[n]
  l0 <- arrayLambda[n] #Lambda inicial

  #   for(threshold in arrayThreshold) {
  #for(m in 1:(length(arrayVel)-1)) {
    b0 <- 0;
    for(mm in 1:length(arrayVel)) {
      b1 <- arrayVel[mm]
      #   for(b1 in arrayVel) {

      e <- vector(length=exp)
      arl <- vector(length=exp) # Guardo Averange Run Length: el instante donde salta la alarma.

      data <- vector(length=(lon+lon2))

      mp <- matrix(nrow=nven, ncol=lon2)
      velocidades <- vector(length=exp)
      velocidades2 <- vector(length=exp)

      time <- vector(length=exp)
      time2 <- vector(length=exp)
      timeTeorica <- vector(length=exp)
      timeDatos <- vector(length=exp)

      #     for(j in 1:exp) {
      j <- 1
      while(j <= exp) {
        cat("Experimento: ", j, "\r")
        # Inicio las constantes
        #       l0 <- 5.0 #Lambda inicial
        #   l0 <- 5
        #       b0 <- 0.09 #Velocidad antes del cambio
        #       b0 <- 0.0
        #     b1 <- 0.4 #Velocidad despues del cambio
        #     b1 <- 0.35 #Solo llega hasta 2
        alarmi <- lon + lon2
        #       alarmi <- -1

        for(i in 1:lon) {
          l <- l0 + i*b0
          data[i] <- rpois(1, lambda=l)
        }

        first=lon+1
        last= lon + lon2
        for(i in first:last) {
          l <- l0 + b0*lon + b1*(i-lon)
          data[i] <- rpois(1, lambda = l)
        }

        p <- vector(length=lon+lon2)
        v <- vector(length=lon+lon2)
        g <- vector(length=lon+lon2)

        p[1] <- 0
        g[1] <- 0
        alarma <- FALSE
        cc <- 'a'
        */

    static void funct_main_partido(){
        // Detecta que ha ocurrido un cambio
        for (int i=1; i<(lon+lon2); i++){
            double lbefore = l0 + i*b0; // Lambda si no hay cambio
            double lafter = lbefore + l0/2; //Un poco despues de lbefore. Lo que suma deb ser constante
            if (poissonFunction(data[i], lbefore) != 0) {
                // s <- log(dpois(data[i], lambda=lafter)/dpois(data[i], lambda=lbefore))
                s = myPos(data[i], lbefore, lafter);
                // p[i] <- p[i-1] + log(dpois(data[i], lambda=lafter)/dpois(data[i], lambda=lbefore))
                p[i] = p[i-1] + s;
                if((g[i-1] + s) < 0){
                    g[i] = 0;
                }
                else {
                    g[i] = g[i-1] + s;
                }
                if(g[i] > threshold & !alarma) {
                    alarmi = i;
                    alarma = true;
                    arl[j] = alarmi - lon;
                }
            }
        }
        // cc = 'b'
        if(alarmi > lon) {
            // DESPUES ESTIMO LA VELOCIDAD DESPUES DEL CAMBIO
            // lv <- l0 + b0*1000
            double lv = l0 + b0 * lon;
            // La siguiente línea es buena idea para datos de twitter
            //       lfin <- lv + v2d(data[lon+1:lon2], l0, lv, lv+b1*lon2)*lon2
            //       lfin <- lv + b0*lon2
            //                 lfin <- lv + b1 * lon2
            //           vel3 <- 0
            double[] datav3 = new double[lon2];
            for (int i = 0; i < lon2; i++) {
                datav3[i] = data[i + lon];
            }
            double vel3 = vv3(datav3);
            double lfin = lv + v3(datav3);
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
        }
    }

        /*

          #Regresion de los datos
          d <- vector(length=nven)
          x <- seq(from=1, to=nven)
          for(i in 1:nven) {
            d[i] <- which.min(mp[i,])
          }
          regresion <- lm(d ~ x)
          #Fin de la regresion
          velocidades[j] <- (lfin-lv)/nven/regresion$coefficients[2] #Tamanyo una ventana / y de la regresion
          velocidades2[j] <- velocidades[j]
          #if(velocidades2[j]>0 && abs(velocidades2[j] - b1) < b1) {
          #  time2[j] <- calculaPuntoCambio(lon, lon2, l0, b0, velocidades2[j], data)
          #}

          # Calculo el punto de cambio con la velocidad teórica
          #timeTeorica[j] <- calculaPuntoCambio(lon, lon2, l0, b0, b1, data)
          # Fin del cálculo con la velocidad teórica

          # Calculo el punto de cambio con la velocidad estimada a partir de los datos
          #if(vel3>0 && abs(vel3 - b1) < b1) {
          #  timeDatos[j] <- calculaPuntoCambio(lon, lon2, l0, b0, vel3, data)
          #}
          # Fin del cálculo con la velocidad estimada a partir de los datos

          #           Doy una nueva pasada con la velocidad calculada
          if(velocidades[j]>0 && abs(velocidades[j] - b1) < b1) {
                      lv <- l0 + b0*lon

                      lfin <- lv + velocidades[j]*lon2

                      for(k in 1:nven) {
                        lk <- lv + (lfin-lv)/nven
                        #         mp[k,1] <- log(dpois(data[lon+1], lk)/dpois(data[lon+1], lv))
                        mp[k,1] <- mypos(data[lon+1], lv, lk)
                      }
                      for(i in 2:lon2) {
                        for(k in 1:nven) {
                          lk <- lv + k * (lfin-lv)/nven
                          lk0 <- lv + (k-1) * (lfin-lv)/nven
                          #           mp[k,i] <- mp[k,i-1] + log(dpois(data[i+lon], lk)/dpois(data[i+lon], lk0))
                          mp[k,i] <- mp[k,i-1] + mypos(data[i+lon], lk0, lk)
                        }
                      }
                      #Regresion de los datos
                      d <- vector(length=nven)
                      x <- seq(from=1, to=nven)
                      for(i in 1:nven) {
                        d[i] <- which.min(mp[i,])
                      }
                      regresion <- lm(d ~ x)
                      #Fin de la regresion
                      velocidades[j] <- (lfin-lv)/nven/regresion$coefficients[2] #Tamanyo una ventana / y de la regresion
          }
          #           Fin de la segunda pasada



          ### FINALMENTE CALCULO EL PUNTO DE CAMBIO
          # Esta condición es para eliminar velocidades erroneas
          if(velocidades[j]>0 && abs(velocidades[j] - b1) < b1) {
#             first <- 1
#             last <- lon + lon2
#             lont <- last - first
#             S <- vector(length=lont)
#             G <- vector(length=lont)
#             for(i in 2+first:last-1) {
#               i1 <- i+1
#               S[i-first] <- 0
#               for(k in i1:last) {
#                 lfirst <- l0 + b0*k
#                 if(velocidades[j]<0) print("fallo")
#                 lsecond <- l0 + b0*i + velocidades[j]*(k-i)
#                 s <- mypos(data[k], lfirst, lsecond)
#                 S[i-first] <- S[i-first] + s
#               }
#             }
#             time[j] <- which.max(S) + first
            SS <- calculaPuntoCambio(lon, lon2, l0, b0, velocidades[j], data)
            time[j] <- which.max(SS) + first
            #time[j] <- calculaPuntoCambio(lon, lon2, l0, b0, velocidades[j], data)
            j <- j+1
          } else {
            velocidades[j] <- -2
            errorVelocidad <- errorVelocidad + 1
          }
        } else {
          time[j] <- -1
          velocidades[j] <- -1
          errorArl <- errorArl + 1
        }

      } ### Fin de los experimentos


      ### Muestro los resultados
      #     output <- c("\n", b1, "___", mean(arl[arl>0]), "___", mean(time[time>0]), "___", sd(time[time>0]), "\n")
      #     output <- toString(c(b1, mean(arl[arl>0]), mean(time[time>0]), sd(time[time>0])))
      #     output <- c(threshold, l0, b1, mean(velocidades), sd(velocidades), mean(arl[arl>0]), mean(time[time>0]), sd(time[time>0]))
      #     output <- sprintf("%f %f %f %f %f %f %f %f %f %f %f %f", threshold, l0, b0,  b1, lv + b1 * lon2, lfin, vel3, mean(velocidades), sd(velocidades), mean(arl[arl>0]), sd(arl[arl>0]), mean(time[time>100]), sd(time[time>100]))
      output <- paste(threshold, l0, b0,  b1, lv + b1 * lon2, lfin, vel3,
                      mean(velocidades), mean(velocidades2), sd(velocidades), mean(arl[arl>0]), sd(arl[arl>0]),
                      mean(time), sd(time), mean(time2), sd(time2),
                      mean(timeTeorica), sd(timeTeorica),
                      mean(timeDatos), sd(timeDatos),
                      errorVelocidad, errorArl,
                      sep=" ")
      print(output)
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
