package de.romjaki.mlplugin.evolutionary;

import de.romjaki.mlplugin.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings({"unchecked"})
public class Population<T> {

    private static final float CROSSOVER_PROBABILITY = 0.45f;
    private static final float MUTATION_PROBABILITY = 0.125f;
    private static final int TOURNAMENT_SIZE = 5;
    private Genetype[] population;
    private float[] fitness;
    private int populationSize;
    private Random random = new Random();

    public Population(int populationSize, Function<Random, Genetype<T>> generator) {
        this.population = new Genetype[populationSize];
        this.populationSize = populationSize;
        for (int i = 0; i < populationSize; i++) {
            this.population[i] = generator.apply(random);
        }
    }

    public Population(Genetype[] population) {
        this.population = population;
        this.populationSize = population.length;
    }

    public void evaluate(Function<Genetype<T>, Float> evaluator)

    {
        this.fitness = new float[populationSize];
        for (int i = 0; i < populationSize; i++) {
            this.fitness[i] = evaluator.apply((Genetype<T>) population[i]);
        }
    }

    public Genetype<T> getFittest() {
        return IntStream.range(0, populationSize)
                .mapToObj(i -> new Pair<>(population[i], fitness[i]))
                .sorted((a, b) -> (int) Math.signum(b.getSecond() - a.getSecond()))
                .map(Pair::getFirst)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    public Population<T> decimate(Function<T, T> changeFunction) {
        List<Genetype<T>> nextGeneration = new ArrayList<>();
        Genetype<T> fittest = getFittest();
        nextGeneration.add(fittest);
        for (int i = 1; i < populationSize; i++) {
            Genetype[] tournamentWinner = IntStream.range(0, TOURNAMENT_SIZE)
                    .map(ignored -> random.nextInt(populationSize))
                    .mapToObj(j -> new Pair<>(population[j], fitness[j]))
                    .sorted((a, b) -> (int) Math.signum(b.getSecond() - a.getSecond()))
                    .map(Pair::getFirst)
                    .limit(2)
                    .toArray(Genetype[]::new);
            Genetype<T> child = tournamentWinner[0].crossover(random, CROSSOVER_PROBABILITY, tournamentWinner[1])
                    .mutate(random, MUTATION_PROBABILITY, changeFunction);

            nextGeneration.add(child);
        }
        return new Population<T>(nextGeneration.toArray(new Genetype[0]));
    }

    public String serialize(Function<T, String> writer) {
        return Arrays.stream(population)
                .map(genetype -> genetype.serialize(writer))
                .collect(Collectors.joining("\n"));
    }

    public static <T> Population<T> deserialize(String text, Function<String, T> reader) {
        return new Population<>(Arrays.stream(text.split("\n"))
                .map(string -> Genetype.deserialize(text, reader))
                .toArray(Genetype[]::new));
    }

    public void save(File file, Function<T, String> writer) throws IOException {
        Files.write(file.toPath(), serialize(writer).getBytes());
    }

    public static <T> Population<T> load(File file, Function<String, T> reader) throws IOException {
        return deserialize(new String(Files.readAllBytes(file.toPath())), reader);
    }
}
