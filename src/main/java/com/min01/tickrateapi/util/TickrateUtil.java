package com.min01.tickrateapi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;

import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.exemption.PlayerExemptionManager;
import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.UpdateAreaTickratePacket;
import com.min01.tickrateapi.network.UpdateDimensionTickratePacket;
import com.min01.tickrateapi.world.TickrateSavedData;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TickrateUtil
{
    public static final Map<ResourceKey<Level>, CustomTimer> LEVEL_MAP = new HashMap<>();
    public static final List<Pair<AABB, Float>> AABB_LIST = new ArrayList<>();
    public static final Map<ResourceKey<Level>, List<Entity>> EXCLUDED = new ConcurrentHashMap<>();

    public static void onLevelTickStart(Level level)
    {
        if(!level.isClientSide && hasDimensionTimer(level.dimension()))
        {
            List<Entity> list = EXCLUDED.computeIfAbsent(level.dimension(), t -> new ArrayList<>());
            for(Entity entity : getAllEntities(level))
            {
                if(!isExcluded(entity))
                {
                    continue;
                }
                if(hasTimer(entity))
                {
                    continue;
                }
                if(!list.contains(entity))
                {
                    list.add(entity);
                }
            }
            list.removeIf(t -> !isExcluded(t) || hasTimer(t));
            if(!EXCLUDED.isEmpty())
            {
                EXCLUDED.keySet().removeIf(t -> !hasDimensionTimer(t));
            }
        }

        for(Entity entity : getAllEntities(level))
        {
            if(!level.isLoaded(entity.blockPosition()))
            {
                continue;
            }

            if(getTickrate(entity) > 1)
            {
                continue;
            }

            TickrateCapabilityImpl.get(entity).tick();
        }
    }

    public static void syncDimensionData(ServerLevel level)
    {
        TickrateSavedData data = TickrateSavedData.get(level.dimension());
        if(data != null)
        {
            TickrateNetwork.sendToAll(new UpdateDimensionTickratePacket(level.dimension(), data.getTimer().tickrate));
            data.getTickrateAreas().forEach(t -> TickrateNetwork.sendToAll(new UpdateAreaTickratePacket(t.getLeft(), t.getRight())));
        }
    }

    public static void syncAllDimensionData(ServerPlayer player)
    {
        MinecraftServer server = player.getServer();
        if(server == null)
        {
            return;
        }
        for(ServerLevel level : server.getAllLevels())
        {
            TickrateSavedData data = TickrateSavedData.get(level.dimension());
            if(data != null)
            {
                TickrateNetwork.sendToPlayer(player, new UpdateDimensionTickratePacket(level.dimension(), data.getTimer().tickrate));
                data.getTickrateAreas().forEach(t -> TickrateNetwork.sendToPlayer(player, new UpdateAreaTickratePacket(t.getLeft(), t.getRight())));
            }
        }
    }

    public static boolean hasDimensionTimer(ResourceKey<Level> dimension)
    {
        TickrateSavedData data = TickrateSavedData.get(dimension);
        if(data != null)
        {
            return data.getTimer().tickrate != 20.0F;
        }
        return LEVEL_MAP.containsKey(dimension) && LEVEL_MAP.get(dimension).tickrate != 20.0F;
    }

    public static CustomTimer getDimensionTimer(ResourceKey<Level> dimension)
    {
        TickrateSavedData data = TickrateSavedData.get(dimension);
        if(data != null)
        {
            return data.getTimer();
        }
        return LEVEL_MAP.get(dimension);
    }

    public static boolean isExcluded(Entity entity)
    {
        if(PlayerExemptionManager.isExempt(entity))
        {
            return true;
        }
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        return cap.isExcluded();
    }

    public static boolean shouldChangeSubEntities(Entity entity)
    {
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        return cap.shouldChangeSubEntities();
    }

    public static boolean shouldExcludeSubEntities(Entity entity)
    {
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        return cap.shouldExcludeSubEntities();
    }

    public static List<Pair<AABB, Float>> getTickrateAreas(ResourceKey<Level> dimension)
    {
        TickrateSavedData data = TickrateSavedData.get(dimension);
        if(data != null)
        {
            return data.getTickrateAreas();
        }
        return AABB_LIST;
    }

    public static void addTickrateArea(ResourceKey<Level> dimension, AABB aabb, float tickrate)
    {
        TickrateSavedData data = TickrateSavedData.get(dimension);
        if(data != null)
        {
            data.addTickrateArea(aabb, tickrate);
            TickrateNetwork.sendToAll(new UpdateAreaTickratePacket(aabb, tickrate));
        }
    }

    public static void setLevelTickrate(ResourceKey<Level> dimension, float tickrate)
    {
        TickrateSavedData data = TickrateSavedData.get(dimension);
        if(data != null)
        {
            data.setTickrate(tickrate);
            TickrateNetwork.sendToAll(new UpdateDimensionTickratePacket(dimension, tickrate));
        }
    }

    public static void includeEntity(Entity entity)
    {
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        cap.exclude(false);
    }

    public static void excludeEntity(Entity entity)
    {
        excludeEntity(entity, true);
    }

    public static void excludeEntity(Entity entity, boolean excludeSubEntities)
    {
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        cap.exclude(true);
        cap.excludeSubEntities(excludeSubEntities);
    }

    public static void changeSubEntities(Entity entity, boolean changeSubEntities)
    {
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        cap.changeSubEntities(changeSubEntities);
    }

    public static void setBaseTickrate(Entity entity, float tickrate)
    {
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        if(PlayerExemptionManager.isExempt(entity))
        {
            cap.resetTickrate();
            cap.exclude(true);
            return;
        }
        cap.setBaseTickrate(tickrate);
    }

    public static void setTickrate(Entity entity, float tickrate)
    {
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        if(PlayerExemptionManager.isExempt(entity))
        {
            cap.resetTickrate();
            cap.exclude(true);
            return;
        }
        cap.setTickrate(tickrate);
    }

    public static float getTickrate(Entity entity)
    {
        if(PlayerExemptionManager.isExempt(entity))
        {
            return 20.0F;
        }
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        return cap.getTickrate();
    }

    public static void resetTickrate(Entity entity)
    {
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        cap.resetTickrate();
    }

    public static CustomTimer getBaseTimer(Entity entity)
    {
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        return cap.getBaseTimer();
    }

    public static CustomTimer getTimer(Entity entity)
    {
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        return cap.getCurrentTimer();
    }

    public static boolean hasTimer(Entity entity)
    {
        if(PlayerExemptionManager.isExempt(entity))
        {
            return false;
        }
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        return cap.hasTimer();
    }

    public static float getTickRateAt(ResourceKey<Level> dimension, Vec3 pos)
    {
        for(Iterator<Pair<AABB, Float>> itr = getTickrateAreas(dimension).iterator(); itr.hasNext();)
        {
            Pair<AABB, Float> pair = itr.next();
            AABB aabb = pair.getLeft();
            if(aabb.contains(pos))
            {
                return pair.getRight();
            }
        }
        return 20.0F;
    }

    public static float getArea(ResourceKey<Level> dimension, AABB boundingBox, Vec3 pos)
    {
        for(Iterator<Pair<AABB, Float>> itr = getTickrateAreas(dimension).iterator(); itr.hasNext();)
        {
            Pair<AABB, Float> pair = itr.next();
            AABB aabb = pair.getLeft();
            if(aabb.intersects(boundingBox) || aabb.contains(pos))
            {
                return pair.getRight();
            }
        }
        return 20.0F;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Iterable<T> getAllEntities(Level level)
    {
        return (Iterable<T>) level.getEntities().getAll();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T getEntityByUUID(Level level, UUID uuid)
    {
        return (T) level.getEntities().get(uuid);
    }
}
