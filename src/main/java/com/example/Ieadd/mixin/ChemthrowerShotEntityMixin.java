package com.example.Ieadd.mixin;

import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChemthrowerShotEntity.class)
public class ChemthrowerShotEntityMixin {

    // Внедряемся в конец метода onHit, который есть прямо в этом классе
    @SuppressWarnings("Mixin")
    @Inject(method = {"m_6532_", "onHit"}, at = @At("TAIL"), remap = false)
    private void onHitTail(HitResult mop, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;

        // 1. Проверяем, что это сервер, попали в блок и снаряд горит
        if (entity.level().isClientSide() || mop.getType() != HitResult.Type.BLOCK || !entity.isOnFire()) {
            return;
        }

        Level level = entity.level();
        RandomSource random = level.getRandom();

        // 2. ПОЛУЧАЕМ УРОВЕНЬ ГОРЮЧЕСТИ (время горения в тиках)
        // Стандартный максимум в IE — 200 тиков (10 секунд).
        int fireTicks = entity.getRemainingFireTicks();

        // 3. ВЫЧИСЛЯЕМ ШАНС
        // Если снаряд горит на 200 тиков, шанс = 100% (1.0F)
        // Если на 100 тиков — шанс 50% (0.5F)
        // Если на 20 тиков — шанс 10% (0.1F)
        float chanceToSpawnFire = Math.min(1.0F, fireTicks / 800.0F);

        // 4. БРОСАЕМ КУБИК
        // Если случайное число больше нашего шанса — огонь НЕ появляется
        if (random.nextFloat() > chanceToSpawnFire) {
            return;
        }

        // 5. ЕСЛИ ПРОШЛИ ПРОВЕРКУ — СОЗДАЕМ ОГОНЬ
        BlockHitResult blockHit = (BlockHitResult) mop;
        BlockPos hitPos = blockHit.getBlockPos();

        // Количество очагов огня тоже зависит от горючести (от 1 до 5)
        int attempts = 1 + (fireTicks / 80);

        // 2. Пытаемся создать ванильный огонь в радиусе 1 блока от места удара
        for (int i = 0; i < 3; i++) {
            BlockPos firePos = hitPos.offset(
                    random.nextInt(3) - 1,
                    random.nextInt(2),
                    random.nextInt(3) - 1
            );

            // 3. Проверяем, можно ли поставить огонь в эту точку
            if (BaseFireBlock.canBePlacedAt(level, firePos, Direction.UP)) {
                level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
            }
        }
    }
}