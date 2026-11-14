package com.example.Ieadd.common;

import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nonnull;

public class CustomRailgunShotEntity extends RailgunShotEntity {

    private boolean hasDroppedItem = false;
    // Флаг: true, если снаряд удаляется из-за попадания
    private boolean isImpactDiscard = false;

    @SuppressWarnings("unchecked")
    public CustomRailgunShotEntity(EntityType<?> type, Level world) {
        super((EntityType<RailgunShotEntity>)type, world);
    }


    public CustomRailgunShotEntity(Level world, @Nonnull LivingEntity living, float velocity, float accuracy, ItemStack ammo) {
        super(world, living, velocity, accuracy, ammo);
    }

    @Override
    public EntityType<?> getType() {
        // Говорим игре, что мы - это НАШ зарегистрированный тип
        return ModEntities.CUSTOM_RAILGUN_SHOT.get();
    }

    // 1. Перехватываем попадание в сущность (моба/игрока)
    @Override
    protected void onHitEntity(EntityHitResult result) {
        // При попадании в сущность IE всегда вызывает discard()
        this.isImpactDiscard = true;
        super.onHitEntity(result);
    }

    // 2. Перехватываем попадание в блок
    @Override
    protected void onHitBlock(BlockHitResult result) {
        // Проверяем, сломается ли снаряд при попадании в блок (согласно нашему getBreakChance)
        if (!this.level().isClientSide && !this.getAmmo().isEmpty()) {
            double breakRoll = this.random.nextDouble();
            if (breakRoll <= this.getProjectileProperties().getBreakChance(this.getShooterUUID(), this.getAmmo())) {
                this.isImpactDiscard = true; // Снаряд разбился от удара
            }
        }
        super.onHitBlock(result);
    }

    // 3. Перехватываем само удаление
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        // Дропаем предмет только на сервере и только если это не было попадание
        if (!this.level().isClientSide && !this.hasDroppedItem && !this.isImpactDiscard) {
            this.hasDroppedItem = true;

            ItemStack ammo = this.getAmmo();
            if (!ammo.isEmpty()) {
                ItemEntity drop = new ItemEntity(
                        this.level(),
                        this.getX(), this.getY(), this.getZ(),
                        ammo.copy()
                );
                this.level().addFreshEntity(drop);
            }
        }
    }
}