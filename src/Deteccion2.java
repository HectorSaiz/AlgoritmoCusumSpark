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
        double[] resta = new double[];vector(length=tam)
        for(i in 1:tam) resta[i] <- data[i+1] - data[i]
        return (mean(resta))
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
    /*

velocidad <- function(data) {
  tam <- length(data)-1
  resta <- vector(length=tam)
  for(i in 1:tam) resta[i] <- data[i+1] - data[i]
  return (mean(resta))
}

v2 <- function(data) {
  #   S <- 0
  #   for(i in 1:length(data)) {
  #     S <- S + data[i]
  #   }
  #   return(S/length(data))

  return (sum(data)/length(data))
}

v3 <- function(data) {
  regresion <- lm(data ~ seq(1, length(data)))
  return (regresion$coefficients[2]*length(data))
}

vv3 <- function(data) {
  regresion <- lm(data ~ seq(1, length(data)))
  return (regresion$coefficients[2])
}

estimacionVelocidad <- function(l0, l1, l2, k) {
  return ((l2-l1-l0*log(l2/l1))/(k*log(l2/l1)))
}

v2d <- function(data, l0, lk0, lk) {
  mm <- vector(length=lon)
  mm[1] <- 0
  for(i in 2:lon) {
    mm[i] <- mm[i-1] + mypos(data[i], lk0, lk)
  }
  return (estimacionVelocidad(l0, lk0, lk, which.min(mm)))
}

calculaPuntoCambio <- function(lon, lon2, l0, b0, velocidad, data) {
  first <- 1
  last <- lon + lon2
  lont <- last - first
  S <- vector(length=lont)
#   G <- vector(length=lont)
  for(i in 2+first:last-1) {
    i1 <- i+1
    S[i-first] <- 0
    for(k in i1:last) {
      lfirst <- l0 + b0*k
      if(velocidad<0) print("fallo")
      lsecond <- l0 + b0*i + velocidad*(k-i)
      s <- mypos(data[k], lfirst, lsecond)
      S[i-first] <- S[i-first] + s
    }
  }
  #return (which.max(S) + first)
  return (S)
}

# arrayVel = c(0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95, 1, 1.5, 2, 2.5, 3)
# arrayVel = c(0.00, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95, 1, 1.5, 2, 2.5, 3)
arrayVel = c(0.05)
# arrayVel = c(0.0, 0.25)
# arrayThreshold = c(7, 9.8, 14.4)
# arrayLambda = c(5, 10, 20)
arrayThreshold = c(7)#, 9.8, 14.4)
arrayLambda = c(5)#, 10, 20)

lon <- 100 #Numero de datos antes del cambio
lon2 <- 50 #Numero de datos despues del cambio
exp <-
nven <- 15 #Numero de ventanas
errorVelocidad <- 0
errorArl <- 0

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
        ## Detecta que ha ocurrido un cambio
        for(i in 2:(lon+lon2)) {
          lbefore <- l0 + i*b0 #Lambda si no hay cambio
          lafter <- lbefore + l0/2 #Un poco despues de lbefore. Lo que suma debe ser constante
          if(dpois(data[i], lambda=lbefore) !=0) {
            #           s <- log(dpois(data[i], lambda=lafter)/dpois(data[i], lambda=lbefore))
            s <- mypos(data[i], lbefore, lafter)
            #           p[i] <- p[i-1] + log(dpois(data[i], lambda=lafter)/dpois(data[i], lambda=lbefore))
            p[i] <- p[i-1] + s
            if((g[i-1] + s) < 0) g[i] <- 0
            else g[i] <- g[i-1] + s
            if(g[i] > threshold & !alarma) {
              alarmi <- i
              alarma <- TRUE
              arl[j] <- alarmi - lon
            }
          }
        }

        #       cc <- 'b'
        if(alarmi > lon) {
          ### DESPUES ESTIMO LA VELOCIDAD DESPUES DEL CAMBIO
          # lv <- l0 + b0*1000
          lv <- l0 + b0*lon
          # La siguiente línea es buena idea para datos de twitter
          #       lfin <- lv + v2d(data[lon+1:lon2], l0, lv, lv+b1*lon2)*lon2
          #       lfin <- lv + b0*lon2
#                 lfin <- lv + b1 * lon2
#           vel3 <- 0
          vel3 <- vv3(data[(lon+1):(lon+lon2)])
          lfin <- lv + v3(data[(lon+1):(lon+lon2)])
          #       lfin <- lv + v2(data[(lon+1):(lon+lon2)])*lon2
          #       print(v2(data[(lon+1):(lon+lon2]))
          #       lfin <- 3*lv
          #       vv <- velocidad(data[lon:(lon+lon2)])
          #       vv <- velocidad(data[lon:alarmi])
          #       lfin <- lv + vv * lon2
          #         lfin <- data[lon+lon2] # Si tomo el último dato es bastante aceptable
          #         lfin <- data[alarmi]

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
