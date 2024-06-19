package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.models.QueueModel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.EventListener;

public class QueueController implements EventListener, CommandExecutor {
    private ArrayList<QueueModel> queues;

    public QueueController(ArrayList<QueueModel> queues){
        this.queues = queues;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender instanceof Player){
            Player p = (Player)sender;
            QueueModel gameToQueueTo = checkAppropriateQueue(args);
            if(gameToQueueTo == null)
                return false;
            gameToQueueTo.appendToQueue(p);
            p.sendMessage("You were added to the " + gameToQueueTo.getWorld().getName() + "queue");
        }
        return true;
    }

    /**
     * If the first arg is nothing, just queue the player in the most full game
     *
     * If the first arg in the command is random, queue in any game
     *
     * If the first arg is not "random" check if it's an integer
     * Then queue the player in the most full version of that gamemode
     * regardless of map
     *
     * If the first arg is not "random" and the second arg is something
     * Check if the first arg is a valid integer
     * Check if the second arg is a valid map
     * Then place the player in the most full game of the cooresponding gamemode, on that specific map
     *
     * @param args The command arguments used
     * @return The queue to add the player to
     * */
    private QueueModel checkAppropriateQueue(String[] args){
        String firstArg = null;
        String secondArg = null;

        if(args.length > 0)
            firstArg = args[0];

        if(args.length > 1)
            secondArg = args[1];

        int gameMode;
        String mapName;

        if(firstArg != null) {
            switch (firstArg) {
                case "random":
                    //TODO Check for any non-full game
                    QueueModel queue1 = queues.get(Math.random()*queues.size()).getWorld().getName();
                    int recursionDepth = 10;
                    int count = 0;
                    while(queue1.isQueueFull()){
                        //this can go on forever, if ALL of the queues are full
                        queue1 = queues.get(Math.random()*queues.size()).getWorld().getName();
                        if(count == recursionDepth){
                            System.out.print("We couldn't find a valid queue for you to use.");
                            return null;
                        }
                        count++;
                    }
                    if(queue1 == null){
                        System.out.println("We couldn't find a valid queue for you to go into.");
                    }
                    //return for random queue
                    break;
                default:
                    try {
                        Integer.parseInt(firstArg);
                    } catch (Exception e) {
                        System.out.println(e);
                        System.out.println("This command is invalid");
                        break;
                    }
                    gameMode = Integer.parseInt(firstArg);
            }
        }
        else{
            //first argument is null, queue any fill
            return null;
        }

        if(secondArg != null) {
            boolean mapInModels = false;
            for (QueueModel q: queues) {
                if(q.getWorld().getName().equals(secondArg)){
                    mapInModels = true;
                    mapName = secondArg;
                    break;
                }
            }
            if(!mapInModels){
                System.out.println("That map was not a valid map");
            }
        }
        else{
            //if the second argument is null, try using only the first one
        }
        for(QueueModel q : queues){
            //maxPlayers of QueueModel needs to be an even number GRRR
            //TODO Make sure that the game is not full as well. Use queue.isQueueFull to check
            if(q.getWorld().getName().equals(mapName) && q.getMaxPlayers()/2 == gameMode){
                return q;
            }
        }
        return null;
    }
}
