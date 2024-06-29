package me.liamgiraldo.litebridge.models;

import org.bukkit.Location;
import org.bukkit.Material;

public class BlockChangeModel {
    private final Location location;
    private final Material before;
    private Material after;

    public BlockChangeModel(Location location, Material before, Material after) {
        this.location = location;
        this.before = before;
        this.after = after;
    }

    public Location getLocation() {
        return location;
    }

    public Material getBefore() {
        return before;
    }

    public Material getAfter() {
        return after;
    }

    public void setAfter(Material type) {
        this.after = type;
    }
}
