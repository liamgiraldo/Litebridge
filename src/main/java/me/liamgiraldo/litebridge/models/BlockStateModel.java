package me.liamgiraldo.litebridge.models;

import org.bukkit.Material;

public class BlockStateModel {
    private final Material material;
    private final byte data;
    public BlockStateModel(Material material, byte data) {
        this.material = material;
        this.data = data;
    }

    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }
}
