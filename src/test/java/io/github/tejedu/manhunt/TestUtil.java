package io.github.tejedu.manhunt;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/**
 * Shared helpers for the MockBukkit-based test suite.
 */
final class TestUtil {

    private TestUtil() {
    }

    /**
     * @return true if the player's inventory contains any non-AIR item, i.e.
     *         they received a prize.
     */
    static boolean hasAnyItem(PlayerMock player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return true;
            }
        }
        return false;
    }
}
