package com.adminspec.capability;

import com.adminspec.AdminSpecMod;
import com.adminspec.spec.MoveContext;
import com.adminspec.spec.Spec;
import com.adminspec.spec.SpecMove;
import com.adminspec.spec.SpecRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-player spec state. Stored as a NeoForge data attachment (see {@link PlayerSpecCapability}).
 *
 * Implements {@link INBTSerializable} so the attachment can be serialized via the
 * {@code AttachmentType.serializable(...)} builder helper.
 */
public final class PlayerSpecData implements INBTSerializable<CompoundTag> {

    private String currentSpecId = null;

    private final Map<String, Boolean> moveToggles = new HashMap<>();
    private final Map<String, Integer> moveCooldowns = new HashMap<>();

    private float reverseFlowCapacity = 1.0f;
    private boolean reverseFlowActive = false;

    private int giantHandCooldown = 0;
    private int yamaSummonCooldown = 0;
    private int heartSwordState = 0;
    private int heartSwordTimer = 0;
    private int heartSwordCooldown = 0;

    private boolean yamaActive = false;

    // ---------------------- Accessors ----------------------

    public String getSpecId() { return currentSpecId; }

    public void setSpecId(String id) {
        if (id == null && currentSpecId != null) {
            moveToggles.clear();
            moveCooldowns.clear();
            reverseFlowActive = false;
            yamaActive = false;
            heartSwordState = 0;
            heartSwordTimer = 0;
        }
        this.currentSpecId = id;
    }

    public boolean isToggleOn(String moveId) { return moveToggles.getOrDefault(moveId, false); }
    public void setToggle(String moveId, boolean on) { moveToggles.put(moveId, on); }

    public int getCooldown(String moveId) { return moveCooldowns.getOrDefault(moveId, 0); }
    public void setCooldown(String moveId, int ticks) { moveCooldowns.put(moveId, Math.max(0, ticks)); }

    public float getReverseFlowCapacity() { return reverseFlowCapacity; }
    public void setReverseFlowCapacity(float v) { this.reverseFlowCapacity = Math.max(0f, Math.min(1f, v)); }
    public boolean isReverseFlowActive() { return reverseFlowActive; }
    public void setReverseFlowActive(boolean v) { this.reverseFlowActive = v; }

    public int getGiantHandCooldown() { return giantHandCooldown; }
    public void setGiantHandCooldown(int v) { this.giantHandCooldown = Math.max(0, v); }

    public int getYamaSummonCooldown() { return yamaSummonCooldown; }
    public void setYamaSummonCooldown(int v) { this.yamaSummonCooldown = Math.max(0, v); }

    public int getHeartSwordState() { return heartSwordState; }
    public void setHeartSwordState(int v) { this.heartSwordState = v; }

    public int getHeartSwordTimer() { return heartSwordTimer; }
    public void setHeartSwordTimer(int v) { this.heartSwordTimer = Math.max(0, v); }

    public int getHeartSwordCooldown() { return heartSwordCooldown; }
    public void setHeartSwordCooldown(int v) { this.heartSwordCooldown = Math.max(0, v); }

    public boolean isYamaActive() { return yamaActive; }
    public void setYamaActive(boolean v) { this.yamaActive = v; }

    // ---------------------- Behavior ----------------------

    public void serverTick(Player player, Spec spec) {
        for (Map.Entry<String, Integer> e : new HashMap<>(moveCooldowns).entrySet()) {
            int v = e.getValue();
            if (v > 0) moveCooldowns.put(e.getKey(), v - 1);
        }

        for (SpecMove m : spec.moves()) {
            m.tick(player);
        }

        if (heartSwordState > 0) {
            if (heartSwordTimer > 0) {
                heartSwordTimer--;
            } else {
                com.adminspec.moves.guyue.FiveFingerFistHeartSwordMove.fireFinger(player, heartSwordState);
                if (heartSwordState < 3) {
                    heartSwordState++;
                    heartSwordTimer = 20;
                } else {
                    heartSwordState = 0;
                    heartSwordTimer = 0;
                    heartSwordCooldown = 200;
                }
            }
        }
    }

    public void activateMove(Player player, int oneBasedIndex, boolean pressed) {
        if (currentSpecId == null) return;
        Spec spec = SpecRegistry.get(currentSpecId);
        if (spec == null) return;
        if (oneBasedIndex < 1 || oneBasedIndex > spec.moves().size()) return;
        SpecMove move = spec.moves().get(oneBasedIndex - 1);
        move.activate(new MoveContext(player, pressed));
    }

    // ---------------------- INBTSerializable ----------------------

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (currentSpecId != null) tag.putString("SpecId", currentSpecId);

        CompoundTag toggles = new CompoundTag();
        for (Map.Entry<String, Boolean> e : moveToggles.entrySet()) {
            toggles.putBoolean(e.getKey(), e.getValue());
        }
        tag.put("Toggles", toggles);

        CompoundTag cds = new CompoundTag();
        for (Map.Entry<String, Integer> e : moveCooldowns.entrySet()) {
            cds.putInt(e.getKey(), e.getValue());
        }
        tag.put("Cooldowns", cds);

        tag.putFloat("ReverseFlowCap", reverseFlowCapacity);
        tag.putBoolean("ReverseFlowActive", reverseFlowActive);
        tag.putBoolean("YamaActive", yamaActive);
        tag.putInt("GiantHandCD", giantHandCooldown);
        tag.putInt("YamaCD", yamaSummonCooldown);
        tag.putInt("HeartSwordState", heartSwordState);
        tag.putInt("HeartSwordTimer", heartSwordTimer);
        tag.putInt("HeartSwordCD", heartSwordCooldown);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag == null) return;
        if (tag.contains("SpecId")) currentSpecId = tag.getString("SpecId");
        if (tag.contains("Toggles")) {
            CompoundTag t = tag.getCompound("Toggles");
            for (String k : t.getAllKeys()) moveToggles.put(k, t.getBoolean(k));
        }
        if (tag.contains("Cooldowns")) {
            CompoundTag c = tag.getCompound("Cooldowns");
            for (String k : c.getAllKeys()) moveCooldowns.put(k, c.getInt(k));
        }
        if (tag.contains("ReverseFlowCap")) reverseFlowCapacity = tag.getFloat("ReverseFlowCap");
        if (tag.contains("ReverseFlowActive")) reverseFlowActive = tag.getBoolean("ReverseFlowActive");
        if (tag.contains("YamaActive")) yamaActive = tag.getBoolean("YamaActive");
        if (tag.contains("GiantHandCD")) giantHandCooldown = tag.getInt("GiantHandCD");
        if (tag.contains("YamaCD")) yamaSummonCooldown = tag.getInt("YamaCD");
        if (tag.contains("HeartSwordState")) heartSwordState = tag.getInt("HeartSwordState");
        if (tag.contains("HeartSwordTimer")) heartSwordTimer = tag.getInt("HeartSwordTimer");
        if (tag.contains("HeartSwordCD")) heartSwordCooldown = tag.getInt("HeartSwordCD");
    }

    public void copyFrom(PlayerSpecData other) {
        this.currentSpecId = other.currentSpecId;
        this.moveToggles.clear();
        this.moveToggles.putAll(other.moveToggles);
        this.moveCooldowns.clear();
        this.moveCooldowns.putAll(other.moveCooldowns);
        this.reverseFlowCapacity = other.reverseFlowCapacity;
        this.reverseFlowActive = other.reverseFlowActive;
        this.yamaActive = other.yamaActive;
        this.giantHandCooldown = other.giantHandCooldown;
        this.yamaSummonCooldown = other.yamaSummonCooldown;
        this.heartSwordState = other.heartSwordState;
        this.heartSwordTimer = other.heartSwordTimer;
        this.heartSwordCooldown = other.heartSwordCooldown;
    }
}
