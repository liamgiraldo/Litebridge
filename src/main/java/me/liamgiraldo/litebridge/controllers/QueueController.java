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
        //the first argument is supposed to be a gamemode. It can also be "random"
        String firstArg = null;
        //the second argument is supposed to be a map.
        String secondArg = null;

        //if a first argument exists, set the first argument
        if(args.length > 0)
            firstArg = args[0];

        //if a second argument exists, set the second argument
        if(args.length > 1)
            secondArg = args[1];

        //gameMode is the game type, example singles, doubles, triples etc..
        int gameMode = -1;

        //mapName is the name of the map to queue for
        String mapName = null;

        //if the first argument exists...
        if(firstArg != null) {
            //check what  the player input as the first argument
            switch (firstArg) {
                //if this was keyword "random"
                case "random":
                    //TODO Check for any non-full game
                    //find a random map
                    QueueModel queue1 = queues.get((int) (Math.random()*queues.size()));
                    mapName = queue1.getWorld().getName();
                    //try finding a random (non full) map 10 times
                    int recursionDepth = 10;
                    int count = 0;
                    while(queue1.isQueueFull()){
                        //this can go on forever, if ALL of the queues are full
                        queue1 = queues.get((int) (Math.random()*queues.size()));
                        //get the map name of the random, not full map
                        mapName = queue1.getWorld().getName();
                        if(count == recursionDepth){
                            //if we couldn't find a random map for you...
                            System.out.print("We couldn't find a valid queue for you to use.");
                            //return null. No map found
                            return null;
                        }
                        count++;
                    }
                    if(queue1 == null){
                        System.out.println("We couldn't find a valid queue for you to go into.");
                        return null;
                    }
                    //We were able to find a random map, get the name of that map. We will to queue it later.
                    mapName = queue1.getWorld().getName();
                    //return for random queue
                    break;
                default:
                    //If the first argument was something other than "random"
                    //We want to see if it was an integer for the gamemode.
                    try {
                        //Try parsing the parameter as an int
                        Integer.parseInt(firstArg);
                    } catch (Exception e) {
                        //if this doesn't work, set the gameMode to null
                        System.out.println(e);
                        System.out.println("This gamemode type doesn't exist");
                        //-1 is essentially null
                        gameMode = -1;
                        break;
                    }
                    //If we were able to parse the integer, set the game mode to that int.
                    gameMode = Integer.parseInt(firstArg);
            }
        }
        //if the first argument is null
        else{
            //try queuing any fill randomly. This is the same as litebridge random
            QueueModel queue1 = queues.get((int) (Math.random()*queues.size()));
            mapName = queue1.getWorld().getName();
            //Grab a random queue
            int recursionDepth = 10;
            int count = 0;
            //if that queue is full, try ten times to get another
            while(queue1.isQueueFull()){
                //this can go on forever, if ALL of the queues are full
                queue1 = queues.get((int) (Math.random()*queues.size()));
                mapName = queue1.getWorld().getName();
                //Get the map name of the valid queue
                if(count == recursionDepth){
                    //if we couldn't find a valid queue for the given map name, set the map name to null.
                    //We will use this later.
                    System.out.print("A non-full queue of the given map name was not found.");
                    mapName = null;
                }
                count++;
            }
            //if this random queue somehow still doesn't exist
            if(queue1 == null){
                System.out.println("We couldn't find a valid queue for you to go into.");
            }
            //by this point, the map should be a non-full, random queue
            //get the map name of that queue
            mapName = queue1.getWorld().getName();
        }

        //if the second argument exists (this also implies that the first argument exists)
        if(secondArg != null) {
            //try to find if a queue of the player given map name, exists
            boolean mapInModels = false;
            for (QueueModel q: queues) {
                if(q.getWorld().getName().equals(secondArg)){
                    if(q.isQueueFull()){
                        //if the map name exists, and that queue is full
                        //set the map name to null, because we shouldn't try queueing into a full game
                        //by technicality the map is real but it's full
                        mapInModels = true;
                        System.out.println("The specified queue that you requested is full.");
                        mapName = null;
                    } else {
                        //if the map name exists, and that queue isn't full, set the map name
                        mapInModels = true;
                        mapName = secondArg;
                    }
                    break;
                }
            }
            if(!mapInModels){
                //if the map was not in the available list of maps
                System.out.println("That map was not a valid map");
                //set that map name to null
                mapName = null;
            }
        }
        else{
            mapName = null;
        }

        //if no gamemode was specified, or the gamemode typed was invalid
        //try to queue using just the map name
        if(mapName == null){
            System.out.println("There is no possible to game to queue with the given params");
            return null;
        }

        //If the map name is verified to exist, and the gamemode is verified to be a number
        if(mapName != null && gameMode != -1) {
            //now we try to queue the specified game
            for (QueueModel q : queues) {
                //maxPlayers of QueueModel needs to be an even number GRRR
                if (q.getWorld().getName().equals(mapName) && q.getMaxPlayers() / 2 == gameMode) {
                    if (!q.isQueueFull()) {
                        //if the found queue not full, queue it!
                        return q;
                    } else {
                        System.out.println("All queues for the specified gamemode are full.");
                        break;
                    }
                }
            }
        }

        if(mapName != null && gameMode == -1){
            for (QueueModel q : queues) {
                //maxPlayers of QueueModel needs to be an even number GRRR
                if (q.getWorld().getName().equals(mapName)) {
                    if (!q.isQueueFull()) {
                        //if the found queue not full, queue it!
                        return q;
                    } else {
                        System.out.println("All queues for the specified map are full.");
                        break;
                    }
                }
            }
        }

        //at this point we literally could not find any game for you to queue at all
        return null;
    }
}
