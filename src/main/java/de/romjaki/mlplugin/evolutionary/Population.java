package de.romjaki.mlplugin.evolutionary;

import de.romjaki.mlplugin.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings({"unchecked"})
public class Population<T> {

    private static final float CROSSOVER_PROBABILITY = 0.45f;
    private static final float MUTATION_PROBABILITY = 0.25f;
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

    public void evaluate(Function<Genetype<T>, Float> evaluator) {
        this.fitness = new float[populationSize];
        for (int i = 0; i < populationSize; i++) {
            this.fitness[i] = evaluator.apply((Genetype<T>) population[i]);
        }
    }

    public Genetype<T> getFittest() {
        return IntStream.range(0, populationSize)
                .mapToObj(i -> new Pair<>(population[i], fitness[i]))
                .sorted((a, b) -> (int) Math.signum(a.getSecond() - b.getSecond()))
                .map(Pair::getFirst)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    public Population<T> decimate(Function<T, T> changeFunction) {
        List<Genetype> parents = IntStream.range(0, populationSize)
                .mapToObj(i -> new Pair<>(population[i], fitness[i]))
                .sorted((a, b) -> (int) Math.signum(b.getSecond() - a.getSecond()))
                .limit(populationSize / 3)
                .map(Pair::getFirst)
                .collect(Collectors.toList());
        parents.addAll(Collections.unmodifiableList(parents));

        Collections.shuffle(parents);

        List<Genetype> childs = new ArrayList<>();

        for (int i = 0; i < populationSize / 3; i++) {
            childs.add(parents.get(i).crossover(random, CROSSOVER_PROBABILITY, parents.get(i + populationSize / 3))
                    .mutate(random, MUTATION_PROBABILITY, changeFunction));
        }
        parents.addAll(childs);
        return new Population<T>(parents.toArray(new Genetype[0]));
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

}
