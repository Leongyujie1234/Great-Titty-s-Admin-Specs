package com.adminspec.command;

import com.adminspec.capability.PlayerSpecCapability;
import com.adminspec.capability.PlayerSpecData;
import com.adminspec.spec.Spec;
import com.adminspec.spec.SpecRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * /admin spec set <specname>   - grants spec to self
 * /admin spec set <specname> <player>
 * /admin spec clear            - removes current spec
 * /admin spec list             - lists registered specs
 *
 * Requires permission level 2 (op).
 */
public final class AdminSpecCommand {

    private AdminSpecCommand() {}

    public static final SuggestionProvider<CommandSourceStack> SPEC_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    () -> {
                        java.util.List<String> ids = new java.util.ArrayList<>();
                        for (Spec s : SpecRegistry.all()) ids.add(s.id());
                        return ids.iterator();
                    }, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("admin")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("spec")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("specname", StringArgumentType.string())
                                                .suggests(SPEC_SUGGESTIONS)
                                                .executes(AdminSpecCommand::setSpecSelf)
                                                .then(Commands.argument("player", StringArgumentType.string())
                                                        .executes(AdminSpecCommand::setSpecOther))))
                                .then(Commands.literal("clear")
                                        .executes(AdminSpecCommand::clearSpec))
                                .then(Commands.literal("list")
                                        .executes(AdminSpecCommand::listSpecs)))
        );
    }

    private static int setSpecSelf(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("Run this as a player."));
            return 0;
        }
        return applySpec(ctx, player, StringArgumentType.getString(ctx, "specname"));
    }

    private static int setSpecOther(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, "player");
        String specName = StringArgumentType.getString(ctx, "specname");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("Player not found: " + playerName));
            return 0;
        }
        return applySpec(ctx, target, specName);
    }

    private static int applySpec(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String specName) {
        Spec spec = SpecRegistry.get(specName);
        if (spec == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown spec: " + specName
                    + ". Use /admin spec list to see registered specs."));
            return 0;
        }

        PlayerSpecData data = PlayerSpecCapability.get(player);

        // If the player already has a spec, call its onRemoved hook.
        if (data.getSpecId() != null) {
            Spec old = SpecRegistry.get(data.getSpecId());
            if (old != null) old.onRemoved(player);
        }

        data.setSpecId(spec.id());

        // Fresh state for the new spec — capacity starts full (per the user's request).
        data.setReverseFlowCapacity(1.0f);
        data.setReverseFlowActive(false);
        data.setYamaActive(false);
        data.setHeartSwordState(0);
        data.setHeartSwordTimer(0);

        ctx.getSource().sendSuccess(
                () -> Component.literal("\u00a7d[Admin Spec] \u00a7aGranted spec \u00a7f" + spec.displayName().getString()
                        + "\u00a7a to \u00a7f" + player.getDisplayName().getString()), true);
        player.sendSystemMessage(Component.literal("\u00a7d[Admin Spec] \u00a7aYou have been granted the spec: "
                + spec.displayName().getString()));
        player.sendSystemMessage(Component.literal("\u00a77Moves bound to keys 1\u20134 (see keybinds menu)."));
        return 1;
    }

    private static int clearSpec(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("Run this as a player."));
            return 0;
        }
        PlayerSpecData data = PlayerSpecCapability.get(player);
        if (data.getSpecId() == null) {
            ctx.getSource().sendFailure(Component.literal("You have no spec active."));
            return 0;
        }
        Spec old = SpecRegistry.get(data.getSpecId());
        if (old != null) old.onRemoved(player);
        String oldName = old != null ? old.displayName().getString() : data.getSpecId();
        data.setSpecId(null);
        ctx.getSource().sendSuccess(
                () -> Component.literal("\u00a7d[Admin Spec] \u00a7cCleared spec \u00a7f" + oldName), true);
        return 1;
    }

    private static int listSpecs(CommandContext<CommandSourceStack> ctx) {
        List<String> names = new ArrayList<>();
        for (Spec s : SpecRegistry.all()) names.add(s.id());
        ctx.getSource().sendSuccess(
                () -> Component.literal("\u00a7d[Admin Spec] \u00a7aRegistered specs: \u00a7f" + String.join(", ", names)), false);
        return names.size();
    }
}
