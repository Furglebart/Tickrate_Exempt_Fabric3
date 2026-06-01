package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.ITickrateEntity;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.exemption.PlayerExemptionManager;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class MixinEntity implements ITickrateEntity
{
    @Unique
    private final ITickrateCapability tickrateapi$capability = new TickrateCapabilityImpl((Entity) (Object) this);

    @Override
    public ITickrateCapability tickrateapi$getTickrateCapability()
    {
        return this.tickrateapi$capability;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci)
    {
        Entity entity = (Entity) (Object) this;
        ITickrateCapability cap = TickrateCapabilityImpl.get(entity);
        if(PlayerExemptionManager.isExempt(entity))
        {
            if(cap.hasTimer() || cap.getBaseTimer().tickrate != 20.0F || cap.getCurrentTimer().tickrate != 20.0F || !cap.isExcluded())
            {
                cap.resetTickrate();
                cap.exclude(true);
            }
            return;
        }

        if(TickrateUtil.getTickrate(entity) > 1)
        {
            cap.tick();
        }

        float tickrate = TickrateUtil.getArea(entity.level().dimension(), entity.getBoundingBox(), entity.position());
        if(tickrate != 20.0F || TickrateUtil.hasDimensionTimer(entity.level().dimension()))
        {
            TickrateUtil.setTickrate(entity, tickrate);
        }
    }

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void saveTickrateData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir)
    {
        tag.put(ITickrateCapability.ID.toString(), this.tickrateapi$capability.serializeNBT());
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void loadTickrateData(CompoundTag tag, CallbackInfo ci)
    {
        if(tag.contains(ITickrateCapability.ID.toString()))
        {
            this.tickrateapi$capability.deserializeNBT(tag.getCompound(ITickrateCapability.ID.toString()));
        }
    }
}
