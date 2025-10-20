package me.negan.lunarevents.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class IsSpawnableChecker {
    public static BlockPos getValidSpawnPos(ServerWorld world, BlockPos tryPos, int attempts) {
        if (!world.isChunkLoaded(tryPos)) {
            System.out.println("Attempt " + attempts + ": chunk not loaded at " + tryPos);
            return null;
        }

        BlockPos spawnPos;
        try {
            spawnPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, tryPos);
        } catch (Exception e) {
            System.out.println("Attempt " + attempts + ": getTopPosition threw error " + e);
            return null;
        }

        if (spawnPos == null) {
            System.out.println("Attempt " + attempts + ": spawnPos is null for tryPos " + tryPos);
            return null;
        }

        if (!world.getFluidState(spawnPos).isEmpty()) {
            System.out.println("Attempt " + attempts + ": fluid at " + spawnPos);
            return null;
        }

        BlockPos below = spawnPos.down();
        Block belowBlock = world.getBlockState(below).getBlock();

        boolean isSolidBase =
                world.getBlockState(below).isSolidBlock(world, below)
                        || belowBlock == Blocks.SNOW;

        if (!isSolidBase) {
            System.out.println("Attempt " + attempts + ": no solid block below " + below);
            return null;
        }

        boolean hasSpace =
                world.getBlockState(spawnPos).getCollisionShape(world, spawnPos).isEmpty()
                        && world.getBlockState(spawnPos.up()).getCollisionShape(world, spawnPos.up()).isEmpty();

        if (!hasSpace) {
            System.out.println("Attempt " + attempts + ": not enough space at " + spawnPos);
            return null;
        }

        int lightLevel = world.getLightLevel(spawnPos);
        if (lightLevel > 10) {
            System.out.println("Attempt " + attempts + ": light level too high (" + lightLevel + ") at " + spawnPos);
            return null;
        }

        return spawnPos;
    }
}
