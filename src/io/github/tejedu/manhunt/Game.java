package io.github.tejedu.manhunt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Game
{
    public Player target;
    public boolean targetVerified;
    public ManHunt plugin;
    public BukkitTask surviveTimer;
    private List<Material> materialList = new ArrayList<Material>();

    public Game(JavaPlugin plugin)
    {
        this.plugin = ((ManHunt)plugin);
        setupNewGame();
    }

    public void setupNewGame()
    {
        materialList.addAll(Prizes.overworldPrizes);
        materialList.addAll(Prizes.netherPrizes);

        List<Player> players = new ArrayList();
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("manhunt.basic")) {
                if (this.plugin.getConfig().getBoolean("round.survivalplayersonly")) {
                    if (player.getGameMode() == GameMode.SURVIVAL)
                    {

                        players.add(player);
                    }
                }
                else {
                    players.add(player);
                }
            }
        }



        if (players.size() < this.plugin.getConfig().getInt("round.minplayers")) {
            this.plugin.queueGame();
            return;
        }



        Collections.shuffle(players);
        this.plugin.getServer()
        .broadcastMessage("Man Hunt has started! " + this.plugin.highlightColor + ChatColor.ITALIC +
                  ((Player)players.get(0)).getDisplayName() + ChatColor.RESET +
                  " is the target! Find and right-click them to earn a reward!");
        this.target = ((Player)players.get(0));
        this.targetVerified = false;


        this.target.sendMessage("In order to earn a prize for surviving enter " + this.plugin.highlightColor + "/mh" +
                    ChatColor.RESET + " into the chat.");


        this.surviveTimer = new SurviveTimer(this.plugin).runTaskLater(this.plugin, 20 * this.plugin.getConfig().getInt("round.maxlength"));
    }

    public ItemStack randomItemStack()
    {
        ItemStack item;
        if (new Random().nextInt(100) > this.plugin.getConfig().getInt("prizes.enchrate")) {
            Material material = materialList.get(new Random().nextInt(materialList.size()));
            item = randomAmount(material);
        } else {
            Material material = Prizes.enchantableList[new Random().nextInt(Prizes.enchantableList.length)];
            item = new ItemStack(material, 1);
            item = randomEnchantment(item);
        }
        return item;
    }

    private ItemStack randomAmount(Material material) {
        return new ItemStack(material, 1 + new Random().nextInt(material.getMaxStackSize()));
    }

    private ItemStack randomEnchantment(ItemStack item) {
        ArrayList<Enchantment> possibleEnchantments = new ArrayList();
        Enchantment[] arrayOfEnchantment;
        int j = (arrayOfEnchantment = Enchantment.values()).length; for (int i = 0; i < j; i++) { Enchantment e = arrayOfEnchantment[i];
                                                      if (e.canEnchantItem(item)) {
                                                          possibleEnchantments.add(e);
                                                      }}

        if (possibleEnchantments.size() > 0) {
            Collections.shuffle(possibleEnchantments);
            Enchantment enchantment = (Enchantment)possibleEnchantments.get(0);
            item.addEnchantment(enchantment, 1 + (int)(Math.random() * (enchantment.getMaxLevel() - 1 + 1)));
        }

        return item;
    }


    public void capture(Player capturer)
    {
        this.surviveTimer.cancel();


        capturer.sendMessage("You captured " + this.plugin.highlightColor + ChatColor.ITALIC + this.target.getDisplayName() +
                     ChatColor.RESET + "!");
        this.target.sendMessage("You were captured by " + this.plugin.highlightColor + ChatColor.ITALIC +
                    capturer.getDisplayName() + ChatColor.RESET + "!");


        ItemStack prize = randomItemStack();


        String prizeName = prize.getType().name().replace("_", " ");
        StringBuilder b = new StringBuilder(prizeName);
        int i = 0;
        do {
            b.replace(i, i + 1, b.substring(i, i + 1).toUpperCase());
            i = b.indexOf(" ", i) + 1;
        } while ((i > 0) && (i < b.length()));
        prizeName = b.toString();
        int prizeAmount = prize.getAmount();

        Bukkit.broadcastMessage("" + this.plugin.highlightColor + ChatColor.ITALIC + capturer.getDisplayName() +
                    ChatColor.RESET + " captured " + this.plugin.highlightColor + ChatColor.ITALIC + this.target.getDisplayName() +
                    ChatColor.RESET + " and won " + prizeAmount + " " + prizeName + "!");


        capturer.getInventory().addItem(new ItemStack[] { prize });

        this.plugin.queueGame();
    }

    public void survive()
    {
        if (this.targetVerified || this.plugin.getConfig().getBoolean("dev.allowafk")) {
            this.target.sendMessage("You survived!");


            ItemStack prize = randomItemStack();


            String prizeName = prize.getType().name().replace("_", " ");
            StringBuilder b = new StringBuilder(prizeName);
            int i = 0;
            do {
                b.replace(i, i + 1, b.substring(i, i + 1).toUpperCase());
                i = b.indexOf(" ", i) + 1;
            } while ((i > 0) && (i < b.length()));
            prizeName = b.toString();
            int prizeAmount = prize.getAmount();

            Bukkit.broadcastMessage("" + this.plugin.highlightColor + ChatColor.ITALIC + this.target.getDisplayName() +
                        ChatColor.RESET + " survived and won " + prizeAmount + " " + prizeName + "!");


            this.target.getInventory().addItem(new ItemStack[] { prize });
        } else {
            this.target.sendMessage("You survived but did not verify that you were not AFK!");
        }

        this.plugin.queueGame();
    }

    public void cancelSurviveTimer() {
        if (this.surviveTimer != null) {
            this.surviveTimer.cancel();
        }
    }
}
