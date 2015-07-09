package es.uji.cusumSpark;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Test;

import static org.junit.Assert.*;

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

    @Test
    public void apacheTest() {
        PoissonDistribution poissonDistribution = new PoissonDistribution(2);
        RandomDataGenerator rdg = new RandomDataGenerator();

        int limit = 1000000;
        long lambda = 0;
        double[] lambdas1 = new double[limit];
        double[] lambdas2 = new double[limit];

        for(int i = 0; i < limit; i++) {
            lambdas1[i] = poissonDistribution.sample();
            lambdas2[i] = rdg.nextPoisson(2);
        }

        //System.out.println(lambda/1000000.0);
        assertEquals(2.0, FuncionesAuxiliares.mean(lambdas1), 0.01);
        assertEquals(2.0, FuncionesAuxiliares.var(lambdas1), 0.01);
        assertEquals(2.0, FuncionesAuxiliares.mean(lambdas2), 0.01);
        assertEquals(2.0, FuncionesAuxiliares.var(lambdas2), 0.01);
    }
}