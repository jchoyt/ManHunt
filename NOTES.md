# ManHunt — Repo Notes

_Last reviewed: 2026-08-07_

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
- Current version: **1.21.0** (targets Minecraft/Spigot API 1.21, Java 17)
- Built with **Gradle** (via the wrapper — `./gradlew build`), compiling
  against `lib/spigot-api-1.21-R0.1-SNAPSHOT.jar` (compile-only; not bundled)
  → produces `ManHunt-<version>.jar` in the project root, bundling
  `plugin.yml` and `config.yml`. (Migrated from Ant's `build.xml` on
  2026-08-07 — see "Build system" below.)
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
- Compiles targeting **Java 21 bytecode** via `options.release = 21` on
  `JavaCompile` (not `sourceCompatibility`/a toolchain) — this lets any modern
  JDK cross-compile without needing an actual JDK 21 install, and avoids both
  a deprecated-API warning and toolchain auto-provisioning/network dependency.
  21 was chosen (bumped up from Java 17 originally) because Minecraft/Spigot
  1.20.5+ (which includes 1.21) requires a Java 21+ JVM to run the server at
  all, so there's no benefit to targeting older bytecode, and Gradle 9.7.0
  fully supports it.
- `lib/spigot-api-1.21-R0.1-SNAPSHOT.jar` is depended on as `compileOnly`
  (files() dependency) since Spigot provides that API at runtime — it's
  correctly excluded from the built plugin jar. `mavenCentral()` and
  Spigot's own repo (hub.spigotmc.org) are declared in `repositories{}` for
  future dependency additions/API jar upgrades, though nothing currently
  resolves from them.
- `jar` task output: base name `ManHunt`, version `1.21.0`, written to the
  **project root** (not the Gradle-default `build/libs/`) to match the old
  Ant behavior — i.e. `./ManHunt-1.21.0.jar`.
- Common commands:
  - `./gradlew build` — compile + assemble + run checks (also verified this
    works with the box's default JDK 25 with no `JAVA_HOME` override needed).
  - `./gradlew clean` — removes `build/` and the root `ManHunt-*.jar`.
  - `./gradlew jar` — just build the plugin jar.
- `.gitignore` updated to ignore `.gradle/` and `build/`; `build.xml` was
  deleted along with the (now-regenerated) Ant `build/` output dir.

## Loose ends / things to check if working on this again

- Untracked files in working tree: `121update.txt` (changelog, now read into
  these notes), `ManHunt-1.21.0.jar` (build artifact), `ManHunt.code-workspace`.
- No `endEnchantableList` — enchanted End-realm prizes can't currently be
  generated even if `includeEnd` is true and an End material is drawn.
- Mixed license headers (MIT in `Prizes.java`, Apache 2.0 in `AfkEvent.java`)
  vs. root `LICENSE` — worth reconciling if this ever gets republished.
