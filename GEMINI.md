# GEMINI.md

This file provides guidance and project memory for Antigravity when working with code in this repository.

## What this is

ManHunt is a Spigot/Bukkit plugin for Minecraft (Java) running a "ManHunt" minigame with prizes. One online player is secretly picked as the **target**; other players attempt to find and right-click them before time runs out. The capturer (or surviving target) receives a random item prize.

- **Package**: `io.github.tejedu.manhunt`
- **Main class**: `ManHunt` (extends `JavaPlugin`)
- **Author**: jchoyt (GitHub) / Entomo (Minecraft)
- **Current version**: `26.3.0` (targets Minecraft/Spigot API 26.3, Java 25)
- **Build tool**: Gradle via wrapper (`./gradlew`)
- **Output**: `ManHunt-<version>.jar` placed directly in the project root.

---

## Build & Test Commands

Requires **JDK 25** (the Gradle wrapper is 9.7.0; bytecode target is Java 25 via `options.release = 25`).

```bash
./gradlew build   # compile, run unit tests, and package plugin jar into project root
./gradlew test    # run MockBukkit unit test suite
./gradlew jar     # build plugin jar only, skipping tests
./gradlew clean   # clean build directory and remove root ManHunt-*.jar
```

### Running Specific Tests

```bash
# Run a specific test class
./gradlew test --tests "io.github.tejedu.manhunt.GameTest"

# Run a specific test method
./gradlew test --tests "io.github.tejedu.manhunt.GameTest.survive_verifiedTarget_receivesPrize"
```

If `./gradlew` fails because default `java` is newer/unsupported:
```bash
JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 ./gradlew test
```

---

## Versioning & Dependencies

- **Single Source of Truth for Version**: `plugin.yml`'s `version:` field is the source of truth (`version: 26.2.0`). `build.gradle` parses it dynamically with regex. To bump the version, edit `plugin.yml` only.
- **Spigot API**: `org.spigotmc:spigot-api:26.2-R0.1-SNAPSHOT` configured as `compileOnly` from `https://hub.spigotmc.org/nexus/content/repositories/snapshots/`.
- **MockBukkit Test Framework**: Pinned to `org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.115.0` with explicit `io.papermc.paper:paper-api:26.1.2.build.72-stable` dependency from `https://repo.papermc.io/repository/maven-public/`.
- **CI**: `.github/workflows/ci.yml` runs `./gradlew build` on JDK 25 (Temurin) on pushes and pull requests to `master`.

---

## Architecture & Game Flow

1. **Queueing**: `ManHunt.queueGame()` schedules a `StartGameTimer` after `round.delay` seconds (default: 150s).
2. **Start**: `StartGameTimer` -> `ManHunt.createGame()` -> `new Game(this)` -> `Game.setupNewGame()`:
   - Gathers online players with `manhunt.basic` (and Survival mode if `round.survivalplayersonly` is true).
   - Requires at least `round.minplayers` (default: 4); re-queues if insufficient players.
   - Shuffles and selects target, broadcasts game start.
   - Starts `SurviveTimer` for `round.maxlength` seconds (default: 900s / 15m).
   - Prompts target to run `/mh` to verify active presence (unless `dev.allowafk` is true).
3. **Hunting / Capture**: `CaptureListener` listens for `PlayerInteractEntityEvent` (right-click).
   - If player with `manhunt.basic` right-clicks target, `Game.capture()` cancels the survive timer, delivers random prize to capturer, broadcasts winner, and re-queues.
   - If target disconnects mid-game, `PlayerQuitEvent` abandons the round and re-queues.
4. **Survival**: If `SurviveTimer` expires without capture -> `Game.survive()`:
   - If target ran `/mh` (`targetVerified`) or `dev.allowafk` is true, target wins random prize.
   - Otherwise, survival prize is withheld and `AfkEvent` (custom Bukkit event) fires for third-party listeners (e.g. TrollMachine).
   - Re-queues next round either way.

### Commands & Permissions

| Command | Permission | Purpose |
|---|---|---|
| `/mh` | `manhunt.basic` (default: true) | Target confirms non-AFK status; unlocks survival prize eligibility. |
| `/mhtarget` | `manhunt.basic` | Displays current target name and remaining time. |
| `/mhstart [seconds]` | `manhunt.admin` (default: op) | Admin command to force-queue a game with optional delay. |
| `/prize <target>` | `manhunt.admin` | Admin command to manually grant a random prize to a player. |

> [!IMPORTANT]
> Admin command enforcement lives **strictly in `plugin.yml`** via Bukkit's dispatcher check. `ManHunt.onCommand()` contains no in-code permission checks. Any new admin command must have its `permission:` correctly declared in `plugin.yml`.

### Key Source Files

- `src/io/github/tejedu/manhunt/ManHunt.java` — Plugin entry point, command dispatcher, game queueing, configuration loader.
- `src/io/github/tejedu/manhunt/Game.java` — Core round lifecycle, target selection, prize creation, capture/survive resolution.
- `src/io/github/tejedu/manhunt/CaptureListener.java` — Right-click capture listener and target quit listener.
- `src/io/github/tejedu/manhunt/Prizes.java` — Static prize pools (`overworldPrizes`, `netherPrizes`, `endPrizes`, `overworldEnchantableList`, `netherEnchantableList`) and `prizeLimits` stack caps.
- `src/io/github/tejedu/manhunt/StartGameTimer.java` / `SurviveTimer.java` — `BukkitRunnable` delay timers.
- `src/io/github/tejedu/manhunt/AfkEvent.java` — Custom Bukkit event fired when unverified target survives.
- `src/test/java/io/github/tejedu/manhunt/` — MockBukkit test suite (41 tests): `GameTest`, `CaptureListenerTest`, `ManHuntTest`, `PrizesTest`, and helper `TestUtil`.

### Testing Quirks & Gotchas

- **`prizes.enchrate` in tests**: Enchanting an `ItemStack` under MockBukkit throws `NullPointerException` (mock lacks craftDelegate). Setting `enchrate` to `0` still has a 1% chance to enchant (`random.nextInt(100) > enchrate` evaluates `0 > 0` as false). Always configure `prizes.enchrate: -1` in tests requiring deterministic non-enchanted prizes.
- **Message draining**: `Game` startup sends the server-wide "Man Hunt has started!" message to the target *before* the "/mh" prompt. Tests inspecting target messages via `PlayerMock.nextMessage()` must drain the broadcast first.

---

## Follow-up Tasks & Loose Ends

### 1. Minecraft/Spigot 26.3 Update ("Dappled Forest") (Completed)
- `spigot-api:26.3-R0.1-SNAPSHOT` configured in `build.gradle`.
- `version: 26.3.0` and `api-version: 26.3` updated in `plugin.yml`.
- All 26.3 materials (poplar set, red shrub, shelf mushroom, wool/concrete stairs/slabs, cushions, straw bed) verified against official `spigot-api:26.3-R0.1-SNAPSHOT` jar.
- Materials remain commented out in `Prizes.java` until MockBukkit releases `mockbukkit-v26.3` (running with `mockbukkit-v26.2` throws `NoSuchFieldError` on missing enum constants).
- Open question remaining for when MockBukkit 26.3 arrives: whether `STRAW_BED` (single-use item) belongs in the prize pool (colored beds were historically excluded) and assigning `prizeLimits`.

### 2. 26.2 Deferred Materials (Completed)
MockBukkit was updated to `mockbukkit-v26.2:4.117.0` and `paper-api:26.2.build.133-stable`. The sulfur/cinnabar materials (~29 items) and `MUSIC_DISC_BOUNCE` in `Prizes.java` have been uncommented and verified via unit tests.

### 3. Missing Realm Lists & Minor Cleanups
- **End Realm**: `endPrizes` exists, but there is no `endEnchantableList`. If End prizes are enabled and an enchantable End item is drawn, enchanted variant generation is bypassed.
- **Licenses**: Unified under MIT (headers in `Prizes.java` and `AfkEvent.java` reconciled with root `LICENSE`).

---

## Claude Artifacts & Memory Inventory

The following artifacts were originally created or managed by Claude:

| Artifact Path | Description |
|---|---|
| `.claude/settings.local.json` | Project-local Claude Code tool execution permissions (Gradle, Java, Git commands). |
| `NOTES.md` | Comprehensive architectural and migration notes written and maintained by Claude (August 2026). |
| `~/.claude/projects/-home-jchoyt-devel-ManHunt/memory/MEMORY.md` | Claude memory index pointer to `manhunt-26.3-followup.md`. |
| `~/.claude/projects/-home-jchoyt-devel-ManHunt/memory/manhunt-26.3-followup.md` | Claude memory file containing step-by-step instructions for completing the 26.3 upgrade. |
| `~/.claude/projects/-home-jchoyt-devel-ManHunt/5b3ed2cd-1672-40ce-ae2b-b565a1e46794.jsonl` | Claude session transcript for the 26.3 speculative branch work. |
| `.github/workflows/ci.yml` | GitHub Actions CI workflow created with Claude co-authorship. |
| Git commit history | Commits `aa81eef`, `c91f7f8`, `9a5aaae`, `131971f`, `edb44b6`, `a1ba858`, `65e6d28`, `1d8355c` co-authored/created by Claude. |
