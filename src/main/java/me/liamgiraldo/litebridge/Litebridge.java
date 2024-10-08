package me.liamgiraldo.litebridge;

import com.samjakob.spigui.SpiGUI;
import dev.etery.litecosmetics.LiteCosmetics;
import me.liamgiraldo.litebridge.commands.*;
import me.liamgiraldo.litebridge.controllers.*;
import me.liamgiraldo.litebridge.files.HotbarConfig;
import me.liamgiraldo.litebridge.listeners.BedLeaveListener;
import me.liamgiraldo.litebridge.listeners.PlayerJoinListener;
import me.liamgiraldo.litebridge.listeners.ReloadListener;
import me.liamgiraldo.litebridge.models.GUIModel;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import me.liamgiraldo.litebridge.models.SpectatorQueueModel;
import me.stephenminer.litecoin.LiteCoin;
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
    private ArrayList<SpectatorQueueModel> spectatorQueues = new ArrayList<>();

    private QueueController queueController;
    private GameController gameController;
    private GUIController guiController;
    private SpectatorController spectatorController;

    private GUIModel guiModel;
    private LobbyManager lobbyManager;

    public static SpiGUI spiGUI;

    private Location lobby;

    private LiteCoin litecoin;
    private LiteCosmetics liteCosmetics;

    public static Litebridge getPlugin(){
        return plugin;
    }

    public SpiGUI getSpiGUI(){
        return spiGUI;
    }

    /**
     * Constructs queues based on existing game models
     * */
    private void constructQueues(ArrayList<GameModel> models, ArrayList<QueueModel> queues, ArrayList<SpectatorQueueModel> spectatorQueues){
//        for (GameModel model:
//                models) {
//            queues.add(new QueueModel(model.getMaxPlayers(), model.getWorld().getName(), model));
//            spectatorQueues.add(new SpectatorQueueModel(model.getWorld(), model));
//        }

        for(int i = 0; i < models.size(); i++){
            queues.add(new QueueModel(models.get(i).getMaxPlayers(), models.get(i).getWorld().getName(), models.get(i)));
            spectatorQueues.add(new SpectatorQueueModel(models.get(i).getWorld(), queues.get(i), models.get(i)));
        }
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        //in the config, grab the lobby location
//        lobby-world: "world"
//        lobby-x: 0
//        lobby-y: 100
//        lobby-z: 0
        this.lobby = new Location(getServer().getWorld(getConfig().getString("lobby-world")), getConfig().getDouble("lobby-x"), getConfig().getDouble("lobby-y"), getConfig().getDouble("lobby-z"));

        spiGUI = new SpiGUI(this);

        mapCreator = new MapCreator(this);
        this.models = mapCreator.constructGameModels();

        System.out.println("All model's max players upon constuctor load");
        for(int i = 0; i < models.size(); i++){
            System.out.println(models.get(i).getMaxPlayers());
        }

        constructQueues(this.models, queues, spectatorQueues);

        this.spectatorController = new SpectatorController(spectatorQueues, lobby, this);

        this.queueController = new QueueController(queues, spectatorQueues);
        System.out.println("All queue's max players upon constructor load");
        for(int i = 0; i < queues.size(); i++){
            System.out.println(queues.get(i).getAssociatedGame().getMaxPlayers());
        }


        litecoin = (LiteCoin) getServer().getPluginManager().getPlugin("LiteCoin");
        if(litecoin == null){
            System.out.println("Litebridge could not find the LiteCoin plugin, continuing without it.");
        }
        else{
            System.out.println("LiteCoin found, will be used for currency transactions.");
        }

        if (getServer().getPluginManager().getPlugin("LiteCosmetics") != null) {
            liteCosmetics = LiteCosmetics.get();
            System.out.println("LiteCosmestics found, will be used for player cosmetics.");
        } else {
            System.out.println("Litebridge couldn't find the LiteCosmetics plugin, continuing without it.");
        }

        this.gameController = new GameController(queues, this, lobby, litecoin, liteCosmetics);

        plugin = this;

        //TODO: Change the location to be a variable in the config file


        this.guiModel = new GUIModel(this, models, queues);
        this.guiController = new GUIController(guiModel, models);

        lobbyManager = new LobbyManager(lobby, this, queues, this.gameController, spectatorQueues);

        System.out.println("Litebridge is running.");

        getServer().getPluginManager().registerEvents(new BedLeaveListener(),this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(gameController, this);
        getServer().getPluginManager().registerEvents(new ReloadListener(gameController), this);
        getServer().getPluginManager().registerEvents(this.guiController, this);
        getServer().getPluginManager().registerEvents(this.spectatorController, this);
        getServer().getPluginManager().registerEvents(this.lobbyManager, this);
        getServer().getPluginManager().registerEvents(this.queueController, this);
//        TODO: Register events in GameController
//        getServer().getPluginManager().registerEvents(, this);
        getServer().getPluginManager().registerEvents(mapCreator, this);
        getCommand("god").setExecutor(new GodCommand());
        getCommand("feed").setExecutor(new FeedCommand());
        getCommand("repeat").setExecutor(new RepeatCommand());
        getCommand("strike").setExecutor(new StrikeCommand());
        getCommand("setjoinmessage").setExecutor(new SetMessageCommand());
        getCommand("bridgewand").setExecutor(mapCreator);
        getCommand("litebridge").setExecutor(queueController);
        getCommand("checkqueues").setExecutor(new CheckqueueCommand(this));
        getCommand("litebridgedebug").setExecutor(new DebugCommand(this));
        getCommand("litebridgegame").setExecutor(new GameCommand(gameController));
        getCommand("litebridgeadmin").setExecutor(new AdminCommand(queues,models,this, gameController));
        getCommand("litebridgespectator").setExecutor(this.spectatorController);
        getCommand("litebridgelobby").setExecutor(this.lobbyManager);

        HotbarConfig.setup();
        HotbarConfig.get().options().copyDefaults(true);
        HotbarConfig.save();
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
        boolean isInModels = false;
        for(int i = 0; i < models.size(); i++){
            if(model.getWorld().getName().equals(models.get(i).getWorld().getName())){
               //if the model already exists within the arraylist of models, just update it
               System.out.println("This model already exists, updating it according to the newly provided one.");
               models.set(i, model);
                isInModels = true;
            }
            //if the queue for this game already exists, don't create a new one, but if it doesn't, create a new one
            if(queues.get(i).getAssociatedGame().getWorld().getName().equals(model.getWorld().getName())){
                System.out.println("This queue already exists, updating it according to the newly provided one.");
                queues.set(i, new QueueModel(model.getMaxPlayers(), model.getWorld().getName(), model));
            }

            //if the spectator queue for this game already exists, don't create a new one, but if it doesn't, create a new one
            if(spectatorQueues.get(i).getWorld().getName().equals(model.getWorld().getName())){
                System.out.println("This spectator queue already exists, updating it according to the newly provided one.");
                spectatorQueues.set(i, new SpectatorQueueModel(model.getWorld(), queues.get(i), model));
            }
       }
        if(isInModels){
            return;
        }
       //by reaching the end of the loop, we know it doesn't exist
        System.out.println("This model didn't exist. Adding it to the existing models.");
        models.add(model);
        QueueModel newQueue = new QueueModel(model.getMaxPlayers(), model.getWorld().getName(), model);
        queues.add(newQueue);
        spectatorQueues.add(new SpectatorQueueModel(model.getWorld(), newQueue, model));
    }

    public void addAQueue(GameModel model){
        queues.add(new QueueModel(model.getMaxPlayers(), model.getWorld().getName(), model));
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

    public void removeGameFromModels(GameModel model){
        for(QueueModel queue: queues){
            if(queue.getAssociatedGame().getWorld().getName().equals(model.getWorld().getName())){
                queues.remove(queue);
                models.remove(model);
                for(SpectatorQueueModel spectatorQueue: spectatorQueues){
                    if(spectatorQueue.getWorld().getName().equals(model.getWorld().getName())){
                        spectatorQueues.remove(spectatorQueue);
                    }
                }
                return;
            }
        }
    }

    public void removeMapFromConfig(String mapName){
        this.getConfig().set(mapName, null);
    }

}
