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

    /**
     * Ein schneller Einstiegspunkt zum trainieren eines Netzwerks
     *
     * @param args die System argumente
     * @throws IOException Bei fehlenden Berechtigungen
     */
    public static void main(String[] args) throws IOException {
        // Erstelle eine zufällige Population
        Population<Float> pop = new Population<>(100,
                random -> new Genetype<>(9, random::nextFloat));

        // Generiere eine erste Fitness
        pop.evaluate(EMain::fitness);
        int gen = 0;

        // Solange niemand weiter als 500 Einheiten kommt
        while (fitness(pop.getFittest()) < 500f) {
            System.out.printf("Generation: %d | %s%n", gen, fitness(pop.getFittest()));
            gen++;
            // Erstelle und bewerte eine neue Generation
            pop = pop.decimate(EMain::changeFloat);
            pop.evaluate(EMain::fitness);
        }
        // Speichere die letzte Generation
        System.out.printf("Finished with  a score of %s after %d generations.%n", fitness(pop.getFittest()), gen);
        pop.save(new File("network.txt"), Objects::toString);
    }

    /**
     * Eine Funktion welche einen Float zufällig mutiert
     *
     * @param fl der zu mutierende Float
     * @return der mutierte Float
     */
    private static Float changeFloat(Float fl) {
        return tween(fl + random.nextFloat() - 0.5f, -1, 1);
    }

    /**
     * Eine Funktion die einen Wert zwischen zwei werte einfügt
     *
     * @param val der einzufügende Wert
     * @param min das minimum
     * @param max das maximum
     * @return der wert welcher zwischen minumum und maximum gesperrt wurde
     */
    private static Float tween(float val, float min, float max) {
        return max(min(val, max), min);
    }

    /**
     * Eine Fitness methode welche den im spiel inbegriffenen {@link Emulator] aufrugt
     *
     * @param genetype der zu bewetende Genetype
     * @return die Fitness / der Score des Genetypes
     */
    public static float fitness(Genetype<Float> genetype) {
        BaseAlgorithm algorithm = new EvolutionaryAlgorithm(genetype.getData(Float[]::new));
        return Emulator.INSTANCE.emulate(15f, world -> algorithm.getJumpFunction().apply(world), true);
    }
}
