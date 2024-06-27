package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.events.QueueFullEvent;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import me.liamgiraldo.litebridge.runnables.GameTimer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;


public class GameController implements CommandExecutor, Listener {
    //I'm using the queues because they have references to the games of which they queue for
    //You'll have to access the games individually.
    private Location lobbyLocation;
    private ArrayList<QueueModel> queues;

    /**
     * Constructs a new GameController
     * We use the GameController to manage all the active games and their corresponding queues.
     *
     * @param queues The queues to manage
     */
    public GameController(ArrayList<QueueModel> queues, Litebridge plugin) {
        this.queues = queues;
        this.lobbyLocation = new Location(plugin.getServer().getWorld("world"), 216, 67, 215);

        //run handleActiveGames every second
        new BukkitRunnable() {
            @Override
            public void run() {
                handleActiveGames();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    //TODO Test this code
    //when a queue becomes full, we want to add all the players in the queue to the associated game
    @EventHandler
    public void onQueueFull(QueueFullEvent e) {
        QueueModel queue = e.getQueue();
        GameModel game = queue.getAssociatedGame();
        Player[] players = queue.getQueue();
        for (Player p : players) {
            if (p == null)
                continue;
            game.addPlayer(p);
        }
        System.out.println("A queue is full!");
        gameStartLogic(game);
        //this lets all games start themselves with an event, however after this, we need the controller to handle the game logic for all of the games.
    }

    //I need to loop over all the games, and handles their logic seperately
    /**
     * pseudocode:
     * for each queue in queues:
     * grab the game associated with each queue
     * loop over every game
     * if the game's state is starting or active, handle the game logic
     * every game has their own GameTimer object, so for every game, we need to execute certain tasks at their own points in the timer
     *
     * */

    private void gameStartLogic(GameModel game) {
        //TODO Implement game start logic
        //If the game is full, we want to start the game
        if (game.checkIfGameIsFull()) {
            System.out.println("Starting a full game!");
            //Set the game state to starting
            game.setGameState(GameModel.GameState.STARTING);
            //teleport the players to their respective spawn points
            Location redSpawn = new Location(game.getWorld(), game.getRedSpawnPoint()[0], game.getRedSpawnPoint()[1], game.getRedSpawnPoint()[2]);
            Location blueSpawn = new Location(game.getWorld(), game.getBlueSpawnPoint()[0], game.getBlueSpawnPoint()[1], game.getBlueSpawnPoint()[2]);
            for (Player player : game.getRedTeam()) {
                player.sendMessage("Game starting in 5 seconds!");
                player.teleport(redSpawn);
            }
            for (Player player : game.getBlueTeam()) {
                player.sendMessage("Game starting in 5 seconds!");
                player.teleport(blueSpawn);
            }
            //start the game timer
            game.startGameTimer(game.getGameTimeInSeconds());

        }
    }

    public void handleActiveGames(){
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.getGameState() == GameModel.GameState.ACTIVE || game.getGameState() == GameModel.GameState.STARTING){
                System.out.println("Handling an active game at world " + game.getWorld().getName() + " with " + game.getPlayers().size() + " players.");
                GameTimer timer = game.getGameTimer();
                int countdown = timer.getCountdown();

                Objective objective = game.getScoreboard().getObjective("bridge");
                objective.getScoreboard().resetScores(ChatColor.GREEN + "Time: " + (countdown + 1)); // Remove the old score
                Score timerScore = objective.getScore(ChatColor.GREEN + "Time: " + countdown); // Create a new score with the updated timer text
                timerScore.setScore(12);

                game.setScoreboardRedGoals(game.getRedGoals());
                game.setScoreboardBlueGoals(game.getBlueGoals());

                for(Player player: game.getPlayers()){
                    player.setScoreboard(game.getScoreboard());
                }
                if(countdown == 0){
                    //for every player in this game's queue, send them back to the lobby
                    for(Player player: queue.getQueue()){
                        if(player == null)
                            continue;
                        //I need to create the lobby in minecraft
                        player.teleport(lobbyLocation);
                    }
                    queue.clearQueue();
                    game.setGameState(GameModel.GameState.INACTIVE);
                }
            }
        }
    }
}
