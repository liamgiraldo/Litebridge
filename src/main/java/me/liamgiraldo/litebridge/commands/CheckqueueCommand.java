package me.liamgiraldo.litebridge.commands;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.models.QueueModel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CheckqueueCommand implements CommandExecutor {
    Litebridge litebridge;
    public CheckqueueCommand(Litebridge litebridge){
        this.litebridge = litebridge;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender instanceof Player){
            Player player = (Player)sender;
            player.sendMessage(grabQueues());
            return true;
        }
        return false;
    }

    private String[] grabQueues(){
        String[] queues = new String[litebridge.getQueues().size()];
        ArrayList<QueueModel> queueModelArrayList = litebridge.getQueues();
        for(int i = 0; i < queueModelArrayList.size(); i++){
            QueueModel model = queueModelArrayList.get(i);
            Player[] players = model.getQueue();
            StringBuilder builder = new StringBuilder();
            builder.append(model.getWorld().getName());
            builder.append("\n");
            System.out.println(model.getMaxPlayers());
            for (int j = 0; j < players.length; j++) {
                Player p = players[j];
                if(p == null) {
                    builder.append("[Empty], ");
                } else {
                    builder.append("[").append(p.getName()).append("], ");
                }
            }
            queues[i] = builder.toString();
        }
        return queues;
    }
}
