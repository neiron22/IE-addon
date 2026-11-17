package com.example.Ieadd.mixin;

import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {

    @Inject(method = "setupAnim", at = @At("TAIL") , remap = true)
    private void onSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {


        if (Minecraft.getInstance().screen != null) return;

        if (entity instanceof Player player) {
            PlayerModel<T> model = (PlayerModel<T>) (Object) this;

            // Получаем углы взгляда головы
            float aimPitch = model.head.xRot;
            float aimYaw = model.head.yRot;

            // --- ЛОГИКА ДЛЯ РЕЛЬСОТРОНА ---
            if (player.getMainHandItem().getItem() instanceof RailgunItem) {
                float baseRightX = -1.5F;
                float baseLeftX = -1.4F;
                float baseRightY = -0.15F;
                float baseLeftY = 0.65F;

                // Заменяем углы на наши базовые + взгляд
                model.rightArm.xRot = baseRightX + aimPitch;
                model.leftArm.xRot = baseLeftX +(aimPitch * 0.7F);
                model.rightArm.yRot = baseRightY + aimYaw;
                model.leftArm.yRot = baseLeftY + (aimYaw* 0.7F);

                // Синхронизируем рукава
                model.rightSleeve.copyFrom(model.rightArm);
                model.leftSleeve.copyFrom(model.leftArm);
            }

            // --- ЛОГИКА ДЛЯ ХИМ. РАСПЫЛИТЕЛЯ ---
            else if (player.getMainHandItem().getItem() instanceof ChemthrowerItem || player.getOffhandItem().getItem() instanceof ChemthrowerItem) {
                // Прибавляем углы взгляда к тем углам, которые уже выставила игра
                model.rightArm.xRot += (aimPitch + -0.15F);
                model.leftArm.xRot += (aimPitch * 0.7F);
                model.rightArm.yRot += (aimYaw +0.1F);
                model.leftArm.yRot += (aimYaw* 0.7F);

                // Синхронизируем рукава
                model.rightSleeve.copyFrom(model.rightArm);
                model.leftSleeve.copyFrom(model.leftArm);
            }
        }
    }
}