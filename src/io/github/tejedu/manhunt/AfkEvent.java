/*
 * Copyright 2018 Jeffrey C. Hoyt. Released under the Aapache 2.0 license
 */

 package io.github.tejedu.manhunt;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * Sends a notification when the player fails to /mh
 */
public class AfkEvent extends Event {
    private Player p = null;
    private static final HandlerList handlers = new HandlerList();

    public AfkEvent(Player afkPlayer) {
        this.p = afkPlayer;
    }

    public Player getPlayer() {
        return p;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
