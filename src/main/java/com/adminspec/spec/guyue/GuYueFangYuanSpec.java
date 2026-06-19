/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.adminspec.spec.guyue;

import com.adminspec.moves.guyue.AncientSwordDragonTransformationMove;
import com.adminspec.moves.guyue.ReverseFlowProtectionSealMove;
import com.adminspec.moves.guyue.SwordEscapeMove;
import com.adminspec.moves.guyue.YamaChildrenMove;
import com.adminspec.spec.Spec;
import com.adminspec.spec.SpecRegistry;
import java.util.List;
import net.minecraft.network.chat.Component;

public final class GuYueFangYuanSpec {
    public static final String ID = "gu_yue_fang_yuan";

    private GuYueFangYuanSpec() {
    }

    public static void register() {
        Spec spec = new Spec(ID, (Component)Component.literal((String)"\u00a7dLiu Guan Yi \u00a77(Rank 7)"), (Component)Component.literal((String)"The thousand-year-old demonic Gu Master. Moves: Sword Escape, Reverse Flow Protection Seal, Ancient Sword Dragon Transformation."), List.of(new SwordEscapeMove(), new ReverseFlowProtectionSealMove(), new AncientSwordDragonTransformationMove(), new YamaChildrenMove()));
        SpecRegistry.register(spec);
    }
}

