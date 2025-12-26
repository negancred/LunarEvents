package me.negan.lunarevents;

import me.negan.lunarevents.config.LunarEventsM;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

public class NightEventManager implements ServerTickEvents.EndWorldTick {

    private static boolean forcedMoon = false;
    private static boolean forcedCrimsonMoon = false;

    private long lastCheckedNight = -1;
    private static long lastMoonEndNight = -1;

    private static int nightsSinceLastBloodMoon;
    private static int bloodMoonCount;
    private static boolean moonActive = false;


    public NightEventManager() {
        LunarEventsM cfg = LunarEventsM.get();

        nightsSinceLastBloodMoon = cfg.getPity();
        bloodMoonCount = cfg.getBloodMoonCount();

        ServerTickEvents.END_WORLD_TICK.register(this);

        System.out.println("[LunarEvents] Loaded pity: " + nightsSinceLastBloodMoon);
        System.out.println("[LunarEvents] Loaded blood moon count: " + bloodMoonCount);
    }

    @Override
    public void onEndTick(ServerWorld world) {
        if (!world.getRegistryKey().equals(world.getServer().getOverworld().getRegistryKey())) return;

        long timeOfDay = world.getTimeOfDay() % 24000L;
        long day = world.getTimeOfDay() / 24000L;
        long nightNumber = day + 1;

        if (moonActive && timeOfDay < 13000L) {
            Lunarevents.getBloodMoonInstance().forceEnd(world);
            return;
        }

        if (timeOfDay >= 13000L && lastCheckedNight != nightNumber) {
            lastCheckedNight = nightNumber;

            if (forcedCrimsonMoon) {
                startMoon(true);
                forcedCrimsonMoon = false;
                forcedMoon = false;
                return;
            }

            if (forcedMoon) {
                startMoon(false);
                forcedMoon = false;
                return;
            }

            double chance = computeBloodMoonChance(nightsSinceLastBloodMoon + 1);
            float roll = world.getRandom().nextFloat();

            if (roll < chance) {
                startMoon(false);
            } else {
                nightsSinceLastBloodMoon++;
                LunarEventsM.get().addPity(1);
            }
        }
    }

    private void startMoon(boolean forceCrimson) {
        moonActive = true;
        boolean crimson =
                forceCrimson ||
                        ((bloodMoonCount + 1) % 6 == 0);

        Lunarevents.setNightEvent(
                crimson ? Lunarevents.CRIMSON_MOON : Lunarevents.BLOOD_MOON
        );

        nightsSinceLastBloodMoon = 0;
        LunarEventsM.get().resetPity();

        System.out.println(
                "[LunarEvents] Started " +
                        (crimson ? "CRIMSON MOON" : "BLOOD MOON") +
                        " (#" + (bloodMoonCount + 1) + ")"
        );
    }

    public static void onMoonEnd(ServerWorld world) {
        moonActive = false;
        long night = world.getTimeOfDay() / 24000L;

        if (lastMoonEndNight == night) return;
        lastMoonEndNight = night;

        bloodMoonCount++;
        LunarEventsM.get().setBloodMoonCount(bloodMoonCount);

        System.out.println(
                "[LunarEvents] Blood Moon completed. Total = " + bloodMoonCount
        );
    }

    public static double computeBloodMoonChance(int n) {
        final int START_N = 2;
        final int LINEAR_END_N = 35;
        final int MAX_NIGHT = 45;

        final double START_P = 0.001;
        final double LINEAR_END_P = 0.13;

        if (n < START_N) return 0.0;

        if (n <= LINEAR_END_N) {
            double t = (double) (n - START_N) / (LINEAR_END_N - START_N);
            return START_P + t * (LINEAR_END_P - START_P);
        }

        int steps = MAX_NIGHT - LINEAR_END_N;
        double r = Math.pow(1.0 / LINEAR_END_P, 1.0 / steps);
        return Math.min(1.0, LINEAR_END_P * Math.pow(r, n - LINEAR_END_N));
    }

    public static void forceNextBloodMoon() {
        forcedMoon = true;
    }

    public static void forceNextCrimsonMoon() {
        forcedCrimsonMoon = true;
        forcedMoon = true;
    }

    public static int getBloodMoonCount() {
        return LunarEventsM.get().getBloodMoonCount();
    }
}
