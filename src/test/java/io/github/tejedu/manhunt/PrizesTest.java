package io.github.tejedu.manhunt;

import org.bukkit.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sanity checks on the static prize data tables. These don't need a mock
 * server since they're just plain Material lists/maps.
 */
class PrizesTest {

    @BeforeAll
    static void setUp() {
        Prizes.setPrizeLimits();
    }

    @Test
    void overworldPrizesIsNotEmpty() {
        assertFalse(Prizes.overworldPrizes.isEmpty());
    }

    @Test
    void netherPrizesIsNotEmpty() {
        assertFalse(Prizes.netherPrizes.isEmpty());
    }

    @Test
    void endPrizesIsNotEmpty() {
        assertFalse(Prizes.endPrizes.isEmpty());
    }

    @Test
    void overworldEnchantableListIsNotEmpty() {
        assertFalse(Prizes.overworldEnchantableList.isEmpty());
    }

    @Test
    void netherEnchantableListIsNotEmpty() {
        assertFalse(Prizes.netherEnchantableList.isEmpty());
    }

    @Test
    void noPrizeListsContainNulls() {
        for (List<Material> list : List.of(
                Prizes.overworldPrizes,
                Prizes.netherPrizes,
                Prizes.endPrizes,
                Prizes.overworldEnchantableList,
                Prizes.netherEnchantableList)) {
            assertFalse(list.contains(null));
        }
    }

    /**
     * Material.POTION is intentionally repeated many times in overworldPrizes
     * to weight the odds of drawing a potion (so each potion effect gets a
     * chance to show up) - see the v1.1.2.1 changelog entry. Every other
     * material should appear at most once per list.
     */
    @Test
    void prizeListsHaveNoUnintentionalDuplicates() {
        for (List<Material> list : List.of(
                Prizes.overworldPrizes,
                Prizes.netherPrizes,
                Prizes.endPrizes,
                Prizes.overworldEnchantableList,
                Prizes.netherEnchantableList)) {
            Set<Material> seen = new HashSet<>();
            for (Material material : list) {
                if (material == Material.POTION) {
                    continue;
                }
                assertTrue(seen.add(material), material + " appears more than once in a prize list");
            }
        }
    }

    @Test
    void setPrizeLimitsPopulatesTheLimitsMap() {
        assertFalse(Prizes.prizeLimits.isEmpty());
    }

    @Test
    void allPrizeLimitsArePositive() {
        for (Integer limit : Prizes.prizeLimits.values()) {
            assertNotNull(limit);
            assertTrue(limit > 0, "Prize limit should be positive, was " + limit);
        }
    }

    @Test
    void knownRarePrizesHaveLimits() {
        // A handful of the rarer prizes that must never be handed out in
        // full stacks - regression check for the "no more 42 nether stars"
        // fix from v1.1.
        assertTrue(Prizes.prizeLimits.containsKey(Material.NETHER_STAR));
        assertTrue(Prizes.prizeLimits.containsKey(Material.BEACON));
        assertTrue(Prizes.prizeLimits.containsKey(Material.NETHERITE_INGOT));
        assertTrue(Prizes.prizeLimits.containsKey(Material.NETHERITE_BLOCK));
    }
}
