/*    */ package io.github.tejedu.manhunt;
/*    */ 
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.EventPriority;
/*    */ import org.bukkit.event.player.PlayerInteractEntityEvent;
/*    */ import org.bukkit.event.player.PlayerQuitEvent;
/*    */ import org.bukkit.plugin.java.JavaPlugin;
/*    */ 
/*    */ public class CaptureListener implements org.bukkit.event.Listener
/*    */ {
/*    */   private ManHunt plugin;
/*    */   
/*    */   public CaptureListener(JavaPlugin plugin)
/*    */   {
/* 16 */     this.plugin = ((ManHunt)plugin);
/*    */   }
/*    */   
/*    */   @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
/*    */   public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
/* 21 */     if (this.plugin.game == null)
/*    */     {
/* 23 */       return;
/*    */     }
/* 25 */     Player capturer = event.getPlayer();
/* 26 */     Player target = this.plugin.game.target;
/* 27 */     if ((event.getRightClicked().equals(target)) && (capturer.hasPermission("manhunt.basic"))) {
/* 28 */       this.plugin.game.capture(capturer);
/*    */     }
/*    */   }
/*    */   
/*    */   @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
/*    */   public void onPlayerQuit(PlayerQuitEvent event)
/*    */   {
/* 35 */     if ((this.plugin.game != null) && (this.plugin.game.target == event.getPlayer()))
/*    */     {
/* 37 */       this.plugin.queueGame();
/*    */     }
/*    */   }
/*    */ }


/* Location:              /home/jchoyt/Downloads/ManHunt.jar!/io/github/tejedu/manhunt/CaptureListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */