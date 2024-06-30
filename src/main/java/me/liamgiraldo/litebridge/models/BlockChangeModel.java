package me.liamgiraldo.litebridge.models;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.Material;

public class BlockChangeModel {
    private final Location location;
    private final XMaterial before;
    private XMaterial after;
    private byte data;

    public BlockChangeModel(Location location, XMaterial before, XMaterial after, byte data) {
        this.location = location;
        this.before = before;
        this.after = after;
        this.data = data;
    }

    public Location getLocation() {
        return location;
    }

    public XMaterial getBefore() {
        return before;
    }

    public XMaterial getAfter() {
        return after;
    }

    public void setAfter(XMaterial type) {
        this.after = type;
    }

    @Override
    public String toString() {
        return "BlockChangeModel{" +
                "location=" + location.toString() +
                ", before=" + before.toString() +
                ", after=" + after.toString() +
                '}';
    }

    public byte getData() {
        return this.data;
    }
}
