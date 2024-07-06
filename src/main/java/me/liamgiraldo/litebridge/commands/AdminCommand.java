package me.liamgiraldo.litebridge.commands;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.controllers.GameController;
import me.liamgiraldo.litebridge.events.ForceStartEvent;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class AdminCommand implements CommandExecutor {
    private ArrayList<QueueModel> queues;
    private ArrayList<GameModel> models;
    private Litebridge litebridge;
    private GameController gameController;
    public AdminCommand(ArrayList<QueueModel> queues, ArrayList<GameModel> models, Litebridge litebridge, GameController gameController){
        this.queues = queues;
        this.models = models;
        this.litebridge = litebridge;
        this.gameController = gameController;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length == 0) {
                return false;
            }
            /**
             * The available first arguments are
             * - map
             * - queue
             * - game
             * - player
             * */
            switch(args[0]){
                case "map":
                    ArrayList<String> newargs = new ArrayList<>();
                    for(int i = 1; i < args.length; i++){
                        newargs.add(args[i]);
                    }
                    String[] newargsArray = new String[newargs.size()];
                    for(int i = 0; i < newargs.size(); i++){
                        newargsArray[i] = newargs.get(i);
                    }
                    return mapCommand(newargsArray, p);
                case "queue":
                    p.sendMessage("Queue commands not implemented yet");
                    //TODO: implement queue commands
                    return queueCommand();
                case "game":
                    break;
                case "player":
                    break;
                default:
                    return false;
            }
        }
        return false;
    }

    private boolean mapCommand(String[] args, Player player){
        /**
         * The available map commands are
         * - register
         * - delete
         * - list
         * - set
         * - get
         * */
        if(args.length == 0) return false;
        //the player needs to provide a map name, so the length of the arguments should be at least 2
        if(args.length < 2) return false;
        String mapName = args[1];
        if(mapName == null) return false;
        for(GameModel model : models){
            if(model.getWorld().getName().equals(mapName)){
                switch(args[0]){
                    case "register":
                        gameController.gameEndInstantly(model);
                        litebridge.addToModels(model);
                        break;
                    case "delete":
                        gameController.gameEndInstantly(model);
                        //we also need to remove this game from the config
                        litebridge.removeMapFromConfig(args[1]);
                        litebridge.removeGameFromModels(model);
                        break;
                    case "list":
                        for(GameModel gameModel : models){
                            player.sendMessage(gameModel.getWorld().getName());
                        }
                        break;
                    case "set":
                        ArrayList<String> newargs = new ArrayList<>();
                        for(int i = 2; i < args.length; i++){
                            newargs.add(args[i]);
                        }
                        String[] newargsArray = new String[newargs.size()];
                        for(int i = 0; i < newargs.size(); i++){
                            newargsArray[i] = newargs.get(i);
                        }
                        gameController.gameEndInstantly(model);
                        return setCommand(newargsArray, player, model);
                    case "get":
                        String arg = args[2];
                        return getCommand(arg, player, model);
                    default:
                        return false;
                }
            }
        }
        return false;
    }

    public boolean setCommand(String[] args, Player player, GameModel model){
        /**
         * The available parameters are:
         *
         *
         * */
        if(args.length == 0) return false;
        //if arguments [1] [2] and [3] are not integers, return false
        switch(args[0]){
            case "maxplayers":
                try{
                    Integer.parseInt(args[1]);
                }
                catch(NumberFormatException e){
                    return false;
                }
                int maxPlayers = Integer.parseInt(args[1]);
                model.setMaxPlayers(maxPlayers);
                break;
            case "maxgoals":
                try{
                    Integer.parseInt(args[1]);
                }
                catch(NumberFormatException e) {
                    return false;
                }
                int maxGoals = Integer.parseInt(args[1]);
                model.setGoalsToWin(maxGoals);
                break;
            case "redgoals":
                try{
                    Integer.parseInt(args[1]);
                }
                catch(NumberFormatException e){
                    return false;
                }
                int redgoals = Integer.parseInt(args[1]);
                model.setRedGoals(redgoals);
                break;
            case "bluegoals":
                try{
                    Integer.parseInt(args[1]);
                }
                catch(NumberFormatException e){
                    return false;
                }
                int bluegoals = Integer.parseInt(args[1]);
                model.setBlueGoals(bluegoals);
                break;
            case "state":
                try{
                    GameModel.GameState.valueOf(args[1]);
                }
                catch(Exception e){
                    return false;
                }
                GameModel.GameState state = GameModel.GameState.valueOf(args[1]);
                model.setGameState(state);
                break;
            case "redspawn":
                try{
                    Integer.parseInt(args[1]);
                    Integer.parseInt(args[2]);
                    Integer.parseInt(args[3]);
                } catch (NumberFormatException e){
                    return false;
                }
                int redX = Integer.parseInt(args[1]);
                int redY = Integer.parseInt(args[2]);
                int redZ = Integer.parseInt(args[3]);
                model.setRedSpawnPoint(new int[]{redX,redY,redZ});
                break;
            case "bluespawn":
                try{
                    Integer.parseInt(args[1]);
                    Integer.parseInt(args[2]);
                    Integer.parseInt(args[3]);
                } catch (NumberFormatException e){
                    return false;
                }
                int blueX = Integer.parseInt(args[1]);
                int blueY = Integer.parseInt(args[2]);
                int blueZ = Integer.parseInt(args[3]);
                model.setBlueSpawnPoint(new int[]{blueX,blueY,blueZ});
                break;
            case "redcagebounds":
                try{
                    Integer.parseInt(args[1]);
                    Integer.parseInt(args[2]);
                    Integer.parseInt(args[3]);
                    Integer.parseInt(args[4]);
                    Integer.parseInt(args[5]);
                    Integer.parseInt(args[6]);
                } catch (NumberFormatException e){
                    return false;
                }
                int redX1 = Integer.parseInt(args[1]);
                int redY1 = Integer.parseInt(args[2]);
                int redZ1 = Integer.parseInt(args[3]);
                int redX2 = Integer.parseInt(args[4]);
                int redY2 = Integer.parseInt(args[5]);
                int redZ2 = Integer.parseInt(args[6]);
                model.setRedCageBounds(new int[][]{{redX1,redY1,redZ1},{redX2,redY2,redZ2}});
                break;
            case "bluecagebounds":
                try{
                    Integer.parseInt(args[1]);
                    Integer.parseInt(args[2]);
                    Integer.parseInt(args[3]);
                    Integer.parseInt(args[4]);
                    Integer.parseInt(args[5]);
                    Integer.parseInt(args[6]);
                } catch (NumberFormatException e){
                    return false;
                }
                int blueX1 = Integer.parseInt(args[1]);
                int blueY1 = Integer.parseInt(args[2]);
                int blueZ1 = Integer.parseInt(args[3]);
                int blueX2 = Integer.parseInt(args[4]);
                int blueY2 = Integer.parseInt(args[5]);
                int blueZ2 = Integer.parseInt(args[6]);
                model.setBlueCageBounds(new int[][]{{blueX1,blueY1,blueZ1},{blueX2,blueY2,blueZ2}});
                break;
            case "redgoalbounds":
                try{
                    Integer.parseInt(args[1]);
                    Integer.parseInt(args[2]);
                    Integer.parseInt(args[3]);
                    Integer.parseInt(args[4]);
                    Integer.parseInt(args[5]);
                    Integer.parseInt(args[6]);
                } catch (NumberFormatException e){
                    return false;
                }
                int redX1Goal = Integer.parseInt(args[1]);
                int redY1Goal = Integer.parseInt(args[2]);
                int redZ1Goal = Integer.parseInt(args[3]);
                int redX2Goal = Integer.parseInt(args[4]);
                int redY2Goal = Integer.parseInt(args[5]);
                int redZ2Goal = Integer.parseInt(args[6]);
                model.setRedGoalBounds(new int[][]{{redX1Goal,redY1Goal,redZ1Goal},{redX2Goal,redY2Goal,redZ2Goal}});
                break;
            case "bluegoalbounds":
                try{
                    Integer.parseInt(args[1]);
                    Integer.parseInt(args[2]);
                    Integer.parseInt(args[3]);
                    Integer.parseInt(args[4]);
                    Integer.parseInt(args[5]);
                    Integer.parseInt(args[6]);
                } catch (NumberFormatException e){
                    return false;
                }
                int blueX1Goal = Integer.parseInt(args[1]);
                int blueY1Goal = Integer.parseInt(args[2]);
                int blueZ1Goal = Integer.parseInt(args[3]);
                int blueX2Goal = Integer.parseInt(args[4]);
                int blueY2Goal = Integer.parseInt(args[5]);
                int blueZ2Goal = Integer.parseInt(args[6]);
                model.setBlueGoalBounds(new int[][]{{blueX1Goal,blueY1Goal,blueZ1Goal},{blueX2Goal,blueY2Goal,blueZ2Goal}});
                break;
            case "worldbounds":
                try{
                    Integer.parseInt(args[1]);
                    Integer.parseInt(args[2]);
                    Integer.parseInt(args[3]);
                    Integer.parseInt(args[4]);
                    Integer.parseInt(args[5]);
                    Integer.parseInt(args[6]);
                } catch (NumberFormatException e){
                    return false;
                }
                int worldX1 = Integer.parseInt(args[1]);
                int worldY1 = Integer.parseInt(args[2]);
                int worldZ1 = Integer.parseInt(args[3]);
                int worldX2 = Integer.parseInt(args[4]);
                int worldY2 = Integer.parseInt(args[5]);
                int worldZ2 = Integer.parseInt(args[6]);
                model.setWorldBounds(new int[][]{{worldX1,worldY1,worldZ1},{worldX2,worldY2,worldZ2}});
                break;
            default:
                return false;
        }
        return false;
    }

    public boolean getCommand(String arg, Player player, GameModel model){
        switch(arg){
            case "maxplayers":
                player.sendMessage("Max players: " + model.getMaxPlayers());
                return true;
            case "maxgoals":
                player.sendMessage("Max goals: " + model.getGoalsToWin());
                return true;
            case "redgoals":
                player.sendMessage("Red goals: " + model.getRedGoals());
                return true;
            case "bluegoals":
                player.sendMessage("Blue goals: " + model.getBlueGoals());
                return true;
            case "state":
                player.sendMessage("State: " + model.getGameState());
                return true;
            case "redspawn":
                player.sendMessage("Red spawn: " + Arrays.toString(model.getRedSpawnPoint()));
                return true;
            case "bluespawn":
                player.sendMessage("Blue spawn: " + Arrays.toString(model.getBlueSpawnPoint()));
                return true;
            case "redcagebounds":
                player.sendMessage("Red cage bounds: " + Arrays.deepToString(model.getRedCageBounds()));
                return true;
            case "bluecagebounds":
                player.sendMessage("Blue cage bounds: " + Arrays.deepToString(model.getBlueCageBounds()));
                return true;
            case "redgoalbounds":
                player.sendMessage("Red goal bounds: " + Arrays.deepToString(model.getRedGoalBounds()));
                return true;
            case "bluegoalbounds":
                player.sendMessage("Blue goal bounds: " + Arrays.deepToString(model.getBlueGoalBounds()));
                return true;
            case "worldbounds":
                player.sendMessage("World bounds: " + Arrays.deepToString(model.getWorldBounds()));
                return true;
            default:
                return false;
        }
    }

    public boolean queueCommand(String[] args, Player player) {
        //TODO: implement queue commands
        String mapName = args[0];
        if(args.length < 2) return false;
        for(QueueModel queue : queues){
            if(queue.getAssociatedGame().getWorld().getName().equals(mapName)){
                switch(args[1]){
                    case "removeplayer":
                        if (args.length < 3) return false;
                        String playerName = args[2];
                        Player playerToRemove = litebridge.getServer().getPlayer(playerName);
                        queue.removeFromQueue(playerToRemove);
                        gameController.teleportPlayerToLobby(playerToRemove);
                        queue.getAssociatedGame().removePlayer(playerToRemove);
                        break;
                    case "addplayer":
                        if (args.length < 3) return false;
                        String playerNameToAdd = args[2];
                        Player playerToAdd = litebridge.getServer().getPlayer(playerNameToAdd);
                        queue.appendToQueue(playerToAdd);
                        queue.getAssociatedGame().addPlayer(playerToAdd);
                        gameController.teleportPlayerBasedOnTeam(playerToAdd, queue.getAssociatedGame());
                        break;
                    case "listplayers":
                        for(Player p : queue.getQueue()){
                            player.sendMessage(p.getName());
                        }
                        break;
                    case "forcestart":
                        ForceStartEvent event = new ForceStartEvent(queue);
                        litebridge.getServer().getPluginManager().callEvent(event);
                        break;
                    case "forceend":
                        gameController.gameEndInstantly(queue.getAssociatedGame());
                        break;
                    default:
                        return false;
                }
            }
        }
        return false;
    }
}
