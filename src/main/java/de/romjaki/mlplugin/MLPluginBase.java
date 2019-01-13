package de.romjaki.mlplugin;

import de.romjaki.pluggabledino.MainKt;
import de.romjaki.pluggabledino.api.EventHandler;
import de.romjaki.pluggabledino.api.IPlugin;
import de.romjaki.pluggabledino.events.GameLostEvent;
import de.romjaki.pluggabledino.events.GameRenderEvent;
import de.romjaki.pluggabledino.events.GameUpdateEvent;
import org.newdawn.slick.Color;

public class MLPluginBase extends IPlugin {

    private float cactiPosition;

    @EventHandler
    public void onGameLost(GameLostEvent event) {
        System.out.println("dein ergnis war " + event.getScore());
    }

    @EventHandler
    public void onUpdate(GameUpdateEvent event) {
        cactiPosition = event.getWorld()
                .getCacti()
                .stream()
                .map(body ->
                        body.getPosition().x
                )
                .filter(x ->
                        x > 0
                )
                .min(Float::compareTo)
                .orElse(10000f);
        if (cactiPosition < 10f) {
            event.getWorld().tryJump();
        }

    }

    @EventHandler
    public void onRender(GameRenderEvent event) {
        event.getGraphics().setColor(Color.red);
        event.getGraphics().drawLine(cactiPosition * MainKt.WIDTH / 50, 0, cactiPosition * MainKt.WIDTH / 50, MainKt.HEIGHT);
    }


}
