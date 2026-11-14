package com.example.Ieadd.mixin;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), remap = false)
    private void onRenderArmWithItem(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci) {

        // Если в руке рельсотрон
        if (stack.getItem() instanceof RailgunItem) {
            int direction = (hand == InteractionHand.MAIN_HAND) ? 1 : -1;

            // --- НАСТРОЙКА ПОЛОЖЕНИЯ РУКИ ОТ ПЕРВОГО ЛИЦА ---
            poseStack.translate(direction * 0.35F, -0.2F, -0.1F);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(direction * 15.0F));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(5.0F));
        }
    }
}