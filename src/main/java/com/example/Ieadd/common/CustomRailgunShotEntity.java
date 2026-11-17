package com.example.Ieadd.common;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
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
    private boolean isStuck = false;
    private BlockPos stuckBlockPos;
    private net.minecraft.core.Direction hitFace = net.minecraft.core.Direction.UP;

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
        double baseGravity = super.getGravity();;

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
            Entity hit = result.getEntity();
            if (hit instanceof LivingEntity livingHit) {
                livingHit.invulnerableTime = 0;
            }
            return;
        }

        // --- ДЛЯ ПИЛЬНОГО ДИСКА ---
        if (!this.level().isClientSide && !this.getAmmo().isEmpty()) {

            // 1. Увеличиваем счетчик попаданий (для гравитации)
            this.hitCount++;

            // 2. СНАЧАЛА НАНОСИМ УРОН (чтобы он прошел, даже если диск сейчас сломается)
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

            // 3. ТЕПЕРЬ ПРОВЕРЯЕМ ПРОЧНОСТЬ
            ItemStack ammoStack = this.getAmmo();
            if (ammoStack.isDamageableItem()) {
                int currentDamage = ammoStack.getDamageValue();
                int durabilityLossPerHit = 800;

                if (currentDamage + durabilityLossPerHit >= ammoStack.getMaxDamage()) {
                    // Диск сломался! Удаляем снаряд и не дропаем сломанный предмет
                    this.isImpactDiscard = true;
                    this.discard();
                    // return тут больше не нужен, так как это конец метода
                } else {
                    ammoStack.setDamageValue(currentDamage + durabilityLossPerHit);
                    // Синхронизируем изменения с клиентом
                    this.setAmmoSynced();
                }


            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.isStuck) {
            // Если блок, в который мы воткнулись, стал пустым — удаляем снаряд
            if (this.stuckBlockPos != null && this.level().isEmptyBlock(this.stuckBlockPos)) {
                this.discard(); // Вызовет onRemovedFromWorld, и предмет выпадет
            }
            return; // Выходим, чтобы не применять эффекты повторно
        }
        // Запоминаем грань, в которую попали
        this.hitFace = result.getDirection();
        this.inGround = true;
        this.isStuck = true; // флаг вкл , физика выкл
        this.stuckBlockPos = result.getBlockPos();
        super.onHitBlock(result);
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        // 3. Жестко фиксируем координаты ровно в точке касания блока
        this.setPos(result.getLocation().x, result.getLocation().y, result.getLocation().z);

        Vec3 hitLoc = result.getLocation();
        net.minecraft.core.Direction face = result.getDirection();
        double offset = -0.05;
        this.setPos(
                hitLoc.x + face.getStepX() * offset,
                hitLoc.y + face.getStepY() * offset,
                hitLoc.z + face.getStepZ() * offset
        );

    }

    @Override
    public void move(MoverType type, Vec3 pos) {
        // Если снаряд застрял, мы блокируем любые попытки сдвинуть его с места
        if (this.isStuck) {
            return;
        }
        super.move(type, pos);
    }
    @Override
    public Vec3 getLightProbePosition(float partialTicks) {
        // Если снаряд застрял в блоке
        if (this.isStuck) {
            // Берем нашу позицию и сдвигаем её на 0.6 блока наружу от грани
            double offset = 0.6;
            return new Vec3(
                    this.getX() + this.hitFace.getStepX() * offset,
                    this.getY() + this.hitFace.getStepY() * offset,
                    this.getZ() + this.hitFace.getStepZ() * offset
            );
        }
        // Если летит — стандартная точка
        return super.getLightProbePosition(partialTicks);
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