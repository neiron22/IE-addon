package com.example.Ieadd.client;

import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.example.Ieadd.Ieadd;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ieadd.MODID, value = Dist.CLIENT)
public class ClientWeaponHandler {

    // 1. Отключаем ЛКМ (Атаку/Ломание блоков), если в руке рельсотрон или распылитель
    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof RailgunItem || mainHand.getItem() instanceof ChemthrowerItem) {
            if (event.isAttack()) { // Если нажата ЛКМ
                event.setCanceled(true);
            }
        }
    }

    // 2. Включаем зум на ЛКМ (пока зажата кнопка использования)
    // 2. Включаем зум на ЛКМ (пока зажата левая кнопка мыши)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // Если игрок в меню — не зумим
        if (player == null || mc.screen != null) return;

        ItemStack mainHand = player.getMainHandItem();
        boolean isRailgun = mainHand.getItem() instanceof RailgunItem railgun && railgun.canZoom(mainHand, player);

        // Проверяем, зажата ли ЛКМ (кнопка атаки)
        boolean isLeftClickDown = mc.options.keyAttack.isDown();

        // Если в руке рельсотрон со скопом И зажата ЛКМ — включаем зум
        if (isRailgun && isLeftClickDown) {
            ZoomHandler.isZooming = true;

            // Если зум только что включился — ставим базовый шаг
            if (ZoomHandler.fovZoom == 1.0F) {
                float[] steps = ((RailgunItem)mainHand.getItem()).getZoomSteps(mainHand, player);
                if (steps != null && steps.length > 0) {
                    ZoomHandler.fovZoom = steps[0];
                }
            }
        } else {
            // Если игрок отпустил ЛКМ или сменил предмет — выключаем зум
            if (ZoomHandler.isZooming) {
                ZoomHandler.isZooming = false;
                ZoomHandler.fovZoom = 1.0F;
            }
        }
    }
}