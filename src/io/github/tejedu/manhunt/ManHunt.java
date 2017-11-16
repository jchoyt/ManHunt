/*    */ package io.github.tejedu.manhunt;
/*    */ 
/*    */ import net.md_5.bungee.api.ChatColor;
/*    */ import org.bukkit.Server;
/*    */ import org.bukkit.command.Command;
/*    */ import org.bukkit.command.CommandSender;
/*    */ import org.bukkit.configuration.file.FileConfiguration;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.plugin.PluginManager;
/*    */ 
/*    */ public class ManHunt extends org.bukkit.plugin.java.JavaPlugin
/*    */ {
/*    */   public org.bukkit.scheduler.BukkitTask startGameTimer;
/*    */   public Game game;
/*    */   public ChatColor highlightColor;
/*    */   
/*    */   public void onEnable()
/*    */   {
/* 19 */     configuration();
/* 20 */     getServer().getPluginManager().registerEvents(new CaptureListener(this), this);
/* 21 */     this.highlightColor = ChatColor.getByChar(getConfig().getString("preferences.chathighlight").charAt(0));
/* 22 */     queueGame();
/*    */   }
/*    */   
/*    */   public void configuration() {
/* 26 */     FileConfiguration config = getConfig();
/* 27 */     config.options().copyDefaults(true);
/* 28 */     saveConfig();
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
/*    */   {
/* 36 */     if (cmd.getName().equalsIgnoreCase("mh")) {
/* 37 */       if (((sender instanceof Player)) && (this.game != null) && ((Player)sender == this.game.target)) {
/* 38 */         this.game.targetVerified = true;
/* 39 */         sender.sendMessage("If you survive for " + getConfig().getInt("round.maxlength") + 
/* 40 */           " seconds you will recieve the prize!");
/*    */       }
/* 42 */       return true;
/*    */     }
/*    */     
/* 45 */     if (cmd.getName().equalsIgnoreCase("mhtarget")) {
/* 46 */       if ((this.game != null) && ((this.game.target instanceof Player))) {
/* 47 */         sender.sendMessage("The target is " + this.highlightColor + ChatColor.ITALIC + this.game.target.getDisplayName() + 
/* 48 */           ChatColor.RESET + "!");
/*    */       } else {
/* 50 */         sender.sendMessage("There is currently no target.");
/*    */       }
/* 52 */       return true;
/*    */     }
/*    */     
/* 55 */     if (cmd.getName().equalsIgnoreCase("mhstart")) {
/* 56 */       if ((args.length > 0) && (!args[0].replaceAll("[^0-9]", "").isEmpty())) {
/* 57 */         queueGame(Integer.parseInt(args[0]));
/*    */       } else {
/* 59 */         queueGame();
/*    */       }
/*    */     }
/* 62 */     return true;
/*    */   }
/*    */   
/*    */   public void queueGame(int seconds)
/*    */   {
/* 67 */     if (this.game != null) {
/* 68 */       this.game.cancelSurviveTimer();
/* 69 */       this.game = null;
/*    */     }
/* 71 */     this.startGameTimer = new StartGameTimer(this).runTaskLater(this, 20 * seconds);
/*    */   }
/*    */   
/*    */   public void queueGame()
/*    */   {
/* 76 */     queueGame(getConfig().getInt("round.delay"));
/*    */   }
/*    */   
/*    */   public void createGame() {
/* 80 */     this.game = new Game(this);
/*    */   }
/*    */ }


/* Location:              /home/jchoyt/Downloads/ManHunt.jar!/io/github/tejedu/manhunt/ManHunt.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */