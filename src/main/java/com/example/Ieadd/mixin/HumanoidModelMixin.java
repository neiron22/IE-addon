package com.example.Ieadd.mixin;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {

    @Inject(method = "setupAnim", at = @At("TAIL"), remap = false)
    private void onSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        // Если сущность - игрок и он держит рельсотрон
        if (entity instanceof Player player && player.getMainHandItem().getItem() instanceof RailgunItem) {
            HumanoidModel<T> model = (HumanoidModel<T>) (Object) this;

            // Принудительно поднимаем обе руки (имитация двуручного хвата)
            model.rightArm.xRot = -1.4F;
            model.rightArm.yRot = -0.2F;

            model.leftArm.xRot = -1.4F;
            model.leftArm.yRot = 0.2F;
        }
    }
}