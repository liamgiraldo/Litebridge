package me.liamgiraldo.litebridge.models;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.runnables.GameTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;

public class GameModel {

    private int[] blueSpawnPoint;
    private int[][] blueGoalBounds;
    private int[][] blueCageBounds;

    private int[] redSpawnPoint;
    private int[][] redGoalBounds;
    private int[][] redCageBounds;

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
        ACTIVE
    }

    private GameState gameState;

    private int goalsToWin;
    private int maxPlayers;

    private ArrayList<Player> players;
    private Player[] redTeam;
    private Player[] blueTeam;

    //We will use this defaultMap copy to replace the used map on the game end.
    private World defaultMap;

    private GameTimer gameTimer;
    private int gameTimeInSeconds = 30;

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
    public GameModel(World world, int[] blueSpawnPoint, int[] redSpawnPoint, int[][] blueGoalBounds, int[][] redGoalBounds, int[][] blueCageBounds, int[][] redCageBounds, int[][] worldBounds, int killPlane, int goalsToWin, int maxPlayers, Litebridge plugin){
        this.world = world;
        this.defaultMap = world;
        this.worldBounds = worldBounds;

        this.blueSpawnPoint = blueSpawnPoint;
        this.blueGoalBounds = blueGoalBounds;
        this.blueCageBounds = blueCageBounds;

        this.redGoalBounds = redGoalBounds;
        this.redSpawnPoint = redSpawnPoint;
        this.redCageBounds = redCageBounds;

        this.goalsToWin = goalsToWin;
        this.maxPlayers = maxPlayers;

        //No players when game model is created
        this.players = new ArrayList<Player>();
        this.redTeam = new Player[maxPlayers /2];
        this.blueTeam = new Player[maxPlayers /2];

        this.killPlane = killPlane;

        //Default state for a game is inactive
        this.gameState = GameState.INACTIVE;

        this.plugin = plugin;
        
        this.scoreboardManager = Bukkit.getScoreboardManager();

        this.scoreboard = scoreboardManager.getNewScoreboard();

        this.objective = scoreboard.registerNewObjective("bridge", "dummy");

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

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
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

    public void addPlayer(Player player) {
        //when we add a player, we also need to assign them to a team (red or blue)
        this.players.add(player);
        if (checkIfGameIsFull()) {
            assignPlayerTeams(this.players);
        }
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
    }

    public void removeAllPlayersFromGame() {
        this.players.clear();
    }

    public boolean checkIfGameIsFull() {
        return this.players.size() == this.maxPlayers;
    }

    public boolean checkIfPlayerIsInGame(Player player) {
        return this.players.contains(player);
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
    private void assignPlayerTeams(ArrayList<Player> players) {
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
}
