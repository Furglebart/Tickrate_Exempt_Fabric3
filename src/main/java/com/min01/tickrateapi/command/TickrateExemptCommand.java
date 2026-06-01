package com.min01.tickrateapi.command;

import java.util.Collection;

import com.min01.tickrateapi.exemption.PlayerExemptionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class TickrateExemptCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("tickrateExempt")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("add")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> addPlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("remove")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> removePlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("addName")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> addName(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("removeName")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> removeName(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("list")
                .executes(ctx -> list(ctx.getSource())))
            .then(Commands.literal("reload")
                .executes(ctx -> reload(ctx.getSource())))
            .then(Commands.literal("clear")
                .executes(ctx -> clear(ctx.getSource()))));
    }

    private static int addPlayer(CommandSourceStack source, ServerPlayer player)
    {
        boolean changed = PlayerExemptionManager.add(player);
        source.sendSuccess(() -> Component.literal((changed ? "Added " : "Already exempt: ") + player.getGameProfile().getName()), true);
        return 1;
    }

    private static int removePlayer(CommandSourceStack source, ServerPlayer player)
    {
        boolean changed = PlayerExemptionManager.remove(player);
        source.sendSuccess(() -> Component.literal((changed ? "Removed " : "Was not exempt: ") + player.getGameProfile().getName()), true);
        return changed ? 1 : 0;
    }

    private static int addName(CommandSourceStack source, String name)
    {
        boolean changed = PlayerExemptionManager.addName(name);
        source.sendSuccess(() -> Component.literal((changed ? "Added offline/name exemption for " : "Already exempt by name: ") + name), true);
        MinecraftServer server = source.getServer();
        PlayerExemptionManager.applyToOnlinePlayers(server);
        return changed ? 1 : 0;
    }

    private static int removeName(CommandSourceStack source, String name)
    {
        boolean changed = PlayerExemptionManager.removeName(name);
        source.sendSuccess(() -> Component.literal((changed ? "Removed offline/name exemption for " : "No name exemption found for ") + name), true);
        MinecraftServer server = source.getServer();
        PlayerExemptionManager.refreshOnlinePlayers(server);
        return changed ? 1 : 0;
    }

    private static int list(CommandSourceStack source)
    {
        Collection<String> entries = PlayerExemptionManager.describeEntries();
        if(entries.isEmpty())
        {
            source.sendSuccess(() -> Component.literal("No tickrate-exempt players are configured."), false);
            return 0;
        }

        String message = "Tickrate-exempt players (" + entries.size() + "): " + String.join(", ", entries);
        source.sendSuccess(() -> Component.literal(message), false);
        return entries.size();
    }

    private static int reload(CommandSourceStack source)
    {
        PlayerExemptionManager.load();
        PlayerExemptionManager.refreshOnlinePlayers(source.getServer());
        source.sendSuccess(() -> Component.literal("Reloaded tickrate exemptions from " + PlayerExemptionManager.getConfigPath()), true);
        return PlayerExemptionManager.size();
    }

    private static int clear(CommandSourceStack source)
    {
        int removed = PlayerExemptionManager.clear(source.getServer());
        source.sendSuccess(() -> Component.literal("Cleared " + removed + " tickrate exemption entries."), true);
        return removed;
    }
}
