package de.romjaki.mlplugin.evolutionary;

import de.romjaki.mlplugin.BaseAlgorithm;
import de.romjaki.pluggabledino.api.Emulator;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Float.max;
import static java.lang.Float.min;

public class EMain {

    private static Random random = ThreadLocalRandom.current();

    public static void main(String[] args) throws IOException {

        Population<Float> pop = new Population<>(100,
                random -> new Genetype<>(9, random::nextFloat));

        pop.evaluate(EMain::fitness);
        int gen = 0;
        while (fitness(pop.getFittest()) < 500f) {
            System.out.printf("Generation: %d | %s%n", gen, fitness(pop.getFittest()));
            gen++;
            pop = pop.decimate(EMain::changeFloat);
            pop.evaluate(EMain::fitness);
        }
        System.out.printf("Finished with  a score of %s after %d generations.%n", fitness(pop.getFittest()), gen);
        pop.save(new File("network.txt"), Objects::toString);
    }

    private static Float changeFloat(Float fl) {
        return tween(fl + random.nextFloat() - 0.5f, -1, 1);
    }

    private static Float tween(float val, float min, float max) {
        return max(min(val, max), min);
    }


    public static float fitness(Genetype<Float> genetype) {
        BaseAlgorithm algorithm = new EvolutionaryAlgorithm(genetype.getData(Float[]::new));
        return Emulator.INSTANCE.emulate(15f, world -> algorithm.getJumpFunction().apply(world), true);
    }
}
