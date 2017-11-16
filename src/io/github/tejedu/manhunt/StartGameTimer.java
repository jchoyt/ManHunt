/*    */ package io.github.tejedu.manhunt;
/*    */ 
/*    */ import org.bukkit.plugin.java.JavaPlugin;
/*    */ import org.bukkit.scheduler.BukkitRunnable;
/*    */ 
/*    */ public class StartGameTimer extends BukkitRunnable
/*    */ {
/*    */   private ManHunt plugin;
/*    */   
/*    */   public StartGameTimer(JavaPlugin plugin)
/*    */   {
/* 12 */     this.plugin = ((ManHunt)plugin);
/*    */   }
/*    */   
/*    */   public void run()
/*    */   {
/* 17 */     this.plugin.createGame();
/*    */   }
/*    */ }


/* Location:              /home/jchoyt/Downloads/ManHunt.jar!/io/github/tejedu/manhunt/StartGameTimer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */