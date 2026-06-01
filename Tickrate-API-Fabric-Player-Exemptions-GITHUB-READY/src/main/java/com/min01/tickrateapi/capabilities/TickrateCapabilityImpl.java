package com.min01.tickrateapi.capabilities;

import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.UpdateTickratePacket;
import com.min01.tickrateapi.util.CustomTimer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class TickrateCapabilityImpl implements ITickrateCapability
{
    private final CustomTimer baseTimer = new CustomTimer(20.0F, 0L);
    private final CustomTimer currentTimer = new CustomTimer(20.0F, 0L);

    private final Entity entity;
    private boolean excluded;
    private boolean excludeSubEntities;
    private boolean shouldChangeSubEntities = true;

    public TickrateCapabilityImpl(Entity entity)
    {
        this.entity = entity;
    }

    public static ITickrateCapability get(Entity entity)
    {
        return ((ITickrateEntity) entity).tickrateapi$getTickrateCapability();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("ChangeSubEntities", this.shouldChangeSubEntities);
        nbt.putBoolean("ExcludeSubEntities", this.excludeSubEntities);
        nbt.putBoolean("Excluded", this.excluded);
        nbt.putFloat("BaseTickrate", this.baseTimer.tickrate);
        nbt.putFloat("CurrentTickrate", this.currentTimer.tickrate);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        this.excluded = nbt.getBoolean("Excluded");
        this.excludeSubEntities = nbt.getBoolean("ExcludeSubEntities");
        this.shouldChangeSubEntities = !nbt.contains("ChangeSubEntities") || nbt.getBoolean("ChangeSubEntities");
        this.baseTimer.setTickrate(nbt.contains("BaseTickrate") ? nbt.getFloat("BaseTickrate") : 20.0F);
        this.currentTimer.setTickrate(nbt.contains("CurrentTickrate") ? nbt.getFloat("CurrentTickrate") : this.baseTimer.tickrate);
    }

    @Override
    public void setBaseTickrate(float tickrate)
    {
        this.baseTimer.setTickrate(tickrate);
        this.sendUpdatePacket();
    }

    @Override
    public void setTickrate(float tickrate)
    {
        this.currentTimer.setTickrate(tickrate);
        this.sendUpdatePacket();
    }

    @Override
    public float getTickrate()
    {
        return this.currentTimer.tickrate;
    }

    @Override
    public boolean hasTimer()
    {
        return this.getTickrate() != 20.0F;
    }

    @Override
    public void resetTickrate()
    {
        this.baseTimer.setTickrate(20.0F);
        this.currentTimer.setTickrate(20.0F);
        this.sendUpdatePacket();
    }

    @Override
    public void tick()
    {
        this.setTickrate(this.baseTimer.tickrate);
    }

    @Override
    public CustomTimer getBaseTimer()
    {
        return this.baseTimer;
    }

    @Override
    public CustomTimer getCurrentTimer()
    {
        return this.currentTimer;
    }

    @Override
    public void exclude(boolean flag)
    {
        this.excluded = flag;
        this.sendUpdatePacket();
    }

    @Override
    public boolean isExcluded()
    {
        return this.excluded;
    }

    @Override
    public void excludeSubEntities(boolean flag)
    {
        this.excludeSubEntities = flag;
        this.sendUpdatePacket();
    }

    @Override
    public void changeSubEntities(boolean flag)
    {
        this.shouldChangeSubEntities = flag;
        this.sendUpdatePacket();
    }

    @Override
    public boolean shouldExcludeSubEntities()
    {
        return this.excludeSubEntities;
    }

    @Override
    public boolean shouldChangeSubEntities()
    {
        return this.shouldChangeSubEntities;
    }

    private void sendUpdatePacket()
    {
        if(!this.entity.level().isClientSide)
        {
            TickrateNetwork.sendToTrackingAndSelf(this.entity, new UpdateTickratePacket(this.entity.getUUID(), this.excluded, this.excludeSubEntities, this.shouldChangeSubEntities, this.baseTimer.tickrate, this.getTickrate()));
        }
    }
}
