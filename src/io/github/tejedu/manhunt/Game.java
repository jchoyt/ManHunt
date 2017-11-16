/*     */ package io.github.tejedu.manhunt;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.List;
/*     */ import java.util.Random;
/*     */ import net.md_5.bungee.api.ChatColor;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.GameMode;
/*     */ import org.bukkit.Material;
/*     */ import org.bukkit.Server;
/*     */ import org.bukkit.configuration.file.FileConfiguration;
/*     */ import org.bukkit.enchantments.Enchantment;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.inventory.PlayerInventory;
/*     */ import org.bukkit.plugin.java.JavaPlugin;
/*     */ import org.bukkit.scheduler.BukkitTask;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Game
/*     */ {
/*     */   public Player target;
/*     */   public boolean targetVerified;
/*     */   public ManHunt plugin;
/*     */   public BukkitTask surviveTimer;
/*     */   
/*     */   public Game(JavaPlugin plugin)
/*     */   {
/*  33 */     this.plugin = ((ManHunt)plugin);
/*  34 */     setupNewGame();
/*     */   }
/*     */   
/*     */   public void setupNewGame()
/*     */   {
/*  39 */     List<Player> players = new ArrayList();
/*  40 */     for (Player player : this.plugin.getServer().getOnlinePlayers()) {
/*  41 */       if (player.hasPermission("manhunt.basic")) {
/*  42 */         if (this.plugin.getConfig().getBoolean("round.survivalplayersonly")) {
/*  43 */           if (player.getGameMode() == GameMode.SURVIVAL)
/*     */           {
/*     */ 
/*  46 */             players.add(player);
/*     */           }
/*     */         }
/*     */         else {
/*  50 */           players.add(player);
/*     */         }
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*  57 */     if (players.size() < this.plugin.getConfig().getInt("round.minplayers")) {
/*  58 */       this.plugin.queueGame();
/*  59 */       return;
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*  64 */     Collections.shuffle(players);
/*  65 */     this.plugin.getServer()
/*  66 */       .broadcastMessage("Man Hunt has started! " + this.plugin.highlightColor + ChatColor.ITALIC + 
/*  67 */       ((Player)players.get(0)).getDisplayName() + ChatColor.RESET + 
/*  68 */       " is the target! Find and right-click them to earn a reward!");
/*  69 */     this.target = ((Player)players.get(0));
/*  70 */     this.targetVerified = false;
/*     */     
/*     */ 
/*  73 */     this.target.sendMessage("In order to earn a prize for surviving enter " + this.plugin.highlightColor + "/mh" + 
/*  74 */       ChatColor.RESET + " into the chat.");
/*     */     
/*     */ 
/*  77 */     this.surviveTimer = new SurviveTimer(this.plugin).runTaskLater(this.plugin, 20 * this.plugin.getConfig().getInt("round.maxlength"));
/*     */   }
/*     */   
/*     */   public ItemStack randomItemStack()
/*     */   {
/*  82 */     Material[] materialList = { Material.STONE, Material.GRASS, Material.DIRT, Material.COBBLESTONE, Material.WOOD, 
/*  83 */       Material.SAPLING, Material.SAND, Material.GRAVEL, Material.GOLD_ORE, Material.IRON_ORE, 
/*  84 */       Material.COAL_ORE, Material.LOG, Material.LEAVES, Material.SPONGE, Material.GLASS, Material.LAPIS_ORE, 
/*  85 */       Material.LAPIS_BLOCK, Material.DISPENSER, Material.SANDSTONE, Material.NOTE_BLOCK, 
/*  86 */       Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, 
/*  87 */       Material.WOOL, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.BROWN_MUSHROOM, 
/*  88 */       Material.RED_MUSHROOM, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.BRICK, Material.TNT, 
/*  89 */       Material.BOOKSHELF, Material.MOSSY_COBBLESTONE, Material.OBSIDIAN, Material.TORCH, Material.WOOD_STAIRS, 
/*  90 */       Material.CHEST, Material.DIAMOND_ORE, Material.WORKBENCH, Material.CROPS, Material.FURNACE, 
/*  91 */       Material.WOODEN_DOOR, Material.LADDER, Material.COBBLESTONE_STAIRS, Material.LEVER, 
/*  92 */       Material.REDSTONE_ORE, Material.STONE_BUTTON, Material.SNOW, Material.ICE, Material.CACTUS, 
/*  93 */       Material.CLAY, Material.JUKEBOX, Material.FENCE, Material.PUMPKIN, Material.NETHERRACK, 
/*  94 */       Material.SOUL_SAND, Material.GLOWSTONE, Material.JACK_O_LANTERN, Material.STAINED_GLASS, 
/*  95 */       Material.TRAP_DOOR, Material.IRON_FENCE, Material.THIN_GLASS, Material.MELON_BLOCK, Material.VINE, 
/*  96 */       Material.FENCE_GATE, Material.BRICK_STAIRS, Material.SMOOTH_STAIRS, Material.WATER_LILY, 
/*  97 */       Material.NETHER_BRICK, Material.NETHER_FENCE, Material.NETHER_BRICK_STAIRS, Material.NETHER_WARTS, 
/*  98 */       Material.ENCHANTMENT_TABLE, Material.BREWING_STAND, Material.CAULDRON, Material.ENDER_STONE, 
/*  99 */       Material.REDSTONE_LAMP_OFF, Material.COCOA, Material.SANDSTONE_STAIRS, Material.EMERALD_ORE, 
/* 100 */       Material.ENDER_CHEST, Material.TRIPWIRE_HOOK, Material.EMERALD_BLOCK, Material.SPRUCE_WOOD_STAIRS, 
/* 101 */       Material.BIRCH_WOOD_STAIRS, Material.JUNGLE_WOOD_STAIRS, Material.BEACON, Material.COBBLE_WALL, 
/* 102 */       Material.FLOWER_POT, Material.CARROT, Material.POTATO, Material.WOOD_BUTTON, Material.SKULL, 
/* 103 */       Material.ANVIL, Material.TRAPPED_CHEST, Material.DAYLIGHT_DETECTOR, Material.REDSTONE_BLOCK, 
/* 104 */       Material.QUARTZ_ORE, Material.HOPPER, Material.QUARTZ_BLOCK, Material.QUARTZ_STAIRS, 
/* 105 */       Material.ACTIVATOR_RAIL, Material.DROPPER, Material.STAINED_CLAY, Material.STAINED_GLASS_PANE, 
/* 106 */       Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS, Material.SLIME_BLOCK, Material.IRON_TRAPDOOR, 
/* 107 */       Material.PRISMARINE, Material.SEA_LANTERN, Material.HAY_BLOCK, Material.CARPET, Material.HARD_CLAY, 
/* 108 */       Material.COAL_BLOCK, Material.PACKED_ICE, Material.RED_SANDSTONE, Material.RED_SANDSTONE_STAIRS, 
/* 109 */       Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.JUNGLE_FENCE_GATE, 
/* 110 */       Material.DARK_OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.SPRUCE_FENCE, Material.BIRCH_FENCE, 
/* 111 */       Material.JUNGLE_FENCE, Material.DARK_OAK_FENCE, Material.ACACIA_FENCE, Material.SPRUCE_DOOR, 
/* 112 */       Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, 
/* 113 */       Material.IRON_SPADE, Material.IRON_PICKAXE, Material.IRON_AXE, Material.FLINT_AND_STEEL, Material.APPLE, 
/* 114 */       Material.BOW, Material.ARROW, Material.COAL, Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT, 
/* 115 */       Material.IRON_SWORD, Material.WOOD_SWORD, Material.WOOD_SPADE, Material.WOOD_PICKAXE, Material.WOOD_AXE, 
/* 116 */       Material.STONE_SWORD, Material.STONE_SPADE, Material.STONE_PICKAXE, Material.STONE_AXE, 
/* 117 */       Material.DIAMOND_SWORD, Material.DIAMOND_SPADE, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, 
/* 118 */       Material.STICK, Material.BOWL, Material.MUSHROOM_SOUP, Material.GOLD_SWORD, Material.GOLD_SPADE, 
/* 119 */       Material.GOLD_PICKAXE, Material.GOLD_AXE, Material.STRING, Material.FEATHER, Material.SULPHUR, 
/* 120 */       Material.WOOD_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.DIAMOND_HOE, Material.GOLD_HOE, 
/* 121 */       Material.SEEDS, Material.WHEAT, Material.BREAD, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, 
/* 122 */       Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.CHAINMAIL_HELMET, 
/* 123 */       Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, 
/* 124 */       Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, 
/* 125 */       Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, 
/* 126 */       Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, 
/* 127 */       Material.FLINT, Material.PORK, Material.GRILLED_PORK, Material.PAINTING, Material.GOLDEN_APPLE, 
/* 128 */       Material.SIGN, Material.WOOD_DOOR, Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET, 
/* 129 */       Material.MINECART, Material.SADDLE, Material.IRON_DOOR, Material.REDSTONE, Material.SNOW_BALL, 
/* 130 */       Material.BOAT, Material.LEATHER, Material.MILK_BUCKET, Material.CLAY_BRICK, Material.CLAY_BALL, 
/* 131 */       Material.SUGAR_CANE, Material.PAPER, Material.BOOK, Material.SLIME_BALL, Material.STORAGE_MINECART, 
/* 132 */       Material.POWERED_MINECART, Material.EGG, Material.COMPASS, Material.FISHING_ROD, Material.WATCH, 
/* 133 */       Material.GLOWSTONE_DUST, Material.RAW_FISH, Material.COOKED_FISH, Material.INK_SACK, Material.BONE, 
/* 134 */       Material.SUGAR, Material.CAKE, Material.BED, Material.DIODE, Material.COOKIE, Material.MAP, 
/* 135 */       Material.SHEARS, Material.MELON, Material.PUMPKIN_SEEDS, Material.MELON_SEEDS, Material.RAW_BEEF, 
/* 136 */       Material.COOKED_BEEF, Material.RAW_CHICKEN, Material.COOKED_CHICKEN, Material.ROTTEN_FLESH, 
/* 137 */       Material.ENDER_PEARL, Material.BLAZE_ROD, Material.GHAST_TEAR, Material.GOLD_NUGGET, 
/* 138 */       Material.NETHER_STALK, Material.POTION, Material.GLASS_BOTTLE, Material.SPIDER_EYE, 
/* 139 */       Material.FERMENTED_SPIDER_EYE, Material.BLAZE_POWDER, Material.MAGMA_CREAM, Material.BREWING_STAND_ITEM, 
/* 140 */       Material.CAULDRON_ITEM, Material.EYE_OF_ENDER, Material.SPECKLED_MELON, Material.MONSTER_EGG, 
/* 141 */       Material.EXP_BOTTLE, Material.FIREBALL, Material.BOOK_AND_QUILL, Material.WRITTEN_BOOK, 
/* 142 */       Material.EMERALD, Material.ITEM_FRAME, Material.FLOWER_POT_ITEM, Material.CARROT_ITEM, 
/* 143 */       Material.POTATO_ITEM, Material.BAKED_POTATO, Material.POISONOUS_POTATO, Material.EMPTY_MAP, 
/* 144 */       Material.GOLDEN_CARROT, Material.SKULL_ITEM, Material.CARROT_STICK, Material.NETHER_STAR, 
/* 145 */       Material.PUMPKIN_PIE, Material.FIREWORK, Material.FIREWORK_CHARGE, Material.ENCHANTED_BOOK, 
/* 146 */       Material.REDSTONE_COMPARATOR, Material.NETHER_BRICK_ITEM, Material.QUARTZ, Material.EXPLOSIVE_MINECART, 
/* 147 */       Material.HOPPER_MINECART, Material.PRISMARINE_SHARD, Material.PRISMARINE_CRYSTALS, Material.RABBIT, 
/* 148 */       Material.COOKED_RABBIT, Material.RABBIT_STEW, Material.RABBIT_FOOT, Material.RABBIT_HIDE, 
/* 149 */       Material.ARMOR_STAND, Material.IRON_BARDING, Material.GOLD_BARDING, Material.DIAMOND_BARDING, 
/* 150 */       Material.LEASH, Material.NAME_TAG, Material.COMMAND_MINECART, Material.MUTTON, Material.COOKED_MUTTON, 
/* 151 */       Material.BANNER, Material.SPRUCE_DOOR_ITEM, Material.BIRCH_DOOR_ITEM, Material.JUNGLE_DOOR_ITEM, 
/* 152 */       Material.ACACIA_DOOR_ITEM, Material.DARK_OAK_DOOR_ITEM, Material.GOLD_RECORD, Material.GREEN_RECORD, 
/* 153 */       Material.RECORD_3, Material.RECORD_4, Material.RECORD_5, Material.RECORD_6, Material.RECORD_7, 
/* 154 */       Material.RECORD_8, Material.RECORD_9, Material.RECORD_10, Material.RECORD_11, Material.RECORD_12 };
/* 155 */     Material[] enchantableList = { Material.LEATHER_BOOTS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET, 
/* 156 */       Material.LEATHER_LEGGINGS, Material.WOOD_AXE, Material.WOOD_PICKAXE, Material.WOOD_SPADE, 
/* 157 */       Material.WOOD_SWORD, Material.STONE_AXE, Material.STONE_PICKAXE, Material.STONE_SPADE, 
/* 158 */       Material.STONE_SWORD, Material.IRON_AXE, Material.IRON_BOOTS, Material.IRON_CHESTPLATE, 
/* 159 */       Material.IRON_HELMET, Material.IRON_LEGGINGS, Material.IRON_PICKAXE, Material.IRON_SPADE, 
/* 160 */       Material.IRON_SWORD, Material.GOLD_AXE, Material.GOLD_BOOTS, Material.GOLD_CHESTPLATE, 
/* 161 */       Material.GOLD_HELMET, Material.GOLD_LEGGINGS, Material.GOLD_PICKAXE, Material.GOLD_SPADE, 
/* 162 */       Material.GOLD_SWORD, Material.DIAMOND_AXE, Material.DIAMOND_BOOTS, Material.DIAMOND_CHESTPLATE, 
/* 163 */       Material.DIAMOND_HELMET, Material.DIAMOND_LEGGINGS, Material.DIAMOND_PICKAXE, Material.DIAMOND_SPADE, 
/* 164 */       Material.DIAMOND_SWORD };
/*     */     ItemStack item;
/* 166 */     ItemStack item; if (new Random().nextInt(100) > this.plugin.getConfig().getInt("prizes.enchrate")) {
/* 167 */       Material material = materialList[new Random().nextInt(materialList.length)];
/* 168 */       item = randomAmount(material);
/*     */     } else {
/* 170 */       Material material = enchantableList[new Random().nextInt(enchantableList.length)];
/* 171 */       item = new ItemStack(material, 1);
/* 172 */       item = randomEnchantment(item);
/*     */     }
/* 174 */     return item;
/*     */   }
/*     */   
/*     */   private ItemStack randomAmount(Material material) {
/* 178 */     return new ItemStack(material, 1 + new Random().nextInt(material.getMaxStackSize()));
/*     */   }
/*     */   
/*     */   private ItemStack randomEnchantment(ItemStack item) {
/* 182 */     ArrayList<Enchantment> possibleEnchantments = new ArrayList();
/*     */     Enchantment[] arrayOfEnchantment;
/* 184 */     int j = (arrayOfEnchantment = Enchantment.values()).length; for (int i = 0; i < j; i++) { Enchantment e = arrayOfEnchantment[i];
/* 185 */       if (e.canEnchantItem(item)) {
/* 186 */         possibleEnchantments.add(e);
/*     */       }
/*     */     }
/*     */     
/* 190 */     if (possibleEnchantments.size() > 0) {
/* 191 */       Collections.shuffle(possibleEnchantments);
/* 192 */       Enchantment enchantment = (Enchantment)possibleEnchantments.get(0);
/* 193 */       item.addEnchantment(enchantment, 1 + (int)(Math.random() * (enchantment.getMaxLevel() - 1 + 1)));
/*     */     }
/*     */     
/* 196 */     return item;
/*     */   }
/*     */   
/*     */ 
/*     */   public void capture(Player capturer)
/*     */   {
/* 202 */     this.surviveTimer.cancel();
/*     */     
/*     */ 
/* 205 */     capturer.sendMessage("You captured " + this.plugin.highlightColor + ChatColor.ITALIC + this.target.getDisplayName() + 
/* 206 */       ChatColor.RESET + "!");
/* 207 */     this.target.sendMessage("You were captured by " + this.plugin.highlightColor + ChatColor.ITALIC + 
/* 208 */       capturer.getDisplayName() + ChatColor.RESET + "!");
/*     */     
/*     */ 
/* 211 */     ItemStack prize = randomItemStack();
/*     */     
/*     */ 
/* 214 */     String prizeName = prize.getType().name().replace("_", " ");
/* 215 */     StringBuilder b = new StringBuilder(prizeName);
/* 216 */     int i = 0;
/*     */     do {
/* 218 */       b.replace(i, i + 1, b.substring(i, i + 1).toUpperCase());
/* 219 */       i = b.indexOf(" ", i) + 1;
/* 220 */     } while ((i > 0) && (i < b.length()));
/* 221 */     prizeName = b.toString();
/* 222 */     int prizeAmount = prize.getAmount();
/*     */     
/* 224 */     Bukkit.broadcastMessage(this.plugin.highlightColor + ChatColor.ITALIC + capturer.getDisplayName() + 
/* 225 */       ChatColor.RESET + " captured " + this.plugin.highlightColor + ChatColor.ITALIC + this.target.getDisplayName() + 
/* 226 */       ChatColor.RESET + " and won " + prizeAmount + " " + prizeName + "!");
/*     */     
/*     */ 
/* 229 */     capturer.getInventory().addItem(new ItemStack[] { prize });
/*     */     
/* 231 */     this.plugin.queueGame();
/*     */   }
/*     */   
/*     */   public void survive()
/*     */   {
/* 236 */     if (this.targetVerified) {
/* 237 */       this.target.sendMessage("You survived!");
/*     */       
/*     */ 
/* 240 */       ItemStack prize = randomItemStack();
/*     */       
/*     */ 
/* 243 */       String prizeName = prize.getType().name().replace("_", " ");
/* 244 */       StringBuilder b = new StringBuilder(prizeName);
/* 245 */       int i = 0;
/*     */       do {
/* 247 */         b.replace(i, i + 1, b.substring(i, i + 1).toUpperCase());
/* 248 */         i = b.indexOf(" ", i) + 1;
/* 249 */       } while ((i > 0) && (i < b.length()));
/* 250 */       prizeName = b.toString();
/* 251 */       int prizeAmount = prize.getAmount();
/*     */       
/* 253 */       Bukkit.broadcastMessage(this.plugin.highlightColor + ChatColor.ITALIC + this.target.getDisplayName() + 
/* 254 */         ChatColor.RESET + " survived and won " + prizeAmount + " " + prizeName + "!");
/*     */       
/*     */ 
/* 257 */       this.target.getInventory().addItem(new ItemStack[] { prize });
/*     */     } else {
/* 259 */       this.target.sendMessage("You survived but did not verify that you were not AFK!");
/*     */     }
/*     */     
/* 262 */     this.plugin.queueGame();
/*     */   }
/*     */   
/*     */   public void cancelSurviveTimer() {
/* 266 */     if (this.surviveTimer != null) {
/* 267 */       this.surviveTimer.cancel();
/*     */     }
/*     */   }
/*     */ }


/* Location:              /home/jchoyt/Downloads/ManHunt.jar!/io/github/tejedu/manhunt/Game.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */