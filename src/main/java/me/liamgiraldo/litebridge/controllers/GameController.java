package me.liamgiraldo.litebridge.controllers;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.sun.org.apache.xpath.internal.operations.Bool;
import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.events.ForceStartEvent;
import me.liamgiraldo.litebridge.events.GameEndEvent;
import me.liamgiraldo.litebridge.events.QueueFullEvent;
import me.liamgiraldo.litebridge.files.HotbarConfig;
import me.liamgiraldo.litebridge.models.BlockChangeModel;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import me.liamgiraldo.litebridge.runnables.GameTimer;
import me.liamgiraldo.litebridge.utils.DoublyLinkedList;
import me.stephenminer.litecoin.LiteCoin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController implements CommandExecutor, Listener {

    private final Location lobbyLocation;
    private final ArrayList<QueueModel> queues;

    private final Litebridge plugin;

    ArrayList<ItemStack> kitItems;

//    private ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
    private ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
    private ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
    private ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);

//    private LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
    private LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
    private LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
    private LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();

    private String gameIsStartingTitle = ChatColor.GOLD + "" + ChatColor.BOLD + "Game is starting";
    private String subtitle = ChatColor.GRAY + "Cages open in:";
    private String playerScoredTitle = ChatColor.GRAY + "" + ChatColor.BOLD + "Scored!";

    /**
     * Map of changes made to every game world
     * */
    private final Map<World, DoublyLinkedList> changes = new HashMap<>();
    private final Map<Player, Player> lastDamagerMap = new HashMap<>();
    private final Map<Player, Integer> arrowCountdowns = new HashMap<>();

    private LiteCoin litecoin;

    /**
     * Constructs a new GameController
     * We use the GameController to manage all the active games and their corresponding queues.
     *
     * @param queues The queues to manage
     * @param plugin The lite-bridge plugin
     */
    public GameController(ArrayList<QueueModel> queues, Litebridge plugin, Location lobbyLocation, LiteCoin litecoin) {
        this.queues = queues;
        this.lobbyLocation = lobbyLocation;

        this.kitItems = new ArrayList<>();
        this.plugin = plugin;

        this.litecoin = litecoin;

        ItemStack sword = new ItemStack(Material.IRON_SWORD, 1);
        kitItems.add(sword);

        ItemStack bow = new ItemStack(Material.BOW, 1);
        kitItems.add(bow);

        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE, 1);
        pickaxe.addEnchantment(Enchantment.DIG_SPEED, 1);
        kitItems.add(pickaxe);

        //Giving clay
        kitItems.add(new ItemStack(Material.STAINED_CLAY, 64));
        //I mean it should be stained clay but this is a hacky fix
        kitItems.add(new ItemStack(Material.WOOL, 64));

        ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 8);
        kitItems.add(goldenApple);

        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        kitItems.add(arrow);

        //run handleActiveGames every second
        new BukkitRunnable() {
            @Override
            public void run() {
                handleActiveGames();
            }
            //once every second
        }.runTaskTimer(plugin, 0L, 20L);
    }

    //every time a player shoots an arrow, we need to start their arrow timer.
    public void startArrowTimer(Runnable onEnd, Runnable onTick, Player player){
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrowCountdowns.get(player) > 0) {
                    if (onTick != null) {
                        onTick.run();
                    }
                    arrowCountdowns.put(player, arrowCountdowns.get(player).intValue() - 1);
                }
                else {
                    this.cancel();
                    if (onEnd != null) {
                        onEnd.run();
                        arrowCountdowns.put(player, 3);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    /**
     * This method is called when a command is executed
     * This method is used to handle commands
     *
     * @param sender The sender of the command
     * @param command The command that was executed
     * @param label The label of the command
     * @param args The arguments of the command
     *
     * @return Whether the command was executed successfully
     * */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    /**
     * Event called upon queue full event
     * This event is used to start a game when a queue is full
     *
     * @param e The QueueFullEvent
     * */
    @EventHandler
    public void onQueueFull(QueueFullEvent e) {
        QueueModel queue = e.getQueue();
        GameModel game = queue.getAssociatedGame();
        Player[] players = queue.getQueue();
        for (Player p : players) {
            if (p == null)
                continue;
            game.addPlayer(p);
        }
        System.out.println("A queue is full!");
        gameStartLogic(game);
        //this lets all games start themselves with an event;
        // however, after this, we need the controller to handle the game logic for all the games.
    }

    @EventHandler
    public void onForceStart(ForceStartEvent e){
        QueueModel queue = e.getQueue();
        GameModel game = queue.getAssociatedGame();
        Player[] players = queue.getQueue();
        for(Player p: players){
            if(p == null)
                continue;
            game.addPlayer(p);
        }
        gameStartLogic(game);
    }

    /**
     * This method handles game starting logic
     * This includes teleporting players to their spawn points, giving them kits, and starting the game timer
     *
     * @param game The game to start
     * */
    private void gameStartLogic(GameModel game) {
        //If the game is full, we want to start the game
//        if (game.checkIfGameIsFull()) {
            System.out.println("Starting a full game!");
            //set the cages back to their original state if they weren't there already
            game.resetCages();
            //Set the game state to starting
            game.setGameState(GameModel.GameState.STARTING);
            for(Player player: game.getPlayers()){
                if(player == null)
                    continue;
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(20);
            }
            //teleport the players to their respective spawn points
            teleportPlayersToSpawn(game);

            giveKitToPlayers(game.getRedTeam(), true);
            giveKitToPlayers(game.getBlueTeam(), false);
            //start the stalling timer
//            game.startStallingTimer(()->{game.clearCages(); game.setGameState(GameModel.GameState.ACTIVE);});
            game.startStallingTimer(()->{
                game.clearCages();
                game.setGameState(GameModel.GameState.ACTIVE);
                for(Player player: game.getPlayers()){
                    if(player == null)
                        continue;
                    player.sendTitle("","");
                }
            },
                    ()->{
                        System.out.println("Stalling timer countdown: " + game.getStallingTimerCountdown());
                sendEachPlayerStartingTitle(game, game.getStallingTimerCountdown());
                    });
            //start the game timer
            game.startGameTimer(game.getGameTimeInSeconds());
//        }
    }

    /**
     * This is the game loop
     * This method is called every second
     * This method is responsible for handling the game logic for all active games
     * This includes updating the scoreboard, checking if the game is over, and handling the game end logic
     * */
    public void handleActiveGames(){
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
//            System.out.println("Checking game in world: " + game.getWorld().getName() + ", state: " + game.getGameState());
            if(game.getGameState() == GameModel.GameState.ACTIVE || game.getGameState() == GameModel.GameState.STARTING){
//                System.out.println("Handling an active game at world " + game.getWorld().getName() + " with " + game.getPlayers().length + " players.");
                GameTimer timer = game.getGameTimer();

                int countdown = timer.getCountdown();

                if(countdown == game.getGameTimeInSeconds() - 5) {
                    game.setGameState(GameModel.GameState.ACTIVE);
                    for(Player player: game.getPlayers()){
                        if(player == null)
                            continue;
                        player.sendMessage(ChatColor.GREEN + "Game starting now!");
                    }
                }

                Objective objective = game.getScoreboard().getObjective("bridge");
                game.updateScoreboard(objective, countdown);

                for(Player player : game.getWorld().getPlayers()){
                    if(player == null)
                        continue;
                    player.setScoreboard(game.getScoreboard());
                }

                for(Player player: game.getPlayers()){
                    if(player == null)
                        continue;
                    player.setScoreboard(game.getScoreboard());

                    reduceArrowsToOne(player);

                    for(ItemStack item: player.getInventory().getContents()){
                        if(item != null){
                            if(item.getType() == Material.STAINED_CLAY)
                                continue;
                            item.setDurability((short) 0);
                        }
                    }
                }

                if(hasTheRedTeamWon(game)){
                    gameEnd(queue);
                }
                if (hasTheBlueTeamWon(game)) {
                    gameEnd(queue);
                }

                //the game is over if the countdown reaches 0
                if(countdown == 0){
                    //for every player in this game's queue, send them back to the lobby
                    gameEnd(queue);
                }
            }
        }
    }

    /**
     * Gives the kit items to a set of player
     * Kit items are defined in the GameController constructor
     *
     * @param players The players to give the kit items to
     * @param redTeam Whether the players are on the red team or not
     * */
    private void giveKitToPlayers(Player[] players, boolean redTeam){
        if(players == null){
            System.out.println("I'm trying to give a kit to a null player array");
            return;
        }
        for (Player player: players){
            //so now players have a customizable hotbar, which is stored in the hotbar.yml file
            //
            if(player == null){
                System.out.println("I'm trying to give a kit to a null player");
                continue;
            }
            player.getInventory().clear();

//            helmetMeta.setColor(redTeam ? Color.RED : Color.BLUE);
            chestplateMeta.setColor(redTeam ? Color.RED : Color.BLUE);
            leggingsMeta.setColor(redTeam ? Color.RED : Color.BLUE);
            bootsMeta.setColor(redTeam ? Color.RED : Color.BLUE);

//            helmet.setItemMeta(helmetMeta);
            chestplate.setItemMeta(chestplateMeta);
            leggings.setItemMeta(leggingsMeta);
            boots.setItemMeta(bootsMeta);

//            player.getInventory().setHelmet(helmet);
            player.getInventory().setChestplate(chestplate);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(boots);

            //TODO Implement kit giving
            ItemStack redClay = XMaterial.RED_TERRACOTTA.parseItem();
            assert redClay != null;
            redClay.setAmount(64);

            ItemStack blueClay = XMaterial.BLUE_TERRACOTTA.parseItem();
            assert blueClay != null;
            blueClay.setAmount(64);

            //Give the player the kit items
            for(ItemStack item: kitItems){
                int itemPosition;
                try{
                    //from the hotbar config, we get the item position
                    itemPosition = HotbarConfig.get().getInt(player.getUniqueId().toString() + "." + item.getType().toString());

                    int secondItemPosition = HotbarConfig.get().getInt(player.getUniqueId().toString() + "." + "BOW");
                    int thirdItemPosition = HotbarConfig.get().getInt(player.getUniqueId().toString() + "." + "DIAMOND_PICKAXE");
                    if(secondItemPosition == 0 && thirdItemPosition == 0){
                        itemPosition = -1;
                    }
                } catch(NullPointerException e){
                    itemPosition = -1;
                }
                if(item.getType() == Material.STAINED_CLAY || item.getType() == Material.WOOL){
                    if (redTeam) {
                        if(itemPosition != -1)
                            player.getInventory().setItem(itemPosition, redClay);
                        else
                            player.getInventory().addItem(redClay);
                    }
                    else {
                        if(itemPosition != -1)
                            player.getInventory().setItem(itemPosition, blueClay);
                        else
                            player.getInventory().addItem(blueClay);
//                        player.getInventory().addItem(blueClay);
                    }
                    continue;
                }
                if(itemPosition != -1)
                    player.getInventory().setItem(itemPosition, item);
                else
                    player.getInventory().addItem(item);
            }
        }
    }

    //on inventory change event, check if the player has gained an arrow,
    //if they have, and their amount is more than 1, set it to 1
    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player player = (Player) e.getWhoClicked();
            for(QueueModel queue: queues){
                GameModel game = queue.getAssociatedGame();
                if(game.checkIfPlayerIsInGame(player)){
                    reduceArrowsToOne(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.checkIfPlayerIsInGame(player)){
                if(event.getItem().getItemStack().getType() == Material.ARROW){
                    reduceArrowsToOne(player);
                }
            }
        }
    }

    /**
     * Checks if a player has more than one arrow in their inventory
     * If a player has more than one arrow in their inventory, we reduce the amount to one
     *
     * @param player The player to check
     * */
    private void reduceArrowsToOne(Player player){
        ItemStack[] items = player.getInventory().getContents();
        int arrowCount = 0;
        for(ItemStack item: items){
            if(item != null){
                if(item.getType() == Material.ARROW){
                    arrowCount += item.getAmount();
                }
            }
        }
        if(arrowCount > 1){
            for(ItemStack item: items){
                if(item != null){
                    if(item.getType() == Material.ARROW){
                        player.getInventory().remove(Material.ARROW);
                        ItemStack arrow = new ItemStack(Material.ARROW, 1);
                        player.getInventory().addItem(arrow);
                    }
                }
            }
        }
    }

    @EventHandler
    private void cancelMobSpawnEvent(CreatureSpawnEvent e){
        //if the world is a game world, and the spawn reason is natural, cancel the event
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.getWorld() == e.getLocation().getWorld()){
                if(e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL){
                    e.setCancelled(true);
                }
            }
        }
    }

    private void giveKitToPlayersNoArrow(Player[] players, boolean redTeam){
        //so we want to do the exact same thing as giveKitToPlayers, but without the arrow
        for(Player p : players){
            if(p == null)
                continue;
            p.getInventory().clear();

//            helmetMeta.setColor(redTeam ? Color.RED : Color.BLUE);
            chestplateMeta.setColor(redTeam ? Color.RED : Color.BLUE);
            leggingsMeta.setColor(redTeam ? Color.RED : Color.BLUE);
            bootsMeta.setColor(redTeam ? Color.RED : Color.BLUE);

//            helmet.setItemMeta(helmetMeta);
            chestplate.setItemMeta(chestplateMeta);
            leggings.setItemMeta(leggingsMeta);
            boots.setItemMeta(bootsMeta);

//            p.getInventory().setHelmet(helmet);
            p.getInventory().setChestplate(chestplate);
            p.getInventory().setLeggings(leggings);
            p.getInventory().setBoots(boots);

            //TODO Implement kit giving
            ItemStack redClay = XMaterial.RED_TERRACOTTA.parseItem();
            assert redClay != null;
            redClay.setAmount(64);

            ItemStack blueClay = XMaterial.BLUE_TERRACOTTA.parseItem();
            assert blueClay != null;
            blueClay.setAmount(64);

            for(ItemStack item: kitItems){
                if(item.getType() == Material.STAINED_CLAY) {
                    if (redTeam)
                        p.getInventory().addItem(redClay);
                    else
                        p.getInventory().addItem(blueClay);
                    continue;
                }
                if(item.getType() == Material.ARROW)
                    continue;
                p.getInventory().addItem(item);
            }
        }
    }

    /**
     * Gives the kit items to a single player
     * Kit items are defined in the GameController constructor
     *
     * @param player The player to give the kit items to
     * */
    private void giveKitToSinglePlayer(Player player){
        boolean isPlayerOnRedTeam = false;

        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.checkIfPlayerIsInGame(player)){
                if(game.checkIfPlayerIsInRedTeam(player))
                    isPlayerOnRedTeam = true;
            }
        }

        player.getInventory().clear();

        if(isPlayerOnRedTeam){
//            helmetMeta.setColor(Color.RED);
            chestplateMeta.setColor(Color.RED);
            leggingsMeta.setColor(Color.RED);
            bootsMeta.setColor(Color.RED);
        } else {
//            helmetMeta.setColor(Color.BLUE);
            chestplateMeta.setColor(Color.BLUE);
            leggingsMeta.setColor(Color.BLUE);
            bootsMeta.setColor(Color.BLUE);
        }

//        helmet.setItemMeta(helmetMeta);
        chestplate.setItemMeta(chestplateMeta);
        leggings.setItemMeta(leggingsMeta);
        boots.setItemMeta(bootsMeta);

//        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);

        //TODO Implement kit giving
        ItemStack redClay = XMaterial.RED_TERRACOTTA.parseItem();
        assert redClay != null;
        redClay.setAmount(64);

        ItemStack blueClay = XMaterial.BLUE_TERRACOTTA.parseItem();
        assert blueClay != null;
        blueClay.setAmount(64);

        //Give the player the kit items
        for(ItemStack item: kitItems){
            int itemPosition;
            try{
                //from the hotbar config, we get the item position
                itemPosition = HotbarConfig.get().getInt(player.getUniqueId().toString() + "." + item.getType().toString());

                int secondItemPosition = HotbarConfig.get().getInt(player.getUniqueId().toString() + "." + "BOW");
                int thirdItemPosition = HotbarConfig.get().getInt(player.getUniqueId().toString() + "." + "DIAMOND_PICKAXE");
                if(secondItemPosition == 0 && thirdItemPosition == 0){
                    itemPosition = -1;
                }
            } catch(NullPointerException e){
                player.sendMessage(ChatColor.GRAY + "An error occurred while trying to give you your kit. Please try again.");
                itemPosition = -1;
            }
            if(item.getType() == Material.STAINED_CLAY || item.getType() == Material.WOOL){
                if (isPlayerOnRedTeam) {
                    if (itemPosition != -1) {
                        player.getInventory().setItem(itemPosition, redClay);
                    }
                    else {
                        player.getInventory().addItem(redClay);
                    }
                }
//                    player.getInventory().addItem(redClay);
                else {
                    if (itemPosition != -1) {
                        player.getInventory().setItem(itemPosition, blueClay);
                    }
                    else {
                        player.getInventory().addItem(blueClay);
                    }
//                    player.getInventory().addItem(blueClay);
                }
                continue;
            }
            if(itemPosition != -1)
                player.getInventory().setItem(itemPosition, item);
            else
                player.getInventory().addItem(item);
        }
    }


    private void giveKitToSinglePlayerNoArrow(Player player){
        boolean isPlayerOnRedTeam = false;

        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.checkIfPlayerIsInGame(player)){
                if(game.checkIfPlayerIsInRedTeam(player))
                    isPlayerOnRedTeam = true;
            }
        }

        player.getInventory().clear();

        if(isPlayerOnRedTeam){
//            helmetMeta.setColor(Color.RED);
            chestplateMeta.setColor(Color.RED);
            leggingsMeta.setColor(Color.RED);
            bootsMeta.setColor(Color.RED);
        } else {
//            helmetMeta.setColor(Color.BLUE);
            chestplateMeta.setColor(Color.BLUE);
            leggingsMeta.setColor(Color.BLUE);
            bootsMeta.setColor(Color.BLUE);
        }

//        helmet.setItemMeta(helmetMeta);
        chestplate.setItemMeta(chestplateMeta);
        leggings.setItemMeta(leggingsMeta);
        boots.setItemMeta(bootsMeta);

//        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);

        //TODO Implement kit giving
        ItemStack redClay = XMaterial.RED_TERRACOTTA.parseItem();
        assert redClay != null;
        redClay.setAmount(64);

        ItemStack blueClay = XMaterial.BLUE_TERRACOTTA.parseItem();
        assert blueClay != null;
        blueClay.setAmount(64);

        //Give the player the kit items
        for(ItemStack item: kitItems){
            if(item.getType() == Material.STAINED_CLAY) {
                if (isPlayerOnRedTeam)
                    player.getInventory().addItem(redClay);
                else
                    player.getInventory().addItem(blueClay);
                continue;
            }
            player.getInventory().addItem(item);
        }
    }

    private XMaterial clayMaterialBasedOnTeam(boolean redTeam){
        if(redTeam)
            return XMaterial.RED_TERRACOTTA;
        else
            return XMaterial.BLUE_TERRACOTTA;
    }

    /**
     * Executes game end logic
     * This method is called when a game ends
     *
     * @param queue The queue associated with the game that should end
     * */
    public void gameEnd(QueueModel queue){
        //When a game ends, we want to reset the game state to inactive,
        //We want to clear the queue,
        //We want to teleport all the players back to the lobby
        //We want to reset the game timer
        GameModel game = queue.getAssociatedGame();
        game.setGameState(GameModel.GameState.ENDING);
        resetWorld(game.getWorld());
        //clear every player's inventory as well,
        //we need to reset the scoreboard for each player
        for(Player player: game.getWorld().getPlayers()){
            if(player == null)
                continue;
            player.setScoreboard(this.plugin.getServer().getScoreboardManager().getNewScoreboard());
        }
        for(Player player: game.getPlayers()){
            if(player==null)
                continue;
            player.getInventory().setArmorContents(null);
            player.getInventory().clear();
            player.setScoreboard(this.plugin.getServer().getScoreboardManager().getNewScoreboard());
            player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
            //check who won the game
            if(hasTheRedTeamWon(game)){
                player.sendMessage(ChatColor.RED + "Red team has won the game!");
                player.sendTitle(ChatColor.RED + "Red team won!", null);
            } else if(hasTheBlueTeamWon(game)){
                player.sendMessage(ChatColor.BLUE + "Blue team has won the game!");
                player.sendTitle(ChatColor.BLUE + "Blue team won!", null);
            } else {
                player.sendMessage(ChatColor.GRAY + "It's a tie!");
                player.sendTitle(ChatColor.GRAY + "Tied game!", null);
            }
        }

        sendMessageToAllPlayersInGame(game, ChatColor.GRAY + "Top Killer: ");

        if(game.getTopKiller() != null)
            sendMessageToAllPlayersInGame(game, ChatColor.GOLD + game.getTopKiller().getName() + ChatColor.GRAY + " with " + ChatColor.GOLD + game.getTopKillerKills() + ChatColor.GRAY + " kills");
//        sendMessageToAllPlayersInGame(game, ChatColor.GOLD + game.getTopKiller().getName() + ChatColor.GRAY + " with " + ChatColor.GOLD + game.getTopKillerKills() + ChatColor.GRAY + " kills");
        else{
            sendMessageToAllPlayersInGame(game, ChatColor.GRAY + "No top killer");
        }

        //REACT WITH A THUMBS UP IF YOU LOVE LITEBRIDGE!!!!!!!!

        if(litecoin!= null){
            if(game.getTopKiller() != null) {
                game.getTopKiller().sendMessage(ChatColor.GOLD + "+1 Litecoin" + ChatColor.DARK_GRAY + " (Top Killer)");
                litecoin.incrementBalance(game.getTopKiller().getUniqueId(), 1);
            }
        }

        game.startStallingTimer(()->{
            for(Player player: game.getPlayers()){
                if(player==null)
                    continue;
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                player.teleport(lobbyLocation);

                if(litecoin != null) {
                    player.sendMessage(ChatColor.GOLD + "+1 Litecoin" + ChatColor.DARK_GRAY + " (Participation)");
                    litecoin.incrementBalance(player.getUniqueId(), 1);
                }
                else{
                    System.out.println("Litecoin is null");
                }
            }

            game.setGameState(GameModel.GameState.INACTIVE);

            GameEndEvent event = new GameEndEvent(game);
            Bukkit.getPluginManager().callEvent(event);

            resetWorld(game.getWorld());
            game.resetGame();
            game.resetPlayerKillCounts();
            game.resetCages();
            queue.clearQueue();
        });

    }

    public void gameEndInstantly(GameModel model){
        for(QueueModel queue: queues){
            if(queue.getAssociatedGame().equals(model)){
                instantGameEnd(queue);
                return;
            }
        }
    }

    /**
     * Executes game end logic
     * This method is called when a game ends
     * This is used if we need to instantly stop a game.
     *
     * @param queue The queue associated with the game that should end
     * */
    private void instantGameEnd(QueueModel queue){
        //When a game ends, we want to reset the game state to inactive,
        //We want to clear the queue,
        //We want to teleport all the players back to the lobby
        //We want to reset the game timer
        GameModel game = queue.getAssociatedGame();
        resetWorld(game.getWorld());
        game.setGameState(GameModel.GameState.INACTIVE);
        //clear every player's inventory as well,
        //we need to reset the scoreboard for each player
        for(Player player: game.getWorld().getPlayers()){
            if(player == null)
                continue;
            player.setScoreboard(this.plugin.getServer().getScoreboardManager().getNewScoreboard());
        }
        for(Player player: game.getPlayers()){
            if(player==null)
                continue;
            player.getInventory().setArmorContents(null);
            player.getInventory().clear();
            player.setScoreboard(this.plugin.getServer().getScoreboardManager().getNewScoreboard());
            player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
            //check who won the game
            if(hasTheRedTeamWon(game)){
                player.sendMessage(ChatColor.RED + "Red team has won the game!");
                player.sendTitle(ChatColor.RED + "Red team won!", null);
            } else if(hasTheBlueTeamWon(game)){
                player.sendMessage(ChatColor.BLUE + "Blue team has won the game!");
                player.sendTitle(ChatColor.BLUE + "Blue team won!", null);
            } else {
                player.sendMessage(ChatColor.GRAY + "It's a tie!");
                player.sendTitle(ChatColor.GRAY + "Tied game!", null);
            }
            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
            player.teleport(lobbyLocation);
        }
        GameEndEvent event = new GameEndEvent(game);
        Bukkit.getPluginManager().callEvent(event);
        resetWorld(game.getWorld());
        game.resetGame();
        game.resetCages();
        game.resetPlayerKillCounts();
        queue.clearQueue();
    }

    public void endAllGamesInstantly(){
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            instantGameEnd(queue);
        }
    }

    private String getChatColorBasedOnTeam(Player p , GameModel g){
        if(g.checkIfPlayerIsInRedTeam(p)){
            return ChatColor.RED + "";
        } else {
            return ChatColor.BLUE + "";
        }
    }

    private void playSoundForAllPlayersInGame(GameModel game, Sound sound){
        for(Player player: game.getPlayers()){
            if(player == null)
                continue;
            player.playSound(player.getLocation(), sound, 1, 1);
        }
    }

    private void playSoundForAllPlayersOnRedTeam(GameModel game, Sound sound){
        for(Player player: game.getRedTeam()){
            if(player == null)
                continue;
            player.playSound(player.getLocation(), sound, 1, 1);
        }
    }

    private void playSoundForAllPlayersOnBlueTeam(GameModel game, Sound sound){
        for(Player player: game.getBlueTeam()){
            if(player == null)
                continue;
            player.playSound(player.getLocation(), sound, 1, 1);
        }
    }

    private void playSoundForSpecificPlayersTeam(GameModel game, Player player, Sound sound){
        if(game.checkIfPlayerIsInRedTeam(player)){
            playSoundForAllPlayersOnRedTeam(game, sound);
        } else {
            playSoundForAllPlayersOnBlueTeam(game, sound);
        }
    }

    private void playSoundForOppositeTeam(GameModel game, Player player, Sound sound){
        if(game.checkIfPlayerIsInRedTeam(player)){
            playSoundForAllPlayersOnBlueTeam(game, sound);
        } else {
            playSoundForAllPlayersOnRedTeam(game, sound);
        }
    }

    private void sendMessageToAllPlayersInGame(GameModel game, String message){
        for(Player player: game.getPlayers()){
            if(player == null)
                continue;
            player.sendMessage(message);
        }
    }

    /**
     * Event called upon player move event
     * This event is used to check if a player has scored a goal
     * If a player has scored a goal, we increment the score of the team that the player is on
     *
     * @param e The PlayerMoveEvent
     * */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        //get the player that moved
        Player player = e.getPlayer();
        Location playerLocation = player.getLocation();
        World world = playerLocation.getWorld();

        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();

            //if the player isn't in this game, we dont need to do anything
            if(!game.checkIfPlayerIsInGame(player))
                continue;

            if(game.getWorld() == world && game.getGameState() == GameModel.GameState.ACTIVE){
                // Calculate bounds for red goal
                int[][] redGoalBounds = game.getRedGoalBounds();
                int[] redMinBounds = getMinBounds(redGoalBounds);
                int[] redMaxBounds = getMaxBounds(redGoalBounds);

                // Calculate bounds for blue goal
                int[][] blueGoalBounds = game.getBlueGoalBounds();
                int[] blueMinBounds = getMinBounds(blueGoalBounds);
                int[] blueMaxBounds = getMaxBounds(blueGoalBounds);

                // Check if player is within red goal bounds
                if(isWithinBounds(playerLocation, redMinBounds, redMaxBounds)){
                    handleRedGoal(player, game);
                }

                // Check if player is within blue goal bounds
                if(isWithinBounds(playerLocation, blueMinBounds, blueMaxBounds)){
                    handleBlueGoal(player, game);
                }

                int killPlane = game.getKillPlane();
                if(playerLocation.getBlockY() <= killPlane){
                    Player damager = lastDamagerMap.get(player);
                    if(damager != null){
                        sendMessageToAllPlayersInGame(game, getChatColorBasedOnTeam(player, game) + player.getDisplayName() + ChatColor.GRAY + " was voided by " + getChatColorBasedOnTeam(damager, game) + damager.getDisplayName());
                        game.incrementPlayerKillCount(damager);
//                        damager.sendMessage("You killed " + player.getDisplayName());
                        damager.playSound(damager.getLocation(), Sound.ORB_PICKUP, 1, 1);
                        playSoundForSpecificPlayersTeam(game, damager, Sound.ORB_PICKUP);
//                        player.sendMessage("You were killed by " + damager.getDisplayName());
                        lastDamagerMap.remove(player);
                    }
                    resetPlayerInventory(player);
                    player.setHealth(20);
                    player.removePotionEffect(PotionEffectType.REGENERATION);
                    player.removePotionEffect(PotionEffectType.ABSORPTION);
//                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                    teleportPlayerBasedOnTeam(player, game);
                    player.playSound(player.getLocation(), Sound.CHICKEN_HURT, 1, 1);
                    playSoundForOppositeTeam(game, player, Sound.ORB_PICKUP);
//                    player.sendMessage(ChatColor.RED + "You fell into the void!");
                    sendMessageToAllPlayersInGame(game, getChatColorBasedOnTeam(player, game) + player.getDisplayName() + ChatColor.GRAY + " fell into the void!");
                    //call the on death event

                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeaveServer(PlayerQuitEvent e){
        Player player = e.getPlayer();
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.checkIfPlayerIsInGame(player) && (game.getGameState() == GameModel.GameState.ACTIVE || game.getGameState() == GameModel.GameState.STARTING)){
                game.removePlayer(player);
                if(game.getAmountOfPlayersOnRedTeam() == 0){
                    game.setBlueGoals(game.getGoalsToWin());
                    gameEnd(queue);
                }
                if(game.getAmountOfPlayersOnBlueTeam() == 0){
                    game.setRedGoals(game.getGoalsToWin());
                    gameEnd(queue);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRejoinServer(PlayerJoinEvent e){
        Player player = e.getPlayer();
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(queue.isPlayerInQueue(player) && (game.getGameState() == GameModel.GameState.ACTIVE || game.getGameState() == GameModel.GameState.STARTING)){
                game.addPlayer(player);
                teleportPlayerBasedOnTeam(player, game);
                giveKitToSinglePlayer(player);
            }
        }
    }

    /**
     * Checks if a player has dropped an item
     * If this is done in a game, we cancel the event
     *
     * @param e The PlayerDropItemEvent
     * */
    @EventHandler
    public void onPlayerDropEvent(org.bukkit.event.player.PlayerDropItemEvent e){
        Player player = e.getPlayer();
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.checkIfPlayerIsInGame(player)){
                e.setCancelled(true);
            }
        }
    }

    /**
     * Checks if the player has lost hunger
     * If the player has lost hunger, we cancel the event
     *
     * @param e The FoodLevelChangeEvent
     * */
    @EventHandler
    public void onHungerLossEvent(org.bukkit.event.entity.FoodLevelChangeEvent e){
        if(e.getEntity() instanceof Player){
            Player player = (Player) e.getEntity();
            for(QueueModel queue: queues){
                GameModel game = queue.getAssociatedGame();
                if(game.checkIfPlayerIsInGame(player)){
                    e.setCancelled(true);
                }
            }
        }
    }

    /**
     * Checks if a player has consumed a golden apple
     * If a player has consumed a golden apple,
     * we cancel the potion effects of the golden apple (leave absorption)
     * We also set the player's health to 20
     *
     * @param e The PlayerItemConsumeEvent
     * */
    @EventHandler
    public void onPlayerEatGoldenAppleEvent(PlayerItemConsumeEvent e){
        Player player = e.getPlayer();
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.checkIfPlayerIsInGame(player)){
                if(e.getItem().getType() == Material.GOLDEN_APPLE){
                    //cancel the potion effects of the golden apple
                    player.removePotionEffect(PotionEffectType.REGENERATION);
                    player.setHealth(20);
                }
            }
        }
    }

    /**
     * Checks if the player is taking fall damage
     * If the player is in a game, we cancel the event
     *
     * @param e The EntityDamageEvent
     * */
    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent e){
        if(e.getEntity() instanceof Player){
            Player player = (Player) e.getEntity();
            for(QueueModel queue: queues){
                GameModel game = queue.getAssociatedGame();
                if(game.checkIfPlayerIsInGame(player)){
                    if(e.getCause() == EntityDamageEvent.DamageCause.FALL){
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * Checks if a piece of armor has lost durability
     * If a piece of armor has lost durability, we cancel the event
     *
     * @param e The PlayerItemDamageEvent
     * */
    @EventHandler
    public void onArmorDurabilityLossEvent(org.bukkit.event.player.PlayerItemDamageEvent e){
        Player player = e.getPlayer();
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.checkIfPlayerIsInGame(player)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCropTrampleEvent(PlayerInteractEvent e){
        Player player = e.getPlayer();
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.checkIfPlayerIsInGame(player)){
                if(e.getAction() == Action.PHYSICAL){
                    e.setCancelled(true);
                }
            }
        }
    }

    /**
     * Checks if a player has damaged another player
     * If a player has damaged another player, we cancel the event if the players are on the same team
     *
     * @param e The EntityDamageByEntityEvent
     * */
    @EventHandler
    public void onPlayerDamageAnotherPlayer(EntityDamageByEntityEvent e){
        //if the damager is an arrow, and the damaged is a player
        if(e.getDamager() instanceof Arrow && e.getEntity() instanceof Player){
            //get the arrow
            Arrow arrow = (Arrow) e.getDamager();
            //get the player that was shot
            Player player = (Player) e.getEntity();
            //if the shooter is a player
            if(arrow.getShooter() instanceof Player){
                //get the player that shot the arrow
                Player damager = (Player) arrow.getShooter();
                //check if they are both in the same (valid) game
                for(QueueModel queue: queues){
                    GameModel game = queue.getAssociatedGame();
                    if(game.checkIfPlayerIsInGame(damager) && game.checkIfPlayerIsInGame(player)){
                        //if they are in the same game, check if they are on the same team
                        if(game.checkIfPlayerIsInRedTeam(damager) && game.checkIfPlayerIsInRedTeam(player)){
                            //if they are on the same team, cancel the event
                            e.setCancelled(true);
                            return;
                        } else if(game.checkIfPlayerIsInBlueTeam(damager) && game.checkIfPlayerIsInBlueTeam(player)) {
                            //if they are on the same team, cancel the event
                            e.setCancelled(true);
                            return;
                        }
                        else{
                            lastDamagerMap.put(player, damager);
                        }
                    }
                }
            }
        }

        //if the damager is a player, and the damaged is a player
        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player){
            Player damager = (Player) e.getDamager();
            Player player = (Player) e.getEntity();
            //check if they are both in the same (valid) game
            for(QueueModel queue: queues){
                GameModel game = queue.getAssociatedGame();
                if(game.checkIfPlayerIsInGame(damager) && game.checkIfPlayerIsInGame(player)){
                    //if they are in the same game, check if they are on the same team
                    if(game.checkIfPlayerIsInRedTeam(damager) && game.checkIfPlayerIsInRedTeam(player)){
                        e.setCancelled(true);
                    } else if(game.checkIfPlayerIsInBlueTeam(damager) && game.checkIfPlayerIsInBlueTeam(player)){
                        e.setCancelled(true);
                    }
                    else{
                        lastDamagerMap.put(player, damager);
                    }
                }
            }
        }
    }

    /**
     * Checks for a player damage event
     * Prevents a player from taking damage if the game's state is anything but active
     *
     * @param e The EntityDamageEvent
     * */
    @EventHandler
    public void onPlayerDamageInNonActiveGame(EntityDamageEvent e){
        if(e.getEntity() instanceof Player){
            Player player = (Player) e.getEntity();
            for(QueueModel queue: queues){
                GameModel game = queue.getAssociatedGame();
                if(game.checkIfPlayerIsInGame(player)){
                    if(game.getGameState() != GameModel.GameState.ACTIVE){
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * Checks if the weather has changed
     * Ony cancels the event if the world is a game world
     * If the weather has changed, we cancel the event
     *
     * @param e The WeatherChangeEvent
     * */
    @EventHandler
    public void onWeatherChange(org.bukkit.event.weather.WeatherChangeEvent e){
        //I only want to cancel the weather change event if the world is a game world
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.getWorld() == e.getWorld()){
                if(e.toWeatherState()){
                    e.setCancelled(true);
                }
            }
        }
    }

    /**
     * Checks if a player has shot a bow.
     * Gives the player an arrow if they have shot a bow
     *
     * @param e The EntityShootBowEvent
     * */
    @EventHandler
    public void onPlayerShootBow(org.bukkit.event.entity.EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            for (QueueModel queue : queues) {
                GameModel game = queue.getAssociatedGame();
                if (game.checkIfPlayerIsInGame(player)) {
                    arrowCountdowns.put(player, 3);
                    startArrowTimer(() -> {
                        ItemStack arrow = new ItemStack(Material.ARROW, 1);
                        player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1);
                        player.getInventory().addItem(arrow);
                        player.setLevel(arrowCountdowns.get(player));
                    }, ()->{
                        player.setLevel(arrowCountdowns.get(player));
                        if(arrowCountdowns.get(player) == 0)
                            player.setLevel(0);
                        }, player);
//                    game.startStallingTimer(() -> {
//                        //Give the player an arrow
//                        ItemStack arrow = new ItemStack(Material.ARROW, 1);
//                        player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1);
//                        player.getInventory().addItem(arrow);
//                    });
                }
            }
        }
    }

    /**
     * Checks if a projectile has hit something, if it did, we remove the arrow
     * This is to prevent arrow duping
     *
     * @param e The ProjectileHitEvent
     * */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e){
        if(e.getEntity() instanceof Arrow){
            Arrow arrow = (Arrow) e.getEntity();
            for(QueueModel queue: queues){
                GameModel game = queue.getAssociatedGame();
                if(game.getWorld() == arrow.getWorld()){
                    arrow.remove();
                }
            }
        }
    }




    /**
     * Checks if the player is going to die from their next hit / damage value,
     * If the player is going to die, we cancel the event and reset their inventory
     * We also teleport the player back to their spawn point
     *
     * @param e The EntityDamageEvent
     * */
    @EventHandler
    public void onNearDeathEvent(EntityDamageEvent e) //Listens to EntityDamageEvent
    {
        if(e.getEntity() instanceof  Player) {
            Player player = (Player) e.getEntity();
            Player damager = null;
            //if the damager is fall damage we don't want to run this code
            if(e.getCause() == EntityDamageEvent.DamageCause.FALL)
                return;
            for(QueueModel queue: queues){
                GameModel game = queue.getAssociatedGame();
                if(game.checkIfPlayerIsInGame(player)){
                    double finalDamage = e.getFinalDamage();
                    if(e instanceof EntityDamageByEntityEvent){
                        EntityDamageByEntityEvent entityDamage = (EntityDamageByEntityEvent) e;
                        if(entityDamage.getDamager() instanceof Arrow){
                            Arrow arrow = (Arrow) entityDamage.getDamager();
                            if(arrow.getShooter() instanceof Player){
                                damager = (Player) arrow.getShooter();
                            }
                            //if the arrow would have killed the player, cancel the event
                            if(player.getHealth() - finalDamage <= 0) {

                                //perhaps fixed?
                                if(game.checkIfPlayerIsInRedTeam(damager) && game.checkIfPlayerIsInRedTeam(player)){
                                    e.setCancelled(true);
                                    return;
                                } else if(game.checkIfPlayerIsInBlueTeam(damager) && game.checkIfPlayerIsInBlueTeam(player)){
                                    e.setCancelled(true);
                                    return;
                                }

                                e.setCancelled(true);
                                player.setHealth(20);
                                resetPlayerInventory(player);
                                teleportPlayerBasedOnTeam(player, game);
//                                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                                if(damager != null){
                                    //for everyone on the damager's team, play a sound and send them a message
                                    sendMessageToAllPlayersInGame(game, getChatColorBasedOnTeam(player, game) + player.getName() + ChatColor.GRAY + " was shot by " + getChatColorBasedOnTeam(damager, game) + damager.getName());
                                    playSoundForSpecificPlayersTeam(game, damager, Sound.ORB_PICKUP);
                                    game.incrementPlayerKillCount(damager);
                                }
                                player.playSound(player.getLocation(), Sound.CHICKEN_HURT, 1, 1);
                                return;
                            }
                        }
                    }
                    if(e instanceof EntityDamageByEntityEvent){
                        EntityDamageByEntityEvent entityDamage = (EntityDamageByEntityEvent) e;
                        if(entityDamage.getDamager() instanceof Player){
                            damager = (Player) entityDamage.getDamager();
                        }
                    }
                    if(player.getHealth() - finalDamage <= 0){

                        //perhaps fixed?
                        if(game.checkIfPlayerIsInRedTeam(damager) && game.checkIfPlayerIsInRedTeam(player)){
                            e.setCancelled(true);
                            return;
                        } else if(game.checkIfPlayerIsInBlueTeam(damager) && game.checkIfPlayerIsInBlueTeam(player)){
                            e.setCancelled(true);
                            return;
                        }

                        e.setCancelled(true);
                        player.setHealth(20);
                        resetPlayerInventory(player);
                        teleportPlayerBasedOnTeam(player, game);
                        player.playSound(player.getLocation(), Sound.CHICKEN_HURT, 1, 1);

                        if(damager != null){
                            sendMessageToAllPlayersInGame(game, getChatColorBasedOnTeam(player, game) + player.getName() + ChatColor.GRAY + " was killed by " + getChatColorBasedOnTeam(damager, game) + damager.getName());
                            playSoundForSpecificPlayersTeam(game, damager, Sound.ORB_PICKUP);
                            game.incrementPlayerKillCount(damager);
                        }
//                        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                    }
                }
            }
        }
    }

    /**
     * Checks if a player has regained health naturally
     * Cancels the event if the player is in a game
     *
     * @param e The PlayerDeathEvent
     * */
    @EventHandler
    private void onNaturalRegenEvent(EntityRegainHealthEvent e){
        if(e.getEntity() instanceof Player){
            Player player = (Player) e.getEntity();
            for(QueueModel queue: queues){
                GameModel game = queue.getAssociatedGame();
                if(game.checkIfPlayerIsInGame(player)){
                    if(e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED || e.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN){
                        e.setCancelled(true);
                    }
                }
            }
        }
    }



    /**
     * Teleports a player to their respective spawn point based on their team
     *
     * @param p The player to teleport
     * @param game The game to teleport the player in
     * */
    public void teleportPlayerBasedOnTeam(Player p, GameModel game){
        if(game.checkIfPlayerIsInRedTeam(p)){
            Location redSpawn = new Location(game.getWorld(), game.getRedSpawnPoint()[0], game.getRedSpawnPoint()[1], game.getRedSpawnPoint()[2]);
            redSpawn.setYaw(game.getRedSpawnYaw());
            p.teleport(redSpawn);
        } else if(game.checkIfPlayerIsInBlueTeam(p)){
            Location blueSpawn = new Location(game.getWorld(), game.getBlueSpawnPoint()[0], game.getBlueSpawnPoint()[1], game.getBlueSpawnPoint()[2]);
            blueSpawn.setYaw(game.getBlueSpawnYaw());
            p.teleport(blueSpawn);
        }
    }

    /**
     * Teleports all players in a game to their respective spawn points
     * This method is called when a game starts
     *
     * @param game The game to teleport the players in
     * */
    private void teleportPlayersToSpawn(GameModel game) {
        Location redSpawn = new Location(game.getWorld(), game.getRedSpawnPoint()[0], game.getRedSpawnPoint()[1], game.getRedSpawnPoint()[2]);
        redSpawn.setYaw(game.getRedSpawnYaw());
        Location blueSpawn = new Location(game.getWorld(), game.getBlueSpawnPoint()[0], game.getBlueSpawnPoint()[1], game.getBlueSpawnPoint()[2]);
        blueSpawn.setYaw(game.getBlueSpawnYaw());

        for (Player player : game.getRedTeam()) {
            if (player != null) {
                player.teleport(redSpawn);
//                System.out.println("Teleporting player " + player.getName() + " to red spawn at " + redSpawn.toString());
                player.sendMessage(ChatColor.GRAY + "You are on the" + ChatColor.RED + " red " + ChatColor.GRAY + "team!");
//                player.sendMessage("Teleporting you to " + redSpawn.toString() + "at " + game.getWorld().getName());
            }
        }
        for (Player player : game.getBlueTeam()) {
            if (player != null) {
                player.teleport(blueSpawn);
//                System.out.println("Teleporting player " + player.getName() + " to blue spawn at " + blueSpawn.toString());
                player.sendMessage(ChatColor.GRAY + "You are on the" + ChatColor.BLUE + " blue " + ChatColor.GRAY + "team!");
//                player.sendMessage("Teleporting you to " + blueSpawn.toString() + "at " + game.getWorld().getName());
            }
        }
    }

    /**
     * Adds placed blocks to the list of world changes only if a game is active
     * Also prevents blocks being placed in certain game states
     *
     * @param e The BlockPlaceEvent
     * */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        Block block = e.getBlock();
        for(QueueModel queue: queues){
            //if the player is in a starting, queueing, or inactive game, cancel the block change
            if(queue.getAssociatedGame().checkIfPlayerIsInGame(p)){
                GameModel game = queue.getAssociatedGame();

                if(game.getGameState() == GameModel.GameState.BUILDING) {
                    return;
                }

                if(game.getGameState() != GameModel.GameState.ACTIVE){
                    e.setCancelled(true);
                    return;
                }

                //if the game is active, only allow players to place blocks within the world boundaries
                if(game.getGameState() == GameModel.GameState.ACTIVE){
                    int[][] worldBounds = game.getWorldBounds();
                    int[] worldMinBounds = getMinBounds(worldBounds);
                    int[] worldMaxBounds = getMaxBounds(worldBounds);
                    if(!isWithinBounds(block.getLocation(), worldMinBounds, worldMaxBounds)){
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            //if the block is in the world of an active game, add the block change to the changes map
            if(queue.getAssociatedGame().getWorld() == block.getWorld() && queue.getAssociatedGame().getGameState() == GameModel.GameState.ACTIVE){
                //Add the block change to the changes map
                if(changes.containsKey(block.getWorld())){
                    changes.get(block.getWorld()).add(new BlockChangeModel(block.getLocation(), XMaterial.AIR, XMaterial.matchXMaterial(block.getType()), block.getData()));
                }
                //If the world doesn't have any block changes, add a new list of block changes
                else{
                    changes.put(block.getWorld(), new DoublyLinkedList());
                    changes.get(block.getWorld()).add(new BlockChangeModel(block.getLocation(), XMaterial.AIR, XMaterial.matchXMaterial(block.getType()),block.getData()));
                }
                return;
            }
        }
    }

    /**
     * Adds broken blocks to the list of world changes only if a game is active
     * Also prevents blocks being broken in certain game states
     *
     * @param e The BlockBreakEvent
     * */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Block block = e.getBlock();
        Player p = e.getPlayer();
        for(QueueModel queue: queues){
            //if the player is in a starting, queueing, or inactive game, cancel the block change
            if(queue.getAssociatedGame().checkIfPlayerIsInGame(p)){
                GameModel game = queue.getAssociatedGame();

                if(game.getGameState() == GameModel.GameState.BUILDING) {
                    return;
                }

                if(game.getGameState() != GameModel.GameState.ACTIVE){
                    e.setCancelled(true);
                    return;
                }

                if(game.getGameState() == GameModel.GameState.ACTIVE) {

                    //this should prevent players from breaking blocks outside of the world boundaries
                    int[][] worldBounds = game.getWorldBounds();
                    int[] worldMinBounds = getMinBounds(worldBounds);
                    int[] worldMaxBounds = getMaxBounds(worldBounds);
                    if(!isWithinBounds(block.getLocation(), worldMinBounds, worldMaxBounds)){
                        e.setCancelled(true);
                        return;
                    }
                    //We can't let the player break any blocks that AREN'T stained clay
                    if (block.getType() != Material.STAINED_CLAY && block.getType() != XMaterial.RED_TERRACOTTA.parseMaterial() && block.getType() != XMaterial.BLUE_TERRACOTTA.parseMaterial()) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            //if the block is in the world of an active game, add the block change to the changes map
            if(queue.getAssociatedGame().getWorld() == block.getWorld() && queue.getAssociatedGame().getGameState() == GameModel.GameState.ACTIVE){
                //Add the block change to the changes map
                if(changes.containsKey(block.getWorld())){
                    changes.get(block.getWorld()).add(new BlockChangeModel(block.getLocation(), XMaterial.matchXMaterial(block.getType()), XMaterial.AIR, block.getData()));
                }
                //If the world doesn't have any block changes, add a new list of block changes
                else{
                    changes.put(block.getWorld(), new DoublyLinkedList());
                    changes.get(block.getWorld()).add(new BlockChangeModel(block.getLocation(), XMaterial.matchXMaterial(block.getType()), XMaterial.AIR,block.getData()));
                }
                return;
            }
        }
    }

    /**
     * Resets the world to its original state
     * This method is called when a game ends
     *
     * @param world The world to reset
     * */
    public void resetWorld(World world) {
        DoublyLinkedList changesInChronologicalOrder = changes.get(world);
        if(changesInChronologicalOrder != null) {
//            System.out.println(changesInChronologicalOrder.toString());
            //So this variable "changesInChronologicalOrder" is a doubly linked list of block changes
            //The changes at the end are the most recent changes
            //We want to iterate through the list in reverse order, so we can undo the changes in the order they were made
            for (int i = changesInChronologicalOrder.size() - 1; i >= 0; i--) {
//                System.out.println("Resetting block " + i + " in world " + world.getName());
                BlockChangeModel change = (BlockChangeModel) changesInChronologicalOrder.get(i);
                change.getLocation().getBlock().setType(change.getBefore().parseMaterial());
                change.getLocation().getBlock().setData(change.getData());
            }
        }
        changes.remove(world);
    }

    /**
     * Gets the minimum bounds of a 3D space
     *
     * @param bounds The bounds to get the minimum of (2D array, 2x3, two (X, Y, Z) sets of three coordinates)
     * @return The minimum bounds of the 3D space
     * */
    private int[] getMinBounds(int[][] bounds) {
        int minX = Math.min(bounds[0][0], bounds[1][0]);
        int minY = Math.min(bounds[0][1], bounds[1][1]);
        int minZ = Math.min(bounds[0][2], bounds[1][2]);
        return new int[]{minX, minY, minZ};
    }

    /**
     * Gets the maximum bounds of a 3D space
     *
     * @param bounds The bounds to get the maximum of (2D array, 2x3, two (X, Y, Z) sets of three coordinates)
     *
     * @return The maximum bounds of the 3D space
     * */
    private int[] getMaxBounds(int[][] bounds) {
        int maxX = Math.max(bounds[0][0], bounds[1][0]);
        int maxY = Math.max(bounds[0][1], bounds[1][1]);
        int maxZ = Math.max(bounds[0][2], bounds[1][2]);
        return new int[]{maxX, maxY, maxZ};
    }

    /**
     * Checks if a location is within the bounds of a 3D space
     *
     * @param location The location to check
     * @param minBounds The minimum bounds of the 3D space
     * @param maxBounds The maximum bounds of the 3D space
     *
     * @return Whether the location is within the bounds of the 3D space
     * */
    private boolean isWithinBounds(Location location, int[] minBounds, int[] maxBounds) {
        return location.getBlockX() >= minBounds[0] && location.getBlockX() <= maxBounds[0] &&
                location.getBlockY() >= minBounds[1] && location.getBlockY() <= maxBounds[1] &&
                location.getBlockZ() >= minBounds[2] && location.getBlockZ() <= maxBounds[2];
    }

    /**
     * Checks, and handles a player scoring a goal in the red goal
     *
     * @param player The player to check / handle the goal for
     * @param game The game to check / handle the goal for
     * */
    private void handleRedGoal(Player player, GameModel game) {
        System.out.println("Player " + player.getName() + " is within the bounds of the red goal!");

        if(game.checkIfPlayerIsInRedTeam(player)){
            player.sendMessage(ChatColor.GRAY + "Wrong goal! Teleporting you back to your spawn point.");
            sendMessageToAllPlayersInGame(game, getChatColorBasedOnTeam(player, game) + player.getName() + ChatColor.GRAY + " fell into their own goal");
            player.setHealth(20);
            resetPlayerInventory(player);
            player.removePotionEffect(PotionEffectType.REGENERATION);
            player.removePotionEffect(PotionEffectType.ABSORPTION);
            teleportPlayerBasedOnTeam(player, game);
        } else if(game.checkIfPlayerIsInBlueTeam(player)) {
            game.setBlueGoals(game.getBlueGoals() + 1);

            String localPlayerScoredTitle = ChatColor.RED + player.getName() + playerScoredTitle;

            //for all players in the game, teleport them back to their spawn points
            for (Player p : game.getPlayers()) {
                if(p == null)
                    continue;
                teleportPlayerBasedOnTeam(p, game);
                p.setHealth(20);
                resetPlayerInventory(p);

                p.sendMessage(getChatColorBasedOnTeam(player, game) + player.getName() + ChatColor.GRAY + " scored a goal for the blue team!");
                p.playSound(p.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);

                //we also need to reset all potion effects
                p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
            }
//            player.teleport(new Location(game.getWorld(), game.getBlueSpawnPoint()[0], game.getBlueSpawnPoint()[1], game.getBlueSpawnPoint()[2]));

            game.resetCages();
//            if (!gameIsWon(game)) {
//                game.startStallingTimer(() -> {
//                    game.clearCages();
//                });
//            }
            if(!gameIsWon(game)){
                game.startStallingTimer(()->{
                    game.clearCages();
                    resetPlayerTitles(game);
                },()->{
                    sendEachPlayerTitleOnGoal(game, game.getStallingTimerCountdown(), player, false);
                });
            }


//            updateScoreboard(game);
            game.updateScoreboard(game.getScoreboard().getObjective("bridge"), game.getGameTimer().getCountdown());
        }
    }

    /**
     * On a player's death, they will be teleported back to their spawn point if they are in a game
     * Player's should never die in a game, but should it happen they will be teleported back to their spawn point
     *
     * @param e The PlayerDeathEvent
     * */
    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e){
        Player player = e.getEntity();
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.checkIfPlayerIsInGame(player)){
                player.setHealth(20);
                resetPlayerInventory(player);
                teleportPlayerBasedOnTeam(player, game);
            }
        }
    }

    /**
     * Checks, and handles a player scoring a goal in the blue goal
     *
     * @param player The player to check / handle the goal for
     * @param game The game to check / handle the goal for
     * */
    private void handleBlueGoal(Player player, GameModel game) {
        System.out.println("Player " + player.getName() + " is within the bounds of the blue goal!");

        if(game.checkIfPlayerIsInBlueTeam(player)){
//            player.sendMessage(ChatColor.GRAY + "Wrong goal! Teleporting you back to your spawn point.");

            sendMessageToAllPlayersInGame(game, getChatColorBasedOnTeam(player, game) + player.getName() + ChatColor.GRAY + " fell into their own goal");
            teleportPlayerBasedOnTeam(player, game);

            player.setHealth(20);
            resetPlayerInventory(player);
            player.removePotionEffect(PotionEffectType.REGENERATION);
            player.removePotionEffect(PotionEffectType.ABSORPTION);
        } else if(game.checkIfPlayerIsInRedTeam(player)){
            game.setRedGoals(game.getRedGoals() + 1);

            String localPlayerScoredTitle = ChatColor.BLUE + player.getName() + playerScoredTitle;

            //for all players in the game, teleport them back to their spawn points
            for(Player p: game.getPlayers()){
                if(p == null)
                    continue;
                resetPlayerInventory(p);
                p.setHealth(20);
                teleportPlayerBasedOnTeam(p, game);

                p.sendMessage(getChatColorBasedOnTeam(player, game) + player.getName() + ChatColor.GRAY + " scored a goal for the red team!");
                p.playSound(p.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);

                p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
            }
//            player.teleport(new Location(game.getWorld(), game.getRedSpawnPoint()[0], game.getRedSpawnPoint()[1], game.getRedSpawnPoint()[2]));

            game.resetCages();
            //if the game is not won, start the stalling timer
            //we don't want to clear the cages if the game is won
            if(!gameIsWon(game)) {
                game.startStallingTimer(() -> {//seconds left in this timer;
                    game.clearCages();
                    resetPlayerTitles(game);
                }, ()->{sendEachPlayerTitleOnGoal(game, game.getStallingTimerCountdown(), player, true);});
            }

            game.updateScoreboard(game.getScoreboard().getObjective("bridge"), game.getGameTimer().getCountdown());
        }
    }

    /**
     * Updates the scoreboard for a game
     * Specifically updates the red goals and blue goals for a game
     * Does not update the timer or other information
     *
     * @param game The game to update the scoreboard for
     * */
    private void updateScoreboard(GameModel game) {
        Objective objective = game.getScoreboard().getObjective("bridge");

        //Reset the old scores
        game.getScoreboard().resetScores(ChatColor.RED + "Red Goals: " + (game.getRedGoals() - 1));
        game.getScoreboard().resetScores(ChatColor.BLUE + "Blue Goals: " + (game.getBlueGoals() - 1));

        //Set the new scores
        objective.getScore(ChatColor.RED + "Red Goals: " + game.getRedGoals()).setScore(11);
        objective.getScore(ChatColor.BLUE + "Blue Goals: " + game.getBlueGoals()).setScore(10);

        //Update the scoreboard for each player
        //actually I want to update the scoreboard for every player in that world

        for (Player player : game.getWorld().getPlayers()) {
            if(player == null)
                continue;
            player.setScoreboard(game.getScoreboard());
        }
    }

    /**
     * Checks if the red team has won the game
     *
     * @param game The game to check if the red team has won
     * */
    private boolean hasTheRedTeamWon(GameModel game){
        return game.getRedGoals() >= game.getGoalsToWin();
    }

    /**
     * Checks if the blue team has won the game
     *
     * @param game The game to check if the blue team has won
     * */
    private boolean hasTheBlueTeamWon(GameModel game){
        return game.getBlueGoals() >= game.getGoalsToWin();
    }

    /**
     * Resets the inventory of a player
     *
     * @param player The player to reset the inventory for
     * */
    private void resetPlayerInventory(Player player){
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        giveKitToSinglePlayer(player);
    }

    /**
     * Resets the inventory of a player without giving them an arrow
     *
     * @param player The player to reset the inventory for
     * */
    private void resetPlayerInventoryNoArrow(Player player){
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        giveKitToSinglePlayerNoArrow(player);
    }


    /**
     * Checks if a game is won
     *
     * @param game The game to check if it is won
     * @return Whether the game is won
     * */
    private boolean gameIsWon(GameModel game){
        return hasTheRedTeamWon(game) || hasTheBlueTeamWon(game);
    }

    /**
     * Converts this manager into a readable string,
     * This string contains all the games, the world that game is taking place in, and the state of that game
     *
     * @return A string containing all the games, the world that game is taking place in, and the state of that game
     * */
    @Override
    public String toString(){
        //return a string containing all of the games, the world that game is taking place in, and the state of that game
        StringBuilder sb = new StringBuilder();
        for(QueueModel queue: queues){
            sb.append("Game in world: ").append(queue.getAssociatedGame().getWorld().getName()).append(" is in state: ").append(queue.getAssociatedGame().getGameState()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Sends all players in a game a title containing the following information:
     * - The player that scored
     * - The time left until the cages open
     * - The team that the player was on
     *
     * @param game The game to send the title for
     * @param countdown The countdown until the cages open
     * @param player The player that scored
     * @param isOnRedTeam Whether the player that scored was on the red team
     * */
    private void sendEachPlayerTitleOnGoal(GameModel game, int countdown, Player player, boolean isOnRedTeam){
        for(Player p: game.getPlayers()){
            if(p == null)
                continue;
            if(countdown > 0)
                p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1, 1);
            if(countdown == 0)
                p.playSound(p.getLocation(), Sound.EXPLODE, 1, 1);
            if(isOnRedTeam){
                p.sendTitle(ChatColor.RED + player.getName() + " scored!", ChatColor.GOLD + "Cages open in: " + ChatColor.GRAY + countdown + " seconds");
            } else {
                p.sendTitle(ChatColor.BLUE + player.getName() + " scored!", ChatColor.GOLD + "Cages open in: " + ChatColor.GRAY + countdown + " seconds");
            }
        }
    }

    private void sendEachPlayerStartingTitle(GameModel game, int countdown){
        for(Player p: game.getPlayers()){
            if(p == null)
                continue;
            if(countdown > 0)
                p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1, 1);
            if(countdown == 0)
                p.playSound(p.getLocation(), Sound.EXPLODE, 1, 1);
            if(game.checkIfPlayerIsInRedTeam(p)){
                p.sendTitle(ChatColor.RED + "Game starting!", ChatColor.GOLD + "Game starts in: " + ChatColor.GRAY + countdown + " seconds");
            } else {
                p.sendTitle(ChatColor.BLUE + "Game starting!", ChatColor.GOLD + "Game starts in: " + ChatColor.GRAY + countdown + " seconds");
            }
        }

    }

    /**
     * Resets all the titles for each player in the game
     * This uses deprecated methods, and should be replaced with a more modern method
     *
     * @param gameModel The game model to reset the titles for
     * */
    private void resetPlayerTitles(GameModel gameModel){
        for(Player p: gameModel.getPlayers()){
            if(p == null)
                continue;
            p.resetTitle();
            p.sendTitle("","");
        }
    }


    /**
     * Gets all the games being managed by this manager
     *
     * @return The games being managed by this manager
     * */
    public ArrayList<GameModel> getGames(){
        ArrayList<GameModel> games = new ArrayList<>();
        for(QueueModel queue: queues){
            games.add(queue.getAssociatedGame());
        }
        return games;
    }

    /**
     * Gets all the queues being managed by this manager
     *
     * @return The queues being managed by this manager
     * */
    public ArrayList<QueueModel> getQueues() {
        return queues;
    }

    public void teleportPlayerToLobby(Player player){
        player.getInventory().clear();
        player.teleport(lobbyLocation);
    }

    /**
     * this is a pretty hacky fix to players not getting teleported back after a game has ended
     * */
    @EventHandler
    public void onPlayerMoveInInactiveGame(PlayerMoveEvent e){
        //if the player is within a world that is associated with a game
        Player player = e.getPlayer();
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.getWorld() == player.getWorld()){
                if(game.getGameState() == GameModel.GameState.INACTIVE){
                    player.teleport(lobbyLocation);
                    player.getInventory().setArmorContents(null);
                }
            }
        }
    }

    @EventHandler
    //if the player tries to move items around in their inventory while in a game, cancel the event
    public void onInventoryClickEvent(InventoryClickEvent e){
        Player player = (Player) e.getWhoClicked();
        for(QueueModel queue: queues){
            GameModel game = queue.getAssociatedGame();
            if(game.checkIfPlayerIsInGame(player)){
                e.setCancelled(true);
            }
        }
    }
}
