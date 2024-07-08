package me.liamgiraldo.litebridge.models;

import com.cryptomorin.xseries.XMaterial;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.files.HotbarConfig;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class GUIModel {
    Litebridge plugin;
    SGMenu bridgemainmenu;
    SGMenu bridgeselectmode;
    SGMenu hotbareditor;
    SGMenu customizemenu;
    SGMenu mapmenu;

    public GUIModel(Litebridge plugin){
        this.plugin = plugin;
        this.bridgemainmenu = plugin.getSpiGUI().create("Bridge Main Menu", 3);
        this.bridgeselectmode = plugin.getSpiGUI().create("Bridge Mode Selection", 3);
        this.hotbareditor = plugin.getSpiGUI().create("Hotbar Editor", 3);
        this.customizemenu = plugin.getSpiGUI().create("Customize", 3);
        this.mapmenu = plugin.getSpiGUI().create("Map Menu", 6);

        ItemBuilder closeitem = new ItemBuilder(Material.BARRIER);
        closeitem.lore("Click here to close the menu");
        closeitem.amount(1);
        closeitem.name("Close");
        SGButton closebutton = new SGButton(
                closeitem.build()
        ).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().closeInventory();
        });

        ItemBuilder mainmenuitem = new ItemBuilder(XMaterial.FEATHER.parseMaterial());
        mainmenuitem.lore("Click here to return to the main menu");
        mainmenuitem.amount(1);
        mainmenuitem.name("Main Menu");
        SGButton mainmenubutton = new SGButton(
                mainmenuitem.build()
        ).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().openInventory(bridgemainmenu.getInventory());
        });

        ItemBuilder hotbareditoritem = new ItemBuilder(XMaterial.DIAMOND_PICKAXE.parseMaterial());
        hotbareditoritem.lore("Click here to edit your hotbar");
        hotbareditoritem.amount(1);
        hotbareditoritem.name("Hotbar Editor");
        SGButton hotbareditorbutton = new SGButton(
                hotbareditoritem.build()
        ).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().openInventory(hotbareditor.getInventory());
        });

        ItemBuilder bridgemodeitem = new ItemBuilder(XMaterial.RED_TERRACOTTA.parseMaterial());
        bridgemodeitem.lore("Click here to play bridge");
        bridgemodeitem.amount(1);
        bridgemodeitem.name("Bridge");
        SGButton modeselectorbutton = new SGButton(bridgemodeitem.build()
        ).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().openInventory(bridgeselectmode.getInventory());
        });

        ItemBuilder customizeitem = new ItemBuilder(XMaterial.DIAMOND.parseMaterial());
        customizeitem.lore("Click here to customize your game");
        customizeitem.amount(1);
        customizeitem.name("Customize");
        SGButton customizebutton = new SGButton(customizeitem.build()).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().openInventory(customizemenu.getInventory());
        });

        bridgemainmenu.setButton(0, 22, closebutton);
        bridgemainmenu.setButton(0, 15, customizebutton);
        bridgemainmenu.setButton(0, 13, hotbareditorbutton);
        bridgemainmenu.setButton(0, 11, modeselectorbutton);

        ItemBuilder solositem = new ItemBuilder(XMaterial.WHITE_TULIP.parseMaterial());
        solositem.lore("Click to play solos");
        solositem.amount(1);
        solositem.name("Solos");
        SGButton solos = new SGButton(solositem.build()).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().openInventory(mapmenu.getInventory());
        });

        ItemBuilder duositem = new ItemBuilder(XMaterial.PINK_TULIP.parseMaterial());
        duositem.lore("Click to play duos");
        duositem.amount(2);
        duositem.name("Duos");
        SGButton duos = new SGButton(duositem.build()).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().openInventory(mapmenu.getInventory());
        });

        ItemBuilder triositem = new ItemBuilder(XMaterial.ORANGE_TULIP.parseMaterial());
        triositem.lore("Click to play trios");
        triositem.amount(3);
        triositem.name("Trios");
        SGButton trios = new SGButton(triositem.build()).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().openInventory(mapmenu.getInventory());
        });

        ItemBuilder squadsitem = new ItemBuilder(XMaterial.RED_TULIP.parseMaterial());
        squadsitem.lore("Click to play squads");
        squadsitem.amount(4);
        squadsitem.name("Squads");
        SGButton squads = new SGButton(squadsitem.build()).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().openInventory(mapmenu.getInventory());
        });

        bridgeselectmode.setButton(0, 10, solos);
        bridgeselectmode.setButton(0, 12, duos);
        bridgeselectmode.setButton(0, 14, trios);
        bridgeselectmode.setButton(0, 16, squads);
        bridgeselectmode.setButton(0, 22, closebutton);
        bridgeselectmode.setButton(0, 18, mainmenubutton);

        ItemBuilder saveitem = new ItemBuilder(Material.SLIME_BALL);
        saveitem.lore("Click to save your hotbar");
        saveitem.amount(1);
        saveitem.name("Save");
        SGButton savebutton = new SGButton(saveitem.build()).withListener((InventoryClickEvent event) -> {
            onSaveHotbar(event);
            event.getWhoClicked().sendMessage("Hotbar saved");
            event.getWhoClicked().closeInventory();

        });

        ItemBuilder infoitem = new ItemBuilder(XMaterial.PAPER.parseMaterial());
        infoitem.lore("Shift items up a row to move them");
        infoitem.amount(1);
        infoitem.name("Hotbar help");
        SGButton infobutton = new SGButton(infoitem.build()).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().sendMessage("Shift the items up a row to move them");
        });

        ItemBuilder pickaxeitem = new ItemBuilder(XMaterial.DIAMOND_PICKAXE.parseMaterial());
        pickaxeitem.lore("Trusty pickaxe");
        pickaxeitem.amount(1);
        pickaxeitem.name("Pickaxe");
        SGButton pickaxebutton = new SGButton(pickaxeitem.build());

        ItemBuilder sworditem = new ItemBuilder(XMaterial.IRON_SWORD.parseMaterial());
        sworditem.lore("Sharp sword");
        sworditem.amount(1);
        sworditem.name("Sword");
        SGButton swordbutton = new SGButton(sworditem.build());

        ItemBuilder bowitem = new ItemBuilder(XMaterial.BOW.parseMaterial());
        bowitem.lore("Magic bow");
        bowitem.amount(1);
        bowitem.name("Bow");
        SGButton bowbutton = new SGButton(bowitem.build());

        ItemBuilder arrowitem = new ItemBuilder(XMaterial.ARROW.parseMaterial());
        arrowitem.lore("Pointy arrow");
        arrowitem.amount(1);
        arrowitem.name("Arrow");
        SGButton arrowbutton = new SGButton(arrowitem.build());

        ItemBuilder goldenappleitem = new ItemBuilder(XMaterial.GOLDEN_APPLE.parseMaterial());
        goldenappleitem.lore("Shiny apple");
        goldenappleitem.amount(1);
        goldenappleitem.name("Golden Apple");
        SGButton goldenapplebutton = new SGButton(goldenappleitem.build());

        ItemBuilder redclayitem = new ItemBuilder(XMaterial.RED_TERRACOTTA.parseMaterial());
        redclayitem.lore("Red clay");
        redclayitem.amount(1);
        redclayitem.name("Red Clay");
        SGButton redclaybutton = new SGButton(redclayitem.build());

        ItemBuilder blueclayitem = new ItemBuilder(Material.WOOL);
        blueclayitem.lore("Blue clay");
        blueclayitem.amount(1);
        blueclayitem.name("Blue Clay");
        SGButton blueclaybutton = new SGButton(blueclayitem.build());

        ItemBuilder diamonditem = new ItemBuilder(XMaterial.DIAMOND.parseMaterial());
        diamonditem.lore("Taunting diamond");
        diamonditem.amount(1);
        diamonditem.name("Taunt");
        SGButton diamondbutton = new SGButton(diamonditem.build());

        int[] paneslots = {19, 20, 21, 23, 24, 25};
        for (int i = 0; i < paneslots.length; i++) {
            hotbareditor.setButton(0, paneslots[i], new SGButton(new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()).name(" ").build()));
        }

        hotbareditor.setButton(0, 9, pickaxebutton);
        hotbareditor.setButton(0, 10, swordbutton);
        hotbareditor.setButton(0, 11, bowbutton);
        hotbareditor.setButton(0, 12, arrowbutton);
        hotbareditor.setButton(0, 13, goldenapplebutton);
        hotbareditor.setButton(0, 14, redclaybutton);
        hotbareditor.setButton(0, 15, blueclaybutton);
        hotbareditor.setButton(0, 16, diamondbutton);

        hotbareditor.setButton(0, 22, closebutton);
        hotbareditor.setButton(0, 18, mainmenubutton);
        hotbareditor.setButton(0, 26, savebutton);
        hotbareditor.setButton(0, 17, infobutton);

        //TODO: There has to be a better way to do this
        plugin.getSpiGUI().setBlockDefaultInteractions(false);
        bridgemainmenu.setBlockDefaultInteractions(true);
        bridgeselectmode.setBlockDefaultInteractions(true);
        customizemenu.setBlockDefaultInteractions(true);
        mapmenu.setBlockDefaultInteractions(true);
        hotbareditor.setBlockDefaultInteractions(false);
    }

    public SGMenu getMainMenu(){
        return bridgemainmenu;
    }

    private void onSaveHotbar(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        HashMap<String, Integer> hotbar = new HashMap<String, Integer>();
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) != null) {
                hotbar.put(inv.getItem(i).getType().toString(), i);
            }
        }
        HotbarConfig.get().set(event.getWhoClicked().getUniqueId().toString(), hotbar);
        HotbarConfig.save();
        HotbarConfig.reload();
    }
}
