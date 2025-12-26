package me.negan.lunarevents.variants;

import me.negan.lunarevents.utils.Initialize;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class SkeletonNormal {

    private static final float SPAWN_CHANCE = 0.32f;
    private static final double HEALTH = 40.0;
    private static final double DAMAGE = 6.0;
    private static final double SPEED = 0.25;
    private static final double SCALE = 1.0;

    static {
        System.out.println("[BloodMoon] Registering Skeleton Normal variant...");
        Variants.registerVariant("Skeleton Normal", SPAWN_CHANCE, SkeletonNormal::spawn);
    }

    public static Entity spawn(ServerWorld world, BlockPos pos) {

        SkeletonEntity skeleton = Initialize.spawnEntity(
                world,
                pos,
                net.minecraft.entity.EntityType.SKELETON
        );

        if (skeleton == null) {
            System.out.println("[BloodMoon] Failed to create Skeleton instance at " + pos);
            return null;
        }

        Initialize.equip(skeleton, EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));

        if (Math.random() < 0.2) {
            Initialize.equip(skeleton, EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        }

        world.getServer().execute(() -> {
            Initialize.applyCoreAttributes(skeleton, HEALTH, DAMAGE, SPEED, SCALE);
            Initialize.targetNearestPlayer(world, skeleton, 100.0);
        });

        return skeleton;
    }
}
