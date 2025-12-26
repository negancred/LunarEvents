package me.negan.lunarevents;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import me.negan.lunarevents.events.BloodMoon;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

            dispatcher.register(
                    CommandManager.literal("mooncount")
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(CommandRegistry::checkMoonCount)
            );
            dispatcher.register(
                    CommandManager.literal("forcecrimsonmoon")
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(CommandRegistry::forceCrimsonMoon)
            );

        });
    }


    private static int forceBloodMoon(CommandContext<ServerCommandSource> context) {
        NightEventManager.forceNextBloodMoon();
        context.getSource().sendFeedback(
                () -> Text.literal("The next night will trigger a Moon event."),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int checkBloodMoonChance(CommandContext<ServerCommandSource> context) {
        int pity = me.negan.lunarevents.config.LunarEventsM.get().getPity() + 1;
        double chance = NightEventManager.computeBloodMoonChance(pity);

        context.getSource().sendFeedback(
                () -> Text.literal("Chance of Moon event tonight: " + Math.round(chance * 100) + "%"),
                false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int forceCrimsonMoon(CommandContext<ServerCommandSource> context) {

        NightEventManager.forceNextCrimsonMoon();

        context.getSource().sendFeedback(
                () -> Text.literal("The next night will be a CRIMSON MOON.")
                        .formatted(net.minecraft.util.Formatting.DARK_RED),
                true
        );

        return Command.SINGLE_SUCCESS;
    }


    private static int checkBloodMoonScore(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            int score = BloodMoon.getLunarScore(player);

            context.getSource().sendFeedback(
                    () -> Text.literal("Your current Moon score: " + score),
                    false
            );
        } catch (Exception e) {
            context.getSource().sendError(
                    Text.literal("This command can only be used by a player.")
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int checkMoonCount(CommandContext<ServerCommandSource> context) {

        int event = Lunarevents.getNightEvent();

        if (event == Lunarevents.CRIMSON_MOON) {
            context.getSource().sendFeedback(
                    () -> Text.literal("⚠ The Crimson Moon is happening RIGHT NOW.")
                            .formatted(Formatting.DARK_RED, Formatting.BOLD),
                    false
            );
            return Command.SINGLE_SUCCESS;
        }

        int count = NightEventManager.getBloodMoonCount();
        int mod = count % 6;
        int remaining = 5 - mod;

        if (remaining == 0) {
            context.getSource().sendFeedback(
                    () -> Text.literal(
                            "⚠ The next Blood Moon will be a Crimson Moon!"
                    ).formatted(Formatting.DARK_RED),
                    false
            );
        } else {
            context.getSource().sendFeedback(
                    () -> Text.literal(
                            "Crimson Moon will occur in " + remaining + " Blood Moon"
                                    + (remaining == 1 ? "" : "s") + "."
                    ).formatted(Formatting.RED),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}
