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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

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

    private String drainOne(PlayerMock player) {
        String message = player.nextMessage();
        assertNotNull(message, "Expected a message but none was sent");
        return message;
    }

    @Test
    void notEnoughPlayersLeavesTargetUnset() {
        // default round.minplayers is 4; only add one eligible player
        PlayerMock lonely = server.addPlayer();
        lonely.setGameMode(GameMode.SURVIVAL);

        Game game = new Game(plugin);

        assertNull(game.target);
    }

    @Test
    void picksAnEligibleSurvivalPlayerAsTarget() {
        plugin.getConfig().set("round.minplayers", 1);
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);

        Game game = new Game(plugin);

        assertEquals(target, game.target);
        assertFalse(game.targetVerified);
        assertTrue(game.roundEnd > System.currentTimeMillis());
    }

    @Test
    void ignoresCreativePlayersWhenSurvivalOnlyIsEnabled() {
        plugin.getConfig().set("round.minplayers", 1);
        plugin.getConfig().set("round.survivalplayersonly", true);
        PlayerMock creative = server.addPlayer("Builder");
        creative.setGameMode(GameMode.CREATIVE);

        Game game = new Game(plugin);

        assertNull(game.target);
    }

    @Test
    void includesCreativePlayersWhenSurvivalOnlyIsDisabled() {
        plugin.getConfig().set("round.minplayers", 1);
        plugin.getConfig().set("round.survivalplayersonly", false);
        PlayerMock creative = server.addPlayer("Builder");
        creative.setGameMode(GameMode.CREATIVE);

        Game game = new Game(plugin);

        assertEquals(creative, game.target);
    }

    @Test
    void targetIsPromptedToVerifyWhenAfkNotAllowed() {
        plugin.getConfig().set("round.minplayers", 1);
        plugin.getConfig().set("dev.allowafk", false);
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);

        new Game(plugin);

        // first message is the server-wide "Man Hunt has started!" broadcast
        drainOne(target);
        assertTrue(drainOne(target).contains("/mh"));
    }

    @Test
    void captureAwardsThePrizeToTheCapturerAndRequeues() {
        plugin.getConfig().set("round.minplayers", 1);
        plugin.getConfig().set("prizes.enchrate", -1); // deterministic plain-item prize (0 still has a 1% enchant chance)
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);
        Game game = new Game(plugin);
        // consume the round-start broadcast and the "/mh" prompt sent to the
        // target during setup
        drainOne(target);
        drainOne(target);

        PlayerMock capturer = server.addPlayer("Hunter");

        game.capture(capturer);

        assertTrue(drainOne(capturer).contains("captured"));
        assertTrue(drainOne(target).contains("captured"));

        boolean gotAPrize = false;
        for (ItemStack item : capturer.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                gotAPrize = true;
                break;
            }
        }
        assertTrue(gotAPrize, "Capturer's inventory should contain the awarded prize");
    }

    @Test
    void verifiedTargetSurvivingReceivesAPrize() {
        plugin.getConfig().set("round.minplayers", 1);
        plugin.getConfig().set("dev.allowafk", false);
        plugin.getConfig().set("prizes.enchrate", -1); // deterministic plain-item prize (0 still has a 1% enchant chance)
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);
        Game game = new Game(plugin);
        game.targetVerified = true;

        game.survive();

        boolean gotAPrize = false;
        for (ItemStack item : target.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                gotAPrize = true;
                break;
            }
        }
        assertTrue(gotAPrize, "Target's inventory should contain the survival prize");
    }

    @Test
    void unverifiedTargetSurvivingFiresAfkEventInsteadOfAPrize() {
        plugin.getConfig().set("round.minplayers", 1);
        plugin.getConfig().set("dev.allowafk", false);
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);
        Game game = new Game(plugin);
        // game.targetVerified left false - never ran /mh

        final boolean[] fired = {false};
        server.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onAfk(AfkEvent event) {
                fired[0] = true;
                assertEquals(target, event.getPlayer());
            }
        }, plugin);

        game.survive();

        assertTrue(fired[0], "AfkEvent should have fired for an unverified target");

        boolean gotAPrize = false;
        for (ItemStack item : target.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                gotAPrize = true;
                break;
            }
        }
        assertFalse(gotAPrize, "An unverified target shouldn't receive a survival prize");
    }

    @Test
    void devAllowAfkGrantsPrizeEvenWithoutVerification() {
        plugin.getConfig().set("round.minplayers", 1);
        plugin.getConfig().set("dev.allowafk", true);
        plugin.getConfig().set("prizes.enchrate", -1); // deterministic plain-item prize (0 still has a 1% enchant chance)
        PlayerMock target = server.addPlayer("Huntee");
        target.setGameMode(GameMode.SURVIVAL);
        Game game = new Game(plugin);
        // game.targetVerified left false

        game.survive();

        boolean gotAPrize = false;
        for (ItemStack item : target.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                gotAPrize = true;
                break;
            }
        }
        assertTrue(gotAPrize, "dev.allowafk should let an unverified target still get a prize");
    }

    @Test
    void getTimeLeftFormatsSeveralMinutesAsPlural() {
        Game game = new Game();
        // :30 safety margin so the integer-division floor in getTimeLeft()
        // can't flake if a few ms elapse between setting roundEnd and
        // calling getTimeLeft()
        game.roundEnd = System.currentTimeMillis() + java.time.Duration.ofSeconds(5 * 60 + 30).toMillis();

        assertEquals("about 5 minutes", game.getTimeLeft());
    }

    @Test
    void getTimeLeftFormatsAboutOneMinute() {
        Game game = new Game();
        game.roundEnd = System.currentTimeMillis() + java.time.Duration.ofSeconds(90).toMillis();

        assertEquals("about a minute", game.getTimeLeft());
    }

    @Test
    void getTimeLeftFormatsUnderAMinute() {
        Game game = new Game();
        game.roundEnd = System.currentTimeMillis() + java.time.Duration.ofSeconds(30).toMillis();

        assertEquals("less than a minute", game.getTimeLeft());
    }

    @Test
    void getTimeLeftHandlesAnAlreadyEndedRound() {
        Game game = new Game();
        game.roundEnd = System.currentTimeMillis() - java.time.Duration.ofSeconds(10).toMillis();

        assertEquals("less than a minute", game.getTimeLeft());
    }
}
