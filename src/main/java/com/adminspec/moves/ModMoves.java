package com.adminspec.moves;

import com.adminspec.AdminSpecMod;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Holds any deferred registers the moves need (currently none, but kept for symmetry
 * and to give the main mod class a stable registration hook).
 */
public final class ModMoves {

    public static final DeferredRegister<?> MOVES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ATTRIBUTE, AdminSpecMod.MOD_ID);

    private ModMoves() {}
}
