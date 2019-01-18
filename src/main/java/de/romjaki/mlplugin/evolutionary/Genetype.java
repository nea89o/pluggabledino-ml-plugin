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
        // Iteriere über alle Datenpunkte im Genetype
        for (int i = 0; i < data.length; i++) {
            // Mit einer Wahrscheinlichkeit von probability
            if (random.nextFloat() <= probability) {
                // Nutze die change Funktion zum ändern des Datenpunkts
                newData[i] = change.apply((T) data[i]);
            } else {
                // Oder übernehme den Datenpunkt 1 zu 1
                newData[i] = data[i];
            }
        }
        // Gebe einen neuen Genetype mit den generierten Daten zurück
        return new Genetype<T>(newData);
    }

    /**
     * Kreuze diesen Genetype mit {@code that} und gebe den neuen Datentyp zurück.
     *
     * @param random Eine {@link java.util.Random} Instanz zum Generieren von Zufallszahlen
     * @param probability Die Wahrscheinlichkeit einen Datenpunkt von dem anderen Genetypen (anstelle diesem) zu übernehmen
     * @param that Ein anderer Genetype mit dem wir gekreuzt werden sollen
     * @return Einen neuen gekreuzten Genetype
     */
    public Genetype<T> crossover(Random random, float probability, Genetype<T> that) {
        // Genetypen verschiedener Länge können nicht gekreuzt werden.
        assert this.data.length == that.data.length;
        Object[] newData = new Object[data.length];

        // Iteriere über alle Datenpunkte
        for (int i = 0; i < this.data.length; i++) {
            // mit einer wahrscheinlichkeit von Probability
            if (random.nextFloat() <= probability) {
                // die Daten des anderen Genetypes kopieren
                newData[i] = that.data[i];
            } else {
                // die Daten dieses Genetypes kopieren
                newData[i] = this.data[i];
            }
        }

        // Gebe den neuen Genetype zurück
        return new Genetype<T>(newData);
    }

    /**
     * Serialisiere diesen Genetype in einen String
     * Gegenteil von {@link Genetype#deserialize(String, Function)}
     * @param writer Eine Funktion zum serialisieren von Datenpunkten
     * @return einen serialisierten String
     */
    public String serialize(Function<T, String> writer) {
        return Arrays.stream(this.data) // Iteriere über alle Daten
                .map(x -> (T) x) // Erzwinge Typsicherheit
                .map(writer) // Mappe zu String mittels des gebenen Writers
                .collect(Collectors.joining(";")); // Jeden Datenwert zu einem Gesamtwerk verschmelzen.
    }

    /**
     * Deserialisiere einen String zu einem Genetyp
     * Gegenteil von {@link Genetype#serialize(Function)}
     *
     * @param text der zu deserialisierende String
     * @param reader Eine Funktion zum deserialisieren der einzelnen Datenpunkte
     * @param <T> Der Typ der Datenpunkte
     * @return Der deserialisierte Genetype
     */
    public static <T> Genetype<T> deserialize(String text, Function<String, T> reader) {
        return new Genetype<>(Arrays.stream(text.split(";"))
                .map(reader)
                .toArray(Object[]::new));
    }

    /**
     * Frage einen bestimmten Datenpunkt an einer bestimmten Stelle in diesem Genetype ab
     *
     * @param i der index des gefragten Datenpunktes
     * @return der Datenpunkt an der gefragten Stelle
     */
    public T getGene(int i) {
        return (T) data[i];
    }

    /**
     * Alle Datenpunkte in einem Array abfragen
     *
     * @param arrayGenerator eine funktion die einen Array mit der bestimmten Länge zurückgibt. (Das ist nötig wegen Javas eigenem Typsystem)
     * @return Den gefüllten Array.
     */
    public T[] getData(Function<Integer, T[]> arrayGenerator) {
        T[] array = arrayGenerator.apply(data.length);
        for (int i = 0; i < data.length; i++) {
            array[i] = (T) data[i];
        }
        return array;
    }
}
