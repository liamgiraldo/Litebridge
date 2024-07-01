package me.liamgiraldo.litebridge.models;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.runnables.GameTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;

public class GameModel {

    private int[] blueSpawnPoint;
    private float blueSpawnYaw;

    private int[][] blueGoalBounds;

    private int[][] blueCageBounds;

    private ArrayList<Block> blueCageBlocks;
    private ArrayList<BlockStateModel> originalBlueCageBlocks;

    private int[] redSpawnPoint;
    private float redSpawnYaw;

    private int[][] redGoalBounds;
    private int[][] redCageBounds;

    private ArrayList<Block> redCageBlocks;
    private ArrayList<BlockStateModel> originalRedCageBlocks;

    private World world;
    private int[][] worldBounds;

    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;
    private Objective objective;

    private int redGoals = 0;
    private int blueGoals = 0;

    private Score redScoreboardScore;
    private Score blueScoreboardScore;

    private Litebridge plugin;

    private int killPlane;

//    /**
//     * The state of this game.
//     * 0 -> inactive
//     * 1 -> queueing
//     * 2 -> starting
//     * 3 -> active
//     * */
//    private int gameState;

    public enum GameState {
        INACTIVE,
        QUEUEING,
        STARTING,
        ENDING,
        ACTIVE
    }

    private GameState gameState;

    private int goalsToWin;
    private int maxPlayers;

    private Player[] players;
    private Player[] redTeam;
    private Player[] blueTeam;

    //We will use this defaultMap copy to replace the used map on the game end.
    private World defaultMap;

    private GameTimer gameTimer;
    private int gameTimeInSeconds = 900;

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

        //TODO: Remove this later, this is just to slightly adjust the spawn points (which we should never have to do)
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

    public void setScoreboardRedGoals(int redGoals) {
        this.redGoals = redGoals;
        scoreboard.resetScores(ChatColor.RED + "Red Goals: " + (redGoals - 1)); // Reset the old score
        redScoreboardScore = objective.getScore(ChatColor.RED + "Red Goals: " + redGoals);
        redScoreboardScore.setScore(11);
//        redScoreboardScore.setScore(11);
    }

    public void setScoreboardBlueGoals(int blueGoals) {
        this.blueGoals = blueGoals;
        scoreboard.resetScores(ChatColor.BLUE + "Blue Goals: " + (blueGoals - 1)); // Reset the old score
        blueScoreboardScore = objective.getScore(ChatColor.BLUE + "Blue Goals: " + blueGoals);
        blueScoreboardScore.setScore(10);
//        blueScoreboardScore.setScore(10);
    }

    public int getGameTimeInSeconds() {
        return gameTimeInSeconds;
    }

    public void setGameTimeInSeconds(int gameTimeInSeconds) {
        this.gameTimeInSeconds = gameTimeInSeconds;
    }

    public GameTimer getGameTimer() {
        return this.gameTimer;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public int getRedGoals() {
        return redGoals;
    }

    public void setRedGoals(int redGoals) {
        this.redGoals = redGoals;
    }

    public int getBlueGoals() {
        return blueGoals;
    }

    public void setBlueGoals(int blueGoals) {
        this.blueGoals = blueGoals;
    }

    public int[] getBlueSpawnPoint() {
        return blueSpawnPoint;
    }

    public void setBlueSpawnPoint(int[] blueSpawnPoint) {
        this.blueSpawnPoint = blueSpawnPoint;
    }

    public int[][] getBlueGoalBounds() {
        return blueGoalBounds;
    }

    public void setBlueGoalBounds(int[][] blueGoalBounds) {
        this.blueGoalBounds = blueGoalBounds;
    }

    public int[][] getBlueCageBounds() {
        return blueCageBounds;
    }

    public void setBlueCageBounds(int[][] blueCageBounds) {
        this.blueCageBounds = blueCageBounds;
    }

    public int[] getRedSpawnPoint() {
        return redSpawnPoint;
    }

    public void setRedSpawnPoint(int[] redSpawnPoint) {
        this.redSpawnPoint = redSpawnPoint;
    }

    public int[][] getRedGoalBounds() {
        return redGoalBounds;
    }

    public void setRedGoalBounds(int[][] redGoalBounds) {
        this.redGoalBounds = redGoalBounds;
    }

    public int[][] getRedCageBounds() {
        return redCageBounds;
    }

    public void setRedCageBounds(int[][] redCageBounds) {
        this.redCageBounds = redCageBounds;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public int[][] getWorldBounds() {
        return worldBounds;
    }

    public void setWorldBounds(int[][] worldBounds) {
        this.worldBounds = worldBounds;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public int getGoalsToWin() {
        return goalsToWin;
    }

    public void setGoalsToWin(int goalsToWin) {
        this.goalsToWin = goalsToWin;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public World getDefaultMap() {
        return defaultMap;
    }

    public void setDefaultMap(World defaultMap) {
        this.defaultMap = defaultMap;
    }

    public int getKillPlane() {
        return killPlane;
    }

    public float getBlueSpawnYaw() {
        return blueSpawnYaw;
    }

    public float getRedSpawnYaw() {
        return redSpawnYaw;
    }

    public void setBlueSpawnYaw(float blueSpawnYaw) {
        this.blueSpawnYaw = blueSpawnYaw;
    }

    public void setRedSpawnYaw(float redSpawnYaw) {
        this.redSpawnYaw = redSpawnYaw;
    }

    public void addPlayer(Player player) {
        //when we add a player, we also need to assign them to a team (red or blue)
        //if the game is full, we should not add the player
        if(!checkIfGameIsFull()){
            for(int i = 0; i < players.length; i++){
                if(players[i] == null){
                    players[i] = player;
                    break;
                }
            }
        }
        else{
            player.sendMessage(ChatColor.RED + "We tried to send you to a full game. You should never see this message!");
        }
        if (checkIfGameIsFull()) {
            assignPlayerTeams(this.players);
        }
    }

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

    public boolean checkIfGameIsFull() {
        for(Player p : players){
            if(p == null){
                return false;
            }
        }
        return true;
    }

    public boolean checkIfPlayerIsInGame(Player player) {
        for(Player p : players){
            if(p == player){
                return true;
            }
        }
        return false;
    }

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

    public int howManyPlayersInRedTeam() {
        int count = 0;
        for(Player p : redTeam){
            if(p != null)
                count++;
        }
        return count;
    }

    public int howManyPlayersInBlueTeam() {
        int count = 0;
        for(Player p : blueTeam){
            if(p != null)
                count++;
        }
        return count;
    }

    public void addPlayerToRedTeam(Player p){
        for(int i = 0; i < redTeam.length; i++){
            if(redTeam[i] == null){
                redTeam[i] = p;
                break;
            }
        }
    }

    public void addPlayerToBlueTeam(Player p){
        for(int i = 0; i < blueTeam.length; i++){
            if(blueTeam[i] == null){
                blueTeam[i] = p;
                break;
            }
        }
    }

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

    public void startGameTimer(int countdown) {
        this.gameTimer = new GameTimer(countdown);
        this.gameTimer.runTaskTimer(Litebridge.getPlugin(), 0, 20);
    }

    /***
     * Assigns a player to a team
     * I'm not quite sure if this should be in the GameModel class or GameController class
     * I suppose it's fine here.
     * @param players The players to assign to a team
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

    public Player[] getRedTeam() {
        return this.redTeam;
    }

    public Player[] getBlueTeam() {
        return this.blueTeam;
    }

    public void updateScoreboard(Objective objective, int countdown) {
        scoreboard.resetScores(ChatColor.GREEN + "Time: 0");
        scoreboard.resetScores(ChatColor.GREEN + "Time: " + (countdown + 1));
        Score timerScore = objective.getScore(ChatColor.GREEN + "Time: " + countdown);
        timerScore.setScore(12);

        setScoreboardRedGoals(getRedGoals());
        setScoreboardBlueGoals(getBlueGoals());
    }

    //I need a method that sets all the blocks in the red cage boundaries to air
    public void clearRedCage() {
        for (Block block : redCageBlocks) {
            block.setType(org.bukkit.Material.AIR);
        }
    }

    //I also need a method that sets all the blocks in the red cage boundaries to it's original block type
    public void resetRedCage() {
        for(int i = 0; i < redCageBlocks.size(); i++){
            Block block = redCageBlocks.get(i);
            BlockStateModel originalBlock = originalRedCageBlocks.get(i);
            block.setType(originalBlock.getMaterial());
            block.setData(originalBlock.getData());
        }
    }

    //I need a method that sets all the blocks in the blue cage boundaries to air
    public void clearBlueCage() {
        for (Block block : blueCageBlocks) {
            block.setType(org.bukkit.Material.AIR);
        }
    }

    //I also need a method that sets all the blocks in the blue cage boundaries to it's original block type
    public void resetBlueCage() {
        for(int i = 0; i < blueCageBlocks.size(); i++){
            Block block = blueCageBlocks.get(i);
            BlockStateModel originalBlock = originalBlueCageBlocks.get(i);
            block.setType(originalBlock.getMaterial());
            block.setData(originalBlock.getData());
        }
    }

    public void resetCages(){
        resetRedCage();
        resetBlueCage();
    }
    public void clearCages(){
        clearRedCage();
        clearBlueCage();
        for(Player p : players){
            p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
        }
    }

    public void startStallingTimer(Runnable onEnd) {
        new BukkitRunnable() {
            @Override
            public void run() {
                onEnd.run();
            }
        }.runTaskLater(plugin, 5 * 20); // 5 seconds * 20 ticks/second
    }

    public void resetGame() {
        setGameState(GameState.INACTIVE);
        removeAllPlayersFromGame();
        setRedGoals(0);
        setBlueGoals(0);
        resetCages();
        gameTimer.cancel();
        startStallingTimer(null);
    }
}
