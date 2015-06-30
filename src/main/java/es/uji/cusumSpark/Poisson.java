package es.uji.cusumSpark; /**
 * Created by hector on 23/06/15.
 */
import java.util.Random;
import java.lang.Math;

/**Distribution.java
 * This class generates various random variables for Poisson distribution
 * not directly supported in Java
 */
public class Poisson extends Random {

    public Poisson() {
        super();
    }

    public int nextPoisson(double lambda) {
        double elambda = Math.exp(-1*lambda);
        double product = 1;
        int count =  0;
        int result=0;
        while (product >= elambda) {
            product *= nextDouble();
            result = count;
            count++; // keep result one behind
        }
        return result;
    }

}

