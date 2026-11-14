package com.example.Ieadd.mixin;

import blusunrize.immersiveengineering.client.models.obj.callback.item.RailgunCallbacks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext; // <-- Новый правильный импорт
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RailgunCallbacks.class)
public class RailgunCallbacksMixin {

    @Inject(method = "handlePerspective", at = @At("HEAD"), remap = false)
    private void onHandlePerspective(RailgunCallbacks.Key key, LivingEntity holder, ItemDisplayContext cameraTransformType, PoseStack mat, CallbackInfo ci) {

        // Проверяем вид от первого лица, используя константы из ItemDisplayContext
        if (cameraTransformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || cameraTransformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {

            // --- ТВОИ ИЗМЕНЕНИЯ ЗДЕСЬ ---

            // Пример: увеличение размера на 20%
            // mat.scale(1.2F, 1.2F, 1.2F);

            // Пример: смещение модели в центр экрана
            // mat.translate(0.0F, -0.1F, 0.0F);

            // Пример: поворот ствола
            // mat.mulPose(com.mojang.math.Axis.YP.rotationDegrees(5.0F));
        }
    }
}