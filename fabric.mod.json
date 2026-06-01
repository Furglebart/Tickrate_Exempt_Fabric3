package com.min01.tickrateapi.mixin;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.tickrateapi.config.TimerConfig;
import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;

@Mixin(value = Minecraft.class, priority = -10000)
public class MixinMinecraft
{
    @Nullable
    @Shadow
    public LocalPlayer player;

    @Nullable
    @Shadow
    public ClientLevel level;

    @Shadow
    private volatile boolean pause;

    @Shadow
    private float pausePartialTick;

    @WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"))
    private void renderGame(GameRenderer instance, float partialTick, long nanoTime, boolean renderLevel, Operation<Void> original)
    {
        original.call(instance, Minecraft.class.cast(this).getFrameTime(), nanoTime, renderLevel);
    }

    @WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Timer;advanceTime(J)I"))
    private int advanceTime(Timer instance, long pGameTime, Operation<Integer> original)
    {
        if(this.level != null && this.player != null)
        {
            if(TickrateUtil.hasTimer(this.player))
            {
                CustomTimer playerTimer = TickrateUtil.getTimer(this.player);
                int j = playerTimer.advanceTime(pGameTime);
                playerTimer.advancedTime = j;
                return j;
            }
            if(this.tickrateapi$useDimensionTimer())
            {
                CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(this.level.dimension());
                int j = dimensionTimer.advanceTime(Util.getMillis());
                dimensionTimer.advancedTime = j;
                return j;
            }
        }
        return original.call(instance, pGameTime);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void tickLevel(ClientLevel instance, BooleanSupplier pHasTimeLeft, Operation<Void> original)
    {
        if(this.tickrateapi$useDimensionTimer())
        {
            CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(this.level.dimension());
            for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, dimensionTimer.advancedTime); ++k)
            {
                original.call(instance, pHasTimeLeft);
            }
            return;
        }
        original.call(instance, pHasTimeLeft);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;tick()V"))
    private void tickGameRenderer(GameRenderer instance, Operation<Void> original)
    {
        if(this.tickrateapi$useDimensionTimer())
        {
            CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(this.level.dimension());
            for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, dimensionTimer.advancedTime); ++k)
            {
                original.call(instance);
            }
            return;
        }
        original.call(instance);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;tick()V"))
    private void tickLevelRenderer(LevelRenderer instance, Operation<Void> original)
    {
        if(this.tickrateapi$useDimensionTimer())
        {
            CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(this.level.dimension());
            for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, dimensionTimer.advancedTime); ++k)
            {
                original.call(instance);
            }
            return;
        }
        original.call(instance);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/MusicManager;tick()V"))
    private void tickMusicManager(MusicManager instance, Operation<Void> original)
    {
        if(this.tickrateapi$useDimensionTimer())
        {
            CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(this.level.dimension());
            for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, dimensionTimer.advancedTime); ++k)
            {
                original.call(instance);
            }
            return;
        }
        original.call(instance);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;tick(Z)V"))
    private void tickSoundManager(SoundManager instance, boolean pIsGamePaused, Operation<Void> original)
    {
        if(this.tickrateapi$useDimensionTimer())
        {
            CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(this.level.dimension());
            for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, dimensionTimer.advancedTime); ++k)
            {
                original.call(instance, pIsGamePaused);
            }
            return;
        }
        original.call(instance, pIsGamePaused);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;tick()V"))
    private void tickParticleEngine(ParticleEngine instance, Operation<Void> original)
    {
        if(this.tickrateapi$useDimensionTimer())
        {
            CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(this.level.dimension());
            for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, dimensionTimer.advancedTime); ++k)
            {
                original.call(instance);
            }
            return;
        }
        original.call(instance);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;tick()V"))
    private void tickTextureManager(TextureManager instance, Operation<Void> original)
    {
        if(this.tickrateapi$useDimensionTimer())
        {
            CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(this.level.dimension());
            for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, dimensionTimer.advancedTime); ++k)
            {
                original.call(instance);
            }
            return;
        }
        original.call(instance);
    }

    @Inject(method = "getFrameTime", at = @At("HEAD"), cancellable = true)
    private void modify_getFrameTime(CallbackInfoReturnable<Float> cir)
    {
        if(this.level != null && this.player != null)
        {
            if(TickrateUtil.hasTimer(this.player))
            {
                CustomTimer playerTimer = TickrateUtil.getTimer(this.player);
                cir.setReturnValue(this.pause ? this.pausePartialTick : playerTimer.partialTick);
                return;
            }
            if(this.tickrateapi$useDimensionTimer())
            {
                CustomTimer dimensionTimer = TickrateUtil.getDimensionTimer(this.level.dimension());
                cir.setReturnValue(this.pause ? this.pausePartialTick : dimensionTimer.partialTick);
            }
        }
    }

    @Unique
    private boolean tickrateapi$useDimensionTimer()
    {
        return this.level != null && this.player != null && TickrateUtil.hasDimensionTimer(this.level.dimension()) && !TickrateUtil.isExcluded(this.player);
    }
}
