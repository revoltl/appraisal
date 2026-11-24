package com.ruivo.appraisal;

import com.ruivo.appraisal.appraisal.ProbabilityMath;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProbabilityMathTest {

    private static DoubleSupplier seeded(long seed) {
        Random random = new Random(seed);
        return random::nextDouble;
    }

    @Test
    void probability350AveragesThreeAndAHalf() {
        DoubleSupplier random = seeded(20261121L);
        int total = 0;
        int rounds = 100_000;
        for (int i = 0; i < rounds; i++) {
            total += ProbabilityMath.independentCount(350.0D, random);
        }
        double average = (double) total / rounds;
        assertTrue(average > 3.47D && average < 3.53D, "350 的平均份数应当接近 3.5，实测 " + average);
    }

    @Test
    void probability100AlwaysYieldsOne() {
        DoubleSupplier random = seeded(7L);
        for (int i = 0; i < 1_000; i++) {
            assertEquals(1, ProbabilityMath.independentCount(100.0D, random));
        }
    }

    @Test
    void probability40HitsAboutFourTimesInTen() {
        DoubleSupplier random = seeded(99L);
        int hits = 0;
        int rounds = 100_000;
        for (int i = 0; i < rounds; i++) {
            hits += ProbabilityMath.independentCount(40.0D, random);
        }
        double rate = (double) hits / rounds;
        assertTrue(rate > 0.39D && rate < 0.41D, "40 的命中率应当接近 0.4，实测 " + rate);
    }

    @Test
    void zeroProbabilityYieldsNothing() {
        DoubleSupplier random = seeded(3L);
        for (int i = 0; i < 100; i++) {
            assertEquals(0, ProbabilityMath.independentCount(0.0D, random));
        }
    }

    @Test
    void weightedIndexFollowsThreeToOne() {
        double[] weights = {300.0D, 100.0D};
        DoubleSupplier random = seeded(4242L);

        int heavyHits = 0;
        int rounds = 100_000;
        for (int i = 0; i < rounds; i++) {
            if (ProbabilityMath.weightedIndex(weights, random) == 0) {
                heavyHits++;
            }
        }

        double share = (double) heavyHits / rounds;
        assertTrue(share > 0.74D && share < 0.76D, "权重 3 比 1 应当接近七成五，实测 " + share);
    }

    @Test
    void weightedIndexIgnoresZeroWeights() {
        double[] weights = {0.0D, 0.0D};
        assertEquals(-1, ProbabilityMath.weightedIndex(weights, seeded(1L)));

        double[] single = {0.0D, 5.0D, 0.0D};
        DoubleSupplier random = seeded(2L);
        for (int i = 0; i < 200; i++) {
            assertEquals(1, ProbabilityMath.weightedIndex(single, random));
        }
    }
}
