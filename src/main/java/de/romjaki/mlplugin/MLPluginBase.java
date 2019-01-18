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

public class MLPluginBase extends IPlugin {

    private float cactiPosition;


    List<Genetype<Float>> genetypes = new ArrayList<>();
    BaseAlgorithm[] algorithm;

    @EventHandler
    public void onInit(InitEvent event) {
        try {
            Population<Float> pop = Population.load(new File("network.txt"), Float::parseFloat);
            pop.evaluate(EMain::fitness);
            genetypes.add(pop.getFittest());
        } catch (IOException e) {
            e.printStackTrace();
        }

        algorithm = new BaseAlgorithm[]{
                new DefaultAlgorithm(),
                new EvolutionaryAlgorithm(genetypes.get(0).getData(Float[]::new))
        };
        event.getSettings().addSettingsElement(new ToggleButton(Arrays.asList(
                "Default", "Evolved"
        ), MainKt.HEIGHT * 2 / 3f, MainKt.WIDTH / 2f));
    }

    @EventHandler
    public void onGameLost(GameLostEvent event) {
        System.out.println("dein Ergebnis war " + event.getScore());
    }

    @EventHandler
    public void onUpdate(GameUpdateEvent event) {
        event.getWorld().getBirdd().clear();
        if (new EvolutionaryAlgorithm(genetypes.get(genetypes.size() - 1).getData(Float[]::new))
                .getJumpFunction().apply(event.getWorld()))
            event.getWorld().tryJump();
    }

    @EventHandler
    public void onRender(GameRenderEvent event) {
        event.getGraphics().setColor(Color.red);
        event.getGraphics().drawLine(cactiPosition * MainKt.WIDTH / 50, 0, cactiPosition * MainKt.WIDTH / 50, MainKt.HEIGHT);
    }


}
