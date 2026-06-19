/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.SharedSuggestionProvider
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package com.adminspec.command;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.spec.Spec;
import com.adminspec.spec.SpecRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class AdminSpecCommand {
    public static final SuggestionProvider<CommandSourceStack> SPEC_SUGGESTIONS = (ctx, builder) -> SharedSuggestionProvider.suggest(() -> {
        ArrayList<String> ids = new ArrayList<String>();
        for (Spec s : SpecRegistry.all()) {
            ids.add(s.id());
        }
        return ids.iterator();
    }, (SuggestionsBuilder)builder);

    private AdminSpecCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"admin").requires(src -> src.hasPermission(2))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"spec").then(Commands.literal((String)"set").then(((RequiredArgumentBuilder)Commands.argument((String)"specname", (ArgumentType)StringArgumentType.string()).suggests(SPEC_SUGGESTIONS).executes(AdminSpecCommand::setSpecSelf)).then(Commands.argument((String)"player", (ArgumentType)StringArgumentType.string()).executes(AdminSpecCommand::setSpecOther))))).then(Commands.literal((String)"clear").executes(AdminSpecCommand::clearSpec))).then(Commands.literal((String)"list").executes(AdminSpecCommand::listSpecs))));
    }

    private static int setSpecSelf(CommandContext<CommandSourceStack> ctx) {
        Entity entity = ((CommandSourceStack)ctx.getSource()).getEntity();
        if (!(entity instanceof ServerPlayer)) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.literal((String)"Run this as a player."));
            return 0;
        }
        ServerPlayer player = (ServerPlayer)entity;
        return AdminSpecCommand.applySpec(ctx, player, StringArgumentType.getString(ctx, (String)"specname"));
    }

    private static int setSpecOther(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, (String)"player");
        String specName = StringArgumentType.getString(ctx, (String)"specname");
        ServerPlayer target = ((CommandSourceStack)ctx.getSource()).getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.literal((String)("Player not found: " + playerName)));
            return 0;
        }
        return AdminSpecCommand.applySpec(ctx, target, specName);
    }

    private static int applySpec(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String specName) {
        Spec old;
        Spec spec = SpecRegistry.get(specName);
        if (spec == null) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.literal((String)("Unknown spec: " + specName + ". Use /admin spec list to see registered specs.")));
            return 0;
        }
        PlayerSpecData data = PlayerSpecCapability.get((Player)player);
        if (data.getSpecId() != null && (old = SpecRegistry.get(data.getSpecId())) != null) {
            old.onRemoved((Player)player);
        }
        data.setSpecId(spec.id());
        data.endSwordEscape();
        ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)("\u00a7d[Admin Spec] \u00a7aGranted spec \u00a7f" + spec.displayName().getString() + "\u00a7a to \u00a7f" + player.getDisplayName().getString())), true);
        player.sendSystemMessage((Component)Component.literal((String)("\u00a7d[Admin Spec] \u00a7aYou have been granted the spec: " + spec.displayName().getString())));
        player.sendSystemMessage((Component)Component.literal((String)"\u00a77Move bound to a key \u2014 see Options \u2192 Controls \u2192 Admin Spec."));
        return 1;
    }

    private static int clearSpec(CommandContext<CommandSourceStack> ctx) {
        Entity entity = ((CommandSourceStack)ctx.getSource()).getEntity();
        if (!(entity instanceof ServerPlayer)) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.literal((String)"Run this as a player."));
            return 0;
        }
        ServerPlayer player = (ServerPlayer)entity;
        PlayerSpecData data = PlayerSpecCapability.get((Player)player);
        if (data.getSpecId() == null) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.literal((String)"You have no spec active."));
            return 0;
        }
        Spec old = SpecRegistry.get(data.getSpecId());
        if (old != null) {
            old.onRemoved((Player)player);
        }
        String oldName = old != null ? old.displayName().getString() : data.getSpecId();
        data.setSpecId(null);
        ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)("\u00a7d[Admin Spec] \u00a7cCleared spec \u00a7f" + oldName)), true);
        return 1;
    }

    private static int listSpecs(CommandContext<CommandSourceStack> ctx) {
        ArrayList<String> names = new ArrayList<String>();
        for (Spec s : SpecRegistry.all()) {
            names.add(s.id());
        }
        ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)("\u00a7d[Admin Spec] \u00a7aRegistered specs: \u00a7f" + String.join((CharSequence)", ", names))), false);
        return names.size();
    }
}

