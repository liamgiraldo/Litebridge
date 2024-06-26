package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

public class GameController implements CommandExecutor, Listener {
    //I'm using the queues because they have references to the games of which they queue for
    //You'll have to access the games individually.
    private ArrayList<QueueModel> queues;

    public GameController(ArrayList<QueueModel> queues){
        this.queues = queues;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }
}
