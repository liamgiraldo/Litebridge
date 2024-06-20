package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.models.GameModel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Array;
import java.util.*;

public class MapCreator implements CommandExecutor, Listener {
    private String[] stepMessages;
    private int instructionStep;

    private World world;
    private String worldName;

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
    private Litebridge litebridge;


    //cyclical dependency. Deadlock maybe?
    public MapCreator(Litebridge litebridge){
        this.litebridge = litebridge;

        stepMessages = new String[]{
                //TODO this isn't taking into account spawn box positions. Need to add those later.
                "This is a stub message. You will never see this normally. Right click to continue.", //0,
                "Set hard world boundaries. 0 building will be allowed outside of these bounds. Right click to continue", //1
                "Right click to select top left bound", //2
                "Right click to select bottom right bound", //3
                "This is a stub message. Right click to continue.", //4
                "This is a stub message. Right click to continue.", //5
                "This is a stub message. Right click to continue.", //6
                "Next, let's select team spawn points. It might be wise to put them in cages. We set those bounds later. Right click to continue.", //7
                "Right click to select a blue team spawn point", //8
                "Right click to select a red ream spawn point", //9
                "Next, let's set the goal box boundaries. Right click to continue.", //10
                "Let's first do the blue team goal box boundaries. Right click to continue.", //11
                "Right click to select the top left bound", //12
                "Right click to select the bottom right bound", //13
                "Now let's do the red team goal box boundaries. Right click to continue.", //14
                "Right click to select the top left bound", //15
                "Right click to select the bottom right bound", //16
                "Type the max players that can be in this game", //17 Typed Max Players
                "Type the amount of goals that are required to win this game", //18 Typed Goals Required
                "Let's now select the cage boundaries. Right click to continue", //19
                "Right click to select the top left blue cage boundary", //20
                "Right click to select the bottom right blue cage boundary", //21
                "Right click to select the top left red cage boundary", //22
                "Right click to select the bottom right red cage boundary", //23
                "Now we need to select the y-layer that defines the kill boundary. Right click to continue", //24
                "Right click on a block to define the y layer kill boundary", //25
                "If this message is displaying, the map data has been saved successfully. Right click to continue.", //26
                "Should you need to reset this map, do /bridgewand again. All done." //27
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
        meta.setLore(Arrays.asList("Used for making bridge maps.","Hopefully this works!"));
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
            //TODO get this to work specifically for the bridge stick
            if (itemUsed != null && itemUsed.getType() == Material.STICK) {
                this.worldName = player.getWorld().getName();
                //if the config for this world doesn't exist make one
                if(!Litebridge.getPlugin().getConfig().isConfigurationSection(player.getWorld().getName())) {
                    setUpConfig(player.getWorld().getName());
                }
                if(Arrays.equals(oldVector,tempVector)){
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
                        Litebridge.getPlugin().getConfig().set(worldName + ".world-bounds", worldBounds);
                        Litebridge.getPlugin().saveConfig();
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
                        Litebridge.getPlugin().getConfig().set(worldName + ".blue-spawn", blueSpawnPoint);
                        Litebridge.getPlugin().saveConfig();
                        player.sendMessage("Blue spawn point " + Arrays.toString(blueSpawnPoint));
                        incrementStep();
                        break;
                    case 9:
                        if(airClicked)
                            break;
                        redSpawnPoint = Arrays.copyOf(tempVector, tempVector.length);
                        Litebridge.getPlugin().getConfig().set(worldName + ".red-spawn", redSpawnPoint);
                        Litebridge.getPlugin().saveConfig();
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
                        Litebridge.getPlugin().getConfig().set(worldName + ".blue-goal", blueGoalBounds);
                        Litebridge.getPlugin().saveConfig();
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
                        Litebridge.getPlugin().getConfig().set(worldName + ".red-goal", redGoalBounds);
                        Litebridge.getPlugin().saveConfig();
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
                        Litebridge.getPlugin().getConfig().set(worldName + ".blue-cage", blueCageBounds);
                        Litebridge.getPlugin().saveConfig();
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
                        Litebridge.getPlugin().getConfig().set(worldName + ".red-cage", redCageBounds);
                        Litebridge.getPlugin().saveConfig();
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
                        Litebridge.getPlugin().getConfig().set(worldName + ".kill-plane", killPlane);
                        Litebridge.getPlugin().saveConfig();

                        this.world = Litebridge.getPlugin().getServer().getWorld(worldName);
                        this.gameModel = new GameModel(world, blueSpawnPoint,redSpawnPoint,blueGoalBounds,redGoalBounds,blueCageBounds,redCageBounds,worldBounds,killPlane,goalsToWin,maxPlayers);
                        Litebridge.getPlugin().addToModels(this.gameModel);
                        printAllWorldParams(player);

                        //should in theory remove stick
                        player.getInventory().clear(player.getInventory().getHeldItemSlot());

                        //back to first step, but they need another stick
                        instructionStep = 0;
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
                Litebridge.getPlugin().getConfig().set(worldName + ".max-players", maxPlayers);
                Litebridge.getPlugin().saveConfig();
                player.sendMessage("Max players: " + Integer.toString(maxPlayers));
                incrementStep();
                player.sendMessage(stepMessages[instructionStep]);
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
                Litebridge.getPlugin().getConfig().set(worldName + ".goals-required", goalsToWin);
                Litebridge.getPlugin().saveConfig();
                player.sendMessage("Goals for game: " + Integer.toString(goalsToWin));
                player.sendMessage("Goals set. Right click the stick to continue.");
                incrementStep();
                player.sendMessage(stepMessages[instructionStep]);
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

    /**
     * Increments the current instruction step by one.
     * If we are at the final step message within stepMessages, reset to the 0th step
     * */
    private void incrementStep(){
        this.instructionStep+=1;
        if(this.instructionStep == stepMessages.length){
            instructionStep = 0;
        }
    }

    /**
     * Sends the player the contents of a double array
     *
     * @param array The double array to send
     * @param player The player to send it to
     * */
    private void printDoubleArray(int[][] array, Player player){
        String first = Arrays.toString(array[0]);
        String second = Arrays.toString(array[1]);
        player.sendMessage(new String[]{first, second});
    }

    /**
     * Prints out all map creation parameters
     *
     * @param p The player to send the parameters to
     * */
    private void printAllWorldParams(Player p){
        String message = "Blue spawn point: " + Arrays.toString(blueSpawnPoint);
        p.sendMessage(message);

        message = "Blue goal bounds: ";
        p.sendMessage(message);
        printDoubleArray(blueGoalBounds, p);

        message = "Blue cage bounds: ";
        p.sendMessage(message);
        printDoubleArray(blueCageBounds, p);

        message = "Red spawn point: " + Arrays.toString(redSpawnPoint);
        p.sendMessage(message);

        message = "Red goal bounds: ";
        p.sendMessage(message);
        printDoubleArray(redGoalBounds, p);

        message = "Red cage bounds: ";
        p.sendMessage(message);
        printDoubleArray(redCageBounds, p);

        message = "World bounds: ";
        p.sendMessage(message);
        printDoubleArray(worldBounds, p);

        message = "Goals to win: " + Integer.toString(goalsToWin);
        p.sendMessage(message);

        message = "Max Players: " + Integer.toString(maxPlayers);
        p.sendMessage(message);

        message = "Kill plane: " + Integer.toString(killPlane);
        p.sendMessage(message);
    }

    private void setUpConfig(String worldname){
        Litebridge.getPlugin().getConfig().createSection(worldname);
        Litebridge.getPlugin().getConfig().createSection(worldname + ".world-bounds");
        Litebridge.getPlugin().getConfig().createSection(worldname + ".blue-spawn");
        Litebridge.getPlugin().getConfig().createSection(worldname + ".red-spawn");
        Litebridge.getPlugin().getConfig().createSection(worldname + ".blue-goal");
        Litebridge.getPlugin().getConfig().createSection(worldname + ".red-goal");
        Litebridge.getPlugin().getConfig().createSection(worldname + ".blue-cage");
        Litebridge.getPlugin().getConfig().createSection(worldname + ".red-cage");
        Litebridge.getPlugin().getConfig().createSection(worldname + ".kill-plane");
        Litebridge.getPlugin().getConfig().createSection(worldname + ".max-players");
        Litebridge.getPlugin().getConfig().createSection(worldname + ".goals-required");
        Litebridge.getPlugin().saveConfig();
    }

    /**
     * Creates game models based on the existing config file.
     *
     * @return ArrayList of game models
     * */
    public ArrayList<GameModel> constructGameModels() {
        ArrayList<GameModel> models = new ArrayList<>();

        for (String section : Litebridge.getPlugin().getConfig().getKeys(false)) {
            World world = Litebridge.getPlugin().getServer().getWorld(section);
            if (world == null) {
                System.out.println(section + " is not a valid world. Continuing the config parse.");
                continue;
            }

            String worldName = world.getName();

            int[] blueSpawnPoint = listToArray(Litebridge.getPlugin().getConfig().getIntegerList(worldName + ".blue-spawn"));
            int[] redSpawnPoint = listToArray(Litebridge.getPlugin().getConfig().getIntegerList(worldName + ".red-spawn"));

            int[][] redGoalBounds = listToArray2D(Litebridge.getPlugin().getConfig().getList(worldName + ".red-goal"));
            int[][] blueGoalBounds = listToArray2D(Litebridge.getPlugin().getConfig().getList(worldName + ".blue-goal"));

            int[][] redCageBounds = listToArray2D(Litebridge.getPlugin().getConfig().getList(worldName + ".red-cage"));
            int[][] blueCageBounds = listToArray2D(Litebridge.getPlugin().getConfig().getList(worldName + ".blue-cage"));

            int[][] worldBounds = listToArray2D(Litebridge.getPlugin().getConfig().getList(worldName + ".world-bounds"));

            int goalsToWin = Litebridge.getPlugin().getConfig().getInt(worldName + ".goals-required");
            int maxPlayers = Litebridge.getPlugin().getConfig().getInt(worldName + ".max-players");

            int killPlane = Litebridge.getPlugin().getConfig().getInt(worldName + ".kill-plane");

            models.add(new GameModel(world, blueSpawnPoint, redSpawnPoint, blueGoalBounds, redGoalBounds, blueCageBounds, redCageBounds, worldBounds, killPlane, goalsToWin, maxPlayers));
        }
        return models;
    }

    /**
     * Converts a list of integers to an array of integers
     *
     * @param list the list to convert
     * @return the resulting array
     */
    private int[] listToArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Converts a list of lists of integers to a 2D array of integers
     *
     * @param list the list of lists to convert
     * @return the resulting 2D array
     */
    private int[][] listToArray2D(List<?> list) {
        int[][] array = new int[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            array[i] = listToArray((List<Integer>) list.get(i));
        }
        return array;
    }
}
