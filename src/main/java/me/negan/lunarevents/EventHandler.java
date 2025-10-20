package me.negan.lunarevents;

import me.negan.lunarevents.events.BloodMoon;
import me.negan.lunarevents.ui.BloodMoonBossBar;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;

public class EventHandler {

    public static void register() {

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
            if (Lunarevents.getNightEvent() != 1) return;

            BloodMoon bloodMoon = Lunarevents.getBloodMoonInstance();
            if (bloodMoon != null && entity instanceof LivingEntity living) {
                bloodMoon.onEntityKilled(serverWorld, living, damageSource);
            }

            if (damageSource.getAttacker() instanceof ServerPlayerEntity killer) {
                BloodMoonBossBar.updatePlayerBar(killer, BloodMoon.getLunarScore(killer));
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (Lunarevents.getNightEvent() != 1) return;

            BloodMoon bloodMoon = Lunarevents.getBloodMoonInstance();
            if (bloodMoon != null) {
                bloodMoon.onPlayerDeath(newPlayer);
                BloodMoonBossBar.updatePlayerBar(newPlayer, BloodMoon.getLunarScore(newPlayer));
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (Lunarevents.getNightEvent() == 1) {
                BloodMoonBossBar.updatePlayerBar(player, BloodMoon.getLunarScore(player));
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            BloodMoonBossBar.removeBar(player);
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (!destination.getRegistryKey().equals(World.OVERWORLD)) {
                me.negan.lunarevents.ui.BloodMoonBossBar.removeBar(player);
                return;
            }

            if (Lunarevents.getNightEvent() == 1) {
                me.negan.lunarevents.ui.BloodMoonBossBar.updatePlayerBar(
                        player,
                        me.negan.lunarevents.events.BloodMoon.getLunarScore(player)
                );
            }
        });

    }
}
