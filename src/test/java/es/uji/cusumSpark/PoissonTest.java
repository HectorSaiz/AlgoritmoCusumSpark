package es.uji.cusumSpark;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;

/**
 * Created by hector on 1/07/15.
 */
public class PoissonTest {
    @Test
    public void lambdaTest() {
        Poisson poisson = new Poisson();
        int limit = 1000000;
        long lambda = 0;
        double[] lambdas = new double[limit];

        for(int i = 0; i < limit; i++) {
            lambdas[i] = poisson.nextPoisson(2);
        }

        //System.out.println(lambda/1000000.0);
        assertEquals(2.0, FuncionesAuxiliares.mean(lambdas), 0.01);
        assertEquals(2.0, FuncionesAuxiliares.var(lambdas), 0.01);
    }
}