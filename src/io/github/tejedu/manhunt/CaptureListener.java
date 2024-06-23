package io.github.tejedu.manhunt;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CaptureListener implements org.bukkit.event.Listener
{
    private ManHunt plugin;

    public CaptureListener(JavaPlugin plugin)
    {
        this.plugin = ((ManHunt)plugin);
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (this.plugin.game == null)
        {
            return;
        }
        Player capturer = event.getPlayer();
        Player target = this.plugin.game.target;
        if ((event.getRightClicked().equals(target)) && (capturer.hasPermission("manhunt.basic"))) {
            this.plugin.game.capture(capturer);
        }
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if ((this.plugin.game != null) && (this.plugin.game.target == event.getPlayer()))
        {
            this.plugin.queueGame();
        }
    }
}
