package me.liamgiraldo.litebridge;

import me.liamgiraldo.litebridge.commands.*;
import me.liamgiraldo.litebridge.controllers.GameController;
import me.liamgiraldo.litebridge.controllers.MapCreator;
import me.liamgiraldo.litebridge.controllers.QueueController;
import me.liamgiraldo.litebridge.listeners.BedLeaveListener;
import me.liamgiraldo.litebridge.listeners.PlayerJoinListener;
import me.liamgiraldo.litebridge.listeners.ReloadListener;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Queue;

public final class Litebridge extends JavaPlugin implements Listener {

    private static Litebridge plugin;
    private MapCreator mapCreator;

    private ArrayList<GameModel> models;
    private ArrayList<QueueModel> queues = new ArrayList<>();

    private QueueController queueController;
    private GameController gameController;

    public static Litebridge getPlugin(){
        return plugin;
    }

    /**
     * Constructs queues based on existing game models
     * */
    private void constructQueues(ArrayList<GameModel> models, ArrayList<QueueModel> queues){
        for (GameModel model:
                models) {
            queues.add(new QueueModel(model.getMaxPlayers(), model.getWorld().getName(), model));
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        mapCreator = new MapCreator(this);
        this.models = mapCreator.constructGameModels();

        System.out.println("All model's max players upon constuctor load");
        for(int i = 0; i < models.size(); i++){
            System.out.println(models.get(i).getMaxPlayers());
        }

        constructQueues(this.models, queues);

        this.queueController = new QueueController(queues);
        System.out.println("All queue's max players upon constructor load");
        for(int i = 0; i < queues.size(); i++){
            System.out.println(queues.get(i).getAssociatedGame().getMaxPlayers());
        }

        this.gameController = new GameController(queues, this);

        plugin = this;
        System.out.println("Litebridge is running.");

        getServer().getPluginManager().registerEvents(new BedLeaveListener(),this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(gameController, this);
        getServer().getPluginManager().registerEvents(new ReloadListener(gameController), this);
//        TODO: Register events in GameController
//        getServer().getPluginManager().registerEvents(, this);
        getServer().getPluginManager().registerEvents(mapCreator, this);
        getCommand("god").setExecutor(new GodCommand());
        getCommand("feed").setExecutor(new FeedCommand());
        getCommand("repeat").setExecutor(new RepeatCommand());
        getCommand("strike").setExecutor(new StrikeCommand());
        getCommand("setspawn").setExecutor(new SetSpawnCommand());
        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("setjoinmessage").setExecutor(new SetMessageCommand());
        getCommand("bridgewand").setExecutor(mapCreator);
        getCommand("litebridge").setExecutor(queueController);
        getCommand("checkqueues").setExecutor(new CheckqueueCommand(this));
        getCommand("litebridgedebug").setExecutor(new DebugCommand(this));
//        getCommand("game").setExecutor(new GameCommand(gameController));


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        System.out.println("Player " + event.getPlayer().getDisplayName() + "has joined");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("uuid")){
            if(sender instanceof Player){
                Player player = (Player)sender;
                player.sendMessage("Your UUID is " + player.getUniqueId().toString() + ".");
            }
        }
        else{
            return false;
        }
        return true;
    }

    //TODO: The usage of this method intrinsically causes a cyclical dependency. I acknowledge this. Deadlocks be damned.
    /**
     * Adds a given model to the currently registered game models
     * If the provided model is already in the registered models, it will update the provided one
     * @param model Model to register
     * */
    public void addToModels(GameModel model){
        for(int i = 0; i < models.size(); i++){
            if(model.getWorld().getName().equals(models.get(i).getWorld().getName())){
               //if the model already exists within the arraylist of models, just update it
               System.out.println("This model already exists, updating it according to the newly provided one.");
               models.set(i, model);
               return;
           }
       }
       //by reaching the end of the loop, we know it doesn't exist
        System.out.println("This model didn't exist. Adding it to the existing models.");
        models.add(model);
    }


    //TODO Be able to add a queue when a new game model (map) is created
    public void addAQueue(GameModel model){

    }

    public ArrayList<QueueModel> getQueues(){
        return this.queues;
    }

    public ArrayList<GameModel> getModels() {
        return this.models;
    }

    @Override
    public String toString(){
        return this.getName();
    }

    public GameModel getGameModelByWorld(World world) {
        for (GameModel model:
                models) {
            if(model.getWorld().equals(world)){
                return model;
            }
        }
        return null;
    }

    public QueueModel getQueueModelByWorld(World world) {
        for (QueueModel model:
                queues) {
            if(model.getWorld().equals(world)){
                return model;
            }
        }
        return null;
    }
}
