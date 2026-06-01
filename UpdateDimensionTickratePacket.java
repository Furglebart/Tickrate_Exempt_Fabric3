package com.min01.tickrateapi.command;

import java.util.Collection;

import com.min01.tickrateapi.util.TickrateUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SetTickrateCommand 
{
	public static void register(CommandDispatcher<CommandSourceStack> p_214446_)
	{
		p_214446_.register(Commands.literal("setTickrate").requires((p_137777_) -> 
		{
			return p_137777_.hasPermission(2);
		}).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("tickrate", FloatArgumentType.floatArg()).executes((ctx) ->
		{
			return setEntityTickrate(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), FloatArgumentType.getFloat(ctx, "tickrate"));
		})))).then(Commands.literal("dimension").then(Commands.argument("world", DimensionArgument.dimension()).then(Commands.argument("tickrate", FloatArgumentType.floatArg()).executes(ctx -> 
		{
			return setLevelTickrate(ctx.getSource(), DimensionArgument.getDimension(ctx, "world"), FloatArgumentType.getFloat(ctx, "tickrate"));
		})))).then(Commands.literal("exclude").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("exclude", BoolArgumentType.bool()).executes(ctx -> 
		{
			return excludeEntities(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), BoolArgumentType.getBool(ctx, "exclude"));
		})))).then(Commands.literal("area").then(Commands.argument("world", DimensionArgument.dimension()).then(Commands.argument("pos1", Vec3Argument.vec3()).then(Commands.argument("pos2", Vec3Argument.vec3()).then(Commands.argument("tickrate", FloatArgumentType.floatArg()).executes(ctx -> 
		{
			return addTickrateArea(ctx.getSource(), DimensionArgument.getDimension(ctx, "world"), Vec3Argument.getVec3(ctx, "pos1"), Vec3Argument.getVec3(ctx, "pos2"), FloatArgumentType.getFloat(ctx, "tickrate"));
		})))))));
	}
	
	private static int addTickrateArea(CommandSourceStack source, ServerLevel serverLevel, Vec3 pos1, Vec3 pos2, float tickrate)
	{
		TickrateUtil.addTickrateArea(serverLevel.dimension(), new AABB(pos1, pos2), tickrate);
		if(tickrate == 20 && !TickrateUtil.hasDimensionTimer(serverLevel.dimension()))
		{
			source.sendSuccess(() -> Component.literal("Removed tickrate area in " + serverLevel.dimension().location().toString()), true);
		}
		else
		{
			source.sendSuccess(() -> Component.literal("Added new tickrate area in " + serverLevel.dimension().location().toString() + " with " + tickrate + " tickrate"), true);
		}
		return 0;
	}
	
	private static int excludeEntities(CommandSourceStack source, Collection<? extends Entity> entities, boolean exclude)
	{
		for(Entity entity : entities) 
		{
			if(exclude)
			{
				TickrateUtil.excludeEntity(entity, true);
				source.sendSuccess(() -> Component.literal("Excluded " + entity.getDisplayName().getString() + " from dimension tickrate"), true);
			}
			else
			{
				TickrateUtil.includeEntity(entity);
				source.sendSuccess(() -> Component.literal("Included " + entity.getDisplayName().getString() + " from dimension tickrate"), true);
			}
		}
		return entities.size();
	}
	
	private static int setLevelTickrate(CommandSourceStack source, ServerLevel serverLevel, float tickrate) 
	{
		TickrateUtil.setLevelTickrate(serverLevel.dimension(), tickrate);
		if(tickrate == 20)
		{
			source.sendSuccess(() -> Component.literal("Reseted Tickrate of " + serverLevel.dimension().location().toString() + " to " + tickrate), true);
		}
		else
		{
			source.sendSuccess(() -> Component.literal("Changed Tickrate of " + serverLevel.dimension().location().toString() + " to " + tickrate), true);
		}
		return 0;
	}
	
	private static int setEntityTickrate(CommandSourceStack source, Collection<? extends Entity> entities, float tickrate) 
	{
		for(Entity entity : entities) 
		{
			if(tickrate == 20)
			{
				TickrateUtil.resetTickrate(entity);
				source.sendSuccess(() -> Component.literal("Reseted Tickrate of " + entity.getDisplayName().getString()), true);
			}
			else
			{
				TickrateUtil.setBaseTickrate(entity, tickrate);
				source.sendSuccess(() -> Component.literal("Changed Tickrate of " + entity.getDisplayName().getString() + " to " + tickrate), true);
			}
		}
		return entities.size();
	}
}
