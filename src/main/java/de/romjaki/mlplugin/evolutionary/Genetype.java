package de.romjaki.mlplugin.evolutionary;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Eine Klasse welche einen Genetype repräsentiert.
 * Jeder Genetype enthälft eine Menge an Daten, welche nötig ist um diesen mutieren zu lassen oder zum ausführen von neuronalen Netzwerken zu nutzen
 *
 * @param <T> Der Typ des unterliegenden Datentypes welcher das Netzwerk repräsentiert.
 */
@SuppressWarnings("unchecked")
public class Genetype<T> {
    private Object[] data;

    /**
     * Ein Konstruktor für das Erstellen eines Genetypes mit bestimmten Daten
     * @param data Die Daten
     */
    public Genetype(Object[] data) {
        this.data = data;
    }

    /**
     * Ein Konstrutor um Daten zu generieren, üblicherweise in Kombination mit einem Zufallsgenerator
     *
     * @param length Die Länge der zu generierenden Daten
     * @param generator Ein Generator welcher mindestens {@code length} Datenpunkte generieren kann
     */
    public Genetype(int length, Supplier<T> generator) {
        this.data = new Object[length];
        // Iteriere über den data Array und initialisiere ihn mit Elementen von generator
        for (int i = 0; i < length; i++) {
            this.data[i] = generator.get();
        }
    }

    /**
     * Eine Methode um eine neue mutierte Version dieses Genetypes zurückzubekommen.
     *
     * @param random Eine {@link java.util.Random} Instanz zum Generieren von Zufallszahlen
     * @param probability Die Wahrscheinlichkeit das eine Mutation bei jedem einzelnen Teildatenpunkt auftritt
     * @param change Eine Funktion die einen Datenpunkt ändert
     * @return Eine neue mutierte Instanz
     */
    public Genetype<T> mutate(Random random, float probability, Function<T, T> change) {
        Object[] newData = new Object[data.length];
        //
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
        System.out.println("Deserializing: "+text);
        return new Genetype<>(Arrays.stream(text.split(";"))
                .map(reader)
                .toArray(Object[]::new));
    }

    public T getGene(int i) {
        return (T) data[i];
    }

    public T[] getData(Function<Integer, T[]> arrayGenerator) {
        T[] array = arrayGenerator.apply(data.length);
        for (int i = 0; i < data.length; i++) {
            array[i] = (T) data[i];
        }
        return array;
    }
}
