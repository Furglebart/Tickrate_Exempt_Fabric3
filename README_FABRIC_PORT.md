# Tickrate API Fabric Port (Minecraft 1.20.1)

This is a best-effort Fabric port of min01's Tickrate API 5.0.2 Forge source for Minecraft 1.20.1.

Target:
- Minecraft: 1.20.1
- Fabric Loader: 0.16.14
- Fabric API: 0.92.6+1.20.1
- Java: 17

What was ported:
- Forge `mods.toml` -> `fabric.mod.json`
- Forge mod initializer -> Fabric `ModInitializer` and `ClientModInitializer`
- Forge commands event -> Fabric command registration callback
- Forge level/client tick events -> Fabric lifecycle tick events
- Forge networking `SimpleChannel` -> Fabric networking API
- Forge capabilities -> Fabric mixin-backed entity data storage
- Forge config -> lightweight `config/tickrate-api.toml` reader/writer
- Forge access transformer -> Fabric access widener
- Existing mixins adjusted to remove Forge-only hooks/classes

Build:
```bash
./gradlew build
```

The output jar should be under:
```text
build/libs/
```

Note: The conversion could not be compiled in the ChatGPT sandbox because the environment could not resolve external Gradle/Fabric Maven hosts. Run the build locally with internet access.
