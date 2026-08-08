package io.github.tejedu.manhunt;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManHuntTest {

    private ServerMock server;
    private ManHunt plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(ManHunt.class);
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
    void onEnableLoadsConfigDefaults() {
        assertEquals(4, plugin.getConfig().getInt("round.minplayers"));
        assertEquals(900, plugin.getConfig().getInt("round.maxlength"));
    }

    @Test
    void mhTargetWithNoActiveGameSaysSo() {
        PlayerMock asker = server.addPlayer();

        asker.performCommand("mhtarget");

        assertTrue(asker.nextMessage().contains("no target"));
    }

    @Test
    void mhTargetWithAnActiveGameNamesTheTarget() {
        plugin.getConfig().set("round.minplayers", 1);
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);
        plugin.createGame();
        PlayerMock asker = server.addPlayer("Asker");

        asker.performCommand("mhtarget");

        assertTrue(asker.nextMessage().contains("Huntee"));
    }

    @Test
    void mhCommandByTheTargetMarksThemVerified() {
        plugin.getConfig().set("round.minplayers", 1);
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);
        plugin.createGame();
        assertFalse(plugin.game.targetVerified);

        target.performCommand("mh");

        assertTrue(plugin.game.targetVerified);
    }

    @Test
    void mhCommandByANonTargetIsIgnored() {
        plugin.getConfig().set("round.minplayers", 1);
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);
        plugin.createGame();
        PlayerMock bystander = server.addPlayer("Bystander");

        bystander.performCommand("mh");

        assertFalse(plugin.game.targetVerified);
    }

    @Test
    void mhStartRequiresAdminPermission() {
        plugin.getConfig().set("round.minplayers", 1);
        PlayerMock nonAdmin = server.addPlayer();
        nonAdmin.setOp(false);
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);

        nonAdmin.performCommand("mhstart 0");
        server.getScheduler().performTicks(20);

        assertNull(plugin.game, "A non-admin shouldn't be able to force-start a round");
    }

    @Test
    void mhStartAsAdminQueuesAnImmediateRound() {
        plugin.getConfig().set("round.minplayers", 1);
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);
        admin.setGameMode(GameMode.CREATIVE); // ineligible as a target, so Huntee is picked deterministically
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);

        admin.performCommand("mhstart 0");
        server.getScheduler().performTicks(20);

        assertEquals(target, plugin.game.target);
    }

    @Test
    void prizeCommandRequiresARecipientArgument() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        admin.performCommand("prize");

        assertTrue(admin.nextMessage().contains("recepient"));
    }

    @Test
    void prizeCommandForAPlayerWhoHasNeverJoinedFails() {
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        admin.performCommand("prize NobodyEver");

        assertTrue(admin.nextMessage().contains("never logged in"));
    }

    @Test
    void prizeCommandForAnOnlinePlayerAwardsAPrize() {
        plugin.getConfig().set("prizes.enchrate", -1); // deterministic plain-item prize (0 still has a 1% enchant chance)
        PlayerMock admin = server.addPlayer();
        admin.setOp(true);
        PlayerMock recipient = server.addPlayer("Lucky");

        admin.performCommand("prize Lucky");

        assertTrue(admin.nextMessage().contains("Lucky"));
        assertTrue(hasAnyItem(recipient), "The named player should have received a prize");
    }

    @Test
    void prizeCommandRequiresAdminPermission() {
        plugin.getConfig().set("prizes.enchrate", -1);
        PlayerMock nonAdmin = server.addPlayer();
        nonAdmin.setOp(false);
        PlayerMock recipient = server.addPlayer("Lucky");

        nonAdmin.performCommand("prize Lucky");

        assertFalse(hasAnyItem(recipient), "A non-admin shouldn't be able to hand out prizes");
    }

    @Test
    void getPlayerByNameFindsAnOnlinePlayerCaseInsensitively() {
        server.addPlayer("Someone");

        assertEquals("Someone", plugin.getPlayerByName("someone").getName());
    }

    @Test
    void getPlayerByNameReturnsNullForAnUnknownPlayer() {
        assertNull(plugin.getPlayerByName("NeverJoined"));
    }
}
