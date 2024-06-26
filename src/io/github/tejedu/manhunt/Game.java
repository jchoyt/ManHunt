package io.github.tejedu.manhunt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

public class Game
{
    public Player target;
    public boolean targetVerified;
    public ManHunt plugin;
    public BukkitTask surviveTimer;
    private List<Material> materialList = new ArrayList<Material>();
    private List<Material> enchantableList = new ArrayList<Material>();
        public long roundEnd;
        private Random random = new Random();
    private Set<PotionType> fakePotions = new HashSet<PotionType>();

    public Game(JavaPlugin plugin)
    {
        this(plugin, true);
    }

    public Game(JavaPlugin plugin, boolean startNewGame)
    {
        this.plugin = ((ManHunt)plugin);
        loadPrizeLists();
        if(startNewGame)
            setupNewGame();
    }

    public Game() {
        this.plugin = ((ManHunt)plugin);
    }


    public void loadPrizeLists() {
        if (this.plugin.getConfig().getBoolean("prizes.includeOverworld")) {
            enchantableList.addAll(Prizes.overworldEnchantableList);
            materialList.addAll(Prizes.overworldPrizes);
        }
        if (this.plugin.getConfig().getBoolean("prizes.includeNether")) {
            enchantableList.addAll(Prizes.netherEnchantableList);
            materialList.addAll(Prizes.netherPrizes);
        }
        if (this.plugin.getConfig().getBoolean("prizes.includeEnd"))
            materialList.addAll(Prizes.endPrizes);
        fakePotions.add(PotionType.AWKWARD);
        fakePotions.add(PotionType.MUNDANE);
        fakePotions.add(PotionType.THICK);
        fakePotions.add(PotionType.WATER);
    }

    public void setupNewGame()
    {
        List<Player> players = new ArrayList();
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("manhunt.basic")) {
                if (this.plugin.getConfig().getBoolean("round.survivalplayersonly")) {
                    if (player.getGameMode() == GameMode.SURVIVAL) {
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
        this.roundEnd = System.currentTimeMillis() + (1000L * this.plugin.getConfig().getInt("round.maxlength"));

        if(!this.plugin.getConfig().getBoolean("allowafk")) {
            this.target.sendMessage("In order to earn a prize for surviving, enter " + this.plugin.highlightColor + "/mh" + ChatColor.RESET + " into the chat.");
        }

        this.surviveTimer = new SurviveTimer(this.plugin).runTaskLater(this.plugin, 20 * this.plugin.getConfig().getInt("round.maxlength"));
    }

    public ItemStack randomItemStack()
    {
        ItemStack item;
        if (random.nextInt(100) > this.plugin.getConfig().getInt("prizes.enchrate")) { //pick regular item
            Material material = materialList.get(random.nextInt(materialList.size()));
            item = randomAmount(material);

            if(Material.POTION.equals(material) || Material.SPLASH_POTION.equals(material)){
                setPotionEffects(item);
            }
        } else { //create enchanted item
            Material material = enchantableList.get(random.nextInt(enchantableList.size()));
            item = new ItemStack(material, 1);
            item = randomEnchantment(item);
        }
        return item;
    }

    /**
        Amount is linear from 1 to max.  Max is defined by an entry in Prizes.prizeLimits or the stack size if no limit is specified.
     */
    private ItemStack randomAmount(Material material) {
        Integer limit = Prizes.prizeLimits.get(material);
        if(limit == null) {
            limit = material.getMaxStackSize();
        }
        return new ItemStack(material, 1 + random.nextInt(limit));
    }

    /* Adds potion effects to ItewmStack of potions */
    private void setPotionEffects(ItemStack stack) {
        PotionMeta meta = (PotionMeta)stack.getItemMeta();
        meta.setBasePotionType(getRandomPotionType());
        stack.setItemMeta(meta);
    }

    /* randomly selects a non-useless potion effect */
    private PotionType getRandomPotionType(){
        PotionType ret = PotionType.WATER;
        while(fakePotions.contains(ret)){
            ret = PotionType.values()[random.nextInt(PotionType.values().length)];
        }
        return ret;
    }

    private ItemStack randomEnchantment(ItemStack item) {
        ArrayList<Enchantment> possibleEnchantments = new ArrayList();
        Iterator<Enchantment> it = Registry.ENCHANTMENT.iterator();
        while (it.hasNext()) {
            Enchantment e = it.next();
            if (e.canEnchantItem(item)) {
                possibleEnchantments.add(e);
            }
        }

        if (possibleEnchantments.size() > 0) {
            // add one or more enchantments
            int anotherEnchant = 75;
            while(anotherEnchant > 0) {
                Collections.shuffle(possibleEnchantments);
                Enchantment enchantment = (Enchantment)possibleEnchantments.get(0);
                // don't double up enchantments
                if(!item.containsEnchantment(enchantment)) {
                    item.addEnchantment(enchantment, 1 + (int)(Math.random() * (enchantment.getMaxLevel() - 1 + 1)));
                    anotherEnchant = anotherEnchant - random.nextInt(100);}
            }
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


        awardPrize(capturer, target);

        this.plugin.queueGame();
    }

    public void survive()
    {
        if (this.targetVerified || this.plugin.getConfig().getBoolean("dev.allowafk")) {
            this.target.sendMessage("You survived!");

            awardPrize(this.target, null);

        } else {
            this.target.sendMessage("You survived but did not verify that you were not AFK!");
            AfkEvent event = new AfkEvent(this.target);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }

        this.plugin.queueGame();
    }

	/*
		Capturer is not null iff a capture occurred,
	*/
	public void awardPrize(Player capturer, Player target) {
		ItemStack prize = randomItemStack();

        //reformat the item name
        String prizeName = prize.getType().name().replace("_", " ");
        StringBuilder b = new StringBuilder(prizeName);
        int i = 0;
        do {
            b.replace(i, i + 1, b.substring(i, i + 1).toUpperCase());
            i = b.indexOf(" ", i) + 1;
        } while ((i > 0) && (i < b.length()));
        prizeName = b.toString();

        // determine who gets the prize
		Player awardPrizeTo = capturer == null ? target : capturer;
		PlayerInventory inventory = awardPrizeTo.getInventory();
		Map<Integer,ItemStack> leftovers = inventory.addItem(new ItemStack[] { prize });

        // if the winner's inventory is full, drop the prize at their feet
		if(!leftovers.isEmpty()) {
			World world = awardPrizeTo.getWorld();
			Location location = awardPrizeTo.getLocation();
			// Drop items at their feet
			for(ItemStack itemStack : leftovers.values()) {
				world.dropItem(location, itemStack);
			}
		}

		if(target != null) {
			// A capture occurred
			Bukkit.broadcastMessage("" + this.plugin.highlightColor + ChatColor.ITALIC + awardPrizeTo.getDisplayName() +
                    ChatColor.RESET + " captured " + this.plugin.highlightColor + ChatColor.ITALIC + this.target.getDisplayName() +
                    ChatColor.RESET + " and won " + prize.getAmount() + " " + prizeName + "!");
		} else {
			// A survive occurred
			Bukkit.broadcastMessage("" +
                    this.plugin.highlightColor +
                    ChatColor.ITALIC +
                    awardPrizeTo.getDisplayName() +
                    ChatColor.RESET + " survived and won " +
                    prize.getAmount() + " " + prizeName + "!");
		}
	}

    public void cancelSurviveTimer() {
        if (this.surviveTimer != null) {
            this.surviveTimer.cancel();
        }
    }

    public String getTimeLeft() {
        long millisLeft = this.roundEnd - System.currentTimeMillis();
        long minutesLeft = millisLeft / 1000L / 60L;
        if(minutesLeft < 1) {
            return "less than a minute";
        } else if (minutesLeft < 2) {
            return "about a minute";
        }
        return String.format("about %d minutes", minutesLeft);
    }

}
