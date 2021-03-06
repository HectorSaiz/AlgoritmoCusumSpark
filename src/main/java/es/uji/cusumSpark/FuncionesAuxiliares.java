package es.uji.cusumSpark;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hector on 30/06/15.
 */
public class FuncionesAuxiliares {
    // Media
    static double mean(double[] x){
        double sum = 0;
        for (int i = 1; i < x.length; i++){
            sum += x[i];
        }
        double mean = sum / (x.length-1);
        return mean;
    }

    static double mean(List<Double> x){
        double sum = 0;
        for (double num : x){
            sum += num;
        }
        double mean = sum / (x.size());
        return mean;
    }

    // Factorial
    static double Factorial(double n) {
        if (n == 0)
            return 1;
        else
            return n * Factorial(n-1);
    }

    // Covarianza (x e y tienen la misma length)
    static double cov(double[] y, double[] x){
        double cov = 0;
        for (int i = 1; i < x.length; i++){
            cov += x[i]*y[i];
        }
        cov = cov / (x.length-1);
        cov -= mean(x)* mean(y);
        return cov;
    }

    static double cov(List<Double> y, List<Double> x){
        double cov = 0;
        for (int i = 0; i < x.size(); i++){
            cov += x.get(i)*y.get(i);
        }
        cov = cov / (x.size());
        cov -= mean(x)* mean(y);
        return cov;
    }

    // Varianza   TODO COMPROBAR FORMULA
    static double var(double[] x){
        double var = 0;
        for (int i = 1; i < x.length; i++){
            var += Math.pow(x[i],2);
        }
        var = var / (x.length-1);
        var -= Math.pow(mean(x), 2);
        return var;
    }

    static double var(List<Double> x){
        double var = 0;
        for (double num: x){
            var += Math.pow(num,2);
        }
        var = var / (x.size());
        var -= Math.pow(mean(x), 2);
        return var;
    }

    static double sd (double[] x) {
        return Math.sqrt(var(x));
    }

    static double sdError(double[] x) {
        return sd(x)/Math.sqrt(x.length-1);
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

    // f(x) de Poisson ( dpois(data,lambda) en R )
    static double poissonFunction(double x, double lambda){
        return Math.pow(lambda, x) / Factorial(x) * Math.pow(Math.E,-lambda);
    }
    static List<Double> regLineal(List<Double> y, List<Double> x){
        double b1 = cov(y,x)/var(x);
        double b0 = mean(y) - b1 * mean(x);
        List<Double> res = new ArrayList<>();
        res.add(b0);
        res.add(b1);
        return res;
    }
}