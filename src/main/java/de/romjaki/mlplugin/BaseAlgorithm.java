package de.romjaki.mlplugin;

import de.romjaki.pluggabledino.game.GameWorld;
import org.jbox2d.dynamics.Body;

import java.util.List;
import java.util.function.Function;

/**
 * Ein Grundgerüst welches das nutzen von verschiedenen Algorithmen erleichtern soll
 */
public abstract class BaseAlgorithm {
    /**
     * Eine abstrakte Methode welche entscheidet ob gesprungen werden sollte.
     *
     * @param nextCactus die Distanz zum nächsten Kaktus
     * @param nextBird   die Distanz zum nächsten Vogel
     * @param speed      die Geschwindigkeit der Welt
     * @return ein boolean entscheident ob man springen sollte
     */
    public abstract boolean shouldJump(float nextCactus, float nextBird, float speed);

    /**
     * Eine Methode welche eine Funktion zurückgibt welche das nutzen einer {@link GameWorld} Instanz erleichtern soll.
     *
     * @return Eine Methode welche abhängig von einer GameWorld Instanz entscheidet ob der Spieler springen sollte.
     */
    public Function<GameWorld, Boolean> getJumpFunction() {
        return gameWorld -> {
            float nextCacti = findNext(gameWorld.getCacti());
            float nextBird = findNext(gameWorld.getBirdd());
            float speed = gameWorld.getSpeed();
            return shouldJump(nextCacti, nextBird, speed);
        };
    }

    /**
     * Eine private Helper Methode welche den nächsten Körper aus einer Liste zurück gibt.
     *
     * @param bodies Die Liste der Körper
     * @return Das nächste Element
     */
    private float findNext(List<Body> bodies) {
        return bodies.stream()
                .map(body ->
                        body.getPosition().x
                )
                .filter(x ->
                        x > 0
                )
                .min(Float::compareTo)
                .orElse(0f);
    }
}
