package de.romjaki.mlplugin.evolutionary;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class Genetype<T> {
    private Object[] data;

    public Genetype(Object[] data) {
        this.data = data;
    }

    public Genetype(int length, Supplier<T> generator) {
        this.data = new Object[length];
        for (int i = 0; i < length; i++) {
            this.data[i] = generator.get();
        }
    }


    public Genetype<T> mutate(Random random, float probability, Function<T, T> change) {
        Object[] newData = new Object[data.length];
        for (int i = 0; i < data.length; i++) {
            if (random.nextFloat() <= probability) {
                newData[i] = change.apply((T) data[i]);
            } else {
                newData[i] = data[i];
            }
        }
        return new Genetype<T>(newData);
    }

    public Genetype<T> crossover(Random random, float probability, Genetype<T> that) {
        assert this.data.length == that.data.length;
        Object[] newData = new Object[data.length];
        for (int i = 0; i < this.data.length; i++) {
            if (random.nextFloat() <= probability) {
                newData[i] = that.data[i];
            } else {
                newData[i] = this.data[i];
            }
        }
        return new Genetype<T>(newData);
    }

    public String serialize(Function<T, String> writer) {
        return Arrays.stream(this.data)
                .map(x -> (T) x)
                .map(writer)
                .collect(Collectors.joining(";"));
    }

    public static <T> Genetype<T> deserialize(String text, Function<String, T> reader) {
        return new Genetype<>(Arrays.stream(text.split(";"))
                .map(reader)
                .toArray(Object[]::new));
    }

    public T getGene(int i) {
        return (T) data[i];
    }
}
