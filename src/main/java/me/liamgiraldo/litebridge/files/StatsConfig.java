package me.liamgiraldo.litebridge.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class StatsConfig {
    private static File file;
    private static FileConfiguration statsFile;

    public static void setup(){
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Litebridge").getDataFolder(), "statistics.yml");
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        statsFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get(){
        return statsFile;
    }

    public static void save(){
        try{
            statsFile.save(file);
        }catch (IOException e){
            System.out.println("Could not save the statistics file");
        }
    }

    public static void reload(){
        statsFile = YamlConfiguration.loadConfiguration(file);
    }
}
