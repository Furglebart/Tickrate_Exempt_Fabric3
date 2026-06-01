package com.min01.tickrateapi.exemption;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class PlayerExemptionManager
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<UUID> EXEMPT_UUIDS = new HashSet<>();
    private static final Set<String> EXEMPT_NAMES = new HashSet<>();
    private static final Set<UUID> MANAGED_UUIDS = new HashSet<>();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("tickrateapi-player-exemptions.json");

    private static int tickCounter;

    private PlayerExemptionManager()
    {
    }

    public static void load()
    {
        EXEMPT_UUIDS.clear();
        EXEMPT_NAMES.clear();

        try
        {
            Files.createDirectories(CONFIG_PATH.getParent());
            if(!Files.exists(CONFIG_PATH))
            {
                save();
                return;
            }

            try(Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8))
            {
                Data data = GSON.fromJson(reader, Data.class);
                if(data == null)
                {
                    return;
                }

                if(data.uuids != null)
                {
                    for(String uuid : data.uuids)
                    {
                        try
                        {
                            EXEMPT_UUIDS.add(UUID.fromString(uuid));
                        }
                        catch(IllegalArgumentException ignored)
                        {
                        }
                    }
                }

                if(data.names != null)
                {
                    for(String name : data.names)
                    {
                        if(name != null && !name.isBlank())
                        {
                            EXEMPT_NAMES.add(normalizeName(name));
                        }
                    }
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void save()
    {
        try
        {
            Files.createDirectories(CONFIG_PATH.getParent());
            Data data = new Data();
            data.uuids = EXEMPT_UUIDS.stream().map(UUID::toString).sorted().toList();
            data.names = EXEMPT_NAMES.stream().sorted().toList();
            try(Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8))
            {
                GSON.toJson(data, writer);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean add(ServerPlayer player)
    {
        boolean changed = EXEMPT_UUIDS.add(player.getUUID());
        changed |= EXEMPT_NAMES.add(normalizeName(player.getGameProfile().getName()));
        if(changed)
        {
            save();
        }
        applyToPlayer(player);
        return changed;
    }

    public static boolean addName(String name)
    {
        if(name == null || name.isBlank())
        {
            return false;
        }
        boolean changed = EXEMPT_NAMES.add(normalizeName(name));
        if(changed)
        {
            save();
        }
        return changed;
    }

    public static boolean remove(ServerPlayer player)
    {
        boolean changed = EXEMPT_UUIDS.remove(player.getUUID());
        changed |= EXEMPT_NAMES.remove(normalizeName(player.getGameProfile().getName()));
        if(changed)
        {
            save();
        }
        clearAppliedExemption(player);
        return changed;
    }

    public static boolean removeName(String name)
    {
        if(name == null || name.isBlank())
        {
            return false;
        }
        boolean changed = EXEMPT_NAMES.remove(normalizeName(name));
        if(changed)
        {
            save();
        }
        return changed;
    }

    public static int clear(MinecraftServer server)
    {
        int count = EXEMPT_UUIDS.size() + EXEMPT_NAMES.size();
        EXEMPT_UUIDS.clear();
        EXEMPT_NAMES.clear();
        save();
        for(ServerPlayer player : server.getPlayerList().getPlayers())
        {
            clearAppliedExemption(player);
        }
        return count;
    }

    public static void applyToOnlinePlayers(MinecraftServer server)
    {
        for(ServerPlayer player : server.getPlayerList().getPlayers())
        {
            applyToPlayer(player);
        }
    }

    public static void refreshOnlinePlayers(MinecraftServer server)
    {
        for(ServerPlayer player : server.getPlayerList().getPlayers())
        {
            if(isExempt(player))
            {
                applyToPlayer(player);
            }
            else
            {
                clearAppliedExemption(player);
            }
        }
    }

    public static void serverTick(MinecraftServer server)
    {
        tickCounter++;
        if(tickCounter >= 20)
        {
            tickCounter = 0;
            applyToOnlinePlayers(server);
        }
    }

    public static void applyToPlayer(ServerPlayer player)
    {
        if(!isExempt(player))
        {
            return;
        }

        MANAGED_UUIDS.add(player.getUUID());
        ITickrateCapability cap = TickrateCapabilityImpl.get(player);
        if(!cap.isExcluded())
        {
            cap.exclude(true);
        }
        if(!cap.shouldExcludeSubEntities())
        {
            cap.excludeSubEntities(true);
        }
        if(cap.hasTimer() || cap.getBaseTimer().tickrate != 20.0F || cap.getCurrentTimer().tickrate != 20.0F)
        {
            cap.resetTickrate();
        }
    }

    private static void clearAppliedExemption(ServerPlayer player)
    {
        if(isExempt(player))
        {
            return;
        }
        if(!MANAGED_UUIDS.remove(player.getUUID()))
        {
            return;
        }

        ITickrateCapability cap = TickrateCapabilityImpl.get(player);
        if(cap.isExcluded())
        {
            cap.exclude(false);
        }
        if(cap.shouldExcludeSubEntities())
        {
            cap.excludeSubEntities(false);
        }
    }

    public static boolean isExempt(Entity entity)
    {
        if(entity instanceof Player player)
        {
            return EXEMPT_UUIDS.contains(player.getUUID()) || EXEMPT_NAMES.contains(normalizeName(player.getGameProfile().getName()));
        }
        return false;
    }

    public static Collection<String> describeEntries()
    {
        List<String> entries = new ArrayList<>();
        EXEMPT_NAMES.stream()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .map(name -> "name:" + name)
            .forEach(entries::add);
        EXEMPT_UUIDS.stream()
            .sorted(Comparator.comparing(UUID::toString))
            .map(uuid -> "uuid:" + uuid)
            .forEach(entries::add);
        return entries;
    }

    public static int size()
    {
        return EXEMPT_UUIDS.size() + EXEMPT_NAMES.size();
    }

    public static Path getConfigPath()
    {
        return CONFIG_PATH;
    }

    private static String normalizeName(String name)
    {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private static final class Data
    {
        @SuppressWarnings("unused")
        String comment = "Players listed here are forced to normal 20 TPS behavior for Tickrate API dimension, area, and entity tickrate changes.";
        List<String> uuids = new ArrayList<>();
        List<String> names = new ArrayList<>();
    }
}
