package me.liamgiraldo.litebridge.models;

import com.sun.tools.javac.util.List;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Vector;

public class GameModel {

    private int[] blueSpawnPoint;
    private int[][] blueGoalBounds;
    private int[][] blueCageBounds;

    private int[] redSpawnPoint;
    private int[][] redGoalBounds;
    private int[][] redCageBounds;

    private World world;
    private int[][] worldBounds;

    /**
     * The state of this game.
     * 0 -> inactive
     * 1 -> queueing
     * 2 -> starting
     * 3 -> active
     * */
    private int gameState;

    private int goalsToWin;
    private int maxPlayers;
    private ArrayList<Player> players;

    //We will use this defaultMap copy to replace the used map on game end.
    private World defaultMap;

    /**
     * @param world The bridge map the game will use
     * @param blueSpawnPoint The blue team's spawn point
     * @param redSpawnPoint The red team's spawn point
     * @param blueGoalBounds The bounds of the blue team's goal
     * @param redGoalBounds The bounds of the red team's goal
     * @param blueCageBounds The two blocks that define the blue cage boundaries
     * @param redCageBounds The two blocks that define the red cage boundaries
     * @param worldBounds The boundaries of the bridge map. Building outside of this region is prohibited.'
     * @param killPlane The y coordinate where the kill plane is set
     * @param goalsToWin The amount of goals required to win this game
     * @param maxPlayers The maximum amount of players permitted in this bridge game
     *
     * */
    public GameModel(World world, int[] blueSpawnPoint, int[] redSpawnPoint,int[][] blueGoalBounds, int[][] redGoalBounds, int[][] blueCageBounds, int[][] redCageBounds, int[][] worldBounds, int killPlane, int goalsToWin, int maxPlayers){
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
        //Default state for a game is inactive
        this.gameState = 0;
    }

}
