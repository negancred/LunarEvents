package me.negan.lunarevents.variants;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public class Variants {

    private static final List<VariantEntry> VARIANTS = new ArrayList<>();
    private static final Random RANDOM = new Random();

    public static void registerVariant(
            String name,
            float chance,
            BiFunction<ServerWorld, BlockPos, Entity> spawner
    ) {
        VARIANTS.add(new VariantEntry(name, chance, spawner));
        System.out.println(
                "[BloodMoon] Registered: " + name + " (" + (chance * 100) + "% chance)"
        );
    }

    public static Entity trySpawnVariant(ServerWorld world, BlockPos pos) {

        for (VariantEntry entry : VARIANTS) {
            if (RANDOM.nextFloat() < entry.chance) {

                Entity spawned = entry.spawner.apply(world, pos);

                if (spawned != null) {
                    System.out.println(
                            "[BloodMoon] Spawned " + entry.name + " at " + pos
                    );
                    return spawned;
                } else {
                    System.out.println(
                            "[BloodMoon] Failed to spawn " + entry.name + " at " + pos
                    );
                }
            }
        }

        return null;
    }

    private record VariantEntry(
            String name,
            float chance,
            BiFunction<ServerWorld, BlockPos, Entity> spawner
    ) {}
}
