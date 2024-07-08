package me.liamgiraldo.litebridge.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class HotbarConfig {
    private static File file;
    private static FileConfiguration hotbarFile;

    public static void setup(){
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Litebridge").getDataFolder(), "hotbar.yml");
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        hotbarFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get(){
        return hotbarFile;
    }

    public static void save(){
        try{
            hotbarFile.save(file);
        }catch (IOException e){
            System.out.println("Could not save the hotbar file");
        }
    }

    public static void reload(){
        hotbarFile = YamlConfiguration.loadConfiguration(file);
    }
}
