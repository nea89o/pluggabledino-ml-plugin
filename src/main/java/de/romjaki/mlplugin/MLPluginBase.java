package de.romjaki.mlplugin;

import de.romjaki.mlplugin.evolutionary.EMain;
import de.romjaki.mlplugin.evolutionary.EvolutionaryAlgorithm;
import de.romjaki.mlplugin.evolutionary.Genetype;
import de.romjaki.mlplugin.evolutionary.Population;
import de.romjaki.pluggabledino.MainKt;
import de.romjaki.pluggabledino.api.EventHandler;
import de.romjaki.pluggabledino.api.IPlugin;
import de.romjaki.pluggabledino.api.ToggleButton;
import de.romjaki.pluggabledino.events.GameLostEvent;
import de.romjaki.pluggabledino.events.GameRenderEvent;
import de.romjaki.pluggabledino.events.GameUpdateEvent;
import de.romjaki.pluggabledino.events.InitEvent;
import org.newdawn.slick.Color;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Die Pluginklasse welche als Plugin ins Spiel geladen wird
 */
public class MLPluginBase extends IPlugin {


    private List<Genetype<Float>> genetypes = new ArrayList<>();
    private BaseAlgorithm[] algorithm;
    private ToggleButton button;

    /**
     * Ein Eventhandler welcher am Start aufgerufen wird
     *
     * @param event Ein Event welches Daten wie die Einstellungen enthält.
     */
    @EventHandler
    public void onInit(InitEvent event) {
        // Lade eie Population und nutze den Fittesten Algorithmus
        try {
            Population<Float> pop = Population.load(new File("network.txt"), Float::parseFloat);
            pop.evaluate(EMain::fitness);
            genetypes.add(pop.getFittest());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Eine Liste der Algorithmen
        algorithm = new BaseAlgorithm[]{
                new DefaultAlgorithm(),
                new EvolutionaryAlgorithm(genetypes.get(0).getData(Float[]::new))
        };

        // Ein Knopf welcher in den Einstellungen angezeigt wird.
        button = new ToggleButton(Arrays.asList(
                "Default", "Evolved"
        ), MainKt.HEIGHT * 2 / 3f, MainKt.WIDTH / 2f);
        event.getSettings().addSettingsElement(button);
    }

    /**
     * Ein Eventhandler welcher bei Kontakt mit einem Kaktus aufgerufen wird.
     *
     * @param event Das Event welches Informationen wie die erreichte Punktzahl enthält
     */
    @EventHandler
    public void onGameLost(GameLostEvent event) {
        System.out.println("dein Ergebnis war " + event.getScore());
    }

    /**
     * Ein Eventhandler welcher einmal pro Tick aufgerufen wird
     *
     * @param event Ein Event welches Informationen wie die Gameworld enthält
     */
    @EventHandler
    public void onUpdate(GameUpdateEvent event) {
        // entferne Vögel welche zurzeit noch nicht von unserem DefaultAlgorithmen berücksichtigt werden
        event.getWorld().getBirdd().clear();

        // Nutze den in den Einstellungen gewählten algorithmus
        if (algorithm[button.getIndex()].getJumpFunction().apply(event.getWorld()))
            event.getWorld().tryJump(); // und springe falls dieser sagt.
        // tryJump springt nur wenn der Spieler auch springen könnte, also nicht in der Luft.
        // Tatsächlich wird bei Spieler input auch nur diese Methode aufgerufen
    }

}
