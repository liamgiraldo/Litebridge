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
    private int[][] blueGoalBounds = new int[3][2];

    private int[] redSpawnPoint = new int[3];
    private int[][] redGoalBounds = new int[3][2];

    private int[][] worldBounds = new int[3][2];

    private int goalsToWin;
    private int maxPlayers;

    private int[][] killPlane = new int[3][2];

    private GameModel gameModel;


    public MapCreator(){
        stepMessages = new String[]{
                //TODO this isn't taking into account spawn box positions. Need to add those later.
                "Set hard world boundaries. 0 building will be allowed outside of these bounds. Right click to continue", //0
                "Right click to select top left bound", //1
                "Right click to select bottom right bound", //2
                "Next, let's set the kill plane. This is one flat rectangle. Right click to continue.", //3
                "Right click to select top left bound", //4
                "Right click to select bottom right bound", //5
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
                "If this message is displaying, the map data has been saved successfully. Right click to continue.", //18
                "Should you need to reset this map, do /bridgewand again. All done." //19
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

    @EventHandler
    public void onStickRightClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            System.out.println("Event handled!");
            Block blockClicked = e.getClickedBlock();
            tempVector[0] = blockClicked.getX();
            tempVector[1] = blockClicked.getY();
            tempVector[2] = blockClicked.getZ();
            ItemStack itemUsed = e.getItem();
            Player player = e.getPlayer();
            if (itemUsed != null && itemUsed.getType() == Material.STICK) {
                player.sendMessage(stepMessages[instructionStep]);
                switch(instructionStep){
                    case 0:
                        //whatever happens here first
                        incrementStep();
                        break;
                    case 1:
                        worldBounds[0] = tempVector;
                        incrementStep();
                        break;
                    case 2:
                        worldBounds[1] = tempVector;
                        incrementStep();
                        break;
                    case 3:
                        incrementStep();
                        break;
                    case 4:
                        killPlane[0] = tempVector;
                        incrementStep();
                        break;
                    case 5:
                        killPlane[1] = tempVector;
                        incrementStep();
                        break;
                    case 6:
                        incrementStep();
                        break;
                    case 7:
                        blueSpawnPoint = tempVector;
                        incrementStep();
                        break;
                    case 8:
                        redSpawnPoint = tempVector;
                        incrementStep();
                        break;
                    case 9:
                        incrementStep();
                        break;
                    case 10:
                        incrementStep();
                        break;
                    case 11:
                        blueGoalBounds[0] = tempVector;
                        incrementStep();
                        break;
                    case 12:
                        blueGoalBounds[1] = tempVector;
                        incrementStep();
                        break;
                    case 13:
                        incrementStep();
                        break;
                    case 14:
                        redGoalBounds[0] = tempVector;
                        incrementStep();
                        break;
                    case 15:
                        redGoalBounds[1] = tempVector;
                        incrementStep();
                        break;
                    //16 and 17 are text based steps, not needed here.
                    case 18:
                        //TODO Game model needs to take in kill bounds
                        this.gameModel = new GameModel(world,blueSpawnPoint,redSpawnPoint,blueGoalBounds,redGoalBounds,worldBounds,goalsToWin,maxPlayers);
                        incrementStep();
                        break;
                    case 19:
                        break;
                    default:
                        System.out.println("Uh oh! Unprocessable step found! Check MapCreator's processNextStep method!");
                }
            }
        }
    }

    @EventHandler
    public boolean onPlayerChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        if(this.instructionStep == 16){
            //TODO this has to be refactored later to be for op only. I'm tired.
            try{
                this.maxPlayers = Integer.parseInt(e.getMessage());
                incrementStep();
                //The message was valid, return true;
                return true;
            }catch(Exception exception){
                player.sendMessage("Your input for max players was invalid. Try again.");
                //The message was invalid, return false;
                return false;
            }
        }
        if(this.instructionStep == 17){
            //TODO this has to be refactored later to be for op only. I'm tired.
            try{
                this.goalsToWin = Integer.parseInt(e.getMessage());
                incrementStep();
                //The message was valid, return true;
                return true;
            }catch(Exception exception){
                player.sendMessage("Your input for max players was invalid. Try again.");
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

}
