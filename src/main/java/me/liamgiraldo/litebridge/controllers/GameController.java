package me.liamgiraldo.litebridge.controllers;

import com.cryptomorin.xseries.XMaterial;
import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.events.QueueFullEvent;
import me.liamgiraldo.litebridge.models.BlockChangeModel;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import me.liamgiraldo.litebridge.runnables.GameTimer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController implements CommandExecutor, Listener {

    private final Location lobbyLocation;
    private final ArrayList<QueueModel> queues;

    private final Litebridge plugin;

    ArrayList<ItemStack> kitItems;

    //Map of world to list of block changes
    private final Map<World, List<BlockChangeModel>> changes = new HashMap<>();

    /**
     * Constructs a new GameController
     * We use the GameController to manage all the active games and their corresponding queues.
     *
     * @param queues The queues to manage
     * @param plugin The lite-bridge plugin
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
            //once every second
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * This method is called when a command is executed
     * This method is used to handle commands
     *
     * @param sender The sender of the command
     * @param command The command that was executed
     * @param label The label of the command
     * @param args The arguments of the command
     *
     * @return Whether the command was executed successfully
     * */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    /**
     * Event called upon queue full event
     * This event is used to start a game when a queue is full
     *
     * @param e The QueueFullEvent
     * */
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

    /**
     * This method handles game starting logic
     * This includes teleporting players to their spawn points, giving them kits, and starting the game timer
     *
     * @param game The game to start
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
     * This method is responsible for handling the game logic for all active games
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

                    for(ItemStack item: player.getInventory().getContents()){
                        if(item != null){
                            item.setDurability((short) 0);
                        }
                    }
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

    /**
     * Gives the kit items to a set of player
     * Kit items are defined in the GameController constructor
     *
     * @param players The players to give the kit items to
     * @param redTeam Whether the players are on the red team or not
     * */
    private void giveKitToPlayers(Player[] players, boolean redTeam){
        for (Player player: players){
            //TODO Implement kit giving
            ItemStack redClay = XMaterial.RED_TERRACOTTA.parseItem();
            assert redClay != null;
            redClay.setAmount(64);

            ItemStack blueClay = XMaterial.BLUE_TERRACOTTA.parseItem();
            assert blueClay != null;
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

    /**
     * Executes game end logic
     * This method is called when a game ends
     *
     * @param queue The queue associated with the game that should end
     * */
    private void gameEnd(QueueModel queue){
        //When a game ends, we want to reset the game state to inactive,
        //We want to clear the queue,
        //We want to teleport all the players back to the lobby
        //We want to reset the game timer
        GameModel game = queue.getAssociatedGame();
        resetWorld(game.getWorld());
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
        game.setBlueGoals(0);
        game.setRedGoals(0);
    }

    /**
     * Event called upon player move event
     * This event is used to check if a player has scored a goal
     * If a player has scored a goal, we increment the score of the team that the player is on
     *
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

                int killPlane = game.getKillPlane();
                if(playerLocation.getBlockY() <= killPlane){
                    teleportPlayerBasedOnTeam(player, game);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent e){
        if(e.getEntity() instanceof Player){
            Player player = (Player) e.getEntity();
            for(QueueModel queue: queues){
                GameModel game = queue.getAssociatedGame();
                if(game.getPlayers().contains(player)){
                    if(e.getCause() == EntityDamageEvent.DamageCause.FALL){
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeathEvent(EntityDamageEvent e) //Listens to EntityDamageEvent
    {
        if(e.getEntity() instanceof  Player) {
            Player player = (Player) e.getEntity();
            for(QueueModel queue: queues){
                GameModel game = queue.getAssociatedGame();
                if(game.getPlayers().contains(player)){
                    if(player.getHealth() - player.getLastDamage() <= 0){
                        e.setCancelled(true);
                        player.setHealth(20);
                        teleportPlayerBasedOnTeam(player, game);
                    }
                }
            }
        }
    }

    private void teleportPlayerBasedOnTeam(Player p, GameModel game){
        if(game.checkIfPlayerIsInRedTeam(p)){
            p.teleport(new Location(game.getWorld(), game.getRedSpawnPoint()[0], game.getRedSpawnPoint()[1], game.getRedSpawnPoint()[2]));
        } else if(game.checkIfPlayerIsInBlueTeam(p)){
            p.teleport(new Location(game.getWorld(), game.getBlueSpawnPoint()[0], game.getBlueSpawnPoint()[1], game.getBlueSpawnPoint()[2]));
        }
    }

    /**
     * Adds a block change to the changes map
     * */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        Block block = e.getBlock();
        for(QueueModel queue: queues){
            //if the player is in a starting, queueing, or inactive game, cancel the block change
            if(queue.getAssociatedGame().getPlayers().contains(p)){
                GameModel game = queue.getAssociatedGame();

                //TODO: Remove this, this is just for testing
                if(game.getGameState() == GameModel.GameState.INACTIVE) {
                    return;
                }

                if(game.getGameState() == GameModel.GameState.STARTING || game.getGameState() == GameModel.GameState.INACTIVE || game.getGameState() == GameModel.GameState.QUEUEING){
                    e.setCancelled(true);
                    return;
                }

                //if the game is active, only allow players to place blocks within the world boundaries
                if(game.getGameState() == GameModel.GameState.ACTIVE){
                    int[][] worldBounds = game.getWorldBounds();
                    int[] worldMinBounds = getMinBounds(worldBounds);
                    int[] worldMaxBounds = getMaxBounds(worldBounds);
                    if(!isWithinBounds(block.getLocation(), worldMinBounds, worldMaxBounds)){
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            //if the block is in the world of an active game, add the block change to the changes map
            if(queue.getAssociatedGame().getWorld() == block.getWorld() && queue.getAssociatedGame().getGameState() == GameModel.GameState.ACTIVE){
                // If there is no existing block change for the location, add a new block change
                changes.computeIfAbsent(block.getWorld(), k -> new ArrayList<>())
                        .add(new BlockChangeModel(block.getLocation(), block.getType(), e.getBlockReplacedState().getType()));
                return;
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Block block = e.getBlock();
        Player p = e.getPlayer();
        for(QueueModel queue: queues){
            //if the player is in a starting, queueing, or inactive game, cancel the block change
            if(queue.getAssociatedGame().getPlayers().contains(p)){
                GameModel game = queue.getAssociatedGame();

                //TODO: This is just for testing, remove this
                if(game.getGameState() == GameModel.GameState.INACTIVE) {
                    return;
                }

                if(game.getGameState() == GameModel.GameState.STARTING || game.getGameState() == GameModel.GameState.INACTIVE || game.getGameState() == GameModel.GameState.QUEUEING){
                    e.setCancelled(true);
                    return;
                }

                if(game.getGameState() == GameModel.GameState.ACTIVE) {
                    //We can't let the player break any blocks that AREN'T stained clay
                    if (block.getType() != Material.STAINED_CLAY && block.getType() != XMaterial.RED_TERRACOTTA.parseMaterial() && block.getType() != XMaterial.BLUE_TERRACOTTA.parseMaterial()) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            //if the block is in the world of an active game, add the block change to the changes map
            if(queue.getAssociatedGame().getWorld() == block.getWorld() && queue.getAssociatedGame().getGameState() == GameModel.GameState.ACTIVE){
                //if the world doesn't have any block changes, add a new list of block changes
                changes.computeIfAbsent(block.getWorld(), k -> new ArrayList<>())
                        .add(new BlockChangeModel(block.getLocation(), block.getType(), Material.AIR));
                return;
            }
        }
    }

    public void resetWorld(World world) {
        List<BlockChangeModel> worldChanges = changes.get(world);
        if (worldChanges != null) {
            for (BlockChangeModel change : worldChanges) {
                change.getLocation().getBlock().setType(change.getBefore());
            }
        }
        changes.remove(world);
    }

    /**
     * Gets the minimum bounds of a 3D space
     *
     * @param bounds The bounds to get the minimum of (2D array, 2x3, two (X, Y, Z) sets of three coordinates)
     * @return The minimum bounds of the 3D space
     * */
    private int[] getMinBounds(int[][] bounds) {
        int minX = Math.min(bounds[0][0], bounds[1][0]);
        int minY = Math.min(bounds[0][1], bounds[1][1]);
        int minZ = Math.min(bounds[0][2], bounds[1][2]);
        return new int[]{minX, minY, minZ};
    }

    /**
     * Gets the maximum bounds of a 3D space
     *
     * @param bounds The bounds to get the maximum of (2D array, 2x3, two (X, Y, Z) sets of three coordinates)
     *
     * @return The maximum bounds of the 3D space
     * */
    private int[] getMaxBounds(int[][] bounds) {
        int maxX = Math.max(bounds[0][0], bounds[1][0]);
        int maxY = Math.max(bounds[0][1], bounds[1][1]);
        int maxZ = Math.max(bounds[0][2], bounds[1][2]);
        return new int[]{maxX, maxY, maxZ};
    }

    /**
     * Checks if a location is within the bounds of a 3D space
     *
     * @param location The location to check
     * @param minBounds The minimum bounds of the 3D space
     * @param maxBounds The maximum bounds of the 3D space
     *
     * @return Whether the location is within the bounds of the 3D space
     * */
    private boolean isWithinBounds(Location location, int[] minBounds, int[] maxBounds) {
        return location.getBlockX() >= minBounds[0] && location.getBlockX() <= maxBounds[0] &&
                location.getBlockY() >= minBounds[1] && location.getBlockY() <= maxBounds[1] &&
                location.getBlockZ() >= minBounds[2] && location.getBlockZ() <= maxBounds[2];
    }

    /**
     * Checks, and handles a player scoring a goal in the red goal
     *
     * @param player The player to check / handle the goal for
     * @param game The game to check / handle the goal for
     * */
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

    /**
     * Checks, and handles a player scoring a goal in the blue goal
     *
     * @param player The player to check / handle the goal for
     * @param game The game to check / handle the goal for
     * */
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

    /**
     * Updates the scoreboard for a game
     * Specifically updates the red goals and blue goals for a game
     * Does not update the timer or other information
     *
     * @param game The game to update the scoreboard for
     * */
    private void updateScoreboard(GameModel game) {
        Objective objective = game.getScoreboard().getObjective("bridge");

        //Reset the old scores
        game.getScoreboard().resetScores(ChatColor.RED + "Red Goals: " + (game.getRedGoals() - 1));
        game.getScoreboard().resetScores(ChatColor.BLUE + "Blue Goals: " + (game.getBlueGoals() - 1));

        //Set the new scores
        objective.getScore(ChatColor.RED + "Red Goals: " + game.getRedGoals()).setScore(11);
        objective.getScore(ChatColor.BLUE + "Blue Goals: " + game.getBlueGoals()).setScore(10);

        //Update the scoreboard for each player
        for (Player player : game.getPlayers()) {
            player.setScoreboard(game.getScoreboard());
        }
    }

    /**
     * Checks if the red team has won the game
     *
     * @param game The game to check if the red team has won
     * */
    private boolean hasTheRedTeamWon(GameModel game){
        return game.getRedGoals() >= game.getGoalsToWin();
    }

    /**
     * Checks if the blue team has won the game
     *
     * @param game The game to check if the blue team has won
     * */
    private boolean hasTheBlueTeamWon(GameModel game){
        return game.getBlueGoals() >= game.getGoalsToWin();
    }
}
