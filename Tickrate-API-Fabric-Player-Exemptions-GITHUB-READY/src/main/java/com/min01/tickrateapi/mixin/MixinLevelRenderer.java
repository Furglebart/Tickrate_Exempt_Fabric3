package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer 
{
	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
	private void renderEntity(LevelRenderer instance, Entity pEntity, double pCamX, double pCamY, double pCamZ, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, Operation<Void> original)
	{
		if(TickrateUtil.hasTimer(pEntity))
		{
			CustomTimer timer = TickrateUtil.getTimer(pEntity);
			original.call(instance, pEntity, pCamX, pCamY, pCamZ, timer.partialTick, pPoseStack, pBufferSource);
			return;
		}
		original.call(instance, pEntity, pCamX, pCamY, pCamZ, pPartialTick, pPoseStack, pBufferSource);
	}
}
