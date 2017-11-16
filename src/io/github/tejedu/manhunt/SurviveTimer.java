/*    */ package io.github.tejedu.manhunt;
/*    */ 
/*    */ import org.bukkit.plugin.java.JavaPlugin;
/*    */ 
/*    */ public class SurviveTimer extends org.bukkit.scheduler.BukkitRunnable
/*    */ {
/*    */   private ManHunt plugin;
/*    */   
/*    */   public SurviveTimer(JavaPlugin plugin) {
/* 10 */     this.plugin = ((ManHunt)plugin);
/*    */   }
/*    */   
/*    */   public void run()
/*    */   {
/* 15 */     if (this.plugin.game != null) {
/* 16 */       this.plugin.game.survive();
/*    */     }
/*    */   }
/*    */ }


/* Location:              /home/jchoyt/Downloads/ManHunt.jar!/io/github/tejedu/manhunt/SurviveTimer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */