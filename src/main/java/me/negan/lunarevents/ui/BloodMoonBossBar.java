package me.negan.lunarevents.ui;

import me.negan.lunarevents.Lunarevents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BloodMoonBossBar {

    private static final int SCORE_MAX = 500;
    private static final Map<UUID, ServerBossBar> PLAYER_BARS = new HashMap<>();

    public static void updatePlayerBar(ServerPlayerEntity player, int lunarScore) {
        if (player == null) return;

        ServerWorld world = player.getWorld();
        if (world == null || !world.getRegistryKey().equals(World.OVERWORLD)) {
            removeBar(player);
            return;
        }

        int event = Lunarevents.getNightEvent();
        boolean crimson = event == Lunarevents.CRIMSON_MOON;
        boolean active = event == Lunarevents.BLOOD_MOON || crimson;

        if (!active) {
            removeBar(player);
            return;
        }

        String title = crimson ? "Crimson Moon" : "Blood Moon";
        BossBar.Color color = crimson ? BossBar.Color.PURPLE : BossBar.Color.RED;

        ServerBossBar bar = PLAYER_BARS.get(player.getUuid());

        if (bar == null) {
            bar = new ServerBossBar(
                    Text.literal(title + "   |  Score: 0 / " + SCORE_MAX),
                    color,
                    BossBar.Style.PROGRESS
            );
            bar.addPlayer(player);
            bar.setVisible(true);
            PLAYER_BARS.put(player.getUuid(), bar);
        } else {
            // Update color/title if event changed mid-session
            bar.setColor(color);
        }

        float progress = Math.max(0f, Math.min(1f, lunarScore / (float) SCORE_MAX));
        bar.setPercent(progress);
        bar.setName(
                Text.literal(title + "   |  Score: " + lunarScore + " / " + SCORE_MAX)
        );
    }

    public static void removeBar(ServerPlayerEntity player) {
        ServerBossBar bar = PLAYER_BARS.remove(player.getUuid());
        if (bar != null) {
            bar.removePlayer(player);
        }
    }

    public static void clearAllBars() {
        for (ServerBossBar bar : PLAYER_BARS.values()) {
            bar.clearPlayers();
        }
        PLAYER_BARS.clear();
    }
}
