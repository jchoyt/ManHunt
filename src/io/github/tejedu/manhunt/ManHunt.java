package io.github.tejedu.manhunt;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

public class ManHunt extends org.bukkit.plugin.java.JavaPlugin
{
    public org.bukkit.scheduler.BukkitTask startGameTimer;
    public Game game;
    public ChatColor highlightColor;

    public void onEnable()
    {
        Prizes.setPrizeLimits();
        configuration();
        getServer().getPluginManager().registerEvents(new CaptureListener(this), this);
        this.highlightColor = ChatColor.getByChar(getConfig().getString("preferences.chathighlight").charAt(0));
        queueGame();
    }

    public void configuration() {
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("mh")) {
            if (((sender instanceof Player)) && (this.game != null) && ((Player)sender == this.game.target)) {
                this.game.targetVerified = true;
                sender.sendMessage("If you survive for " + this.game.getTimeLeft() + ", you will receive a prize! Be sure to keep an inventory slot open.");
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("mhtarget")) {
            if ((this.game != null) && ((this.game.target instanceof Player))) {
                sender.sendMessage("The target is " + this.highlightColor + ChatColor.ITALIC + this.game.target.getDisplayName() +
                           ChatColor.RESET + ". You have " + this.game.getTimeLeft() + " to find them!");
            } else {
                sender.sendMessage("There is currently no target.");
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("mhstart")) {
            if (sender.hasPermission("manhunt.admin")){
                if ((args.length > 0) && (!args[0].replaceAll("[^0-9]", "").isEmpty())) {
                    queueGame(Integer.parseInt(args[0]));
                } else {
                    queueGame();
                }
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("prize")) {

            if (args.length != 1) {
                sender.sendMessage("Specify a recepient!");
                return false;
            }

            // check that the player is online
            OfflinePlayer offlinePlayer = this.getPlayerByName(args[0]);
            if(offlinePlayer == null) {
                sender.sendMessage(args[0] + " has never logged in to this server.");
                return false;
            }
            Player target = offlinePlayer.getPlayer();

            if (target == null) {
                sender.sendMessage(args[0] + " is not online!");
                return false;
            }
            sender.sendMessage("Giving " + target.getDisplayName() + " a prize. I hope they've earned it.");
            game.awardPrize(target, null);
            return true;
        }
        return true;
    }

    public void queueGame(int seconds)
    {
        if (this.game != null) {
            this.game.cancelSurviveTimer();
            this.game = null;
        }
        this.startGameTimer = new StartGameTimer(this).runTaskLater(this, 20 * seconds);
    }

    public void queueGame()
    {
        queueGame(getConfig().getInt("round.delay"));
    }

    public void createGame() {
        this.game = new Game(this);
    }

    /*
     * If a player has ever logged into this server, this returns the OfflinePlayer
     * representation of that player. Returns null if the player has never logged
     * to this server. To see if the player is currently online, use OfflinePlayer.getPlayer()
     * which will return a Player object if they are online or null if they are not.
     */
    public OfflinePlayer getPlayerByName(String name) {
        OfflinePlayer[]  offlinePlayers = this.getServer().getOfflinePlayers();
        OfflinePlayer targetPlayer = null;
        for(OfflinePlayer p: offlinePlayers) {
            if(name.equalsIgnoreCase(p.getName())) {
                targetPlayer = p;
                break;
            }
        }
        return targetPlayer;
    }
}
