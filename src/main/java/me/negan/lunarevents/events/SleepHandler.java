package me.negan.lunarevents.events;

import me.negan.lunarevents.Lunarevents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SleepHandler {

    public static void register() {
        EntitySleepEvents.START_SLEEPING.register((LivingEntity entity, BlockPos sleepingPos) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            if (Lunarevents.getNightEvent() > 0) {
                player.wakeUp(true, false);
                if (Lunarevents.getNightEvent() == 1) {
                    player.sendMessage(
                            Text.literal("You cannot sleep during Blood Moon!").formatted(Formatting.RED),
                            false
                    );
                }
            }
        });
    }
}
