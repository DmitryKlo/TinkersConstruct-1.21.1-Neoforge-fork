Tconstrict 1.21.1 unofficial NeoForge port
==========================================

This repository is an unofficial Minecraft 1.21.1 NeoForge port based on the SlimeKnights Tinkers' Construct codebase.

Upstream project: https://github.com/SlimeKnights/TinkersConstruct

This port was developed with the help of Codex and Cursor agents.

This repository is not an official Slime Knights release and is not affiliated with or endorsed by Slime Knights. The goal is to preserve the Tinkers' Construct gameplay loop for testing and development on Minecraft 1.21.1.

Status
------

The port is in active beta. The current work focuses on restoring the core gameplay systems and fixing porting regressions found during in-game testing:

- smeltery, foundry, melter, heater, casting table, casting basin, tanks, drains, faucets, channels, and fuel handling
- melting recipes for vanilla and TConstruct materials, including ore/raw material flows and recipe reload coverage
- alloy creation inside smeltery-style tanks using the loaded TConstruct alloy recipes
- casting recipes, cast/table interactions, partial fluid transfer during casting, and cooling state handling
- tool materials, tool definitions, modifiers, item capabilities, and representative combat/mining interactions
- inventory and container synchronization for smeltery, casting, tables, stations, and tool-related menus
- UI fixes for fuel tanks, molten fluid lists, tank fill indicators, slot alignment, and fluid amount display
- rendering fixes for block entity fluids, casting table contents, cast/item transforms, fuel tanks, and several texture/model edge cases
- JEI and Jade runtime compatibility for development and recipe/block inspection

Installation
------------

Use Java 21 and NeoForge for Minecraft 1.21.1.

Ready-to-install jars are kept in this repository:

- [tconstruct-1.21.1-3.11.2-port.0.jar](dist/mods/tconstruct-1.21.1-3.11.2-port.0.jar)
- [mantle-1.21.1-1.12.0-port.0.jar](dist/mods/mantle-1.21.1-1.12.0-port.0.jar)
- [jei-1.21.1-neoforge-19.21.0.247.jar](dist/mods/jei-1.21.1-neoforge-19.21.0.247.jar)
- [jade-15.10.5+neoforge runtime jar](dist/mods/nvQzSEkH-yd8FKCmx.jar)

Install the jars from `dist/mods` into a Minecraft 1.21.1 NeoForge `mods` directory.

To build installable jars and runtime companion mods:

```bash
./gradlew installModJars
```

The task writes the current mod jars and configured runtime mod jars to:

```text
build/install/mods
```

The generated directory contains the local TConstruct/Mantle jars plus the configured JEI and Jade runtime jars used for testing.

Development
-----------

Run the automated GameTest suite:

```bash
./gradlew runGameTestServer
```

Run a development client:

```bash
./gradlew runClient
```

Testing feedback
----------------

Please open GitHub issues for crashes, broken mechanics, incorrect recipes, rendering problems, missing translations, server/client desyncs, and other regressions.

Useful issue details:

- Minecraft, NeoForge, Mantle, and mod versions
- client or dedicated server
- crash report or latest.log
- exact reproduction steps
- screenshots or short videos for rendering/UI bugs

License
-------

This port is distributed under the MIT License. See LICENSE.
