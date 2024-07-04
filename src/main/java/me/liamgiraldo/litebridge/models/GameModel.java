package me.liamgiraldo.litebridge.models;

import jdk.internal.org.jline.utils.Display;
import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.runnables.GameTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.Arrays;

public class GameModel {

    /**
     * The blue spawn point
     * */
    private int[] blueSpawnPoint;

    /**
     * The yaw of the blue spawn point
     * */
    private float blueSpawnYaw;

    /**
     * The bounds of the blue goal
     * */
    private int[][] blueGoalBounds;

    /**
     * The bounds of the blue cage
     * */
    private int[][] blueCageBounds;

    /**
     * The blocks in the blue cage
     * */
    private ArrayList<Block> blueCageBlocks;

    /**
     * The original blocks in the blue cage
     * */
    private ArrayList<BlockStateModel> originalBlueCageBlocks;

    /**
     * The red spawn point
     * */
    private int[] redSpawnPoint;

    /**
     * The yaw of the red spawn point
     * */
    private float redSpawnYaw;

    /**
     * The bounds of the red goal
     * */
    private int[][] redGoalBounds;

    /**
     * The bounds of the red cage
     * */
    private int[][] redCageBounds;

    /**
     * The original blocks in the blue cage
     * */
    private ArrayList<Block> redCageBlocks;

    /**
     * The original blocks in the blue cage
     * */
    private ArrayList<BlockStateModel> originalRedCageBlocks;

    /**
     * The world for this game
     * */
    private World world;

    /**
     * The boundaries of the world
     * This is used to prevent players from building outside the map / mining outside bounds
     * */
    private int[][] worldBounds;

    /**
     * The scoreboard manager for this game
     * */
    private ScoreboardManager scoreboardManager;

    /**
     * The scoreboard for this game
     * */
    private Scoreboard scoreboard;

    /**
     * The objective for this game
     * Used for the scoreboard
     * */
    private Objective objective;

    /**
     * The red goals for this game
     * */
    private int redGoals = 0;

    /**
     * The blue goals for this game
     * */
    private int blueGoals = 0;

    /**
     * The red scoreboard's score
     * */
    private Score redScoreboardScore;

    /**
     * The blue scoreboard's score
     * */
    private Score blueScoreboardScore;

    /**
     * The plugin instance
     * */
    private Litebridge plugin;

    /**
     * The y coordinate where the kill plane is set
     * */
    private int killPlane;

    /**
     * Enums for the state of the game
     * INACTIVE -> The game is not running
     * QUEUEING -> The game is in the queueing phase
     * STARTING -> The game is starting
     * ENDING -> The game is ending
     * ACTIVE -> The game is active
     * */
    public enum GameState {
        INACTIVE,
        QUEUEING,
        STARTING,
        ENDING,
        ACTIVE
    }

    /**
     * The state of this game
     * */
    private GameState gameState;

    /**
     * The number of goals required to win this game
     * */
    private int goalsToWin;

    /**
     * The maximum number of players permitted in this bridge game
     * */
    private int maxPlayers;


    /**
     * The players in this game
     * */
    private Player[] players;

    /**
     * The red team for this game
     * */
    private final Player[] redTeam;

    /**
     * The blue team for this game
     * */
    private final Player[] blueTeam;

    /**
     * The default map for this game
     *
     * @deprecated We no longer use copying and pasting maps for resetting.
     * */
    private World defaultMap;

    /**
     * The game timer for this game
     * */
    private GameTimer gameTimer;

    /***
     * The amount of time in seconds that the game will run for
     * */
    private int gameTimeInSeconds = 900;

    /**
     * The amount in seconds that a stalling timer will run for
     * */
    private int stallingTimerCountdown = 5;

    /**
     * @param world          The bridge map the game will use
     * @param blueSpawnPoint The blue team's spawn point
     * @param redSpawnPoint  The red team's spawn point
     * @param blueGoalBounds The bounds of the blue team's goal
     * @param redGoalBounds  The bounds of the red team's goal
     * @param blueCageBounds The two blocks that define the blue cage boundaries
     * @param redCageBounds  The two blocks that define the red cage boundaries
     * @param worldBounds    The boundaries of the bridge map. Building outside of this region is prohibited.'
     * @param killPlane      The y coordinate where the kill plane is set
     * @param goalsToWin     The number of goals required to win this game
     * @param maxPlayers     The maximum number of players permitted in this bridge game
     */
    public GameModel(World world, int[] blueSpawnPoint, int[] redSpawnPoint, int[][] blueGoalBounds, int[][] redGoalBounds, int[][] blueCageBounds, int[][] redCageBounds, int[][] worldBounds, int killPlane, int goalsToWin, int maxPlayers, Litebridge plugin, float blueSpawnYaw, float redSpawnYaw){
        this.world = world;
        this.defaultMap = world;
        this.worldBounds = worldBounds;

        this.blueSpawnPoint = blueSpawnPoint;
        this.blueGoalBounds = blueGoalBounds;
        this.blueCageBounds = blueCageBounds;

        this.redGoalBounds = redGoalBounds;
        this.redSpawnPoint = redSpawnPoint;
        this.redCageBounds = redCageBounds;

        //it's fiiiiiiiiine
        this.redSpawnPoint[1] += 1;
        this.blueSpawnPoint[1] += 1;

        this.goalsToWin = goalsToWin;
        this.maxPlayers = maxPlayers;

        //No players when game model is created
        this.players = new Player[maxPlayers];
        this.redTeam = new Player[maxPlayers /2];
        this.blueTeam = new Player[maxPlayers /2];

        this.killPlane = killPlane;

        //Default state for a game is inactive
        this.gameState = GameState.INACTIVE;

        this.plugin = plugin;

        this.redSpawnYaw = redSpawnYaw;
        this.blueSpawnYaw = blueSpawnYaw;
        
        this.scoreboardManager = Bukkit.getScoreboardManager();

        this.scoreboard = scoreboardManager.getNewScoreboard();

        this.objective = scoreboard.registerNewObjective("bridge", "dummy");

        this.gameTimer = new GameTimer(gameTimeInSeconds);

        //The red cage blocks consist of all of the blocks in the red cage bounds
        this.redCageBlocks = new ArrayList<Block>();

        //this method is wrong.
        int[] redBound1 = redCageBounds[0];
        int[] redBound2 = redCageBounds[1];
        int minRedX = Math.min(redBound1[0], redBound2[0]);
        int maxRedX = Math.max(redBound1[0], redBound2[0]);
        int minRedY = Math.min(redBound1[1], redBound2[1]);
        int maxRedY = Math.max(redBound1[1], redBound2[1]);
        int minRedZ = Math.min(redBound1[2], redBound2[2]);
        int maxRedZ = Math.max(redBound1[2], redBound2[2]);
        //now we have the min and max x, y, and z values for the red cage bounds
        //we can iterate through all of the blocks in the red cage bounds
        for (int x = minRedX; x <= maxRedX; x++) {
            for (int y = minRedY; y <= maxRedY; y++) {
                for (int z = minRedZ; z <= maxRedZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    redCageBlocks.add(block);
                }
            }
        }

        this.blueCageBlocks = new ArrayList<Block>();
        int[] blueBound1 = blueCageBounds[0];
        int[] blueBound2 = blueCageBounds[1];
        int minBlueX = Math.min(blueBound1[0], blueBound2[0]);
        int maxBlueX = Math.max(blueBound1[0], blueBound2[0]);
        int minBlueY = Math.min(blueBound1[1], blueBound2[1]);
        int maxBlueY = Math.max(blueBound1[1], blueBound2[1]);
        int minBlueZ = Math.min(blueBound1[2], blueBound2[2]);
        int maxBlueZ = Math.max(blueBound1[2], blueBound2[2]);
        //now we have the min and max x, y, and z values for the blue cage bounds
        //we can iterate through all of the blocks in the blue cage bounds
        for (int x = minBlueX; x <= maxBlueX; x++) {
            for (int y = minBlueY; y <= maxBlueY; y++) {
                for (int z = minBlueZ; z <= maxBlueZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blueCageBlocks.add(block);
                }
            }
        }



        this.originalRedCageBlocks = new ArrayList<BlockStateModel>();
        for (Block block : redCageBlocks) {
            originalRedCageBlocks.add(new BlockStateModel(block.getType(), block.getData()));
        }

        this.originalBlueCageBlocks = new ArrayList<BlockStateModel>();
        for (Block block : blueCageBlocks) {
            originalBlueCageBlocks.add(new BlockStateModel(block.getType(), block.getData()));
        }

        //is it possible to make a deep copy of the redCageBlocks and blueCageBlocks?
        //I'm not sure if this is necessary, but it might be useful to have a copy of the original blocks
        //in case we need to reset the cage boundaries


        objective.setDisplayName(ChatColor.GOLD + "Litebridge");
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

//        redScoreboardScore = objective.getScore(ChatColor.RED + "Red Goals: ");
//        redScoreboardScore.setScore(11);
//
//        blueScoreboardScore = objective.getScore(ChatColor.BLUE + "Blue Goals: ");
//        blueScoreboardScore.setScore(10);

    }

    /**
     * Sets the red goals
     *
     * @param redGoals The red goals to set
     * */
    public void setScoreboardRedGoals(int redGoals) {
        this.redGoals = redGoals;
        scoreboard.resetScores(ChatColor.RED + "Red Goals: " + (redGoals - 1)); // Reset the old score
        redScoreboardScore = objective.getScore(ChatColor.RED + "Red Goals: " + redGoals);
        redScoreboardScore.setScore(11);
//        redScoreboardScore.setScore(11);
    }

    /**
     * Sets the blue goals
     *
     * @param blueGoals The red goals to set
     * */
    public void setScoreboardBlueGoals(int blueGoals) {
        this.blueGoals = blueGoals;
        scoreboard.resetScores(ChatColor.BLUE + "Blue Goals: " + (blueGoals - 1)); // Reset the old score
        blueScoreboardScore = objective.getScore(ChatColor.BLUE + "Blue Goals: " + blueGoals);
        blueScoreboardScore.setScore(10);
//        blueScoreboardScore.setScore(10);
    }

    /**
     * Gets the game time in seconds
     *
     * @return The game time in seconds
     * */
    public int getGameTimeInSeconds() {
        return gameTimeInSeconds;
    }

    /**
     * Sets the game time in seconds
     *
     * @param gameTimeInSeconds The game time in seconds
     * */
    public void setGameTimeInSeconds(int gameTimeInSeconds) {
        this.gameTimeInSeconds = gameTimeInSeconds;
    }

    /**
     * Gets the game timer for this game
     *
     * @return The game timer for this game
     * */
    public GameTimer getGameTimer() {
        return this.gameTimer;
    }

    /**
     * Gets the scoreboard for this game
     *
     * @return The scoreboard for this game
     * */
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    /**
     * Gets the red goals
     *
     * @return The red goals
     * */
    public int getRedGoals() {
        return redGoals;
    }

    /**
     * Sets the red goals
     *
     * @param redGoals The red goals to set
     * */
    public void setRedGoals(int redGoals) {
        this.redGoals = redGoals;
    }

    /**
     * Gets the blue goals
     *
     * @return The blue goals
     * */
    public int getBlueGoals() {
        return blueGoals;
    }

    /**
     * Sets the blue goals
     *
     * @param blueGoals The blue goals to set
     * */
    public void setBlueGoals(int blueGoals) {
        this.blueGoals = blueGoals;
    }

    /**
     * Gets the blue spawn point
     *
     * @return The blue spawn point
     * */
    public int[] getBlueSpawnPoint() {
        return blueSpawnPoint;
    }

    /**
     * Sets the blue spawn point
     *
     * @param blueSpawnPoint The blue spawn point to set
     * */
    public void setBlueSpawnPoint(int[] blueSpawnPoint) {
        this.blueSpawnPoint = blueSpawnPoint;
    }

    /**
     * Gets the blue goal bounds
     *
     * @return The blue goal bounds
     * */
    public int[][] getBlueGoalBounds() {
        return blueGoalBounds;
    }

    /**
     * Sets the blue goal bounds
     *
     * @param blueGoalBounds The blue goal bounds to set
     * */
    public void setBlueGoalBounds(int[][] blueGoalBounds) {
        this.blueGoalBounds = blueGoalBounds;
    }

    /**
     * Gets the blue cage bounds
     *
     * @return The blue cage bounds
     * */
    public int[][] getBlueCageBounds() {
        return blueCageBounds;
    }

    /**
     * Sets the blue cage bounds
     *
     * @param blueCageBounds The blue cage bounds to set
     * */
    public void setBlueCageBounds(int[][] blueCageBounds) {
        this.blueCageBounds = blueCageBounds;
    }

    /**
     * Gets the red team's spawn point
     *
     * @return The red team's spawn point
     * */
    public int[] getRedSpawnPoint() {
        return redSpawnPoint;
    }

    /**
     * Sets the red team's spawn point
     *
     * @param redSpawnPoint The red team's spawn point
     * */
    public void setRedSpawnPoint(int[] redSpawnPoint) {
        this.redSpawnPoint = redSpawnPoint;
    }

    /**
     * Gets the red goal bounds
     *
     * @return The red goal bounds
     * */
    public int[][] getRedGoalBounds() {
        return redGoalBounds;
    }

    /**
     * Sets the red goal bounds
     *
     * @param redGoalBounds The red goal bounds to set
     * */
    public void setRedGoalBounds(int[][] redGoalBounds) {
        this.redGoalBounds = redGoalBounds;
    }

    /**
     * Gets the red cage bounds
     *
     * @return The red cage bounds
     * */
    public int[][] getRedCageBounds() {
        return redCageBounds;
    }

    /**
     * Sets the red cage bounds
     *
     * @param redCageBounds The red cage bounds to set
     * */
    public void setRedCageBounds(int[][] redCageBounds) {
        this.redCageBounds = redCageBounds;
    }

    /**
     * Gets the world for this game
     *
     * @return The world for this game
     * */
    public World getWorld() {
        return world;
    }

    /**
     * Sets the world for this game
     *
     * @param world The world to set
     * */
    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Gets the world bounds
     *
     * @return The world bounds
     * */
    public int[][] getWorldBounds() {
        return worldBounds;
    }

    /**
     * Sets the world bounds
     *
     * @param worldBounds The world bounds to set
     * */
    public void setWorldBounds(int[][] worldBounds) {
        this.worldBounds = worldBounds;
    }

    /**
     * Gets the game state
     *
     * @return The game state
     * */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Sets the game state
     *
     * @param gameState The game state to set
     * */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Gets the number of goals required to win this game
     *
     * @return The number of goals required to win this game
     * */
    public int getGoalsToWin() {
        return goalsToWin;
    }

    /**
     * Sets the number of goals required to win this game
     *
     * @param goalsToWin The number of goals required to win this game
     * */
    public void setGoalsToWin(int goalsToWin) {
        this.goalsToWin = goalsToWin;
    }

    /**
     * Gets the maximum number of players allowed in this game
     *
     * @return The maximum number of players allowed in this game
     * */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Sets the maximum number of players allowed in this game
     *
     * @param maxPlayers The maximum number of players allowed in this game
     * */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    /**
     * Gets the players in this game
     *
     * @return The players in this game
     * */
    public Player[] getPlayers() {
        return players;
    }

    /**
     * Sets the players in this game
     *
     * @param players The players in this game
     * */
    public void setPlayers(Player[] players) {
        this.players = players;
    }

    /**
     * Gets the default map for this game
     *
     * @return The default map for this game
     * @deprecated We no longer use copying and pasting maps for resetting.
     * */
    public World getDefaultMap() {
        return defaultMap;
    }

    /**
     * Sets the default map for this game
     *
     * @param defaultMap The default map for this game
     * @deprecated We no longer use copying and pasting maps for resetting.
     * */
    public void setDefaultMap(World defaultMap) {
        this.defaultMap = defaultMap;
    }

    /**
     * Gets the kill plane
     *
     * @return The y coordinate of the kill plane
     * */
    public int getKillPlane() {
        return killPlane;
    }

    /**
     * Gets the yaw of the blue spawn point
     *
     * @return The yaw of the blue spawn point
     * */
    public float getBlueSpawnYaw() {
        return blueSpawnYaw;
    }

    /**
     * Gets the yaw of the red spawn point
     *
     * @return The yaw of the red spawn point
     * */
    public float getRedSpawnYaw() {
        return redSpawnYaw;
    }

    /**
     * Sets the yaw of the blue spawn point
     *
     * @param blueSpawnYaw The yaw of the blue spawn point
     * */
    public void setBlueSpawnYaw(float blueSpawnYaw) {
        this.blueSpawnYaw = blueSpawnYaw;
    }

    /**
     * Sets the yaw of the red spawn point
     *
     * @param redSpawnYaw The yaw of the red spawn point
     * */
    public void setRedSpawnYaw(float redSpawnYaw) {
        this.redSpawnYaw = redSpawnYaw;
    }

    /**
     * Adds a player to this game,
     * This method also assigns the player to a team (using assignPlayerToTeam(Player))
     * If the game is full, the player will not be added
     *
     * @param player The player to add to the game
     * */
    public void addPlayer(Player player) {
        //when we add a player, we also need to assign them to a team (red or blue)
        //if the game is full, we should not add the player
        if(!checkIfGameIsFull()){
            for(int i = 0; i < players.length; i++){
                if(players[i] == null){
                    players[i] = player;
                    //make sure the player gets assigned to a team
                    assignPlayerToTeam(player);
                    break;
                }
            }
        }
        else{
            player.sendMessage(ChatColor.RED + "We tried to send you to a full game. You should never see this message!");
        }
    }

    /**
     * Assigns a player to a team
     * This method is called when a player is added to the game
     * It assigns the player to the team with the fewest players
     * If both teams have the same number of players, it assigns the player to a random team
     *
     * @param p The player to assign to a team
     * */
    private void assignPlayerToTeam(Player p){
        if(howManyPlayersInRedTeam() < howManyPlayersInBlueTeam()){
            addPlayerToRedTeam(p);
        }
        else if(howManyPlayersInBlueTeam() < howManyPlayersInRedTeam()){
            addPlayerToBlueTeam(p);
        }
        else{
            //if both teams have the same number of players, assign randomly
            if(Math.random() < 0.5){
                addPlayerToRedTeam(p);
            }
            else{
                addPlayerToBlueTeam(p);
            }
        }
    }

    /**
     * Removes a player from the game
     * This includes removing the player from whatever team they are on
     *
     * @param player The player to remove from the game
     * */
    public void removePlayer(Player player) {
        for(int i = 0; i < players.length; i++){
            if(players[i] == player){
                players[i] = null;
                break;
            }
        }
        for(int i = 0; i < redTeam.length; i++){
            if(redTeam[i] == player){
                redTeam[i] = null;
                break;
            }
        }
        for(int i = 0; i < blueTeam.length; i++){
            if(blueTeam[i] == player){
                blueTeam[i] = null;
                break;
            }
        }
    }

    /**
     * Removes all players from the game
     * This includes removing all players from the red and blue teams
     * */
    public void removeAllPlayersFromGame() {
        for(int i = 0; i < players.length; i++){
            players[i] = null;
        }
        for(int i = 0; i < redTeam.length; i++){
            redTeam[i] = null;
        }
        for(int i = 0; i < blueTeam.length; i++){
            blueTeam[i] = null;
        }
    }

    /**
     * Checks if this game is full
     *
     * @return True if the game is full, false otherwise
     * */
    public boolean checkIfGameIsFull() {
        for(Player p : players){
            if(p == null){
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a player is in the game
     *
     * @param player The player to check
     * @return True if the player is in the game, false otherwise
     * */
    public boolean checkIfPlayerIsInGame(Player player) {
        for(Player p : players){
            if(p == player){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a player is in the red team
     *
     * @param player The player to check
     * @return True if the player is in the red team, false otherwise
     * */
    public boolean checkIfPlayerIsInRedTeam(Player player) {
        for(Player p : redTeam){
            if(p == null)
                continue;
            if(p == player){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks how many players are on the red team
     *
     * @return The number of players on the red team
     * */
    public int howManyPlayersInRedTeam() {
        int count = 0;
        for(Player p : redTeam){
            if(p != null)
                count++;
        }
        return count;
    }

    /**
     * Checks how many players are in the blue team
     *
     * @return  The number of players in the blue team
     * */
    public int howManyPlayersInBlueTeam() {
        int count = 0;
        for(Player p : blueTeam){
            if(p != null)
                count++;
        }
        return count;
    }


    /**
     * Adds a player to the red team if there is available space
     *
     * @param p The player to add to the red team
     * */
    public void addPlayerToRedTeam(Player p){
        for(int i = 0; i < redTeam.length; i++){
            if(redTeam[i] == null){
                redTeam[i] = p;
                break;
            }
        }
    }

    /**
     * Adds a player to the blue team if there is available space
     *
     * @param p The player to add to the blue team
     * */
    public void addPlayerToBlueTeam(Player p){
        for(int i = 0; i < blueTeam.length; i++){
            if(blueTeam[i] == null){
                blueTeam[i] = p;
                break;
            }
        }
    }

    /**
     * Checks if a player is in the blue team
     *
     * @return True if the player is in the blue team, false otherwise
     * */
    public boolean checkIfPlayerIsInBlueTeam(Player player) {
        for(Player p : blueTeam){
            if(p == null)
                continue;
            if(p == player){
                return true;
            }
        }
        return false;
    }

    /**
     * Starts this game's timer
     *
     * @param countdown The countdown to start the game timer at
     * */
    public void startGameTimer(int countdown) {
        this.gameTimer = new GameTimer(countdown);
        this.gameTimer.runTaskTimer(Litebridge.getPlugin(), 0, 20);
    }

    /***
     * Assigns a player to a team
     * I'm not quite sure if this should be in the GameModel class or GameController class
     * I suppose it's fine here.
     * @param players The players to assign to a team
     * @deprecated This method has been replaced with assignPlayerToTeam(Player). This also breaks the game somehow.
     * */
    private void assignPlayerTeams(Player[] players) {
        //First, check which team has fewer players
        //Then assign each player in the array list to either the red team or the blue team
        //Assign based on which team has fewer players,
        // and then if both teams have the same amount, assign randomly
        int redTeamSize = this.redTeam.length;
        int blueTeamSize = this.blueTeam.length;
        for (Player player : players) {
            if (howManyPlayersInRedTeam() < howManyPlayersInBlueTeam()) {
                addPlayerToRedTeam(player);
            } else if (blueTeamSize < redTeamSize) {
                addPlayerToBlueTeam(player);
            } else {
                //If both teams have the same number of players, assign randomly
                if (Math.random() < 0.5) {
                    addPlayerToRedTeam(player);
                } else {
                    addPlayerToBlueTeam(player);
                }
            }
        }
    }

    /**
     * Gets the red team
     * @return The red team
     * */
    public Player[] getRedTeam() {
        return this.redTeam;
    }

    /**
     * Gets the blue team
     * @return The blue team
     * */
    public Player[] getBlueTeam() {
        return this.blueTeam;
    }

    /**
     * Updates the scoreboard with the current game time, red goals, and blue goals
     *
     * @param objective The objective to update
     * @param countdown The current countdown
     * */
    public void updateScoreboard(Objective objective, int countdown) {
        scoreboard.resetScores(ChatColor.GREEN + "Time: 0");
        scoreboard.resetScores(ChatColor.GREEN + "Time: " + (countdown + 1));
        Score timerScore = objective.getScore(ChatColor.GREEN + "Time: " + countdown);
        timerScore.setScore(12);

        setScoreboardRedGoals(getRedGoals());
        setScoreboardBlueGoals(getBlueGoals());
    }

    /**
     * Clears the red cage by setting all blocks in the red cage boundaries to air
     * */
    public void clearRedCage() {
        for (Block block : redCageBlocks) {
            block.setType(org.bukkit.Material.AIR);
        }
    }

    /**
     * Resets the red cage to its original state
     * Sets all blocks in the red cage boundaries to their original block type
     * */
    public void resetRedCage() {
        for(int i = 0; i < redCageBlocks.size(); i++){
            Block block = redCageBlocks.get(i);
            BlockStateModel originalBlock = originalRedCageBlocks.get(i);
            block.setType(originalBlock.getMaterial());
            block.setData(originalBlock.getData());
        }
    }

    /**
     * Clears the blue cage by setting all blocks in the blue cage boundaries to air
     * */
    public void clearBlueCage() {
        for (Block block : blueCageBlocks) {
            block.setType(org.bukkit.Material.AIR);
        }
    }

    /**
     * Resets the blue cage to its original state
     * Sets all blocks in the blue cage boundaries to their original block type
     * */
    public void resetBlueCage() {
        for(int i = 0; i < blueCageBlocks.size(); i++){
            Block block = blueCageBlocks.get(i);
            BlockStateModel originalBlock = originalBlueCageBlocks.get(i);
            block.setType(originalBlock.getMaterial());
            block.setData(originalBlock.getData());
        }
    }

    /**
     * Resets the cages for both teams
     * */
    public void resetCages(){
        resetRedCage();
        resetBlueCage();
    }

    /**
     * Clears the cages for both teams in this game.
     * Clears the cages by setting all cage boundary blocks to air
     * */
    public void clearCages(){
        clearRedCage();
        clearBlueCage();
        for(Player p : players){
            p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
        }
    }


    /**
     * Starts a stalling timer of 5 seconds, runs onTick every second and onEnd when the timer ends;
     * This isn't going to work if a player scores a goal in less than 5 seconds before the last one
     * I'm aware of this, but I'm not sure if it's a problem
     *
     * @param onEnd The runnable to run when the timer ends
     * @param onTick The runnable to run every second
     * */
    public void startStallingTimer(Runnable onEnd, Runnable onTick) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (stallingTimerCountdown > 0) {
                    if (onTick != null) {
                        onTick.run();
                    }
                    stallingTimerCountdown--;
                } else {
                    this.cancel();
                    if (onEnd != null) {
                        onEnd.run();
                        stallingTimerCountdown = 5;
                    }
                }
            }
        }.runTaskTimer(plugin, 0,20);
    }

    /**
     * Starts a stalling timer of 5 seconds. Runs onEnd when the timer ends
     *
     * @param onEnd The runnable to run when the timer ends
     * */
    public void startStallingTimer(Runnable onEnd) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (onEnd != null) {
                    onEnd.run();
                }
            }
        }.runTaskLater(plugin, 5 * 20); // 5 seconds * 20 ticks/second
    }


    /**
     * Resets the game to its initial state
     * Does the following:
     * - Sets the game state to inactive
     * - Removes all players from the game
     * - Resets the goals for both teams
     * - Resets the cages for both teams
     * - Cancels the game timer
     * - Resets the scoreboard
     * - Starts the stalling timer (although this is probably unnecessary)
     * */
    public void resetGame() {
        setGameState(GameState.INACTIVE);
        removeAllPlayersFromGame();
        setRedGoals(0);
        setBlueGoals(0);
        resetCages();
        gameTimer.cancel();

        if (objective != null) {
            objective.unregister();
        }
        objective = scoreboard.registerNewObjective("bridge", "dummy");
        objective.setDisplayName(ChatColor.GOLD + "Litebridge");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        startStallingTimer(null, null);
    }

    /**
     * Converts the game model's data to a usable string
     * */
    @Override
    public String toString(){
        return ChatColor.AQUA + "" + ChatColor.BOLD + "GameModel{" +
                ChatColor.GREEN + "world=" + ChatColor.WHITE + world.getName() + ChatColor.GREEN + ", " +
                "blueSpawnPoint=" + ChatColor.WHITE + arrayToString(blueSpawnPoint) + ChatColor.GREEN + ", " +
                "blueGoalBounds=" + ChatColor.WHITE + doubleArrayToString(blueGoalBounds) + ChatColor.GREEN + ", " +
                "blueCageBounds=" + ChatColor.WHITE + doubleArrayToString(blueCageBounds) + ChatColor.GREEN + ", " +
                "redSpawnPoint=" + ChatColor.WHITE + arrayToString(redSpawnPoint) + ChatColor.GREEN + ", " +
                "redGoalBounds=" + ChatColor.WHITE + doubleArrayToString(redGoalBounds) + ChatColor.GREEN + ", " +
                "redCageBounds=" + ChatColor.WHITE + doubleArrayToString(redCageBounds) + ChatColor.GREEN + ", " +
                "worldBounds=" + ChatColor.WHITE + doubleArrayToString(worldBounds) + ChatColor.GREEN + ", " +
                "gameState=" + ChatColor.WHITE + gameState + ChatColor.GREEN + ", " +
                "goalsToWin=" + ChatColor.WHITE + goalsToWin + ChatColor.GREEN + ", " +
                "maxPlayers=" + ChatColor.WHITE + maxPlayers + ChatColor.GREEN + ", " +
                "players=" + ChatColor.WHITE + Arrays.toString(players) + ChatColor.GREEN + ", " +
                "redTeam=" + ChatColor.WHITE + Arrays.toString(redTeam) + ChatColor.GREEN + ", " +
                "blueTeam=" + ChatColor.WHITE + Arrays.toString(blueTeam) + ChatColor.GREEN + ", " +
                "defaultMap=" + ChatColor.WHITE + defaultMap + ChatColor.GREEN + ", " +
                "gameTimer=" + ChatColor.WHITE + gameTimer.toString() + ChatColor.GREEN + ", " +
                "gameTimeInSeconds=" + ChatColor.WHITE + gameTimeInSeconds + ChatColor.GREEN + ", " +
                "plugin=" + ChatColor.WHITE + plugin.toString() + ChatColor.GREEN + ", " +
                "killPlane=" + ChatColor.WHITE + killPlane + ChatColor.GREEN + "}";
    }

    /**
     * Converts a 2D array of integers to a string
     *
     * @param array The 2D array of integers to convert
     * @return The string representation of the 2D array
     * */
    private String doubleArrayToString(int[][] array){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            builder.append(arrayToString(array[i]));
            builder.append(" ");
        }
        return builder.toString();
    }

    /**
     * Converts a 1D integer array to a string
     * Why you wouldn't use Arrays.toString is beyond me, but I wrote it so im going to use it.
     *
     * @param array The 1D integer array to convert
     * */
    private String arrayToString(int[] array){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            builder.append(array[i]);
            builder.append(" ");
        }
        return builder.toString();
    }

    /**
     * Returns the winner of this game in string form
     * Returns "Red" if the red team has reached the max goals
     * Returns "Blue" if the blue team has reached the max goals
     * Returns "No one" if neither team has reached the max goals
     *
     * @return The winner of this game in string form
     * */
    public String returnWinner() {
        //a winner is someone who has reached the max goals
        if (getRedGoals() >= getGoalsToWin()) {
            return "Red";
        } else if (getBlueGoals() >= getGoalsToWin()) {
            return "Blue";
        } else {
            return "No one";
        }
    }

    /**
     * Returns the countdown of the stalling timer
     *
     * @return The countdown of the stalling timer
     * */
    public int getStallingTimerCountdown() {
        return stallingTimerCountdown;
    }
}
