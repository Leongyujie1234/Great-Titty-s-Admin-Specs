/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 */
package com.adminspec.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public final class ClientDragonFormState {
    private static boolean active = false;
    private static CameraType previousCameraType = null;

    private ClientDragonFormState() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean value) {
        if (active == value) {
            return;
        }
        active = value;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        if (value) {
            previousCameraType = mc.options.getCameraType();
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        } else if (previousCameraType != null) {
            mc.options.setCameraType(previousCameraType);
            previousCameraType = null;
        }
    }
}

