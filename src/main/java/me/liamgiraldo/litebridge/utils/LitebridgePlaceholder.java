package me.liamgiraldo.litebridge.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.liamgiraldo.litebridge.Litebridge;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LitebridgePlaceholder extends PlaceholderExpansion {

    private Litebridge plugin;
    public LitebridgePlaceholder(Litebridge plugin) {
        this.plugin = plugin;
    }


    @Override
    public @NotNull String getIdentifier() {
        return "lb";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; //
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_");
        if(args.length == 0) return null;
        

        return null; //
    }
}
