package me.negan.lunarevents.events;

import me.negan.lunarevents.Lunarevents;
import me.negan.lunarevents.NightEventManager;
import me.negan.lunarevents.ui.BloodMoonBossBar;
import me.negan.lunarevents.utils.Initialize;
import me.negan.lunarevents.utils.IsSpawnableChecker;
import me.negan.lunarevents.variants.Variants;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.*;

public class BloodMoon extends NightEvent {

    private static final int TICK_INTERVAL = 120;
    private static final int PLAYER_MAX_HOSTILES = 90;

    private static final int SCORE_PER_KILL = 8;
    private static final int SCORE_PER_DEATH = 30;
    private static final int SCORE_MAX = 500;

    private static final double CRIMSON_DAMAGE = 1.3;
    private static final double CRIMSON_SPEED  = 1.2;
    private static final double CRIMSON_SCALE  = 1.1;
    private static final double CRIMSON_XP     = 1.75;

    private final Random random = new Random();
    private int tickCounter = 0;

    private static final Map<UUID, Integer> lunarScores = new HashMap<>();


    @Override
    protected void onNightStart(ServerWorld world) {
        if (!world.getRegistryKey().equals(World.OVERWORLD)) return;

        int event = Lunarevents.getNightEvent();
        if (event != Lunarevents.BLOOD_MOON && event != Lunarevents.CRIMSON_MOON) return;

        boolean crimson = event == Lunarevents.CRIMSON_MOON;

        Text msg = Text.literal(
                crimson ? "The Crimson Moon awakens..." : "Blood Moon is rising..."
        ).formatted(
                crimson ? Formatting.DARK_RED : Formatting.RED,
                Formatting.ITALIC
        );

        world.getServer().getPlayerManager().broadcast(msg, false);

        for (ServerPlayerEntity player : world.getPlayers()) {
            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.AMBIENT_CAVE.value(),
                    SoundCategory.AMBIENT,
                    0.8f,
                    crimson ? 0.6f : 1.0f
            );
        }
    }

    @Override
    protected void onNightTick(ServerWorld world, long timeOfDay) {
        if (!world.getRegistryKey().equals(World.OVERWORLD)) return;

        int event = Lunarevents.getNightEvent();
        if (event != Lunarevents.BLOOD_MOON && event != Lunarevents.CRIMSON_MOON) return;

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.isSpectator()) continue;

            int score = lunarScores.getOrDefault(player.getUuid(), 0);
            BloodMoonBossBar.updatePlayerBar(player, score);

            Box area = new Box(
                    player.getX() - 64, player.getY() - 32, player.getZ() - 64,
                    player.getX() + 64, player.getY() + 32, player.getZ() + 64
            );

            int hostiles = world.getEntitiesByClass(HostileEntity.class, area, e -> true).size();
            if (hostiles < PLAYER_MAX_HOSTILES) {
                spawnHostileNearPlayer(world, player);
            }
        }
    }

    @Override
    protected void onNightEnd(ServerWorld world) {
        if (!world.getRegistryKey().equals(World.OVERWORLD)) return;

        int event = Lunarevents.getNightEvent();
        if (event != Lunarevents.BLOOD_MOON && event != Lunarevents.CRIMSON_MOON) return;

        boolean crimson = event == Lunarevents.CRIMSON_MOON;

        NightEventManager.onMoonEnd(world);
        for (ServerPlayerEntity player : world.getPlayers()) {
            int score = lunarScores.getOrDefault(player.getUuid(), 0);

            int xp = (int) (100 + (score / (double) SCORE_MAX) * 900);
            if (crimson) xp = (int) (xp * CRIMSON_XP);

            player.addExperience(xp);
            player.sendMessage(
                    Text.literal("You earned " + xp + " XP during "
                                    + (crimson ? "Crimson Moon!" : "Blood Moon!"))
                            .formatted(Formatting.GREEN),
                    true
            );

            lunarScores.put(player.getUuid(), 0);
        }

        BloodMoonBossBar.clearAllBars();
        Lunarevents.setNightEvent(Lunarevents.NONE);
    }

    private void spawnHostileNearPlayer(ServerWorld world, ServerPlayerEntity player) {
        for (int i = 0; i < 6; i++) {
            int dist = 40 + random.nextInt(11);
            double angle = random.nextDouble() * Math.PI * 2;

            BlockPos pos = player.getBlockPos().add(
                    (int) (Math.cos(angle) * dist),
                    0,
                    (int) (Math.sin(angle) * dist)
            );

            BlockPos spawn = IsSpawnableChecker.getValidSpawnPos(world, pos, i);
            if (spawn == null) continue;

            Entity entity = Variants.trySpawnVariant(world, spawn);
            if (entity instanceof LivingEntity living) {
                applyCrimsonModifiers(living);
                return;
            }
        }
    }

    private void applyCrimsonModifiers(LivingEntity entity) {
        if (Lunarevents.getNightEvent() != Lunarevents.CRIMSON_MOON) return;

        if (!(entity instanceof ZombieEntity
                || entity instanceof SkeletonEntity
                || entity instanceof SpiderEntity)) return;

        if (!(entity instanceof MobEntity mob)) return;

        boolean isZombieBrute = mob.getCommandTags().contains("lunarevents:zombie_brute");

        Initialize.applyMultipliers(
                mob,
                CRIMSON_DAMAGE,
                CRIMSON_SPEED,
                isZombieBrute ? 1.0 : CRIMSON_SCALE
        );
    }


    public void onEntityKilled(ServerWorld world, LivingEntity victim, DamageSource source) {
        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;

        if (victim instanceof ZombieEntity || victim.getType() == EntityType.SKELETON) {
            lunarScores.merge(player.getUuid(), SCORE_PER_KILL, Integer::sum);
        }
    }

    public void onPlayerDeath(ServerPlayerEntity player) {
        lunarScores.merge(player.getUuid(), -SCORE_PER_DEATH, Integer::sum);
    }

    public static int getLunarScore(ServerPlayerEntity player) {
        return lunarScores.getOrDefault(player.getUuid(), 0);
    }
    public void forceEnd(ServerWorld world) {
        onNightEnd(world);
    }

}
