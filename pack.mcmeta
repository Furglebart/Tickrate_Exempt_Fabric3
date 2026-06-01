package com.min01.tickrateapi.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.config.TimerConfig;
import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(value = Level.class, priority = -10000)
public class MixinLevel
{
    @Inject(method = "guardEntityTick", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void guardEntityTick(Consumer<T> pConsumerEntity, T pEntity, CallbackInfo ci)
    {
        Level level = (Level) (Object) this;
        CustomTimer entityTimer = TickrateUtil.getTimer(pEntity);
        CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(level.dimension());
        if(level.isClientSide)
        {
            if(!(pEntity instanceof Player))
            {
                if(TickrateUtil.hasTimer(pEntity))
                {
                    ci.cancel();
                    int j = entityTimer.advanceTime(Util.getMillis());
                    for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, j); ++k)
                    {
                        this.tickrateapi$entityTick(pConsumerEntity, pEntity);
                    }
                }
                else if(TickrateUtil.hasDimensionTimer(level.dimension()) && !TickrateUtil.isExcluded(pEntity))
                {
                    ci.cancel();
                    for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, dimensionTimer.advancedTime); ++k)
                    {
                        this.tickrateapi$entityTick(pConsumerEntity, pEntity);
                    }
                }
            }
        }
        else
        {
            if(TickrateUtil.hasTimer(pEntity))
            {
                ci.cancel();
                long currentTime = Util.getMillis();
                long tickrate = (long) (1000L / entityTimer.tickrate);

                long i = currentTime - entityTimer.nextTickTime;
                if(i > 2000L && entityTimer.nextTickTime - entityTimer.lastOverloadWarning >= 15000L)
                {
                    long j = i / tickrate;
                    entityTimer.nextTickTime += j * tickrate;
                    entityTimer.lastOverloadWarning = entityTimer.nextTickTime;
                }

                while(currentTime >= entityTimer.nextTickTime)
                {
                    entityTimer.nextTickTime += tickrate;
                    this.tickrateapi$entityTick(pConsumerEntity, pEntity);
                }

                entityTimer.mayHaveDelayedTasks = true;
                entityTimer.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + tickrate, entityTimer.nextTickTime);
            }
            else if(TickrateUtil.hasDimensionTimer(level.dimension()) && TickrateUtil.isExcluded(pEntity))
            {
                ci.cancel();
            }
        }
    }

    @Unique
    private <T extends Entity> void tickrateapi$entityTick(Consumer<T> pConsumerEntity, T pEntity)
    {
        try
        {
            pConsumerEntity.accept(pEntity);
        }
        catch(Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking entity");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being ticked");
            pEntity.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }
}
