package com.example.Ieadd.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class RailgunClientExtensions implements IClientItemExtensions {

    // --- 1. ПОЗА ДЛЯ ТРЕТЬЕГО ЛИЦА (ArmPose) ---
    // Создаем кастомную позу, где обе руки подняты и сведены вместе
    public static final HumanoidModel.ArmPose RAILGUN_TWO_HANDED_POSE = HumanoidModel.ArmPose.create(
            "RAILGUN_TWO_HANDED",
            true, // Двуручный хват (игра будет учитывать обе руки)
            (model, entity, arm) -> {
                // Настраиваем правую руку
                model.rightArm.xRot = -1.4F; // Поднимаем вперед (вверх)
                model.rightArm.yRot = -0.3F; // Сводим к центру
                model.rightArm.zRot = 0.0F;

                // Настраиваем левую руку (она должна "держать" ствол)
                model.leftArm.xRot = -1.4F;
                model.leftArm.yRot = 0.4F;   // Сводим к центру с другой стороны
                model.leftArm.zRot = 0.0F;

                // Примечание: если у вас модель с изгибами (Alex), здесь также можно настроить предплечья
            }
    );

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
        // Если предмет в руке - рельсотрон, включаем нашу позу
        if (itemStack.getItem() instanceof blusunrize.immersiveengineering.common.items.RailgunItem) {
            return RAILGUN_TWO_HANDED_POSE;
        }
        return null; // Для остальных предметов - стандартное поведение
    }

    // --- 2. ПОЛОЖЕНИЕ ОТ ПЕРВОГО ЛИЦА ---
    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        if (itemInHand.getItem() instanceof blusunrize.immersiveengineering.common.items.RailgunItem) {
            // Настраиваем положение от первого лица (сдвигаем в центр, чтобы имитировать хват двумя руками)
            int direction = (arm == HumanoidArm.RIGHT) ? 1 : -1;

            poseStack.translate(direction * 0.4F, -0.3F, -0.6F);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(direction * 15.0F));

            return true; // Отменяем стандартную отрисовку
        }
        return false;
    }
}