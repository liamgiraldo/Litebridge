package me.liamgiraldo.litebridge.models;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class CustomItem {
    private String name;
    private ItemStack itemStack;
    private ItemMeta itemMeta;
    ArrayList<String> lore;
    public CustomItem(String name, ItemStack itemStack) {
        this.name = name;
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
        this.lore = new ArrayList<>();
    }

    public void addLore(String lore){
        this.lore.add(lore);
        this.itemMeta.setLore(this.lore);
        this.itemStack.setItemMeta(this.itemMeta);
    }

    public void removeLore(String lore){
        this.lore.remove(lore);
        this.itemMeta.setLore(this.lore);
        this.itemStack.setItemMeta(this.itemMeta);
    }

    public void setDisplayName(String name){
        this.name = name;
        this.itemMeta.setDisplayName(name);
        this.itemStack.setItemMeta(this.itemMeta);
    }

    public String getDisplayName(){
        return this.name;
    }

    public ItemStack getItemStack(){
        return this.itemStack;
    }

    public ItemMeta getItemMeta(){
        return this.itemMeta;
    }

    public ArrayList<String> getLore(){
        return this.lore;
    }
}
