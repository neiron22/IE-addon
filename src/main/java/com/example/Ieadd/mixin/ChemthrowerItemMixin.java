package com.example.Ieadd.mixin;

import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChemthrowerItem.class)
public class ChemthrowerItemMixin {

    // Перехватываем переменную 'chem' в момент её создания
    @ModifyVariable(method = {"m_5929_" , "onUseTick"}, at = @At(value = "STORE"), ordinal = 0, remap = false)
    private ChemthrowerShotEntity modifyChemthrowerShot(ChemthrowerShotEntity chem, Level level, LivingEntity player, ItemStack stack, int duration) {
        if (!level.isClientSide && chem != null) {
            // Вычисляем точку вылета
            Vec3 eyePos = player.getEyePosition(1.0F);
            Vec3 lookVec = player.getViewVector(1.0F);

            // Проверяем, в какой руке предмет, чтобы сдвигать вправо или влево
            int direction = (player.getMainHandItem().getItem() instanceof ChemthrowerItem) ? 1 : -1;

            // Векторы вправо и вниз относительно взгляда
            Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize().scale(-direction);
            Vec3 downVec = lookVec.cross(rightVec).normalize();

            // НАСТРОЙКИ СМЕЩЕНИЯ (Подберите под модель распылителя)
            double forwardOffset = 1.2; // Вперед к дулу
            double downOffset = -0.52;   // Вниз на уровень рук
            double rightOffset = -0.2;   // Вправо (под правую руку)

            Vec3 spawnPos = eyePos
                    .add(lookVec.scale(forwardOffset))
                    .add(downVec.scale(downOffset))
                    .add(rightVec.scale(rightOffset));

            chem.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        }
        return chem;
    }
}