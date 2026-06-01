package com.min01.tickrateapi;

import com.min01.tickrateapi.command.SetTickrateCommand;
import com.min01.tickrateapi.command.TickrateExemptCommand;
import com.min01.tickrateapi.config.TimerConfig;
import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.exemption.PlayerExemptionManager;
import com.min01.tickrateapi.network.UpdateTickratePacket;
import com.min01.tickrateapi.util.TickrateUtil;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.MinecraftServer;

public class TickrateAPI implements ModInitializer
{
    public static final String MODID = "tickrateapi";

    private static MinecraftServer currentServer;

    @Override
    public void onInitialize()
    {
        TimerConfig.load();
        PlayerExemptionManager.load();
        TickrateNetwork.registerMessages();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
        {
            SetTickrateCommand.register(dispatcher);
            TickrateExemptCommand.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            currentServer = server;
            PlayerExemptionManager.load();
            PlayerExemptionManager.applyToOnlinePlayers(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
        {
            if(currentServer == server)
            {
                currentServer = null;
            }
            TickrateUtil.EXCLUDED.clear();
            PlayerExemptionManager.save();
        });

        ServerTickEvents.START_WORLD_TICK.register(TickrateUtil::onLevelTickStart);
        ServerTickEvents.END_SERVER_TICK.register(PlayerExemptionManager::serverTick);
        ServerWorldEvents.LOAD.register((server, world) -> TickrateUtil.syncDimensionData(world));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
        {
            TickrateUtil.syncAllDimensionData(handler.player);
            PlayerExemptionManager.applyToPlayer(handler.player);
        });
        EntityTrackingEvents.START_TRACKING.register((entity, player) ->
        {
            ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
            if(cap.hasTimer() || cap.isExcluded() || cap.shouldExcludeSubEntities() || !cap.shouldChangeSubEntities() || cap.getBaseTimer().tickrate != 20.0F)
            {
                TickrateNetwork.sendToPlayer(player, new UpdateTickratePacket(entity.getUUID(), cap.isExcluded(), cap.shouldExcludeSubEntities(), cap.shouldChangeSubEntities(), cap.getBaseTimer().tickrate, cap.getTickrate()));
            }
        });
    }

    public static MinecraftServer getCurrentServer()
    {
        return currentServer;
    }
}
