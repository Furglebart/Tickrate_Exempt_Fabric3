package com.min01.tickrateapi.network;

import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class UpdateDimensionTickratePacket
{
    private final ResourceKey<Level> dimension;
    private final float tickrate;

    public UpdateDimensionTickratePacket(ResourceKey<Level> dimension, float tickrate)
    {
        this.dimension = dimension;
        this.tickrate = tickrate;
    }

    public static UpdateDimensionTickratePacket read(FriendlyByteBuf buf)
    {
        return new UpdateDimensionTickratePacket(buf.readResourceKey(Registries.DIMENSION), buf.readFloat());
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeResourceKey(this.dimension);
        buf.writeFloat(this.tickrate);
    }

    public static void handle(UpdateDimensionTickratePacket message)
    {
        if(message.tickrate == 20.0F)
        {
            TickrateUtil.LEVEL_MAP.remove(message.dimension);
        }
        else
        {
            TickrateUtil.LEVEL_MAP.put(message.dimension, new CustomTimer(message.tickrate, 0L));
        }
    }
}
