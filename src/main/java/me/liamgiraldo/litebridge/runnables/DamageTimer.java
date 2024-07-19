package me.liamgiraldo.litebridge.runnables;

import org.bukkit.scheduler.BukkitRunnable;

public class DamageTimer extends BukkitRunnable {

    private int countdown;
    private boolean damagedTimerActive = false;

    public DamageTimer(){
        this.countdown = 5;
    }

    @Override
    public void run() {
        if (countdown <= 0) {
            // Timer has finished, cancel this BukkitRunnable
            damagedTimerActive = false;
            this.cancel();
        } else {
            // Decrement the countdown and do something
            damagedTimerActive = true;
            countdown--;
            // For example, you can print the remaining time to the console:
//            System.out.println("Time remaining: " + countdown);
        }
    }

    public int getCountdown() {
        return countdown;
    }

    public String getCountdownInMinutes() {
        int minutes = countdown / 60;
        int seconds = countdown % 60;
        if(seconds < 10) {
            return minutes + ":0" + seconds;
        }
        return minutes + ":" + seconds;
    }

    @Override
    public String toString(){
        return "DamageTimer{" +
                "countdown=" + countdown +
                '}';
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public void resetCountdown() {
        this.countdown = 5;
    }

    public boolean isDamagedTimerActive() {
        return damagedTimerActive;
    }

    public void cancelTimer() {
        damagedTimerActive = false;
        this.cancel();
    }
}