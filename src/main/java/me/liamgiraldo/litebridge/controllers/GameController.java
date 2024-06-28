package me.liamgiraldo.litebridge.controllers;

import com.cryptomorin.xseries.XMaterial;
import jdk.javadoc.internal.doclint.HtmlTag;
import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.events.QueueFullEvent;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import me.liamgiraldo.litebridge.runnables.GameTimer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import static jdk.javadoc.internal.doclint.HtmlTag.BlockType.BLOCK;


public class GameController implements CommandExecutor, Listener {
    //I'm using the queues because they have references to the games of which they queue for
    //You'll have to access the games individually.
    private Location lobbyLocation;
    private ArrayList<QueueModel> queues;

    private Litebridge plugin;

    ArrayList<ItemStack> kitItems;

    /**
     * Constructs a new GameController
     * We use the GameController to manage all the active games and their corresponding queues.
     *
     * @param queues The queues to manage
     */
    public GameController(ArrayList<QueueModel> queues, Litebridge plugin) {
        this.queues = queues;
        this.lobbyLocation = new Location(plugin.getServer().getWorld("world"), 216, 67, 215);

        this.kitItems = new ArrayList<>();
        this.plugin = plugin;

        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE, 1);
        pickaxe.addEnchantment(Enchantment.DIG_SPEED, 1);
        kitItems.add(pickaxe);

        ItemStack sword = new ItemStack(Material.IRON_SWORD, 1);
        kitItems.add(sword);

        ItemStack bow = new ItemStack(Material.BOW, 1);
        kitItems.add(bow);

        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        kitItems.add(arrow);

        ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 8);
        kitItems.add(goldenApple);

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
        //this lets all games start themselves with an event;
        // however, after this, we need the controller to handle the game logic for all the games.
    }

    //I need to loop over all the games, and handles their logic separately
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

                System.out.println("Teleporting player " + player.getName() + " to red spawn at " + redSpawn.toString());
            }
            giveKitToPlayers(game.getRedTeam(), true);
            for (Player player : game.getBlueTeam()) {
                player.sendMessage("Game starting in 5 seconds!");
                player.teleport(blueSpawn);

                System.out.println("Teleporting player " + player.getName() + " to blue spawn at " + blueSpawn.toString());
            }
            giveKitToPlayers(game.getBlueTeam(), false);
            //start the game timer
            game.startGameTimer(game.getGameTimeInSeconds());

        }
    }

    /**
     * This is the game loop
     * This method is called every second
     *This method is responsible for handling the game logic for all active games
     * This includes updating the scoreboard, checking if the game is over, and handling the game end logic
     * */
    public void handleActiveGames(){
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.getGameState() == GameModel.GameState.ACTIVE || game.getGameState() == GameModel.GameState.STARTING){
                System.out.println("Handling an active game at world " + game.getWorld().getName() + " with " + game.getPlayers().size() + " players.");
                GameTimer timer = game.getGameTimer();

                int countdown = timer.getCountdown();

                if(countdown == game.getGameTimeInSeconds() - 5) {
                    game.setGameState(GameModel.GameState.ACTIVE);
                    for(Player player: game.getPlayers()){
                        player.sendMessage("Game starting now!");
                    }
                }

                Objective objective = game.getScoreboard().getObjective("bridge");
                game.updateScoreboard(objective, countdown);

                for(Player player: game.getPlayers()){
                    player.setScoreboard(game.getScoreboard());
                }

                if(hasTheRedTeamWon(game)){
                    //TODO: need to change this later to activate fanfare, THEN end the game
                    gameEnd(queue);
                }
                if (hasTheBlueTeamWon(game)) {
                    //TODO: need to change this later to activate fanfare, THEN end the game
                    gameEnd(queue);
                }

                //the game is over if the countdown reaches 0
                if(countdown == 0){
                    //for every player in this game's queue, send them back to the lobby
                    gameEnd(queue);
                }
            }
        }
    }

    private void giveKitToPlayers(Player[] players, boolean redTeam){
        for (Player player: players){
            //TODO Implement kit giving
            ItemStack redClay = XMaterial.RED_TERRACOTTA.parseItem();
            redClay.setAmount(64);

            ItemStack blueClay = XMaterial.BLUE_TERRACOTTA.parseItem();
            blueClay.setAmount(64);

            //Give the player the kit items
            for(ItemStack item: kitItems){
                player.getInventory().addItem(item);
            }
            if(redTeam)
                player.getInventory().addItem(redClay);
            else
                player.getInventory().addItem(blueClay);
        }
    }

    private void gameEnd(QueueModel queue){
        //When a game ends, we want to reset the game state to inactive,
        //We want to clear the queue,
        //We want to teleport all the players back to the lobby
        //We want to reset the game timer
        GameModel game = queue.getAssociatedGame();
        game.setGameState(GameModel.GameState.INACTIVE);
        for(Player player: game.getPlayers()){
            player.teleport(lobbyLocation);
        }
        //clear every player's inventory as well,
        //we need to reset the scoreboard for each player
        for(Player player: game.getPlayers()){
            if(player==null)
                continue;
            player.getInventory().clear();
            player.setScoreboard(this.plugin.getServer().getScoreboardManager().getNewScoreboard());
        }
        queue.clearQueue();
        game.removeAllPlayersFromGame();
    }

    /**
     * Event called upon player move event
     * This event is used to check if a player has scored a goal
     * If a player has scored a goal, we increment the score of the team that the player is on
     * @param e The PlayerMoveEvent
     * */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        //get the player that moved
        Player player = e.getPlayer();
        Location playerLocation = player.getLocation();
        World world = playerLocation.getWorld();

        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();

            if(game.getWorld() == world && game.getGameState() == GameModel.GameState.ACTIVE){
                System.out.println("Player " + player.getName() + " moved in an active game!");

                // Calculate bounds for red goal
                int[][] redGoalBounds = game.getRedGoalBounds();
                int[] redMinBounds = getMinBounds(redGoalBounds);
                int[] redMaxBounds = getMaxBounds(redGoalBounds);

                // Calculate bounds for blue goal
                int[][] blueGoalBounds = game.getBlueGoalBounds();
                int[] blueMinBounds = getMinBounds(blueGoalBounds);
                int[] blueMaxBounds = getMaxBounds(blueGoalBounds);

                // Check if player is within red goal bounds
                if(isWithinBounds(playerLocation, redMinBounds, redMaxBounds)){
                    handleRedGoal(player, game);
                }

                // Check if player is within blue goal bounds
                if(isWithinBounds(playerLocation, blueMinBounds, blueMaxBounds)){
                    handleBlueGoal(player, game);
                }
            }
        }
    }

    private int[] getMinBounds(int[][] bounds) {
        int minX = Math.min(bounds[0][0], bounds[1][0]);
        int minY = Math.min(bounds[0][1], bounds[1][1]);
        int minZ = Math.min(bounds[0][2], bounds[1][2]);
        return new int[]{minX, minY, minZ};
    }

    private int[] getMaxBounds(int[][] bounds) {
        int maxX = Math.max(bounds[0][0], bounds[1][0]);
        int maxY = Math.max(bounds[0][1], bounds[1][1]);
        int maxZ = Math.max(bounds[0][2], bounds[1][2]);
        return new int[]{maxX, maxY, maxZ};
    }

    private boolean isWithinBounds(Location location, int[] minBounds, int[] maxBounds) {
        return location.getBlockX() >= minBounds[0] && location.getBlockX() <= maxBounds[0] &&
                location.getBlockY() >= minBounds[1] && location.getBlockY() <= maxBounds[1] &&
                location.getBlockZ() >= minBounds[2] && location.getBlockZ() <= maxBounds[2];
    }

    private void handleRedGoal(Player player, GameModel game) {
        System.out.println("Player " + player.getName() + " is within the bounds of the red goal!");

        if(game.checkIfPlayerIsInRedTeam(player)){
            player.sendMessage("Wrong goal! Teleporting you back to your spawn point.");
            player.teleport(new Location(game.getWorld(), game.getRedSpawnPoint()[0], game.getRedSpawnPoint()[1], game.getRedSpawnPoint()[2]));
        } else if(game.checkIfPlayerIsInBlueTeam(player)){
            game.setBlueGoals(game.getBlueGoals() + 1);
            player.sendMessage("You scored a goal for the blue team!");
            player.teleport(new Location(game.getWorld(), game.getBlueSpawnPoint()[0], game.getBlueSpawnPoint()[1], game.getBlueSpawnPoint()[2]));

            updateScoreboard(game);
        }
    }

    private void handleBlueGoal(Player player, GameModel game) {
        System.out.println("Player " + player.getName() + " is within the bounds of the blue goal!");

        if(game.checkIfPlayerIsInBlueTeam(player)){
            player.sendMessage("Wrong goal! Teleporting you back to your spawn point.");
            player.teleport(new Location(game.getWorld(), game.getBlueSpawnPoint()[0], game.getBlueSpawnPoint()[1], game.getBlueSpawnPoint()[2]));
        } else if(game.checkIfPlayerIsInRedTeam(player)){
            game.setRedGoals(game.getRedGoals() + 1);
            player.sendMessage("You scored a goal for the red team!");
            player.teleport(new Location(game.getWorld(), game.getRedSpawnPoint()[0], game.getRedSpawnPoint()[1], game.getRedSpawnPoint()[2]));

            updateScoreboard(game);
        }
    }

    private void updateScoreboard(GameModel game) {
        Objective objective = game.getScoreboard().getObjective("bridge");

        // Reset the old scores
        game.getScoreboard().resetScores(ChatColor.RED + "Red Goals: " + (game.getRedGoals() - 1));
        game.getScoreboard().resetScores(ChatColor.BLUE + "Blue Goals: " + (game.getBlueGoals() - 1));

        // Set the new scores
        objective.getScore(ChatColor.RED + "Red Goals: " + game.getRedGoals()).setScore(11);
        objective.getScore(ChatColor.BLUE + "Blue Goals: " + game.getBlueGoals()).setScore(10);

        // Update the scoreboard for each player
        for (Player player : game.getPlayers()) {
            player.setScoreboard(game.getScoreboard());
        }
    }

    private boolean hasTheRedTeamWon(GameModel game){
        if(game.getRedGoals() >= game.getGoalsToWin()){
            return true;
        }
        return false;
    }
    private boolean hasTheBlueTeamWon(GameModel game){
        if(game.getBlueGoals() >= game.getGoalsToWin()){
            return true;
        }
        return false;
    }

}
