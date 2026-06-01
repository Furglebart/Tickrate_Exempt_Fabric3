package com.min01.tickrateapi.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.tickrateapi.config.TimerConfig;
import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(ParticleEngine.class)
public class MixinParticleEngine
{
	private final Map<Particle, CustomTimer> timerMap = new HashMap<>();
	
	@WrapOperation(method = "tickParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;tick()V"))
	private void tickParticle(Particle instance, Operation<Void> original)
	{
		Minecraft mc = Minecraft.getInstance();
		Vec3 pos = new Vec3(instance.x, instance.y, instance.x);
		AABB aabb = new AABB(pos, pos).inflate(1.0F);
		float tickrate = TickrateUtil.getArea(mc.level.dimension(), aabb, pos);
		if(!instance.isAlive())
		{
			this.timerMap.remove(instance);
		}
		if(tickrate != 20.0F)
		{
			CustomTimer timer = this.timerMap.computeIfAbsent(instance, t -> 
			{
				return new CustomTimer(tickrate, 0L);
			});
			int j = timer.advanceTime(Util.getMillis());
			for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, j); ++k)
			{
				original.call(instance);
			}
			return;
		}
		original.call(instance);
	}
}
