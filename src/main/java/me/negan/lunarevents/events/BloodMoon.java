package me.negan.lunarevents.events;

import me.negan.lunarevents.Lunarevents;
import me.negan.lunarevents.ui.BloodMoonBossBar;
import me.negan.lunarevents.utils.IsSpawnableChecker;
import net.minecraft.entity.*;
import me.negan.lunarevents.variants.Variants;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;


import java.util.*;

public class BloodMoon extends NightEvent {

    private static final int TICK_INTERVAL = 100;
    private static final int MAX_HOSTILES = 90;
    private static final int PLAYER_MAX_HOSTILES = 90;

    private static final int SCORE_PER_KILL = 8;
    private static final int SCORE_PER_DEATH = 30;
    private static final int SCORE_MIN = 0;
    private static final int SCORE_MAX = 250;


    private int tickCounter = 0;

    private final Random random = new Random();
    private static final Map<UUID, Integer> lunarScores = new HashMap<>();


    public BloodMoon() {
        super();
    }

    @Override
    protected void onNightStart(ServerWorld world) {
        if (!world.getRegistryKey().equals(World.OVERWORLD)) return;

        if (Lunarevents.getNightEvent() == 1) {
            System.out.println("Blood Moon Started");

            String[] intros = {
                    "Blood Moon is rising...",
                    "The night glows crimson under the blood moon...",
                    "You feel a chill... something dark awakens.",
                    "The blood moon thirsts tonight..."
            };

            String chosenIntro = intros[world.getRandom().nextInt(intros.length)];

            Text message = Text.literal(chosenIntro)
                    .formatted(Formatting.RED, Formatting.ITALIC)
                    .styled(style -> style.withHoverEvent(
                            new HoverEvent.ShowText(
                                    Text.literal("You cannot sleep during this period!\n").formatted(Formatting.DARK_RED)
                                            .append(Text.literal("Mobs are much stronger and will spawn in\n").formatted(Formatting.DARK_RED))
                                            .append(Text.literal("large amounts.\n").formatted(Formatting.DARK_RED))
                            )
                    ));

            world.getServer().getPlayerManager().broadcast(message, false);

            for (ServerPlayerEntity player : world.getPlayers()) {
                float pitch = 0.9f + world.getRandom().nextFloat() * 0.2f;


                world.getServer().execute(() -> {
                    world.playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.AMBIENT_CAVE.value(),
                            SoundCategory.AMBIENT,
                            0.8f,
                            1.0f
                    );
                });
            }
        }
    }


    @Override
    protected void onNightEnd(ServerWorld world) {
        if (Lunarevents.getNightEvent() != 1) return;

        for (ServerPlayerEntity player : world.getPlayers()) {
            int score = getLunarScore(player);
            int xpReward;

            if (score <= 0) {
                xpReward = 50;
            } else if (score >= SCORE_MAX) {
                xpReward = 1000;
            } else {
                xpReward = (int) (100 + (score / (double) SCORE_MAX) * 900);
            }

            player.addExperience(xpReward);
            lunarScores.put(player.getUuid(), 0);

            player.sendMessage(
                    Text.literal("You earned " + xpReward + " XP during the Blood Moon!")
                            .formatted(Formatting.GREEN),
                    true
            );
        }

        Lunarevents.setNightEvent(0);
        BloodMoonBossBar.clearAllBars();
    }


    @Override
    protected void onNightTick(ServerWorld world, long timeOfDay) {
        if (!world.getRegistryKey().equals(World.OVERWORLD)) return;
        if (Lunarevents.getNightEvent() != 1) return;

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.isSpectator() || !world.isChunkLoaded(player.getBlockPos())) continue;

            int lunarScore = getLunarScore(player);
            BloodMoonBossBar.updatePlayerBar(player, lunarScore);

            Box checkArea = new Box(
                    player.getX() - 64, player.getY() - 32, player.getZ() - 64,
                    player.getX() + 64, player.getY() + 32, player.getZ() + 64
            );

            int nearbyHostiles = world.getEntitiesByClass(HostileEntity.class, checkArea, e -> true).size();
            if (nearbyHostiles < PLAYER_MAX_HOSTILES) {
                spawnHostileNearPlayer(world, player);
            }
        }

        if (timeOfDay % 40 == 0) {
            for (HostileEntity hostile : world.getEntitiesByClass(
                    HostileEntity.class,
                    new Box(-30000, -64, -30000, 30000, 320, 30000),
                    e -> true)) {

                hostile.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, false, false));
            }
        }
    }
    private void spawnHostileNearPlayer(ServerWorld world, ServerPlayerEntity player) {
        for (int attempts = 0; attempts < 6; attempts++) {
            try {
                int distance = 40 + random.nextInt(11);
                double angle = random.nextDouble() * Math.PI * 2;

                int dx = (int) (Math.cos(angle) * distance);
                int dz = (int) (Math.sin(angle) * distance);

                BlockPos playerPos = player.getBlockPos();
                BlockPos tryPos = playerPos.add(dx, 0, dz);

                BlockPos spawnPos = IsSpawnableChecker.getValidSpawnPos(world, tryPos, attempts);
                if (spawnPos == null) continue;

                if (Variants.trySpawnVariant(world, spawnPos)) {
                    return;
                }

            } catch (Exception e) {
                System.out.println("[BloodMoon] Spawn attempt " + attempts + " failed due to exception:");
                e.printStackTrace(System.out);
            }
        }

        System.out.println("[BloodMoon] Failed to find valid spawn near " + player.getName().getString());
    }



    private void addLunarScore(ServerPlayerEntity player, int amount) {
        UUID id = player.getUuid();
        int current = lunarScores.getOrDefault(id, 0);
        int updated = Math.max(SCORE_MIN, Math.min(SCORE_MAX, current + amount));
        lunarScores.put(id, updated);
    }

    public static int getLunarScore(ServerPlayerEntity player) {
        return lunarScores.getOrDefault(player.getUuid(), 0);
    }

    public void onEntityKilled(ServerWorld world, LivingEntity victim, DamageSource source) {
        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;

        if (victim instanceof ZombieEntity || victim.getType() == EntityType.SKELETON) {
            addLunarScore(player, SCORE_PER_KILL);
        }
    }

    public void onPlayerDeath(ServerPlayerEntity player) {
        addLunarScore(player, -SCORE_PER_DEATH);
    }

}
