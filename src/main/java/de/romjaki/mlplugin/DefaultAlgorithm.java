package de.romjaki.mlplugin;

/**
 * Ein sehr simpler algorithmus
 */
public class DefaultAlgorithm extends BaseAlgorithm {
    @Override
    public boolean shouldJump(float nextCactus, float nextBird, float speed) {
        return nextCactus < 10f; // falls der nächste Kaktus weniger als 10 einheiten entfernt ist, spring.
    }
}
