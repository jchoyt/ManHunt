package io.github.tejedu.manhunt;

import org.bukkit.plugin.java.JavaPlugin;

public class SurviveTimer extends org.bukkit.scheduler.BukkitRunnable
{
    private ManHunt plugin;

    public SurviveTimer(JavaPlugin plugin) {
        this.plugin = ((ManHunt)plugin);
    }

    public void run()
    {
        if (this.plugin.game != null) {
            this.plugin.game.survive();
        }
    }
}
