package me.negan.lunarevents.variants;

import me.negan.lunarevents.utils.Initialize;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ZombieBrute {

    public static final float SPAWN_CHANCE = 0.08f;
    public static final double HEALTH = 110.0;
    public static final double DAMAGE = 4.0;
    public static final double SPEED = 0.19;
    public static final double SCALE = 1.4;
    public static final ItemStack WEAPON = new ItemStack(Items.IRON_AXE);

    static {
        System.out.println("[BloodMoon] Registering Zombie Brute variant...");
        Variants.registerVariant("Zombie Brute", SPAWN_CHANCE, ZombieBrute::spawn);
    }

    public static Entity spawn(ServerWorld world, BlockPos pos) {

        ZombieEntity brute = Initialize.spawnEntity(
                world,
                pos,
                net.minecraft.entity.EntityType.ZOMBIE
        );

        if (brute == null) {
            System.out.println("[BloodMoon] Failed to spawn Zombie Brute at " + pos);
            return null;
        }
        brute.addCommandTag("lunarevents:zombie_brute");
        Initialize.equip(brute, EquipmentSlot.MAINHAND, WEAPON.copy());

        world.getServer().execute(() -> {
            Initialize.applyCoreAttributes(brute, HEALTH, DAMAGE, SPEED, SCALE);
            Initialize.targetNearestPlayer(world, brute, 100.0);
        });

        return brute;
    }
}
