package me.liamgiraldo.litebridge.commands;

import me.liamgiraldo.litebridge.controllers.GameController;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommand implements CommandExecutor {
    private GameController gameController;
    public GameCommand(GameController gameController) {
        this.gameController = gameController;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;

            StringBuilder builder = new StringBuilder();
            /**
             * The command arguments available are:
             * - endall
             * - game
             * */
            if(args.length == 0) {
                player.sendMessage("You didn't provide any arguments");
            }
            switch(args[0]) {
                case "endall":
                    gameController.endAllGamesInstantly();
                    builder.append("All games have been ended.");
                    return true;
                case "game":
                    if(args.length == 1) {
                        player.sendMessage("You didn't provide any arguments. Available arguments are: end, cagereset, mapreset.");
                        break;
                    }
                    boolean valid = processGameCommand(args[1]);
                    if(!valid) {
                        player.sendMessage("Invalid argument. Available arguments are: end, cagereset, mapreset.");
                        break;
                    }
                    builder.append("Game command has been processed.");
                    return true;
                default:
                    player.sendMessage("Invalid argument. Available arguments are: endall, game.");
                    break;
            }
        }
        return false;
    }

    private boolean processGameCommand(String arg) {
        QueueModel queue = null;
        for(QueueModel q : gameController.getQueues()) {
            if(q.getWorld().getName().equals(arg)) {
                queue = q;
                break;
            }
        }
        if(queue == null) {
            return false;
        }
        switch(arg){
            /**
             * The commands available are:
             * - end
             * - cagereset
             * - mapreset
             * */
            case "end":
                gameController.gameEnd(queue);
                return true;
            case "cagereset":
                queue.getAssociatedGame().resetCages();
                return true;
            case "mapreset":
                gameController.resetWorld(queue.getWorld());
                return true;
            default:
                return false;
        }
    }
}
