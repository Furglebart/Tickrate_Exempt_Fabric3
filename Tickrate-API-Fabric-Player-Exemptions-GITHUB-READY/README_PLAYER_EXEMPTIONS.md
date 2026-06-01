# Tickrate API Fabric 1.20.1 - Player Exemptions

This project is a Fabric 1.20.1 version of Tickrate API with an added persistent player-exemption system.

## Target

- Minecraft: 1.20.1
- Fabric Loader: 0.16.14
- Fabric API: 0.92.6+1.20.1
- Java: 17

## What the exemption system does

Players in the exemption list are forced back to normal 20 TPS behavior for this mod's:

- dimension tickrate changes
- area tickrate changes
- entity/player tickrate changes

The mod reapplies exemptions when a player joins and once per second while the server is running, so exempt players stay excluded even after new tickrate commands are used.

## Commands

All commands require permission level 2.

```mcfunction
/tickrateExempt add <onlinePlayer>
/tickrateExempt remove <onlinePlayer>
/tickrateExempt addName <playerName>
/tickrateExempt removeName <playerName>
/tickrateExempt list
/tickrateExempt reload
/tickrateExempt clear
```

Examples:

```mcfunction
/tickrateExempt add Logan
/tickrateExempt addName Logan
/tickrateExempt list
```

## Config file

The persistent list is stored here after the first run:

```text
.minecraft/config/tickrateapi-player-exemptions.json
```

Example:

```json
{
  "comment": "Players listed here are forced to normal 20 TPS behavior for Tickrate API dimension, area, and entity tickrate changes.",
  "uuids": [],
  "names": ["logan"]
}
```

## Building

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

The output jar will be in:

```text
build/libs/
```

Use the jar that does not contain `sources`, `dev`, or `javadoc` in the filename.

## Important limitation

This exemption system controls tickrate changes made by this Tickrate API port. No standalone Fabric mod can guarantee immunity from every possible tickrate mod, because another mod can change the global server loop or player movement in its own custom way without exposing an API.
