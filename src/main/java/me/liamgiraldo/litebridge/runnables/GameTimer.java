package me.liamgiraldo.litebridge.runnables;

import me.liamgiraldo.litebridge.models.GameModel;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTimer extends BukkitRunnable {

    private int countdown;

    public GameTimer(int countdown) {
        this.countdown = countdown;
    }

    @Override
    public void run() {
        if (countdown <= 0) {
            // Timer has finished, cancel this BukkitRunnable
            this.cancel();
        } else {
            // Decrement the countdown and do something
            countdown--;
            // For example, you can print the remaining time to the console:
            System.out.println("Time remaining: " + countdown);
        }
    }

    public int getCountdown() {
        return countdown;
    }
}