# Project Report: AdminSpec Mod — DIO "The World" Particle Issue

## Overview

An AdminSpec mod for **Minecraft 1.21.1 / NeoForge 21.1.172 / Java 21** that ports the **DIO "The World"** stand from **JCraft-EoE/jcraft-eoe** (a JoJo's Bizarre Adventure mod). Contains three special moves (Barrage, Forward Charge, Time Stop), a throwable Knife, and other unrelated content (Liu Guan Yi / "guyue" dragon specs).

**GitHub:** `Leongyujie1234/Fang-Yuans-Admin-Specs`, branch `main`, release `v2`
**JAR:** `build/libs/adminspec-1.0.0.jar` (~1.14 MB, AzureLib 2.3.28 bundled via jarJar)

## The Particle Problem

**No particles are spawned during any DIO move — Barrage, Charge, or Time Stop.** The moves are visually silent. There is zero visual feedback when:
- The stand punches during barrage
- The stand charges forward
- Time stop activates/deactivates
- The stand entity appears or despawns

## Files Involved

### DIO Move Files (need particle code added):

| File | Lines | Purpose |
|---|---|---|
| `src/main/java/com/adminspec/moves/dio/DioBarrageMove.java` | 69 | Barrage: fires every 3 ticks for 40 ticks, AABB hit-scan, 280t cooldown |
| `src/main/java/com/adminspec/moves/dio/DioChargeMove.java` | 83 | Charge: 7t windup, stand lunges 7.5m, 19t duration, 100t cooldown |
| `src/main/java/com/adminspec/moves/dio/DioTimeStopMove.java` | 66 | Time Stop: 45t windup, 80t freeze, 1400t cooldown, sends overlay packet |
| `src/main/java/com/adminspec/moves/dio/DioStandState.java` | 133 | Shared state: cooldown maps, ActiveTimestop class, serverTick() freeze loop |
| `src/main/java/com/adminspec/entity/TheWorldStandEntity.java` | 133 | GeoEntity mob: animation controller, follower logic, `playAnimation()` |

### Reference Files (working particle patterns to copy):

| File | Lines | Pattern |
|---|---|---|
| `src/main/java/com/adminspec/moves/guyue/AncientSwordDragonTransformationMove.java` | ~200 | **Primary reference.** Server `sendParticles()` + client-side `DragonBreathVfxPayload` for dense visual effects |
| `src/main/java/com/adminspec/network/DragonBreathVfxPayload.java` | ~60 | Client-side particle payload: spawns dense beam particles |
| `src/main/java/com/adminspec/moves/guyue/ReverseFlowProtectionSealMove.java` | ~80 | Per-tick ambient particles (marker + ring + aura) |
| `src/main/java/com/adminspec/moves/guyue/SwordEscapeMove.java` | ~70 | Dash trail particles + burst particles |
| `src/main/java/com/adminspec/network/ModPayloads.java` | ~50 | Payload registration: register new payloads here |
| `src/main/java/com/adminspec/client/ClientSetup.java` | ~100 | Client payload handlers registered here |

### Currently Working Particle Patterns in This Codebase

The guyue dragon moves use **two tiers** of particle effects:

**Tier 1: Server-side `sendParticles()`** — visible to all players, called from `tick()`:
```java
sl.sendParticles(ParticleTypes.EXPLOSION, x, y, z, count, dx, dy, dz, speed);
sl.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
sl.sendParticles(ParticleTypes.CRIT, x, y, z, count, spreadX, spreadY, spreadZ, speed);
sl.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), scale), x, y, z, count, dx, dy, dz, speed);
```

**Tier 2: Client-side `CustomPacketPayload`** — dense/complex VFX sent from server to client:
1. Define a payload record class (e.g., `DragonBreathVfxPayload`)
2. Register in `ModPayloads.java`
3. Register handler in `ClientSetup.java`
4. Send via `PacketDistributor.sendToPlayer()` / `sendToPlayersNear()`
5. Handler calls `level.addParticle()` client-side for dense effects

### Particle Types Used in This Codebase (all vanilla, no custom registrations needed)

- `ParticleTypes.EXPLOSION` — burst markers
- `ParticleTypes.FLASH` — bright flash
- `ParticleTypes.CRIT` — crit spark
- `ParticleTypes.ENCHANTED_HIT` — magic spark
- `ParticleTypes.SWEEP_ATTACK` — sweep slash
- `ParticleTypes.ELECTRIC_SPARK` — electric bolt
- `ParticleTypes.END_ROD` — trail particle
- `ParticleTypes.CAMPFIRE_SIGNAL_SMOKE` — smoke column
- `ParticleTypes.LAVA` — lava pop
- `ParticleTypes.SOUL_FIRE_FLAME` — blue flame
- `ParticleTypes.DRAGON_BREATH` — purple fog (time stop ambient)
- `DustParticleOptions` — custom colored dust
- `ParticleTypes.FALLING_WATER` — falling drops
- `ParticleTypes.SPLASH` — splash burst

## DIO Move Parameters (JCraft-exact)

| Move | Cooldown | Duration | Windup | Damage | Range | Interval | Knockback |
|---|---|---|---|---|---|---|---|
| Barrage | 280t (14s) | 40t | — | 1.0 | 5.0m | 3t | 0.4 |
| Charge | 100t (5s) | 19t | 7t | 5.0 | 7.5m | — | 1.5 |
| Time Stop | 1400t (70s) | 80t freeze | 45t | — | 96 blocks | — | — |

**Server timestop mechanism:** `DioStandState.serverTick()` freezes all entities within 96 blocks of the caster's position via `setNoActionTime(2)` + teleport reset each tick. The `SERVER_TIMESTOPS` list tracks active timestops.

## What Needs to Be Added

1. **Barrage** — hit particles at each target every punch: `ENCHANTED_HIT`, `CRIT`, and a colored `DustParticleOptions` burst. Follow dragon breath phase 2 pattern.
2. **Charge** — trail particles behind the lunge stand (`END_ROD` + `CRIT` per-tick), impact burst on entity hit (`EXPLOSION` + `FLASH`). Follow `SwordEscapeMove.java` and dragon breath phase 3 patterns.
3. **Time Stop** — activation burst (`DRAGON_BREATH` ambient + `FLASH` + sparkle ring), ambient particles during freeze, deactivation burst.
4. **Stand Summon/Despawn** — burst/swirl particles when `ensureStand()` creates the stand entity.

## Build

```
Build command: .\gradlew.bat build
Output JAR: build/libs/adminspec-1.0.0.jar
Dependencies: AzureLib 2.3.28 (bundled via jarJar)
Test framework: none
```
