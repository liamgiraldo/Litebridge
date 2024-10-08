package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.events.QueueFullEvent;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import me.liamgiraldo.litebridge.models.SpectatorQueueModel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;

public class QueueController implements EventListener, CommandExecutor, Listener {
    private ArrayList<QueueModel> queues;
    private ArrayList<SpectatorQueueModel> spectatorQueues;

    public QueueController(ArrayList<QueueModel> queues, ArrayList<SpectatorQueueModel> spectatorQueues){
        this.queues = queues;
        this.spectatorQueues = spectatorQueues;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender instanceof Player){
            Player p = (Player)sender;

            if(args.length > 0) {
                if (args[0].equals("leave")) {
                    leaveQueue(p);
                    return true;
                }
            }
            //Check if the player is already in any queues.
            for(int i = 0; i < queues.size(); i++){
                for(int j = 0; j < queues.get(i).getQueue().length; j++){
                    if(queues.get(i).getQueue()[j] == null)
                        continue;
                    if(p.getUniqueId() == queues.get(i).getQueue()[j].getUniqueId()){
                        p.sendMessage("You're already queueing for a game.");
                        p.sendMessage("If you want to requeue, do /q leave, then /q again.");
                        return false;
                    }
                }
            }

            for(int i = 0; i < spectatorQueues.size(); i++){
                for(int j = 0; j < spectatorQueues.get(i).getSpectators().size(); j++){
                    if(spectatorQueues.get(i).getSpectators().get(j) == null)
                        continue;
                    if(p.getUniqueId() == spectatorQueues.get(i).getSpectators().get(j).getUniqueId()){
                        p.sendMessage("You're spectating a game.");
                        p.sendMessage("If you want to queue for a game, do /ls leave, then /q again.");
                        return false;
                    }
                }
            }

            QueueModel gameToQueueTo = checkAppropriateQueue(args);
            if(gameToQueueTo == null)
                return false;
            gameToQueueTo.appendToQueue(p);

            for(Player p1 : gameToQueueTo.getQueue()){
                if(p1 == null) {
                    System.out.println("null");
                }else {
                    System.out.println(p1.getName());
                }
            }

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

                    //if the random queue is full, try ten times to get another
                    //if the random queue's game is active, skip it
                    while(queue1.isQueueFull() || queue1.getAssociatedGame().getGameState() != GameModel.GameState.INACTIVE){
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

            //we shouldn't find a random queue if someone does /q.
            //we should find the most full queue, and add the player to that.
            //find the most full queue
            QueueModel queue1 = findQueueWithMostPlayersButNotFull();
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
                if(count >= recursionDepth){
                    //if we couldn't find a valid queue for the given map name, set the map name to null.
                    //We will use this later.
                    System.out.print("A non-full queue of the given map name was not found.");
                    mapName = null;
                    break;
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
                    if (!q.isQueueFull() && q.getAssociatedGame().getGameState() == GameModel.GameState.INACTIVE){
                        //if the found queue not full, queue it!
                        return q;
                    } else {
                        System.out.println("All queues for the specified gamemode are full or active.");
                        break;
                    }
                }
            }
        }

        if(mapName != null && gameMode == -1){
            for (QueueModel q : queues) {
                //maxPlayers of QueueModel needs to be an even number GRRR
                if (q.getWorld().getName().equals(mapName)) {
                    if (!q.isQueueFull() && q.getAssociatedGame().getGameState() == GameModel.GameState.INACTIVE){
                        //if the found queue not full, queue it!
                        return q;
                    } else {
                        System.out.println("All queues for the specified map are full or active.");
                        break;
                    }
                }
            }
        }

        //at this point we literally could not find any game for you to queue at all
        return null;
    }

    /**
     * Searches for the provided player throughout all queues
     * If the player is found, remove them from that queue.
     *
     * @param p Player to remove from a queue
     * */
    private void leaveQueue(Player p){
        for(QueueModel q: queues){
            for(Player players: q.getQueue()){
                if(players == null)
                    continue;
                if(players.getUniqueId() == p.getUniqueId()){
                    if(q.getAssociatedGame().getGameState() != GameModel.GameState.INACTIVE){
                        p.sendMessage("You can't leave your queue while your game is in progress.");
                        return;
                    }
                    q.removeFromQueue(p);
                    p.sendMessage("You were removed from a queue.");
                    return;
                }
            }
        }
        System.out.println("This player wasn't even in a queue.");
    }

    @Override
    public String toString(){
        //return a string containing all of the queues, and their players
        StringBuilder builder = new StringBuilder();
        for(QueueModel q: queues){
            builder.append(q.getWorld().getName());
            builder.append("\n");
            for(Player p: q.getQueue()){
                if(p == null){
                    builder.append("[Empty], ");
                } else {
                    builder.append("[").append(p.getName()).append("], ");
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private QueueModel findQueueWithMostPlayersButNotFull(){
        QueueModel mostPlayers = null;
        //for each queue
        for(QueueModel q : queues){
            //if the queue is null, skip it
            if(q == null) {
                continue;
            }
            //if the queue's game is active, skip it
            if(q.getAssociatedGame().getGameState() != GameModel.GameState.INACTIVE){
                continue;
            }
            else{
                //if the mostPlayers queue is null, set it to the current queue
                if(mostPlayers == null){
                    mostPlayers = q;
                }
                //if the current queue has more players than the mostPlayers queue and the current queue isn't full
                else if(q.getPercentageFull() > mostPlayers.getPercentageFull() && !q.isQueueFull()){
                    //set the mostPlayers queue to the current queue
                    mostPlayers = q;
                }
            }
        }
        return mostPlayers;
    }

    @EventHandler
    private void onPlayerLeaveServerEvent(PlayerQuitEvent event){
        //only kick the player out of the queue if their queue's game is inactive
        if(event.getPlayer() != null){
            for(QueueModel q: queues){
                if(q.isPlayerInQueue(event.getPlayer())){
                    if(q.getAssociatedGame().getGameState() == GameModel.GameState.INACTIVE){
                        q.removeFromQueue(event.getPlayer());
                    }
                }
            }
        }
        leaveQueue(event.getPlayer());
    }
}
