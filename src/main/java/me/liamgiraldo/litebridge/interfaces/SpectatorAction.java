package me.liamgiraldo.litebridge.interfaces;

import org.bukkit.entity.Player;

public interface SpectatorAction {
    void perform(Player player);

    @FunctionalInterface
    interface SpectatorJoin {
        void onSpectatorJoin(Player player);
    }
}
