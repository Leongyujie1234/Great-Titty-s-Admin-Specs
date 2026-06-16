package com.adminspec.spec;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side registry of all Specs by id.
 * Specs register their moves here during commonSetup.
 */
public final class SpecRegistry {

    private static final Map<String, Spec> SPECS = new HashMap<>();

    private SpecRegistry() {}

    public static void register(Spec spec) {
        if (SPECS.containsKey(spec.id())) {
            throw new IllegalStateException("Duplicate spec id: " + spec.id());
        }
        SPECS.put(spec.id(), spec);
    }

    public static Spec get(String id) {
        return SPECS.get(id);
    }

    public static int size() {
        return SPECS.size();
    }

    public static Iterable<Spec> all() {
        return SPECS.values();
    }

    /** Called once during FMLCommonSetupEvent. */
    public static void registerDefaults() {
        com.adminspec.spec.guyue.GuYueFangYuanSpec.register();
    }
}
