package io.github.tejedu.manhunt;

import java.util.ArrayList;
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

    public Game(JavaPlugin plugin)
    {
        this.plugin = ((ManHunt)plugin);
        setupNewGame();
    }

    public void setupNewGame()
    {
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
        Material[] materialList = { Material.STONE, Material.GRASS, Material.DIRT, Material.COBBLESTONE, Material.WOOD,
                        Material.SAPLING, Material.SAND, Material.GRAVEL, Material.GOLD_ORE, Material.IRON_ORE,
                        Material.COAL_ORE, Material.LOG, Material.LEAVES, Material.SPONGE, Material.GLASS, Material.LAPIS_ORE,
                        Material.LAPIS_BLOCK, Material.DISPENSER, Material.SANDSTONE, Material.NOTE_BLOCK,
                        Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH,
                        Material.WOOL, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.BROWN_MUSHROOM,
                        Material.RED_MUSHROOM, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.BRICK, Material.TNT,
                        Material.BOOKSHELF, Material.MOSSY_COBBLESTONE, Material.OBSIDIAN, Material.TORCH, Material.WOOD_STAIRS,
                        Material.CHEST, Material.DIAMOND_ORE, Material.WORKBENCH, Material.CROPS, Material.FURNACE,
                        Material.WOODEN_DOOR, Material.LADDER, Material.COBBLESTONE_STAIRS, Material.LEVER,
                        Material.REDSTONE_ORE, Material.STONE_BUTTON, Material.SNOW, Material.ICE, Material.CACTUS,
                        Material.CLAY, Material.JUKEBOX, Material.FENCE, Material.PUMPKIN, Material.NETHERRACK,
                        Material.SOUL_SAND, Material.GLOWSTONE, Material.JACK_O_LANTERN, Material.STAINED_GLASS,
                        Material.TRAP_DOOR, Material.IRON_FENCE, Material.THIN_GLASS, Material.MELON_BLOCK, Material.VINE,
                        Material.FENCE_GATE, Material.BRICK_STAIRS, Material.SMOOTH_STAIRS, Material.WATER_LILY,
                        Material.NETHER_BRICK, Material.NETHER_FENCE, Material.NETHER_BRICK_STAIRS, Material.NETHER_WARTS,
                        Material.ENCHANTMENT_TABLE, Material.BREWING_STAND, Material.CAULDRON, Material.ENDER_STONE,
                        Material.REDSTONE_LAMP_OFF, Material.COCOA, Material.SANDSTONE_STAIRS, Material.EMERALD_ORE,
                        Material.ENDER_CHEST, Material.TRIPWIRE_HOOK, Material.EMERALD_BLOCK, Material.SPRUCE_WOOD_STAIRS,
                        Material.BIRCH_WOOD_STAIRS, Material.JUNGLE_WOOD_STAIRS, Material.BEACON, Material.COBBLE_WALL,
                        Material.FLOWER_POT, Material.CARROT, Material.POTATO, Material.WOOD_BUTTON, Material.SKULL,
                        Material.ANVIL, Material.TRAPPED_CHEST, Material.DAYLIGHT_DETECTOR, Material.REDSTONE_BLOCK,
                        Material.QUARTZ_ORE, Material.HOPPER, Material.QUARTZ_BLOCK, Material.QUARTZ_STAIRS,
                        Material.ACTIVATOR_RAIL, Material.DROPPER, Material.STAINED_CLAY, Material.STAINED_GLASS_PANE,
                        Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS, Material.SLIME_BLOCK, Material.IRON_TRAPDOOR,
                        Material.PRISMARINE, Material.SEA_LANTERN, Material.HAY_BLOCK, Material.CARPET, Material.HARD_CLAY,
                        Material.COAL_BLOCK, Material.PACKED_ICE, Material.RED_SANDSTONE, Material.RED_SANDSTONE_STAIRS,
                        Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.JUNGLE_FENCE_GATE,
                        Material.DARK_OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.SPRUCE_FENCE, Material.BIRCH_FENCE,
                        Material.JUNGLE_FENCE, Material.DARK_OAK_FENCE, Material.ACACIA_FENCE, Material.SPRUCE_DOOR,
                        Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR,
                        Material.IRON_SPADE, Material.IRON_PICKAXE, Material.IRON_AXE, Material.FLINT_AND_STEEL, Material.APPLE,
                        Material.BOW, Material.ARROW, Material.COAL, Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT,
                        Material.IRON_SWORD, Material.WOOD_SWORD, Material.WOOD_SPADE, Material.WOOD_PICKAXE, Material.WOOD_AXE,
                        Material.STONE_SWORD, Material.STONE_SPADE, Material.STONE_PICKAXE, Material.STONE_AXE,
                        Material.DIAMOND_SWORD, Material.DIAMOND_SPADE, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE,
                        Material.STICK, Material.BOWL, Material.MUSHROOM_SOUP, Material.GOLD_SWORD, Material.GOLD_SPADE,
                        Material.GOLD_PICKAXE, Material.GOLD_AXE, Material.STRING, Material.FEATHER, Material.SULPHUR,
                        Material.WOOD_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.DIAMOND_HOE, Material.GOLD_HOE,
                        Material.SEEDS, Material.WHEAT, Material.BREAD, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
                        Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.CHAINMAIL_HELMET,
                        Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
                        Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
                        Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
                        Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS,
                        Material.FLINT, Material.PORK, Material.GRILLED_PORK, Material.PAINTING, Material.GOLDEN_APPLE,
                        Material.SIGN, Material.WOOD_DOOR, Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET,
                        Material.MINECART, Material.SADDLE, Material.IRON_DOOR, Material.REDSTONE, Material.SNOW_BALL,
                        Material.BOAT, Material.LEATHER, Material.MILK_BUCKET, Material.CLAY_BRICK, Material.CLAY_BALL,
                        Material.SUGAR_CANE, Material.PAPER, Material.BOOK, Material.SLIME_BALL, Material.STORAGE_MINECART,
                        Material.POWERED_MINECART, Material.EGG, Material.COMPASS, Material.FISHING_ROD, Material.WATCH,
                        Material.GLOWSTONE_DUST, Material.RAW_FISH, Material.COOKED_FISH, Material.INK_SACK, Material.BONE,
                        Material.SUGAR, Material.CAKE, Material.BED, Material.DIODE, Material.COOKIE, Material.MAP,
                        Material.SHEARS, Material.MELON, Material.PUMPKIN_SEEDS, Material.MELON_SEEDS, Material.RAW_BEEF,
                        Material.COOKED_BEEF, Material.RAW_CHICKEN, Material.COOKED_CHICKEN, Material.ROTTEN_FLESH,
                        Material.ENDER_PEARL, Material.BLAZE_ROD, Material.GHAST_TEAR, Material.GOLD_NUGGET,
                        Material.NETHER_STALK, Material.POTION, Material.GLASS_BOTTLE, Material.SPIDER_EYE,
                        Material.FERMENTED_SPIDER_EYE, Material.BLAZE_POWDER, Material.MAGMA_CREAM, Material.BREWING_STAND_ITEM,
                        Material.CAULDRON_ITEM, Material.EYE_OF_ENDER, Material.SPECKLED_MELON, Material.MONSTER_EGG,
                        Material.EXP_BOTTLE, Material.FIREBALL, Material.BOOK_AND_QUILL, Material.WRITTEN_BOOK,
                        Material.EMERALD, Material.ITEM_FRAME, Material.FLOWER_POT_ITEM, Material.CARROT_ITEM,
                        Material.POTATO_ITEM, Material.BAKED_POTATO, Material.POISONOUS_POTATO, Material.EMPTY_MAP,
                        Material.GOLDEN_CARROT, Material.SKULL_ITEM, Material.CARROT_STICK, Material.NETHER_STAR,
                        Material.PUMPKIN_PIE, Material.FIREWORK, Material.FIREWORK_CHARGE, Material.ENCHANTED_BOOK,
                        Material.REDSTONE_COMPARATOR, Material.NETHER_BRICK_ITEM, Material.QUARTZ, Material.EXPLOSIVE_MINECART,
                        Material.HOPPER_MINECART, Material.PRISMARINE_SHARD, Material.PRISMARINE_CRYSTALS, Material.RABBIT,
                        Material.COOKED_RABBIT, Material.RABBIT_STEW, Material.RABBIT_FOOT, Material.RABBIT_HIDE,
                        Material.ARMOR_STAND, Material.IRON_BARDING, Material.GOLD_BARDING, Material.DIAMOND_BARDING,
                        Material.LEASH, Material.NAME_TAG, Material.COMMAND_MINECART, Material.MUTTON, Material.COOKED_MUTTON,
                        Material.BANNER, Material.SPRUCE_DOOR_ITEM, Material.BIRCH_DOOR_ITEM, Material.JUNGLE_DOOR_ITEM,
                        Material.ACACIA_DOOR_ITEM, Material.DARK_OAK_DOOR_ITEM, Material.GOLD_RECORD, Material.GREEN_RECORD,
                        Material.RECORD_3, Material.RECORD_4, Material.RECORD_5, Material.RECORD_6, Material.RECORD_7,
                        Material.RECORD_8, Material.RECORD_9, Material.RECORD_10, Material.RECORD_11, Material.RECORD_12 };
        Material[] enchantableList = { Material.LEATHER_BOOTS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET,
                           Material.LEATHER_LEGGINGS, Material.WOOD_AXE, Material.WOOD_PICKAXE, Material.WOOD_SPADE,
                           Material.WOOD_SWORD, Material.STONE_AXE, Material.STONE_PICKAXE, Material.STONE_SPADE,
                           Material.STONE_SWORD, Material.IRON_AXE, Material.IRON_BOOTS, Material.IRON_CHESTPLATE,
                           Material.IRON_HELMET, Material.IRON_LEGGINGS, Material.IRON_PICKAXE, Material.IRON_SPADE,
                           Material.IRON_SWORD, Material.GOLD_AXE, Material.GOLD_BOOTS, Material.GOLD_CHESTPLATE,
                           Material.GOLD_HELMET, Material.GOLD_LEGGINGS, Material.GOLD_PICKAXE, Material.GOLD_SPADE,
                           Material.GOLD_SWORD, Material.DIAMOND_AXE, Material.DIAMOND_BOOTS, Material.DIAMOND_CHESTPLATE,
                           Material.DIAMOND_HELMET, Material.DIAMOND_LEGGINGS, Material.DIAMOND_PICKAXE, Material.DIAMOND_SPADE,
                           Material.DIAMOND_SWORD };
        ItemStack item;
        ItemStack item; if (new Random().nextInt(100) > this.plugin.getConfig().getInt("prizes.enchrate")) {
            Material material = materialList[new Random().nextInt(materialList.length)];
            item = randomAmount(material);
        } else {
            Material material = enchantableList[new Random().nextInt(enchantableList.length)];
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

        Bukkit.broadcastMessage(this.plugin.highlightColor + ChatColor.ITALIC + capturer.getDisplayName() +
                    ChatColor.RESET + " captured " + this.plugin.highlightColor + ChatColor.ITALIC + this.target.getDisplayName() +
                    ChatColor.RESET + " and won " + prizeAmount + " " + prizeName + "!");


        capturer.getInventory().addItem(new ItemStack[] { prize });

        this.plugin.queueGame();
    }

    public void survive()
    {
        if (this.targetVerified) {
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

            Bukkit.broadcastMessage(this.plugin.highlightColor + ChatColor.ITALIC + this.target.getDisplayName() +
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
