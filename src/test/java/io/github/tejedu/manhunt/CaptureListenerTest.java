package io.github.tejedu.manhunt;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptureListenerTest {

    private ServerMock server;
    private ManHunt plugin;
    private PlayerMock target;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(ManHunt.class);

        plugin.getConfig().set("round.minplayers", 1);
        plugin.getConfig().set("prizes.enchrate", -1); // deterministic plain-item prize (0 still has a 1% enchant chance)
        target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);
        plugin.createGame();
        assertSame(target, plugin.game.target, "test setup should have picked our player as the target");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private boolean hasAnyItem(PlayerMock player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return true;
            }
        }
        return false;
    }

    @Test
    void rightClickingTheTargetCapturesThem() {
        PlayerMock capturer = server.addPlayer("Hunter");

        server.getPluginManager().callEvent(new PlayerInteractEntityEvent(capturer, target));

        assertTrue(hasAnyItem(capturer), "Capturer should have received a prize");
    }

    @Test
    void rightClickingSomeoneWhoIsNotTheTargetDoesNothing() {
        PlayerMock capturer = server.addPlayer("Hunter");
        PlayerMock bystander = server.addPlayer("Bystander");

        server.getPluginManager().callEvent(new PlayerInteractEntityEvent(capturer, bystander));

        assertFalse(hasAnyItem(capturer), "Right-clicking a non-target shouldn't award a prize");
    }

    @Test
    void rightClickingWithoutPermissionDoesNothing() {
        PlayerMock capturer = server.addPlayer("Hunter");
        capturer.addAttachment(plugin).setPermission("manhunt.basic", false);

        server.getPluginManager().callEvent(new PlayerInteractEntityEvent(capturer, target));

        assertFalse(hasAnyItem(capturer), "A player without manhunt.basic shouldn't be able to capture");
    }

    @Test
    void theTargetDisconnectingRequeuesTheRound() {
        target.disconnect();

        assertNull(plugin.game, "Losing the target mid-round should clear the current game");
    }

    @Test
    void aBystanderDisconnectingDoesNotAffectTheRound() {
        PlayerMock bystander = server.addPlayer("Bystander");
        var gameBeforeDisconnect = plugin.game;

        bystander.disconnect();

        assertNotNull(plugin.game);
        assertSame(gameBeforeDisconnect, plugin.game, "An unrelated player quitting shouldn't touch the round");
    }
}
