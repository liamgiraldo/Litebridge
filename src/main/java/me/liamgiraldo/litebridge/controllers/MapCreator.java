package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.models.GameModel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.Vector;

public class MapCreator implements CommandExecutor, Listener {
    private String[] stepMessages;
    private int instructionStep;

    /***
     * World world, Vector<Integer> blueSpawnPoint, Vector<Integer> redSpawnPoint, ArrayList<Vector<Integer>> blueGoalBounds, ArrayList<Vector<Integer>> redGoalBounds, ArrayList<Vector<Integer>> worldBounds, int goalsToWin, int maxPlayers
     *
     * ***/
    private World world;
    private int[] blueSpawnPoint = new int[3];
    private int[][] blueGoalBounds = new int[2][3];
    private int[][] blueCageBounds = new int[2][3];

    private int[] redSpawnPoint = new int[3];
    private int[][] redGoalBounds = new int[2][3];
    private int[][] redCageBounds = new int[2][3];

    private int[][] worldBounds = new int[2][3];

    private int goalsToWin;
    private int maxPlayers;

    private int killPlane;

    private GameModel gameModel;


    public MapCreator(){

        stepMessages = new String[]{
                //TODO this isn't taking into account spawn box positions. Need to add those later.
                "This is a stub message. You will never see this normally. Right click to continue.",
                "Set hard world boundaries. 0 building will be allowed outside of these bounds. Right click to continue", //0
                "Right click to select top left bound", //1
                "Right click to select bottom right bound", //2
                "This is a stub message. Right click to continue.", //3
                "This is a stub message. Right click to continue.", //4
                "This is a stub message. Right click to continue.", //5
                "Next, let's select team spawn points. It might be wise to put them in cages. We set those bounds later. Right click to continue.", //6
                "Right click to select a blue team spawn point", //7
                "Right click to select a red ream spawn point", //8
                "Next, let's set the goal box boundaries. Right click to continue.", //9
                "Let's first do the blue team goal box boundaries. Right click to continue.", //10
                "Right click to select the top left bound", //11
                "Right click to select the bottom right bound", //12
                "Now let's do the red team goal box boundaries. Right click to continue.", //13
                "Right click to select the top left bound", //14
                "Right click to select the bottom right bound", //15
                "Type the max players that can be in this game", //16 Typed Max Players
                "Type the amount of goals that are required to win this game", //17 Typed Goals Required
                "Let's now select the cage boundaries. Right click to continue", //18
                "Right click to select the top left blue cage boundary", //19
                "Right click to select the bottom right blue cage boundary", //20
                "Right click to select the top left red cage boundary", //21
                "Right click to select the bottom right red cage boundary", //22
                "Now we need to select the y-layer that defines the kill boundary. Right click to continue", //23
                "Right click on a block to define the y layer kill boundary", //24
                "If this message is displaying, the map data has been saved successfully. Right click to continue.", //25
                "Should you need to reset this map, do /bridgewand again. All done." //25
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
            givePlayerBridgeWand(player);
        }
        return true;
    }

    /**
     * Gives the player a bridge wand.
     * Name of stick is Bridge Stick
     * @param player Player to give stick to.
     * */
    private void givePlayerBridgeWand(Player player){
        ItemStack stack = new ItemStack(Material.STICK);
        stack.setAmount(1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Bridge Stick");
        meta.setLore(Arrays.asList("&7Used for making bridge maps.","&7Hopefully this works!"));
        stack.setItemMeta(meta);
        player.getInventory().addItem(stack);
    }

    private int[] tempVector = new int[3];
    private int[] oldVector = new int[3];

    @EventHandler
    public void onStickRightClick(PlayerInteractEvent e) {
        //TODO save all these parameters to config.
        //Before the first stick click, the case is 0, but...
        //immediately gets set to 1.
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block blockClicked = e.getClickedBlock();
            boolean airClicked = true;
            if(tempVector != null)
                oldVector = Arrays.copyOf(tempVector, tempVector.length);
            if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                tempVector[0] = blockClicked.getX();
                tempVector[1] = blockClicked.getY();
                tempVector[2] = blockClicked.getZ();
                airClicked = false;
            }
            ItemStack itemUsed = e.getItem();
            Player player = e.getPlayer();
            player.sendMessage("block coords are " + Arrays.toString(tempVector));
            if (itemUsed != null && itemUsed.getType() == Material.STICK) {
                if(tempVector == oldVector){
                    player.sendMessage("You can't have the block position be the same as the last one.");
                    return;
                }
                switch(instructionStep){
                    case 0:
                        //stubstep
                        incrementStep();
                        break;
                    case 1:
                        //whatever happens here first
                        incrementStep();
                        break;
                    case 2:
                        if(airClicked)
                            break;
                        worldBounds[0] = Arrays.copyOf(tempVector, tempVector.length);
                        incrementStep();
                        break;
                    case 3:
                        if(airClicked)
                            break;
                        worldBounds[1] = Arrays.copyOf(tempVector, tempVector.length);
                        player.sendMessage("World bounds ");
                        printDoubleArray(worldBounds, player);
                        incrementStep();
                        break;
                    case 4:
                        incrementStep();
                        break;
                    case 5:
                        //the original kill plane code went here.
                        incrementStep();
                        break;
                    case 6:
                        //the original kill plane code went here.
                        incrementStep();
                        break;
                    case 7:
                        incrementStep();
                        break;
                    case 8:
                        if(airClicked)
                            break;
                        blueSpawnPoint = Arrays.copyOf(tempVector, tempVector.length);
                        player.sendMessage("Blue spawn point " + Arrays.toString(blueSpawnPoint));
                        incrementStep();
                        break;
                    case 9:
                        if(airClicked)
                            break;
                        redSpawnPoint = Arrays.copyOf(tempVector, tempVector.length);
                        player.sendMessage("Red spawn point " + Arrays.toString(redSpawnPoint));
                        incrementStep();
                        break;
                    case 10:
                        incrementStep();
                        break;
                    case 11:
                        incrementStep();
                        break;
                    case 12:
                        if(airClicked)
                            break;
                        blueGoalBounds[0] = Arrays.copyOf(tempVector, tempVector.length);
                        incrementStep();
                        break;
                    case 13:
                        if(airClicked)
                            break;
                        blueGoalBounds[1] = Arrays.copyOf(tempVector, tempVector.length);
                        player.sendMessage("Blue goal bounds ");
                        printDoubleArray(blueGoalBounds, player);
                        incrementStep();
                        break;
                    case 14:
                        incrementStep();
                        break;
                    case 15:
                        if(airClicked)
                            break;
                        redGoalBounds[0] = Arrays.copyOf(tempVector, tempVector.length);
                        incrementStep();
                        break;
                    case 16:
                        if(airClicked)
                            break;
                        redGoalBounds[1] = Arrays.copyOf(tempVector, tempVector.length);
                        player.sendMessage("Red goal bounds ");
                        printDoubleArray(redGoalBounds, player);
                        incrementStep();
                        break;
                    case 17:
                        break;
                    case 18:
                        break;
                    //16 and 17 are text based steps, not needed here.
                    case 19:
                        incrementStep();
                        break;
                    case 20:
                        if(airClicked)
                            break;
                        blueCageBounds[0] = Arrays.copyOf(tempVector, tempVector.length);
                        incrementStep();
                        break;
                    case 21:
                        if(airClicked)
                            break;
                        blueCageBounds[1] = Arrays.copyOf(tempVector, tempVector.length);
                        incrementStep();
                        break;
                    case 22:
                        if(airClicked)
                            break;
                        redCageBounds[0] = Arrays.copyOf(tempVector, tempVector.length);
                        incrementStep();
                        break;
                    case 23:
                        if(airClicked)
                            break;
                        redCageBounds[1] = Arrays.copyOf(tempVector, tempVector.length);
                        incrementStep();
                        break;
                    case 24:
                        incrementStep();
                        break;
                    //final step
                    case 25:
                        if(airClicked)
                            break;
                        killPlane = tempVector[1];
                        this.gameModel = new GameModel(world, blueSpawnPoint,redSpawnPoint,blueGoalBounds,redGoalBounds,blueCageBounds,redCageBounds,worldBounds,killPlane,goalsToWin,maxPlayers);
                        printAllWorldParams(player);
                        break;
                    case 26:
                        incrementStep();
                        break;
                    default:
                        System.out.println("Uh oh! Unprocessable step found! Check MapCreator's event handler!");
                }
                player.sendMessage(stepMessages[instructionStep]);
            }
        }
    }

    /**
     * the wayy to fix this shit
     * funcs[]
     * loop over functions
     *
     * addStep(func*){
     *     ()->{
     *         incrementStep();
     *         break;
     *     }
     * }
     * */

    @EventHandler
    public boolean onPlayerChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        if(this.instructionStep == 17){
            //TODO this has to be refactored later to be for op only. I'm tired.
            try{
                this.maxPlayers = Integer.parseInt(e.getMessage());
                player.sendMessage("Max players: " + Integer.toString(maxPlayers));
                incrementStep();
                //The message was valid, return true;
                return true;
            }catch(Exception exception){
                player.sendMessage("Your input for max players was invalid. Try again.");
                //The message was invalid, return false;
                return false;
            }
        }
        if(this.instructionStep == 18){
            //TODO this has to be refactored later to be for op only. I'm tired.
            try{
                this.goalsToWin = Integer.parseInt(e.getMessage());
                player.sendMessage("Goals for game: " + Integer.toString(goalsToWin));
                player.sendMessage("Goals set. Right click the stick to continue.");
                incrementStep();
                //The message was valid, return true;
                return true;
            }catch(Exception exception){
                player.sendMessage("Your input for goals was invalid. Try again.");
                //The message was invalid, return false;
                return false;
            }
        }
        //If we made it here something went really wrong
        return false;
    }

    private void incrementStep(){
        this.instructionStep+=1;
        if(this.instructionStep == stepMessages.length){
            instructionStep = 0;
        }
    }

    private void printDoubleArray(int[][] array, Player player){
        String first = Arrays.toString(array[0]);
        String second = Arrays.toString(array[1]);
        player.sendMessage(new String[]{first, second});
    }

    private void printAllWorldParams(Player p){
        String message = "Blue spawn point: " + Arrays.toString(blueSpawnPoint);
        p.sendMessage(message);

        message = "Blue goal bounds: ";
        p.sendMessage(message);
        printDoubleArray(blueGoalBounds);

        message = "Blue cage bounds: ";
        p.sendMessage(message);
        printDoubleArray(blueCageBounds);

        message = "Red spawn point: " + Arrays.toString(redSpawnPoint);
        p.sendMessage(message);

        message = "Red goal bounds: ";
        p.sendMessage(message);
        printDoubleArray(redGoalBounds);

        message = "Red cage bounds: ";
        p.sendMessage(message);
        printDoubleArray(redCageBounds);

        message = "World bounds: ";
        p.sendMessage(message);
        printDoubleArray(worldBounds);

        message = "Goals to win: " + Integer.toString(goalsToWin);
        p.sendMessage(message);

        message = "Max Players: " + Integer.toString(maxPlayers);
        p.sendMessage(message);

        message = "Kill plane: " + Integer.toString(killPlane);
        p.sendMessage(message);
    }
}
