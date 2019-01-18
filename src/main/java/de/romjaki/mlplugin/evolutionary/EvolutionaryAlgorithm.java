package de.romjaki.mlplugin.evolutionary;

import de.romjaki.mlplugin.BaseAlgorithm;

/**
 * Ein Evolutionärer Algorithmus welcher gegeben ein neuronales Netzwerk aus unserer {@link Population} Entscheidungen trifft
 */
public class EvolutionaryAlgorithm extends BaseAlgorithm {


    private Float[] data;

    /**
     * @param data Die Daten des {@link Genetype}
     */
    public EvolutionaryAlgorithm(Float[] data) {
        this.data = data;
    }

    @Override
    public boolean shouldJump(float nextCactus, float nextBird, float speed) {
        float layer1a = nextCactus * data[0]
                + nextBird * data[1]
                + data[2];

        float layer1b = nextBird * data[3]
                + speed * data[4]
                + data[5];

        float layer2 = layer1a * data[6]
                + layer1b * data[7]
                + data[8];
        // rechne alle daten miteinander zusammen. Da das Netzwerk nur Gleitkommazahlen verwendet vergleichen wir unser
        // Endergebnis mit 0.5 um zu entscheiden ob wir springen müssen.
        return layer2 >= 0.5;
    }
}
