package de.romjaki.mlplugin;

public class DefaultAlgorithm extends BaseAlgorithm {
    @Override
    public boolean shouldJump(float nextCactus, float nextBird, float speed) {
        return nextCactus< 10f;
    }
}
