package com.min01.tickrateapi.network;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

public class UpdateTickratePacket
{
    public final UUID uuid;
    public final boolean excluded;
    public final boolean excludeSubEntities;
    public final boolean shouldChangeSubEntities;
    public final float baseTickrate;
    public final float currentTickrate;

    public UpdateTickratePacket(UUID uuid, boolean excluded, boolean excludeSubEntities, boolean changeSubEntities, float baseTickrate, float currentTickrate)
    {
        this.uuid = uuid;
        this.excluded = excluded;
        this.excludeSubEntities = excludeSubEntities;
        this.shouldChangeSubEntities = changeSubEntities;
        this.baseTickrate = baseTickrate;
        this.currentTickrate = currentTickrate;
    }

    public static UpdateTickratePacket read(FriendlyByteBuf buf)
    {
        return new UpdateTickratePacket(buf.readUUID(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(), buf.readFloat());
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeUUID(this.uuid);
        buf.writeBoolean(this.excluded);
        buf.writeBoolean(this.excludeSubEntities);
        buf.writeBoolean(this.shouldChangeSubEntities);
        buf.writeFloat(this.baseTickrate);
        buf.writeFloat(this.currentTickrate);
    }
}
