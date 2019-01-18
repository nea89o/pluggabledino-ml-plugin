package de.romjaki.mlplugin.evolutionary;

import de.romjaki.mlplugin.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Eine Population von {@link Genetype}n welche eine Generation in einem Evolutionärem Algorithmus darstellen.
 *
 * @param <T>
 */
@SuppressWarnings({"unchecked"})
public class Population<T> {

    private static final float CROSSOVER_PROBABILITY = 0.45f;
    private static final float MUTATION_PROBABILITY = 0.125f;
    private static final int TOURNAMENT_SIZE = 5;
    private Genetype[] population;
    private float[] fitness;
    private int populationSize;
    private Random random = new Random();

    /**
     * Ein Konstruktor welcher zum generieren von zufälligen Generationen genutzt werden kann.
     * Erzeugt {@code populationSize} genetypen welche von {@code generator} generiert werden
     *
     * @param populationSize die Größe der Population
     * @param generator      ein Generator für {@link Genetype}n
     */
    public Population(int populationSize, Function<Random, Genetype<T>> generator) {
        this.population = new Genetype[populationSize];
        this.populationSize = populationSize;
        // Iteriere über die (leere) Gentypen
        for (int i = 0; i < populationSize; i++) {
            // Initialisiere jedes Element
            this.population[i] = generator.apply(random);
        }
    }

    /**
     * Erzeuge eine Population mit festgesetzten Genetypen
     *
     * @param population die Genetypen
     */
    public Population(Genetype[] population) {
        this.population = population;
        this.populationSize = population.length;
    }

    /**
     * Weist jedem {@link Genetype} eine Fitness zu welche in anderen Methoden genutzt wird.
     * MUSS VOR VIELEN METHODEN AUFGERUFEN WERDEN
     *
     * @param evaluator eine Methode welche eine einzelnen {@link Genetype} bewertet.
     */
    public void evaluate(Function<Genetype<T>, Float> evaluator) {
        // Initialisiere die Fitness als leere Liste.
        this.fitness = new float[populationSize];
        List<Thread> threads = new ArrayList<>();
        AtomicInteger threadcount = new AtomicInteger();
        try {
            for (int i = 0; i < populationSize; i++) {
                // Das ist nötig wegen Javas Closure system. J ist effektiv eine Referenz auf unser aktuelles i
                int[] j = new int[]{i};
                // Nicht mehr als 8 Threads zum bewerten gleichzeitig laufen haben
                while (threadcount.get() > 8) Thread.sleep(1);

                // aktuellen Threadcount erhöhen.
                threadcount.incrementAndGet();
                // Thread zum Fitness berechnen erstellen und starten-
                Thread t = new Thread(() -> {
                    this.fitness[j[0]] = evaluator.apply((Genetype<T>) population[j[0]]);
                    threadcount.decrementAndGet();
                });
                t.start();

                // und in einer Liste speichern
                threads.add(t);
            }
            // Alle Threads bis zum Ende abwarten um zu verhindern das die letzten Genetypen ausgelassen werden.
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gibt den stärksten Algorithmus zurück
     * {@link Population#evaluate(Function)} muss vorher aufgerufen werden.
     *
     * @return den stärksten Algorithmus
     */
    public Genetype<T> getFittest() {
        return IntStream.range(0, populationSize)// Iteriere von 0 zu populationSize
                .mapToObj(i -> new Pair<>(population[i], fitness[i])) // Nehme den Genetype mit der entsprechenden Fitness
                .sorted((a, b) -> (int) Math.signum(b.getSecond() - a.getSecond())) // Sortiert nach der Fitness
                .map(Pair::getFirst) // jetzt nur noch den Genetype
                .findFirst() // den ersten/stärksten
                .orElseThrow(IllegalStateException::new); // falls keine Genetypen vorhanden sind.
    }

    /**
     * Merzt schlechte {@link Genetype}n aus und generiert neue. Gibt die neue Population zurück.
     * {@link Population#evaluate(Function)} muss vorher aufgerufen werden.
     *
     * @param changeFunction Eine funktion zum mutieren einzelner Datenpunkte im {@link Genetype}
     * @return die neue bessere Generation
     */
    public Population<T> decimate(Function<T, T> changeFunction) {
        // Liste der neuen Generation
        List<Genetype<T>> nextGeneration = new ArrayList<>();
        Genetype<T> fittest = getFittest();
        // der Beste kommt immer in die neue Generation
        nextGeneration.add(fittest);
        // populationSize - 1 mal
        for (int i = 1; i < populationSize; i++) {
            Genetype[] tournamentWinner = IntStream.range(0, TOURNAMENT_SIZE) // TOURNAMENT_SIZE
                    .map(ignored -> random.nextInt(populationSize)) // so viele zufällige Genetypen
                    .mapToObj(j -> new Pair<>(population[j], fitness[j]))
                    .sorted((a, b) -> (int) Math.signum(b.getSecond() - a.getSecond())) // sortiert nach Fitness
                    .map(Pair::getFirst)
                    .limit(2) // die zwei besten
                    .toArray(Genetype[]::new);
            Genetype<T> child = tournamentWinner[0] // den besseren mit dem zweitbesten kreuzen
                    .crossover(random, CROSSOVER_PROBABILITY, tournamentWinner[1])  // aber mit besseren chancen des weitergebens der Datenpunkte für den besseren
                    .mutate(random, MUTATION_PROBABILITY, changeFunction);  // zusätzlich eine zufällige Änderung für neue Datenpunkte im Genpool
            // Das Kind in die neue Generation
            nextGeneration.add(child);
        }
        // Die neue Population zurückgeben
        return new Population<T>(nextGeneration.toArray(new Genetype[0]));
    }

    /**
     * Nehme einen zufälligen {@link Genetype}
     *
     * @return einen zufälligen Genetype aus dieser Population
     */
    public Genetype<T> getRandom() {
        return population[random.nextInt(populationSize)];
    }

    /**
     * Serialisiere diese Population zu einem String
     * Gegenteil von {@link Population#deserialize(String, Function)}
     *
     * @param writer eine Funktion um die einzelnen Datentypen in {@link String}s zu verwandeln
     * @return die Population als String
     */
    public String serialize(Function<T, String> writer) {
        return Arrays.stream(population)
                .map(genetype -> genetype.serialize(writer)) // Die einzelnen Genetypen serialisieren
                .collect(Collectors.joining("\n")); // Mit Neuzeilen verbinden
    }

    /**
     * Deserialisiere einen String zu einer Population
     * Gegenteil von {@link Population#serialize(Function)}
     *
     * @param text   der zu deserialisierende {@link String}
     * @param reader eine Funktion um die einzelnen Datentyp aus einem String auszulesen
     * @param <T>    Der Typ der einzelnen Datentypen im {@link Genetype}
     * @return die Population die in dem String gespeichert war
     */
    public static <T> Population<T> deserialize(String text, Function<String, T> reader) {
        return new Population<>(
                Arrays.stream(text.split("[\\n\\r]+")) // An Neuzeilen spalten
                        .map(string -> Genetype.deserialize(string, reader)) // Genetypen deserialisieren
                        .toArray(Genetype[]::new));
    }

    /**
     * Speichert die Population in eine Datei.
     * Gegenteil von {@link Population#load(File, Function)}.
     *
     * @param file   die Datei in die die Population gespeichert werden soll
     * @param writer Eine Funktion zum serialisieren von einzelnen Datentypen
     * @throws IOException Falls Schreibrechte oder &auml;hnliches fehlen.
     *                     Siehe {@link Files#write(Path, byte[], OpenOption...)}
     */
    public void save(File file, Function<T, String> writer) throws IOException {
        Files.write(file.toPath(), serialize(writer).getBytes());
    }

    /**
     * Lädt eine Population aus einer Datei.
     * Gegenteil von {@link Population#save(File, Function)}
     *
     * @param file   die Datei aus der die Population geladen werden soll
     * @param reader Eine Funktion zum deserialisieren von einzelnen Datentypen
     * @param <T>    Der Typ der einzelnen Datentypen
     * @return Die geladene Population
     * @throws IOException Falls Leserechte fehlen, die Datei nicht vorhanden ist oder &auml;hnliches fehlt.
     *                     Siehe {@link Files#readAllBytes(Path)}
     */
    public static <T> Population<T> load(File file, Function<String, T> reader) throws IOException {
        return deserialize(new String(Files.readAllBytes(file.toPath())).replaceAll("\r", ""), reader);
    }


}
