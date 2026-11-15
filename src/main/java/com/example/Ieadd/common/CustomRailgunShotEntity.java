package com.example.Ieadd.common;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.UUID;

public class CustomRailgunShotEntity extends RailgunShotEntity {

    private boolean hasDroppedItem = false;
    // Флаг: true, если снаряд удаляется из-за попадания
    private boolean isImpactDiscard = false;
    //private boolean hasPlayedHitSound = false;
    //private boolean isStuck = false;

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

    private int hitCount = 0;

    // Переопределяем гравитацию
    @Override
    public double getGravity() {
        // Получаем базовую гравитацию (0.005 * 0.2 = 0.001)
        double baseGravity = super.getGravity();

        // Увеличиваем гравитацию на 50% с каждым попаданием (1.0 + 0.5 * hitCount)
        // Можете изменить 0.5 на 1.0, если хотите, чтобы он падал быстрее
        return baseGravity * (1.0 + (this.hitCount * 0.5));
    }


    // 1. Перехватываем попадание в сущность (моба/игрока)
    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Если это НЕ пильный диск
        if (this.getAmmo().getItem() != ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "sawblade"))) {
            this.isImpactDiscard = true;
            super.onHitEntity(result);
            return;
        }

        // --- ДЛЯ ПИЛЬНОГО ДИСКА ---
        if (!this.level().isClientSide && !this.getAmmo().isEmpty()) {

            // 1. Увеличиваем счетчик попаданий (для гравитации)
            this.hitCount++;

            // 2. Уменьшаем прочность диска (на 1 единицу за каждое попадание)
            ItemStack ammoStack = this.getAmmo();
            if (ammoStack.isDamageableItem()) { // Проверяем, имеет ли предмет прочность
                int currentDamage = ammoStack.getDamageValue();

                int durabilityLossPerHit = 600;


                if (currentDamage + durabilityLossPerHit >= ammoStack.getMaxDamage()) {
                    // Диск сломался! Удаляем снаряд и не дропаем сломанный предмет
                    this.isImpactDiscard = true;
                    this.discard();
                    return; // Выходим из метода, дальше урон наносить не нужно
                } else {
                    ammoStack.setDamageValue(currentDamage + durabilityLossPerHit);
                    // Синхронизируем изменения с клиентом (чтобы модель/данные обновились)
                    this.setAmmoSynced();
                }
            }



            // 3. Наносим урон (как в вашем предыдущем коде)
            RailgunHandler.IRailgunProjectile props = this.getProjectileProperties();
            if (props != null) {
                Entity hit = result.getEntity();
                UUID shooterUuid = this.getShooterUUID();

                double damage = props.getDamage(this.level(), hit, shooterUuid, this);
                DamageSource source = props.getDamageSource(this.level(), hit, shooterUuid, this);
                if (source == null) {
                    source = IEDamageSources.causeRailgunDamage(this, this.getOwner());
                }
                hit.hurt(source, (float)(damage * IEServerConfig.TOOLS.railgun_damage.get()));
                props.onHitTarget(this.level(), result, shooterUuid, this);
            }
        }
    }

    // 2. Перехватываем попадание в блок
    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.setDeltaMovement(0, 0, 0);
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