package me.negan.lunarevents.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class LunarEventsM {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "lunarevents.json");

    private static LunarEventsM INSTANCE;

    public int pity = 0;

    public static LunarEventsM get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }


    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("[LunarEventsM] Failed to save config: " + e.getMessage());
        }
    }

    private static LunarEventsM load() {
        if (!CONFIG_FILE.exists()) {
            System.out.println("[LunarEventsM] Config not found, creating new one...");
            LunarEventsM config = new LunarEventsM();
            config.save();
            return config;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            LunarEventsM loaded = GSON.fromJson(reader, LunarEventsM.class);
            System.out.println("[LunarEventsM] Loaded config successfully. Current pity: " + loaded.pity);
            return loaded;
        } catch (Exception e) {
            System.err.println("[LunarEventsM] Failed to load config, creating new one: " + e.getMessage());
            LunarEventsM config = new LunarEventsM();
            config.save();
            return config;
        }
    }

    public void addPity(int amount) {
        this.pity += amount;
        System.out.println("[LunarEventsM] Pity increased by " + amount + ", total: " + this.pity);
        save();
    }

    public void resetPity() {
        this.pity = 0;
        System.out.println("[LunarEventsM] Pity reset.");
        save();
    }


    public int getPity() {
        return this.pity;
    }
}
