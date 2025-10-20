package me.negan.lunarevents.utils;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Random;


public class Initialize {
    private static final Random RANDOM = new Random();

    public static <T extends MobEntity> T spawnEntity(ServerWorld world, BlockPos pos, EntityType<T> type) {
        T entity = type.create(world, SpawnReason.EVENT);
        if (entity == null) return null;

        entity.refreshPositionAndAngles(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                world.random.nextFloat() * 360F,
                0F
        );

        entity.setPersistent();
        if (!world.spawnEntity(entity)) return null;
        return entity;
    }

    public static void applyAttributeModifier(
            EntityAttributeInstance attribute,
            String idPath,
            double delta,
            EntityAttributeModifier.Operation operation
    ) {
        if (attribute == null) return;

        Identifier id = Identifier.of("lunarevents", idPath);
        if (attribute.getModifier(id) != null) return;
        attribute.addPersistentModifier(new EntityAttributeModifier(id, delta, operation));
    }

    public static void equip(MobEntity mob, EquipmentSlot slot, ItemStack stack) {
        mob.equipStack(slot, stack);
        mob.setEquipmentDropChance(slot, 0.0f);
    }


    public static void targetNearestPlayer(ServerWorld world, HostileEntity mob, double range) {
        var nearest = world.getClosestPlayer(mob, range);
        if (nearest != null) {
            mob.setTarget(nearest);
        }
    }

    public static ItemStack randomWeapon(float chance, ItemStack... weapons) {
        if (RANDOM.nextFloat() < chance && weapons.length > 0) {
            return weapons[RANDOM.nextInt(weapons.length)];
        }
        return ItemStack.EMPTY;
    }

    public static void applyCoreAttributes(
            MobEntity entity,
            double health,
            double damage,
            double speed,
            double scale
    ) {
        var hp = entity.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (hp != null) {
            double delta = health - hp.getBaseValue();
            applyAttributeModifier(hp, entity.getType().toString() + "_health", delta, EntityAttributeModifier.Operation.ADD_VALUE);
            entity.setHealth((float) health);
        }

        var dmg = entity.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (dmg != null) {
            double delta = damage - dmg.getBaseValue();
            applyAttributeModifier(dmg, entity.getType().toString() + "_damage", delta, EntityAttributeModifier.Operation.ADD_VALUE);
        }

        var spd = entity.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (spd != null) {
            double delta = speed - spd.getBaseValue();
            applyAttributeModifier(spd, entity.getType().toString() + "_speed", delta, EntityAttributeModifier.Operation.ADD_VALUE);
        }

        var scl = entity.getAttributeInstance(EntityAttributes.SCALE);
        if (scl != null) {
            double delta = scale - scl.getBaseValue();
            applyAttributeModifier(scl, entity.getType().toString() + "_scale", delta, EntityAttributeModifier.Operation.ADD_VALUE);
        }
    }
}
