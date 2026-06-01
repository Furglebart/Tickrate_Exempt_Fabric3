package com.min01.tickrateapi.network;

import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.util.TickrateUtil;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public class TickrateClientNetwork
{
    public static void registerReceivers()
    {
        ClientPlayNetworking.registerGlobalReceiver(TickrateNetwork.UPDATE_TICKRATE, (client, handler, buf, responseSender) ->
        {
            UpdateTickratePacket packet = UpdateTickratePacket.read(buf);
            client.execute(() -> handleUpdateTickrate(packet));
        });
        ClientPlayNetworking.registerGlobalReceiver(TickrateNetwork.UPDATE_DIMENSION_TICKRATE, (client, handler, buf, responseSender) ->
        {
            UpdateDimensionTickratePacket packet = UpdateDimensionTickratePacket.read(buf);
            client.execute(() -> UpdateDimensionTickratePacket.handle(packet));
        });
        ClientPlayNetworking.registerGlobalReceiver(TickrateNetwork.UPDATE_AREA_TICKRATE, (client, handler, buf, responseSender) ->
        {
            UpdateAreaTickratePacket packet = UpdateAreaTickratePacket.read(buf);
            client.execute(() -> UpdateAreaTickratePacket.handle(packet));
        });
    }

    private static void handleUpdateTickrate(UpdateTickratePacket message)
    {
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null)
        {
            return;
        }
        Entity entity = TickrateUtil.getEntityByUUID(level, message.uuid);
        if(entity == null)
        {
            return;
        }
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        cap.exclude(message.excluded);
        cap.excludeSubEntities(message.excludeSubEntities);
        cap.changeSubEntities(message.shouldChangeSubEntities);
        cap.setBaseTickrate(message.baseTickrate);
        cap.setTickrate(message.currentTickrate);
    }
}
