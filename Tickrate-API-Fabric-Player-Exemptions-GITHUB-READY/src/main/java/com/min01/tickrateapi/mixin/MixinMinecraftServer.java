package com.min01.tickrateapi.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.StreamSupport;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.level.Level;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer
{
    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Shadow
    private ProfilerFiller profiler;

    @Shadow
    private Map<ResourceKey<Level>, long[]> perWorldTickTimes;

    @Shadow
    private int tickCount;

    @Redirect(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private Iterable<ServerLevel> tickrateapi$filterNormalWorldTicks(MinecraftServer instance)
    {
        return StreamSupport.stream(instance.getAllLevels().spliterator(), false)
            .filter(level -> !TickrateUtil.hasDimensionTimer(level.dimension()))
            .toList();
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickServer(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.AFTER))
    private void afterTickServer(CallbackInfo ci)
    {
        for(ServerLevel level : this.levels.values())
        {
            if(!TickrateUtil.hasDimensionTimer(level.dimension()))
            {
                continue;
            }
            this.tickrateapi$tickServerLevel(level);

            List<Entity> excluded = TickrateUtil.EXCLUDED.getOrDefault(level.dimension(), new ArrayList<>());
            for(Entity entity : excluded)
            {
                if(!entity.isRemoved())
                {
                    if(this.tickrateapi$shouldDiscardEntity(entity))
                    {
                        entity.discard();
                    }
                    else
                    {
                        entity.checkDespawn();
                        Entity vehicle = entity.getVehicle();
                        if(vehicle != null)
                        {
                            if(!vehicle.isRemoved() && vehicle.hasPassenger(entity))
                            {
                                return;
                            }
                            entity.stopRiding();
                        }
                        if(!entity.isRemoved() && !(entity instanceof EnderDragonPart))
                        {
                            level.guardEntityTick(level::tickNonPassenger, entity);
                        }
                    }
                }
            }
        }
    }

    @Unique
    private boolean tickrateapi$shouldDiscardEntity(Entity pEntity)
    {
        MinecraftServer server = MinecraftServer.class.cast(this);
        if(server.isSpawningAnimals() || !(pEntity instanceof Animal) && !(pEntity instanceof WaterAnimal))
        {
            return !server.areNpcsEnabled() && pEntity instanceof Npc;
        }
        else
        {
            return true;
        }
    }

    @Unique
    private void tickrateapi$tickServerLevel(ServerLevel serverLevel)
    {
        CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(serverLevel.dimension());

        long currentTime = Util.getMillis();
        long tickrate = (long) (1000L / dimensionTimer.tickrate);

        long i = currentTime - dimensionTimer.nextTickTime;
        if(i > 2000L && dimensionTimer.nextTickTime - dimensionTimer.lastOverloadWarning >= 15000L)
        {
            long j = i / tickrate;
            dimensionTimer.nextTickTime += j * tickrate;
            dimensionTimer.lastOverloadWarning = dimensionTimer.nextTickTime;
        }

        while(currentTime >= dimensionTimer.nextTickTime)
        {
            dimensionTimer.nextTickTime += tickrate;
            BooleanSupplier pHasTimeLeft = dimensionTimer::haveTime;

            long tickStart = Util.getNanos();
            this.profiler.push(() -> serverLevel + " " + serverLevel.dimension().location());

            if(this.tickCount % 20 == 0)
            {
                this.profiler.push("timeSync");
                this.synchronizeTime(serverLevel);
                this.profiler.pop();
            }

            this.profiler.push("tick");

            try
            {
                serverLevel.tick(pHasTimeLeft);
            }
            catch(Throwable throwable)
            {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception ticking world");
                serverLevel.fillReportDetails(crashreport);
                throw new ReportedException(crashreport);
            }

            this.profiler.pop();
            this.perWorldTickTimes.computeIfAbsent(serverLevel.dimension(), k -> new long[100])[this.tickCount % 100] = Util.getNanos() - tickStart;
        }

        dimensionTimer.mayHaveDelayedTasks = true;
        dimensionTimer.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + tickrate, dimensionTimer.nextTickTime);
    }

    @Shadow
    private void synchronizeTime(ServerLevel pLevel)
    {
    }
}
