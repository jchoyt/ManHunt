# ManHunt — Repo Notes

_Last reviewed: 2026-08-08_

## What it is

A **Spigot/Bukkit plugin for Minecraft** (Java) that runs a "ManHunt" minigame
with prizes. One online player is secretly picked as the **target**; everyone
else tries to find and right-click them before time runs out. Whoever
captures the target — or the target itself, if it survives — wins a random
item prize.

- Package: `io.github.tejedu.manhunt`
- Main class: `ManHunt` (extends `JavaPlugin`)
- Author: jchoyt (github) / Entomo (minecraft)
- Repo: https://github.com/jchoyt/ManHunt
- Current version: **26.2.0** (targets Minecraft/Spigot API 26.2, Java 25 —
  see "Spigot 26.2 migration" below)
- Built with **Gradle** (via the wrapper — `./gradlew build`), compiling
  against `org.spigotmc:spigot-api:26.2-R0.1-SNAPSHOT` (compile-only, resolved
  from Spigot's snapshot repo; not bundled) → produces `ManHunt-<version>.jar`
  in the project root, bundling `plugin.yml` and `config.yml`. (Migrated from
  Ant's `build.xml` on 2026-08-07 — see "Build system" below.)
- License: MIT (Prizes.java header) / Apache 2.0 (AfkEvent.java header) —
  mixed headers, `LICENSE` file at repo root is the authoritative one to check.

## Game flow

1. **Queueing**: `ManHunt.queueGame()` schedules a `StartGameTimer` after
   `round.delay` seconds (config, default 150s).
2. **Start**: `StartGameTimer` → `ManHunt.createGame()` → `new Game(this)` →
   `Game.setupNewGame()`:
   - Gathers online players with `manhunt.basic` permission (and, if
     `round.survivalplayersonly` is true, only Survival-mode players).
   - Requires at least `round.minplayers` (default 4); otherwise re-queues.
   - Shuffles and picks the first as **target**, broadcasts it.
   - Starts a `SurviveTimer` for `round.maxlength` seconds (default 900 =
     15 min).
   - Tells the target to run `/mh` to prove they're not AFK (unless
     `dev.allowafk`).
3. **Hunting**: `CaptureListener` listens for `PlayerInteractEntityEvent`
   (right-click). If a player with `manhunt.basic` right-clicks the target,
   `Game.capture()` fires:
   - Cancels the survive timer.
   - Messages both players.
   - Awards a random prize to the capturer.
   - Re-queues the next round.
   - If the target logs out mid-round, the round is abandoned and requeued
     (`PlayerQuitEvent` handler).
4. **Survival**: If the timer expires without capture, `SurviveTimer` →
   `Game.survive()`:
   - If the target ran `/mh` during the round (`targetVerified`) or
     `dev.allowafk` is set, they get a prize.
   - Otherwise they're told they survived but didn't verify, and an
     `AfkEvent` custom Bukkit event fires (for other plugins/listeners to
     react to, e.g. logging suspected AFK farming).
   - Re-queues the next round either way.

## Prizes (`Prizes.java` + `Game.randomItemStack()`)

- `Prizes.java` is a big static data file: `overworldPrizes`,
  `overworldEnchantableList`, `netherPrizes`, `netherEnchantableList`,
  `endPrizes` — `List<Material>` allow-lists (with most of Minecraft's
  material list present but commented out — curated by hand), plus
  `prizeLimits: Map<Material,Integer>` capping stack sizes for rare items
  (e.g. nether stars, beacons, netherite) so no one gets 64 of something
  broken.
- Config `prizes.includeOverworld/includeNether/includeEnd` toggles which
  realm's prize pool is active (End material list exists but there's no
  `endEnchantableList`, so enchanted End prizes aren't currently possible).
- `netherPrizes` has `Material.IRON_CHAIN` in place of the old `CHAIN`
  material, which Spigot 26.2 removed/renamed as part of a new copper-chain
  block family (see "Prize list content update for 26.2" below).
- `prizes.enchrate` (%) chance the prize is an enchanted item instead of a
  plain one.
  - Plain items: random amount from the curated list; amount is
    `1 + random(limit or maxStackSize)`. Potions/splash potions get a random
    non-useless `PotionType` effect (excludes water/awkward/mundane/thick).
  - Enchanted items: picks a random enchantable material, then repeatedly
    rolls (weighted, decreasing-probability loop) to add one or more
    non-duplicate enchantments at a random valid level.
- Prize delivery: added to the winner's inventory; if it doesn't fit, it's
  dropped on the ground at their feet. Broadcasts a server-wide message
  naming the winner and prize (title-cased material name).

## Commands (`plugin.yml`)

| Command | Permission | Purpose |
|---|---|---|
| `/mh` | `manhunt.basic` (default: everyone) | Target confirms they're not AFK; unlocks their survival prize eligibility. |
| `/mhtarget` | `manhunt.basic` | Shows current target's name and time remaining. |
| `/mhstart [seconds]` | `manhunt.admin` (default: op) | Admin: force-(re)queue a game, optionally with a custom delay. |
| `/prize <target>` | `manhunt.admin` | Admin: manually award a random prize to an online player (bypasses the game loop — builds a throwaway `Game(this, false)`). |

Both admin commands are gated **only** by their `permission:` declaration
above — `ManHunt.onCommand()` has no in-code permission checks at all.
Bukkit's command dispatcher reads that declaration and checks
`sender.hasPermission(...)` itself before ever calling `onCommand`, so a
non-admin's `/mhstart` or `/prize` never reaches the plugin's code. (An
earlier version of `/mhstart` redundantly re-checked `manhunt.admin` inside
`onCommand`; removed 2026-08-08 to keep enforcement in one place, matching
`/prize`. Deliberate: if you ever add a new admin command, remember its
enforcement lives entirely in `plugin.yml` — get that `permission:` line
right, since there's no code-level backstop.)

## Config (`config.yml`)

```
preferences.chathighlight   # chat color code (0-9,a-f) for highlighted names
round.minplayers            # min online eligible players to start (default 4)
round.delay                 # seconds between round end and next round start (150)
round.maxlength             # seconds target has to survive (900 = 15 min)
round.survivalplayersonly   # only Survival-mode players eligible (true)
prizes.enchrate             # % chance of an enchanted prize (10)
prizes.includeOverworld/includeNether/includeEnd  # which prize pools are active
dev.allowafk                # let AFK (unverified) targets still get survive prize
```

## Source files

- `ManHunt.java` — plugin entrypoint, command handling, config loading, game
  queueing.
- `Game.java` — one round's state/logic: pick target, prize generation,
  capture/survive resolution.
- `CaptureListener.java` — Bukkit event listener for right-click captures and
  target disconnects.
- `Prizes.java` — static prize/material data tables.
- `StartGameTimer.java` / `SurviveTimer.java` — `BukkitRunnable` delay timers
  for round start / round survival deadline.
- `AfkEvent.java` — custom Bukkit `Event`, fired when a target survives
  without having verified via `/mh` (extension point for other plugins).

## History / notes from `121update.txt` (changelog)

- Originally decompiled from a jar running on "the a#m Server" (v1.0).
- Iteratively added items through Minecraft 1.19, split by realm, added
  stack limits for rare prizes, enchanted-item support, potion effects,
  `/mhtarget`, AFK detection (`AfkEvent`), `/prize` admin command,
  full-inventory drop-at-feet handling.
- Noted future idea: actual enchanted books as prizes (not yet implemented).

## Build system (Gradle, migrated from Ant 2026-08-07)

- `build.gradle` / `settings.gradle` + the Gradle Wrapper (`gradlew`,
  `gradlew.bat`, `gradle/wrapper/`) replace the old `build.xml`/Ant setup.
  Wrapper is pinned to **Gradle 9.7.0** — needed because the box's system
  `gradle` (4.4.1) and Gradle 8.x can't run their Groovy build-script engine
  on JDK 25 ("Unsupported class file major version 69"); 9.7.0 can. Always
  build via `./gradlew`, not a bare `gradle`.
- Source isn't in the Maven-standard `src/main/java` layout — `build.gradle`
  points the `main` sourceSet's `java` dir straight at `./src`, and its
  `resources` dir at `.` (root), including only `plugin.yml`/`config.yml`, to
  reproduce the old jar contents exactly.
- Compiles targeting **Java 25 bytecode** via `options.release = 25` on
  `JavaCompile` (not `sourceCompatibility`/a toolchain) — this lets any modern
  JDK cross-compile without needing an actual JDK 25 install, and avoids both
  a deprecated-API warning and toolchain auto-provisioning/network dependency.
  25 was chosen (bumped from 21 during the 26.2 migration, see below) because
  Minecraft/Spigot 26.1+ requires a Java 25+ JVM to run the server at all, so
  there's no benefit to targeting older bytecode, and Gradle 9.7.0 fully
  supports it.
- `spigot-api` is a `compileOnly` Gradle dependency (`org.spigotmc:spigot-api:
  26.2-R0.1-SNAPSHOT`, resolved from Spigot's snapshot repo declared in
  `repositories{}`) since Spigot provides that API at runtime — it's
  correctly excluded from the built plugin jar. Previously a manually
  vendored jar in `lib/`; switched to a real dependency during the 26.2
  migration (see below) since Spigot's repo was already configured.
- `jar` task output: base name `ManHunt`, written to the **project root**
  (not the Gradle-default `build/libs/`) to match the old Ant behavior —
  i.e. `./ManHunt-<version>.jar`.
- **Version number**: `plugin.yml`'s `version:` field is the single source
  of truth — `build.gradle` reads it at build time
  (`file('plugin.yml').readLines().find { it.startsWith('version:') }`)
  rather than duplicating it. To bump the version, edit `plugin.yml` only;
  see README's "Setting the version number" section.
- Common commands:
  - `./gradlew build` — compile + assemble + run checks (also verified this
    works with the box's default JDK 25 with no `JAVA_HOME` override needed).
  - `./gradlew clean` — removes `build/` and the root `ManHunt-*.jar`.
  - `./gradlew jar` — just build the plugin jar.
- `.gitignore` updated to ignore `.gradle/` and `build/`; `build.xml` was
  deleted along with the (now-regenerated) Ant `build/` output dir.

## Unit tests (MockBukkit, added 2026-08-08)

- `src/test/java/io/github/tejedu/manhunt/` (standard `src/test/java` layout,
  unlike main's flat `./src`) holds 4 JUnit 5 test classes, 41 tests total:
  `PrizesTest` (pure data sanity, no mock server), `GameTest` (round setup,
  target selection/eligibility, capture, survive verified/unverified/allowafk,
  `getTimeLeft()` formatting), `CaptureListenerTest` (right-click capture +
  permission gating + target-disconnect requeue), `ManHuntTest` (all four
  commands, including that `/prize` and `/mhstart` genuinely reject non-admins
  — `/prize` has **no in-code permission check at all**, it relies entirely on
  `manhunt.admin` being correctly declared for it in `plugin.yml`, so this
  test is a real regression guard, not just belt-and-suspenders).
- Uses **[MockBukkit](https://github.com/MockBukkit/MockBukkit)**, which
  fakes a whole Bukkit server (`ServerMock`, `PlayerMock`, scheduler, event
  bus, permissions) so plugin code runs close to for-real rather than
  against hand-mocked `ItemStack`/`Location`/etc. As of the 26.2 migration,
  pinned to **`mockbukkit-v26.1.2:4.115.0`** — one drop behind the plugin's
  actual `api-version: 26.2` target, because MockBukkit had no `v26.2` build
  at the time. This version doesn't transitively pull a matching `paper-api`
  (older MockBukkit builds did, via Gradle module metadata; this one
  doesn't), so `paper-api:26.1.2.build.72-stable` is declared explicitly as
  a second `testImplementation` — that exact version came from the
  MockBukkit jar's `META-INF/MANIFEST.MF` `Paper-Version` attribute; check
  it again before bumping either dependency. Also note Paper switched to a
  `<mcversion>.build.<N>-<stability>` version scheme (no more `-R0.1-
  SNAPSHOT`) somewhere along the way.
- The test JVM previously needed pinning to Java 21 (`test { javaLauncher }`)
  to dodge a ByteBuddy proxy-class-injection bug in an older MockBukkit's
  plugin loader on JDK 25 (`NullPointerException: No jar file selected`).
  That bug **doesn't reproduce** with `mockbukkit-v26.1.2:4.115.0` (bundles
  a much newer ByteBuddy, 1.18.11 vs. the old 1.15.x) — confirmed by running
  the suite on JDK 25 repeatedly. `javaLauncher` is now pinned to **25**
  instead, matching production. `foojay-resolver-convention` in
  `settings.gradle` still lets Gradle auto-download that JDK if a machine
  doesn't have one.
- **`prizes.enchrate` gotcha**: several tests need a deterministic
  non-enchanted prize (enchanting an `ItemStack` under MockBukkit throws
  `NullPointerException: ...craftDelegate is null`, since MockBukkit doesn't
  back enchantment mutation the same way a real CraftBukkit server does).
  Setting `prizes.enchrate` to `0` is **not enough** — `Game.randomItemStack()`
  tests `random.nextInt(100) > enchrate`, so `enchrate=0` still hits the
  enchanted branch on a `0` roll (1% of the time) and can flake. Tests use
  `-1` instead, which is always beaten by `nextInt(100)`'s 0–99 range.
- Message-order gotcha in tests: a freshly-created `Game` sends the
  server-wide "Man Hunt has started!" broadcast to the target **before** the
  "/mh to verify" prompt — tests that read the target's queued messages via
  `PlayerMock.nextMessage()` need to drain the broadcast first.

## Spigot 26.2 migration (2026-08-08)

Minecraft/Spigot switched to a `year.release.patch` versioning scheme in
2026 (quarterly drops instead of the old `1.XX` series); this plugin moved
from targeting 1.21 to **26.2** (released June 16, 2026). Plan document, if
still present: `~/.claude/plans/sorted-coalescing-hanrahan.md`.

- `plugin.yml`: `api-version: 1.21` → `26.2`, `version:` → `26.2.0`.
- **Verified via diffing the real 26.2 jar against this codebase** (not
  guessed) that of the 620 unique `Material.*` references anywhere in the
  source, only `Material.CHAIN` (in `Prizes.java`) fails to resolve — see
  the Prizes section above. Everything else this plugin touches (`ChatColor`,
  `GameMode`, `PlayerInteractEntityEvent`, `PlayerQuitEvent`, `BukkitRunnable`,
  `PotionMeta`/`PotionType`, `ItemStack` enchant methods, `Registry.ENCHANTMENT`,
  `Enchantment`, `JavaPlugin` config methods) is unchanged in 26.2's API.
- `lib/` directory (and its vendored `spigot-api-1.21-R0.1-SNAPSHOT.jar`)
  deleted entirely — `spigot-api` is now a normal Gradle `compileOnly`
  dependency (see "Build system" above), so there's nothing to manually
  download and replace on future version bumps.
- Java bytecode target and test JVM both bumped to **25** (see "Build
  system" and "Unit tests" above) — Minecraft 26.1+ requires Java 25 to run
  the server.
- MockBukkit bumped to `mockbukkit-v26.1.2:4.115.0` (closest available; no
  `v26.2` build exists yet) with an explicit `paper-api:26.1.2.build.72-
  stable` test dependency (see "Unit tests" above for why that's needed now
  and wasn't before). All 41 existing tests passed against it without any
  test-code changes needed, and repeated runs (8x) showed no flakiness — the
  one-drop-behind gap didn't turn out to cause any observed friction in
  practice, contrary to the caution flagged when this was planned.

## Prize list content update for 26.2 (2026-08-08)

Follow-up to the Spigot 26.2 migration above, which deliberately left the
prize list's content untouched. This pass adds the new materials introduced
between 1.21.0 and 26.2 to `Prizes.java`, following the file's established
pattern of a version-labeled trailing block per list plus curated
`prizeLimits` entries.

**Important caveat on confidence**: my training knowledge is solid through
roughly the "Happy Ghast / Copper Golem" era (~1.21.6), but 26.1 and 26.2
released after that cutoff. Everything added is a *real* material — verified
against the actual API jars, not invented — but which specific update each
one shipped in, and how "fun" it actually is as a random prize, is inference
from material names rather than first-hand game knowledge. Treat this as a
first-pass draft for review, not a final call.

- Fixed the `CHAIN` gap from the Spigot migration: `netherPrizes` now has
  `Material.IRON_CHAIN` in its place.
- Added, roughly by theme: copper tools/armor/spear (→
  `overworldEnchantableList`, alongside the other gear tiers;
  `NETHERITE_SPEAR` → `netherEnchantableList`); copper accessories/decor
  (chain, bars, chest, lantern, torch, golem statue); a new "nautilus armor"
  tier (→ `overworldPrizes`, netherite variant → `netherPrizes`, matching
  where other netherite gear lives); Resin family (1.21.4 Winter Drop);
  Pale Oak wood set + Pale Garden atmosphere items (`OPEN_EYEBLOSSOM`,
  `CLOSED_EYEBLOSSOM`, `CREAKING_HEART`, `FIREFLY_BUSH`); Bundles (1.21.2)
  and Happy Ghast harnesses (one representative color active each, matching
  the existing `CYAN_WOOL` treatment — the other 15 colors of each are
  omitted rather than individually listed, to avoid a huge low-value diff);
  new wood-type "Shelf" furniture blocks (two representative types active);
  `DRIED_GHAST` and `NETHERITE_HORSE_ARMOR` (→ `netherPrizes`); new music
  discs, including `MUSIC_DISC_BLOCKS` — an old Alpha-era disc that looks to
  have been missed when the file was first built, not new content, but it
  now matches the established 100%-of-discs-active pattern.
- **Deferred (commented out) for a real, mechanical reason, not content
  judgment**: `Material.SULFUR`/`CINNABAR` families (~29 materials) and
  `MUSIC_DISC_BOUNCE` are genuinely exclusive to 26.2 — verified against
  the real API jar — but don't exist in `paper-api:26.1.2`, which is what
  MockBukkit's test double is pinned to (see "Unit tests" above). Referencing
  them breaks `Prizes`'s static initializer at test time with
  `NoSuchFieldError`. They compile and would work fine in the actual shipped
  jar; they're commented out purely so the test suite stays green until
  MockBukkit ships a `v26.2` build. Search `Prizes.java` for "26.2-only" to
  find and uncomment them once that happens.
- Verified every material name used anywhere in `Prizes.java` (699 unique
  references) against the real 26.2 API jar directly — zero typos, zero
  unresolvable references (beyond the intentionally-deferred ones above).
  `PrizesTest`'s existing sanity checks (no nulls, no unintentional
  duplicates, limits positive) all still pass unmodified.

## Speculative 26.3 prize entries (2026-08-31)

Added a commented-out block to the end of `overworldPrizes` for Minecraft
26.3 ("Dappled Forest", unofficially Third Drop 2026) — poplar wood set
(logs/planks/stairs/slab/signs/button/pressure plate/door/fence/fence
gate/trapdoor/shelf/boats/sapling/leaves), red shrub, shelf mushroom, one
representative color each of the new wool stairs/slabs, concrete
stairs/slabs, and cushions (16 colors each), and straw bed.

**This is explicitly unverified, unlike everything else in `Prizes.java`.**
26.3 has not shipped as of this writing — Mojang is targeting September
2026, and as of 2026-08-31 neither `hub.spigotmc.org`'s snapshot repo nor
`repo.papermc.io` have any 26.3 artifact (`spigot-api` tops out at
`26.2-R0.1-SNAPSHOT`, `paper-api` at `26.2.build.121-stable`) — there's no
real API to compile or diff against yet, unlike the careful jar-diffing done
for the 26.2 update above. Material names were inferred from public
snapshot patch notes (snapshots 1–10,
https://minecraft.wiki/w/Java_Edition_26.3) plus this project's established
Bukkit-naming conventions, not confirmed. Snapshot content routinely gets
renamed, reworked, or cut before a stable release, so treat every name in
that block as a guess pending verification.

Deliberately excluded from that block: Abandoned Camp / structure treasure
maps (`FILLED_MAP` instances with embedded location data, not plain
obtainable Materials — same reason ordinary maps were never in this list).

**Do not uncomment any of it** until: a real 26.3 `spigot-api` build exists,
every name is re-verified against that real jar (same process as "Prize
list content update for 26.2" above), and `prizeLimits` entries are chosen
for anything that should be capped. Also worth a call at that point on
whether `STRAW_BED` (a one-time-use throwaway item) fits this list's
spirit — ordinary colored beds were never added here either, which reads
like a deliberate exclusion rather than an oversight.

## Loose ends / things to check if working on this again

- The sulfur/cinnabar materials and `MUSIC_DISC_BOUNCE` commented out above —
  uncomment once MockBukkit ships `v26.2` support (search for "26.2-only").
- The whole 26.3 block (search for "Speculative: Minecraft 26.3") is
  unverified guesswork pending a real 26.3 API release — re-verify against
  a real jar before uncommenting anything, per the section above.
- This prize-list update is a first-pass curation (see caveat above) —
  worth a proper in-game/gameplay review of what got included vs. excluded,
  especially the "pick one representative color/variant" calls for bundles,
  harnesses, and shelves.
- No `endEnchantableList` — enchanted End-realm prizes can't currently be
  generated even if `includeEnd` is true and an End material is drawn.
- Mixed license headers (MIT in `Prizes.java`, Apache 2.0 in `AfkEvent.java`)
  vs. root `LICENSE` — worth reconciling if this ever gets republished.
