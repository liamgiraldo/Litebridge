package me.liamgiraldo.litebridge.commands;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.models.GameModel;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class DebugCommand implements CommandExecutor {
    private final Litebridge plugin;
    public DebugCommand(Litebridge plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
            if(args.length == 0){
                player.sendMessage(ChatColor.RED + "No arguments provided. Please provide an argument.");
                return false;
            }
            else{
                /**
                 * The possible arguments are:
                 * - "config" - prints the config file
                 * - "queues" - prints the queues
                 * - "models" - prints the models (games)
                 * - "worlds" - prints the worlds
                 * - "player" a player to gather information about
                 * - "queue" a queue to gather information about
                 * - "game" a game to gather information about
                 * - "me" to gather information about the player who executed the command
                 * */
                StringBuilder builder = new StringBuilder();
                builder.append(ChatColor.YELLOW).append("Debugging ").append(args[0]).append("... \n");
                switch (args[0]){
                    case "config":
                        for(String key : plugin.getConfig().getKeys(false)){
                            builder.append(key).append(": ").append(plugin.getConfig().get(key)).append("\n");
                        }
                        break;
                    case "queues":
                        for(int i = 0; i < plugin.getQueues().size(); i++){
                            builder.append(plugin.getQueues().get(i).toString()).append("\n");
                        }
                        break;
                    case "models":
                        for(int i = 0; i < plugin.getModels().size(); i++){
                            builder.append(plugin.getModels().get(i).toString()).append("\n");
                        }
                        break;
                    case "worlds":
                        builder.append(plugin.getServer().getWorlds().toString());
                        break;
                    case "player":
                        if(args.length < 3){
                            player.sendMessage(ChatColor.RED + "Please provide a player name and an argument.");
                            player.sendMessage(ChatColor.RED + "Command format is /debug player <playerName> [uuid|name|world|game|queue|team]");
                            return false;
                        }
                        String playerName = args[1];
                        String arg = args[2];
                        builder.append(gatherInfoAboutPlayer(playerName, arg));
                        break;
                    case "queue":
                        if(args.length < 3){
                            player.sendMessage(ChatColor.RED + "Please provide a queue name and an argument.");
                            player.sendMessage(ChatColor.RED + "Command format is /debug queue <queueName> [players|world|max|game]");
                            return false;
                        }
                        String queueName = args[1];
                        String queueArg = args[2];
                        builder.append(gatherInformationAboutAQueue(queueName, queueArg));
                        break;
                    case "game":
                        if(args.length < 3){
                            player.sendMessage(ChatColor.RED + "Please provide a game name and an argument.");
                            player.sendMessage(ChatColor.RED + "Command format is /debug game <gameName> [players|world|max|queue|red|blue|winner|state|time|score]");
                            return false;
                        }
                        String gameName = args[1];
                        String gameArg = args[2];
                        builder.append(gatherInformationAboutAGame(gameName, gameArg));
                        break;
                    case "me":
                        if(args.length < 2){
                            player.sendMessage(ChatColor.RED + "Please provide an argument.");
                            player.sendMessage(ChatColor.RED + "Command format is /debug me [uuid|name|world|game|queue|team]");
                            return false;
                        }
                        String meArg = args[1];
                        builder.append(gatherInfoAboutPlayer(player.getName(), meArg));
                        break;
                    default:
                        builder.append(ChatColor.RED + "Invalid argument. Please provide a valid argument.");
                        return false;
                }
                String finalMessage = builder.toString();
                player.sendMessage(finalMessage);
                return true;
            }
        }
        return false;
    }

    private String gatherInfoAboutPlayer(String playerName, String arg){
        StringBuilder builder = new StringBuilder();
        Player player = plugin.getServer().getPlayer(playerName);
        if(player == null){
            return ChatColor.RED + "Player not found.";
        }
        switch (arg){
            case "uuid":
                builder.append(ChatColor.GREEN).append("UUID: ").append(ChatColor.WHITE).append(player.getUniqueId().toString());
                break;
            case "name":
                builder.append(ChatColor.GREEN).append("Name: ").append(ChatColor.WHITE).append(player.getName());
                break;
            case "world":
                builder.append(ChatColor.GREEN).append("World: ").append(ChatColor.WHITE).append(player.getWorld().getName());
                break;
            case "game":
                if(plugin.getGameModelByWorld(player.getWorld()) == null || plugin.getGameModelByWorld(player.getWorld()).getGameState() == GameModel.GameState.INACTIVE){
                    builder.append(ChatColor.GREEN).append("Game: ").append(ChatColor.WHITE).append("No game found.");
                } else {
                    builder.append(ChatColor.GREEN).append("Game: ").append(ChatColor.WHITE).append(plugin.getGameModelByWorld(player.getWorld()).toString());
                }
                break;
            case "queue":
                if(plugin.getQueueModelByWorld(player.getWorld()) == null || plugin.getQueueModelByWorld(player.getWorld()).isPlayerInQueue(player) == false){
                    builder.append(ChatColor.GREEN).append("Queue: ").append(ChatColor.WHITE).append("No queue found.");
                } else {
                    builder.append(ChatColor.GREEN).append("Queue: ").append(ChatColor.WHITE).append(plugin.getQueueModelByWorld(player.getWorld()).toString());
                }
                break;
            case "team":
                if(plugin.getGameModelByWorld(player.getWorld()) == null || plugin.getGameModelByWorld(player.getWorld()).getGameState() == GameModel.GameState.INACTIVE){
                    builder.append(ChatColor.GREEN).append("Team: ").append(ChatColor.WHITE).append("No team found.");
                } else {
                    boolean isRed = Objects.requireNonNull(plugin.getGameModelByWorld(player.getWorld())).checkIfPlayerIsInRedTeam(player);
                    boolean isBlue = Objects.requireNonNull(plugin.getGameModelByWorld(player.getWorld())).checkIfPlayerIsInBlueTeam(player);
                    if(!isRed && !isBlue){
                        builder.append(ChatColor.GREEN).append("Team: ").append(ChatColor.WHITE).append("No team found.");
                        break;
                    }
                    builder.append(ChatColor.GREEN).append("Team: ").append(ChatColor.WHITE).append(isRed ? "Red" : "Blue");
                }
                break;
            default:
                builder.append(ChatColor.RED).append("Invalid argument. Please provide a valid argument.");
                builder.append(ChatColor.RED).append("Valid arguments are: [uuid|name|world|game|queue|team]");
                break;
        }
        return builder.toString();
    }

    private String gatherInformationAboutAGame(String gameName, String arg){
        if(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)) == null){
            return ChatColor.RED + "Game not found.";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.YELLOW).append(ChatColor.UNDERLINE).append("Debugging ").append(gameName).append("... \n");
        switch(arg){
            case "players":
                builder.append(ChatColor.GREEN).append("Players: ");
                for(int i = 0; i < plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getPlayers().length; i++){
                    builder.append(ChatColor.WHITE).append(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getPlayers()[i].getName()).append("\n");
                }
                break;
            case "world":
                builder.append(ChatColor.GREEN).append("World: ").append(ChatColor.WHITE).append(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getWorld().getName());
                break;
            case "max":
                builder.append(ChatColor.GREEN).append("Max Players: ").append(ChatColor.WHITE).append(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getMaxPlayers());
                break;
            case "queue":
                builder.append(ChatColor.GREEN).append("Queue: ");
                for(int i = 0; i < plugin.getQueues().size(); i++){
                    if(plugin.getQueues().get(i).getAssociatedGame().getWorld().getName().equals(gameName)){
                        builder.append(ChatColor.WHITE).append(plugin.getQueues().get(i).toString()).append("\n");
                    }
                }
                break;
            case "red":
                builder.append(ChatColor.GREEN).append("Red Team: ");
                for(int i = 0; i < plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getRedTeam().length; i++){
                    builder.append(ChatColor.WHITE).append(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getRedTeam()[i].getName()).append("\n");
                }
                break;
            case "blue":
                builder.append(ChatColor.GREEN).append("Blue Team: ");
                for(int i = 0; i < plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getBlueTeam().length; i++){
                    builder.append(ChatColor.WHITE).append(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getBlueTeam()[i].getName()).append("\n");
                }
                break;
            case "winner":
                builder.append(ChatColor.GREEN).append("Winner: ").append(ChatColor.WHITE).append(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).returnWinner());
                break;
            case "state":
                builder.append(ChatColor.GREEN).append("State: ").append(ChatColor.WHITE).append(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getGameState());
                break;
            case "time":
                builder.append(ChatColor.GREEN).append("Time: ").append(ChatColor.WHITE).append(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getGameTimer().getCountdown());
                break;
            case "score":
                builder.append(ChatColor.GREEN).append("Red Goals: ").append(ChatColor.WHITE).append(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getRedGoals());
                builder.append(ChatColor.GREEN).append("Blue Goals: ").append(ChatColor.WHITE).append(plugin.getGameModelByWorld(plugin.getServer().getWorld(gameName)).getBlueGoals());
                break;
            default:
                builder.append(ChatColor.RED).append("Invalid argument. Please provide a valid argument.");
                builder.append(ChatColor.RED).append("Valid arguments are: [players|world|max|queue|red|blue|winner|state|time|score]");
                break;
        }
        return builder.toString();
    }

    private String gatherInformationAboutAQueue(String queueName, String arg){
        //Find a queue by name
        //if that queue doesn't exist, return "Queue not found"
        //if it does exist, return the information about that queue
        if(plugin.getQueueModelByWorld(plugin.getServer().getWorld(queueName)) == null){
            return ChatColor.RED + "Queue not found.";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.YELLOW).append(ChatColor.UNDERLINE).append("Debugging ").append(queueName).append("... \n");
        /**
         * The valid arguments are:
         * - "players" - prints the players in the queue
         * - "world" - prints the world the queue is in
         * - "max" - prints the max players in the queue
         * - "game" - prints the game associated with the queue
         * */
        switch(arg){
            case "players":
                builder.append(ChatColor.GREEN).append("Players: ");
                for(int i = 0; i < plugin.getQueueModelByWorld(plugin.getServer().getWorld(queueName)).getQueue().length; i++){
                    if(plugin.getQueueModelByWorld(plugin.getServer().getWorld(queueName)).getQueue()[i] == null){
                        builder.append(ChatColor.WHITE).append("[Empty], ");
                    } else {
                        builder.append(ChatColor.WHITE).append(plugin.getQueueModelByWorld(plugin.getServer().getWorld(queueName)).getQueue()[i].toString()).append(", ");
                    }
                }
                break;
            case "world":
                builder.append(ChatColor.GREEN + "World: " + ChatColor.WHITE).append(plugin.getQueueModelByWorld(plugin.getServer().getWorld(queueName)).getWorld().getName());
                break;
            case "max":
                builder.append(ChatColor.GREEN + "Max Players: " + ChatColor.WHITE).append(plugin.getQueueModelByWorld(plugin.getServer().getWorld(queueName)).getMaxPlayers());
                break;
            case "game":
                builder.append(ChatColor.GREEN + "Game: " + ChatColor.WHITE).append(plugin.getQueueModelByWorld(plugin.getServer().getWorld(queueName)).getAssociatedGame().toString());
                break;
            default:
                builder.append(ChatColor.RED + "Invalid argument. Valid arguments are: [players|world|max|game]");
                break;
        }
        return builder.toString();
    }
}
