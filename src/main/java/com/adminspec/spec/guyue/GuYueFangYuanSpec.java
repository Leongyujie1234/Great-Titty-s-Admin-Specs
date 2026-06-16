package com.adminspec.spec.guyue;

import com.adminspec.moves.guyue.EmperorYamaMove;
import com.adminspec.moves.guyue.FiveFingerFistHeartSwordMove;
import com.adminspec.moves.guyue.GiantHandMove;
import com.adminspec.moves.guyue.ReverseFlowProtectionSealMove;
import com.adminspec.spec.Spec;
import com.adminspec.spec.SpecRegistry;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Gu Yue Fang Yuan (Rank 7) — the first spec in the mod.
 *
 * Move layout:
 *   1. Reverse Flow Protection Seal  (toggle)
 *   2. Giant Hand
 *   3. Emperor Yama                   (toggle + summon)
 *   4. Five Finger Fist Heart Sword
 */
public final class GuYueFangYuanSpec {

    public static final String ID = "gu_yue_fang_yuan";

    private GuYueFangYuanSpec() {}

    public static void register() {
        Spec spec = new Spec(
                ID,
                Component.literal("\u00a7dGu Yue Fang Yuan \u00a77(Rank 7)"),
                Component.literal("The thousand-year-old demonic Gu Master. Four moves: Reverse Flow Protection Seal, Giant Hand, Emperor Yama, Five Finger Fist Heart Sword."),
                List.of(
                        new ReverseFlowProtectionSealMove(),
                        new GiantHandMove(),
                        new EmperorYamaMove(),
                        new FiveFingerFistHeartSwordMove()
                )
        );
        SpecRegistry.register(spec);
    }
}
