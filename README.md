ManHunt
=======

A Spigot/Bukkit plugin that runs a game of ManHunt with prizes.

Each round, one online player (in Survival mode) is secretly picked as the
**target**. Everyone else races to find and right-click the target before
time runs out. Whoever captures the target — or the target itself, if it
survives the round and confirms it isn't AFK via `/mh` — wins a randomized
prize (a curated Minecraft item, sometimes enchanted, sometimes a potion
with a real effect).

Commands: `/mh`, `/mhtarget`, `/mhstart [seconds]` (admin), `/prize <target>`
(admin). Round timing, minimum players, and the prize pool are all
configurable in `config.yml`.

Building
--------

The project builds with [Gradle](https://gradle.org/), via the included
wrapper — you don't need Gradle installed, just a JDK.

```sh
./gradlew build
```

This compiles the plugin against the Spigot API jar in `lib/` and produces
`ManHunt-<version>.jar` in the project root, ready to drop into a server's
`plugins/` folder.

Other useful tasks:

```sh
./gradlew jar     # just build the plugin jar, skip checks
./gradlew clean    # remove build/ and the root ManHunt-*.jar
```

### Setting the version number

The plugin's version lives in one place: the `version:` field in
[`plugin.yml`](plugin.yml). The Gradle build reads it from there, so bumping
the version is just:

1. Edit `version:` in `plugin.yml`.
2. Run `./gradlew build` — the output jar name picks up the new version
   automatically (e.g. `ManHunt-1.22.0.jar`).

There's nothing to keep in sync in `build.gradle` — it derives its version
from `plugin.yml` at build time.

Version history
----------------
* v1.21.0
  * Updated to Java 21 / Spigot 1.21, added 1.21 materials, removed reliance
    on deprecated code, migrated the build from Ant to Gradle
* v1.19.1
  * removed spawn eggs that slipped in with the 1.19 update
* v1.6.03
    * Lowered limit for Netherite prizes
* v1.6.02
    * Added the /prize command and removed some un-givable prizes
* v1.6.01
    * Added /prize <target> command to give a prize to someone. Admin only.
* v1.6
    * Added 1.15 blocks - mostly bees and honey
    * Added 1.16 blocks - it's a Nether extravaganza!!
* v1.5
    * Added 1.14 blocks
* v1.2
    * if inventory is full, drop the stack at the player's feet
    * added AfkEvent which fires when a player doesn't acknowledge they are the target
* v1.1.2.1
    * Added more potion entries to the prize list - one for each potion type
    * [Bug] - didn't have potions in a used list
* v1.1.2
    * Enchanted prizes can have more than one enchantment on them.
    * Potions and splash potions have effects
* v1.1.1
    * /mhtarget will tell you who the current huntee is and how long you have to find them
* v1.1
    * New items are added. All the new items through 1.12 have been added, including glazed terracotta, shulker shells, purpur blocks, concrete, etc.
    * All items should provide awards in your inventory. In the past, a couple items wouldn't provide the award (e.g., doors) - this has been fixed.
    * Items are split out by realm. Right now we haven't accessed the End, so no End items will be awarded. This is configurable by staff (IOW, it's not automatic, and the choice of staff).
    * Some items have limits on how many will be awarded. For example, no more getting a stack of 42 nether stars or beacons - you'll only get 1-3 now.
    * Skulls will be awarded, but they are always skeleton skulls. Talk to staff to get those swapped out for your chosen type.
* v1.0
    * Decompiled from the ManHunt jar file on the a#m Server

Possible further enhancements:
* actual enchanted books
