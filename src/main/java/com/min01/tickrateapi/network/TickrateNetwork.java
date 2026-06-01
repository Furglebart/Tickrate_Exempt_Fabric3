package com.min01.tickrateapi.network;

import com.min01.tickrateapi.TickrateAPI;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class TickrateNetwork
{
    public static final ResourceLocation UPDATE_TICKRATE = new ResourceLocation(TickrateAPI.MODID, "update_tickrate");
    public static final ResourceLocation UPDATE_DIMENSION_TICKRATE = new ResourceLocation(TickrateAPI.MODID, "update_dimension_tickrate");
    public static final ResourceLocation UPDATE_AREA_TICKRATE = new ResourceLocation(TickrateAPI.MODID, "update_area_tickrate");

    public static void registerMessages()
    {
        // Server-to-client packet ids are registered by Fabric when the packets are sent.
        // Client receivers are installed from TickrateAPIClient to avoid loading client-only classes on a dedicated server.
    }

    public static void sendToAll(UpdateTickratePacket message)
    {
        if(TickrateAPI.getCurrentServer() == null)
        {
            return;
        }
        for(ServerPlayer player : TickrateAPI.getCurrentServer().getPlayerList().getPlayers())
        {
            FriendlyByteBuf buf = PacketByteBufs.create();
            message.write(buf);
            ServerPlayNetworking.send(player, UPDATE_TICKRATE, buf);
        }
    }

    public static void sendToAll(UpdateDimensionTickratePacket message)
    {
        if(TickrateAPI.getCurrentServer() == null)
        {
            return;
        }
        for(ServerPlayer player : TickrateAPI.getCurrentServer().getPlayerList().getPlayers())
        {
            FriendlyByteBuf buf = PacketByteBufs.create();
            message.write(buf);
            ServerPlayNetworking.send(player, UPDATE_DIMENSION_TICKRATE, buf);
        }
    }

    public static void sendToAll(UpdateAreaTickratePacket message)
    {
        if(TickrateAPI.getCurrentServer() == null)
        {
            return;
        }
        for(ServerPlayer player : TickrateAPI.getCurrentServer().getPlayerList().getPlayers())
        {
            FriendlyByteBuf buf = PacketByteBufs.create();
            message.write(buf);
            ServerPlayNetworking.send(player, UPDATE_AREA_TICKRATE, buf);
        }
    }

    public static void sendToPlayer(ServerPlayer player, UpdateDimensionTickratePacket message)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.write(buf);
        ServerPlayNetworking.send(player, UPDATE_DIMENSION_TICKRATE, buf);
    }

    public static void sendToPlayer(ServerPlayer player, UpdateAreaTickratePacket message)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.write(buf);
        ServerPlayNetworking.send(player, UPDATE_AREA_TICKRATE, buf);
    }

    public static void sendToPlayer(ServerPlayer player, UpdateTickratePacket message)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.write(buf);
        ServerPlayNetworking.send(player, UPDATE_TICKRATE, buf);
    }

    public static void sendToTrackingAndSelf(Entity entity, UpdateTickratePacket message)
    {
        if(entity.level().isClientSide)
        {
            return;
        }
        for(ServerPlayer player : PlayerLookup.tracking(entity))
        {
            FriendlyByteBuf buf = PacketByteBufs.create();
            message.write(buf);
            ServerPlayNetworking.send(player, UPDATE_TICKRATE, buf);
        }
        if(entity instanceof ServerPlayer player)
        {
            FriendlyByteBuf buf = PacketByteBufs.create();
            message.write(buf);
            ServerPlayNetworking.send(player, UPDATE_TICKRATE, buf);
        }
    }
}
