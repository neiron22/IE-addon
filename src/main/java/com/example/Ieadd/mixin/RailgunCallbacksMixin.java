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

    @Inject(method = "handlePerspective", at = @At("RETURN"), remap = false) // <-- ИЗМЕНИТЬ НА "RETURN"
    private void onHandlePerspective(RailgunCallbacks.Key key, LivingEntity holder, ItemDisplayContext cameraTransformType, PoseStack mat, CallbackInfo ci) {

        // --- ПЕРВОЕ ЛИЦО (у вас уже настроено) ---
        if (cameraTransformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || cameraTransformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            // mat.translate(0.0F, 0.0F, 0.0F);
            // mat.mulPose(com.mojang.math.Axis.YP.rotationDegrees(5.0F));
        }

        // --- ТРЕТЬЕ ЛИЦО (новая настройка) ---
        if (cameraTransformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || cameraTransformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            //System.out.println("!!! РЕНДЕР РЕЛЬСОТРОНА ОТ ТРЕТЬЕГО ЛИЦА ВЫЗВАН!");
            // 1. Смещение (Двигаем пушку внутри ладони)
            // X: влево/вправо, Y: вверх/вниз, Z: ближе к телу/дальше от тела
            mat.translate(-1.8F, -0.68F, 0.0F);

            // 2. Поворот (Крутим саму пушку)
            // YP - вокруг вертикальной оси, XP - вокруг горизонтальной, ZP - крен
            mat.mulPose(com.mojang.math.Axis.XP.rotationDegrees(0.0F));
            mat.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-15.0F));
            mat.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-35.0F));

            // 3. Масштаб (если пушка слишком большая или маленькая)
            // mat.scale(1.0F, 1.0F, 1.0F);
        }
    }
}