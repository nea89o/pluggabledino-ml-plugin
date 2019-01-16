package de.romjaki.mlplugin;

import de.romjaki.pluggabledino.game.GameWorld;
import org.jbox2d.dynamics.Body;

import java.util.List;
import java.util.function.Function;

public abstract class BaseAlgorithm {

    public abstract boolean shouldJump(float nextCactus, float nextBird, float speed);

    public Function<GameWorld, Boolean> getJumpFunction() {
        return gameWorld -> {
            float nextCacti = findNext(gameWorld.getCacti());
            float nextBird = findNext(gameWorld.getBirdd());
            float speed = gameWorld.getSpeed();
            return shouldJump(nextCacti, nextBird, speed);
        };
    }

    private float findNext(List<Body> bodies) {
        return bodies.stream()
                .map(body ->
                        body.getPosition().x
                )
                .filter(x ->
                        x > 0
                )
                .min(Float::compareTo)
                .orElse(10000f);
    }
}
