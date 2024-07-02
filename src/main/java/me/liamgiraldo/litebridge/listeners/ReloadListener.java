package me.liamgiraldo.litebridge.listeners;

import me.liamgiraldo.litebridge.controllers.GameController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ReloadListener implements Listener {
    private final GameController controller;
    public ReloadListener(GameController controller){
        this.controller = controller;
    }

    @EventHandler
    public void onCommandPreprocessEvent(PlayerCommandPreprocessEvent event){
        if(event.getMessage().equalsIgnoreCase("/reload")){
            //end all of the games
            controller.endAllGamesInstantly();
        }
    }
}
