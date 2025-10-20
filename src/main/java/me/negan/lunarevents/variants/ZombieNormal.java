package me.negan.lunarevents.variants;

import me.negan.lunarevents.utils.Initialize;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class ZombieNormal {

    private static final float SPAWN_CHANCE = 0.7f;
    private static final double HEALTH = 46.0;
    private static final double DAMAGE = 5.0;
    private static final double SPEED = 0.23;
    private static final double SCALE = 1.0;
    private static final Random RANDOM = new Random();

    static {
        System.out.println("[BloodMoon] Registering Zombie Normal variant...");
        Variants.registerVariant("Zombie Normal", SPAWN_CHANCE, ZombieNormal::spawn);
    }

    public static boolean spawn(ServerWorld world, BlockPos pos) {
        ZombieEntity zombie = Initialize.spawnEntity(world, pos, net.minecraft.entity.EntityType.ZOMBIE);
        if (zombie == null) return false;


        ItemStack weapon = Initialize.randomWeapon(0.3f,
                new ItemStack(Items.IRON_SWORD),
                new ItemStack(Items.IRON_AXE)
        );
        if (!weapon.isEmpty()) Initialize.equip(zombie, EquipmentSlot.MAINHAND, weapon);


        world.getServer().execute(() -> {
            Initialize.applyCoreAttributes(zombie, HEALTH, DAMAGE, SPEED, SCALE);
            Initialize.targetNearestPlayer(world, zombie, 100.0);
        });

        return true;
    }
}
