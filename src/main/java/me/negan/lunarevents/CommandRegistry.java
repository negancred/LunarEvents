package me.negan.lunarevents;

import com.mojang.brigadier.Command;
import me.negan.lunarevents.events.BloodMoon;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class CommandRegistry {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("forcebloodmoon")
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(CommandRegistry::forceBloodMoon)
            );

            dispatcher.register(
                    CommandManager.literal("bloodmoonchance")
                            .executes(CommandRegistry::checkBloodMoonChance)
            );

            dispatcher.register(
                    CommandManager.literal("bloodmoonscore")
                            .executes(CommandRegistry::checkBloodMoonScore)
            );
        });
    }

    private static int forceBloodMoon(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getServer().getOverworld();
        long timeOfDay = world.getTimeOfDay() % 24000L;

        if (timeOfDay >= 13000L && timeOfDay < 23000L) {
            source.sendError(Text.literal("Cannot set Blood Moon at night! Wait until daytime."));
            return Command.SINGLE_SUCCESS;
        }

        NightEventManager.forceNextBloodMoon();

        source.sendFeedback(() -> Text.literal("Tonight will be a Blood Moon!"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int checkBloodMoonChance(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        int pity = NightEventManager.getNightsSinceLastBloodMoon() + 1;
        double chance = NightEventManager.computeBloodMoonChance(pity);

        if (NightEventManager.isBloodMoonForced()) {
            source.sendFeedback(() -> Text.literal("Chance of Blood Moon: 100% (Forced)"), false);
            return Command.SINGLE_SUCCESS;
        }

        int percent = (int) Math.round(chance * 100);
        source.sendFeedback(() -> Text.literal("Chance of Blood Moon: " + percent + "%"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int checkBloodMoonScore(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        try {
            ServerPlayerEntity player = source.getPlayer();
            assert player != null;
            int score = BloodMoon.getLunarScore(player);

            source.sendFeedback(
                    () -> Text.literal("Your Blood Moon score: " + score),
                    false
            );
        } catch (Exception e) {
            source.sendError(Text.literal("This command can only be used by a player."));
        }
        return Command.SINGLE_SUCCESS;
    }
}
