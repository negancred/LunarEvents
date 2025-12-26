package me.negan.lunarevents.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class NightEvent implements ServerTickEvents.EndWorldTick {
    private final Map<ServerWorld, Boolean> nightActive = new WeakHashMap<>();

    public NightEvent() {
        ServerTickEvents.END_WORLD_TICK.register(this);
    }
    @Override
    public void onEndTick(ServerWorld world) {
        long timeOfDay = world.getTimeOfDay() % 24000L;
        boolean isNight = timeOfDay >= 13000L;

        boolean wasNight = nightActive.getOrDefault(world, false);

        if (isNight && !wasNight) {
            nightActive.put(world, true);
            onNightStart(world);
        }

        if (!isNight && wasNight) {
            nightActive.put(world, false);
            onNightEnd(world);
        }

        if (isNight) {
            onNightTick(world, timeOfDay);
        }
    }


    protected void onNightStart(ServerWorld world) {}
    protected void onNightTick(ServerWorld world, long timeOfDay) {}
    protected void onNightEnd(ServerWorld world) {}
}
