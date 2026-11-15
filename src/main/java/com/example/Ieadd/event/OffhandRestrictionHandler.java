package com.example.Ieadd.event;

import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.example.Ieadd.Ieadd;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ieadd.MODID)
public class OffhandRestrictionHandler {

    // 1. Запрещаем ИСПОЛЬЗОВАТЬ предметы в левой руке
    @SubscribeEvent
    public static void onStartUsingItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack stack = event.getItem();

            // Если пытаемся использовать рельсотрон или распылитель в левой руке — отменяем
            if (player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                if (stack.getItem() instanceof RailgunItem || stack.getItem() instanceof ChemthrowerItem) {
                    event.setCanceled(true);
                }
            }
        }
    }

    // 2. Следим за левой рукой во время использования основного предмета
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Проверяем только на сервере, в конце тика, чтобы не спамить
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        // Если в правой руке рельсотрон или распылитель, и игрок сейчас их использует
        boolean isMainHandTool = mainHand.getItem() instanceof RailgunItem || mainHand.getItem() instanceof ChemthrowerItem;

        if (isMainHandTool && !offHand.isEmpty()) {

            // Пытаемся засунуть предмет из левой руки в инвентарь
            boolean added = player.getInventory().add(offHand.copy());

            if (added) {
                // Если поместился — очищаем левую руку
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            } else {
                // Если инвентарь полон — выбрасываем предмет из игрока
                player.drop(offHand.copy(), true); // true = выбрасываем весь стак
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        }
    }
}