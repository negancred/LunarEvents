package me.negan.lunarevents;

import me.negan.lunarevents.events.BloodMoon;
import me.negan.lunarevents.events.SleepHandler;
import net.fabricmc.api.ModInitializer;

public class Lunarevents implements ModInitializer {
    public static final String MOD_ID = "lunarevents";

    public static int currentNightEvent = 0;

    private static BloodMoon bloodMoonInstance;

    @Override
    public void onInitialize() {
        System.out.println("LunarEvents v.2.1 By NEGAN");
        me.negan.lunarevents.config.LunarEventsM.get();
        new NightEventManager();
        bloodMoonInstance = new BloodMoon();
        SleepHandler.register();
        EventHandler.register();
        CommandRegistry.register();
        try {
            Class.forName("me.negan.lunarevents.variants.ZombieBrute");
            Class.forName("me.negan.lunarevents.variants.ZombieNormal");
            Class.forName("me.negan.lunarevents.variants.SkeletonNormal");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public static void setNightEvent(int eventId) {
        currentNightEvent = eventId;
    }

    public static int getNightEvent() {
        return currentNightEvent;
    }

    public static BloodMoon getBloodMoonInstance() {
        return bloodMoonInstance;
    }
}
