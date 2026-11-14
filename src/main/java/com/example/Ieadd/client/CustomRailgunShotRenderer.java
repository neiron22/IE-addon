package com.example.Ieadd.client;

import com.example.Ieadd.common.CustomRailgunShotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;

public class CustomRailgunShotRenderer extends EntityRenderer<CustomRailgunShotEntity> {
    private final ItemRenderer itemRenderer;

    public CustomRailgunShotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.0F; // Убираем тень, снарядам она не нужна
    }

    @Override
    public void render(CustomRailgunShotEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // 1. Получаем вектор направления, куда летит снаряд
        // getViewVector(1.0f) возвращает нормализованный вектор (x, y, z)
        Vec3 lookVec = entity.getViewVector(1.0f);

        // 2. Вычисляем Yaw (поворот по горизонтали) из компонент X и Z
        // atan2(z, x) даёт угол в радианах, переводим в градусы и вычитаем 90,
        // так как в Minecraft ось Z — это "вперёд" по умолчанию для моделей.
        float yaw = (float) (Math.atan2(lookVec.z, lookVec.x) * 180.0 / Math.PI) - 90.0F;

        // 3. Вычисляем Pitch (наклон вверх/вниз) из Y и горизонтальной длины
        float pitch = (float) (-(Math.atan2(lookVec.y, Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z)) * 180.0 / Math.PI));

        // 4. Применяем повороты
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));   // Сначала Yaw
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch)); // Затем Pitch
        // Поворачиваем модель к камере (билборд)
        //poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        // Масштабируем (подберите размер)
        poseStack.scale(1.2F, 1.0F, 2.0F);



        //poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        // для выравнивания
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));

        ItemStack ammo = entity.getAmmo();
        if (!ammo.isEmpty()) {
            // ItemRenderer автоматически прочитает ваш JSON и нарисует 2D-модель
            this.itemRenderer.renderStatic(
                    ammo,
                    ItemDisplayContext.GROUND, // Этот контекст идеален для плоских предметов
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CustomRailgunShotEntity entity) {
        // Этот метод обязателен, но при использовании ItemRenderer он не используется напрямую
        return new ResourceLocation("minecraft", "textures/item/iron_ingot.png");
    }
}