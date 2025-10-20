package me.negan.lunarevents;


import me.negan.lunarevents.config.LunarEventsM;
import net.minecraft.server.world.ServerWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class NightEventManager implements ServerTickEvents.EndWorldTick {
    private static boolean forcedBloodMoon = false;


    private long lastCheckedNight = -1;
    private static int nightsSinceLastBloodMoon;

    public NightEventManager() {

        nightsSinceLastBloodMoon = LunarEventsM.get().getPity();

        ServerTickEvents.END_WORLD_TICK.register(this);
        System.out.println("[LunarEvents] Loaded persistent pity: " + nightsSinceLastBloodMoon);
    }

    public static int getNightsSinceLastBloodMoon() {
        return nightsSinceLastBloodMoon;
    }

    public static void resetPity() {
        nightsSinceLastBloodMoon = 0;
        LunarEventsM.get().resetPity();
    }

    @Override
    public void onEndTick(ServerWorld world) {
        if (!world.getRegistryKey().equals(world.getServer().getOverworld().getRegistryKey())) return;

        long timeOfDay = world.getTimeOfDay() % 24000L;
        long day = world.getTimeOfDay() / 24000L;
        long nightNumber = day + 1;

        if (timeOfDay >= 13000L && lastCheckedNight != nightNumber) {
            lastCheckedNight = nightNumber;

            if (forcedBloodMoon) {
                Lunarevents.setNightEvent(1);
                resetPity();
                forcedBloodMoon = false;
                System.out.println("[LunarEvents] 🌕 Blood Moon forced manually.");
                return;
            }

            double chance = computeBloodMoonChance(nightsSinceLastBloodMoon + 1);
            float roll = world.getRandom().nextFloat();

            System.out.println("[LunarEvents] (Pity " + nightsSinceLastBloodMoon + ") || Chance = " + (chance * 100.0) + "% || Rolled: " + roll);

            if (roll < chance) {
                Lunarevents.setNightEvent(1);
                resetPity();
                System.out.println("[LunarEvents] 🌕 Blood Moon triggered (rolled " + roll + " < " + chance + ")");
            } else {
                nightsSinceLastBloodMoon++;
                Lunarevents.setNightEvent(0);
                LunarEventsM.get().addPity(1);
            }
        }
    }

    public static double computeBloodMoonChance(int n) {
        final int START_N = 2;
        final int LINEAR_END_N = 35;
        final int MAX_NIGHT = 45;

        final double START_P = 0.001;
        final double LINEAR_END_P = 0.18F;

        if (n < START_N) return 0.0;

        if (n <= LINEAR_END_N) {
            double t = (double)(n - START_N) / (LINEAR_END_N - START_N);
            return START_P + t * (LINEAR_END_P - START_P);
        }

        int steps = MAX_NIGHT - LINEAR_END_N;
        double r = Math.pow(1.0 / LINEAR_END_P, 1.0 / steps);
        double value = LINEAR_END_P * Math.pow(r, n - LINEAR_END_N);
        return Math.min(1.0, value);
    }

    public static void forceNextBloodMoon() {
        forcedBloodMoon = true;
    }

    public static boolean isBloodMoonForced() {
        return forcedBloodMoon;
    }
}
