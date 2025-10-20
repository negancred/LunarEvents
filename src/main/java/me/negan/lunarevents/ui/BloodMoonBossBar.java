package me.negan.lunarevents.ui;

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

    private static final int SCORE_MAX = 250;
    private static final Map<UUID, ServerBossBar> PLAYER_BARS = new HashMap<>();

    public static void updatePlayerBar(ServerPlayerEntity player, int lunarScore) {
        if (player == null) return;

        ServerWorld world = player.getWorld();
        if (world == null || !world.getRegistryKey().equals(World.OVERWORLD)) {
            removeBar(player);
            return;
        }

        ServerBossBar bar = PLAYER_BARS.get(player.getUuid());
        if (bar == null) {
            bar = new ServerBossBar(
                    Text.literal("Blood Moon   |  Score: 0 / " + SCORE_MAX),
                    BossBar.Color.RED,
                    BossBar.Style.PROGRESS
            );
            bar.addPlayer(player);
            bar.setVisible(true);
            PLAYER_BARS.put(player.getUuid(), bar);
        } else if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }

        float progress = Math.max(0f, Math.min(1f, lunarScore / (float) SCORE_MAX));
        bar.setPercent(progress);
        bar.setName(Text.literal("Blood Moon   |  Score: " + lunarScore + " / " + SCORE_MAX));
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
