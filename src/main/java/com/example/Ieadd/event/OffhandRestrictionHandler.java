package com.example.Ieadd.event;

import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.example.Ieadd.Ieadd;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingSwapItemsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ieadd.MODID)
public class OffhandRestrictionHandler {

    // 1. Запрещаем свап рук (нажатие клавиши "F"), если в левую руку летит наш инструмент
    @SubscribeEvent
    public static void onSwapHands(LivingSwapItemsEvent.Hands event) {
        if (event.getEntity() instanceof Player player) {

            ItemStack goingToOffHand = event.getItemSwappedToOffHand(); // из правой в левую
            ItemStack goingToMainHand = event.getItemSwappedToMainHand(); // из левой в правую

            if (goingToOffHand.getItem() instanceof RailgunItem || goingToOffHand.getItem() instanceof ChemthrowerItem ||
                    goingToMainHand.getItem() instanceof RailgunItem || goingToMainHand.getItem() instanceof ChemthrowerItem) {
                event.setCanceled(true);
            }
        }
    }

    // 2. Страховка: если предмет как-то попал в левую руку (шифт-клик, подбор и т.д.), выкидываем его оттуда
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;
        ItemStack offHand = player.getOffhandItem();

        // Если в ЛЕВОЙ руке оказался рельсотрон или распылитель
        if (offHand.getItem() instanceof RailgunItem || offHand.getItem() instanceof ChemthrowerItem) {

            ItemStack copy = offHand.copy();

            // Сразу очищаем левую руку
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);

            // Пытаемся переложить в правую руку (если там пусто)
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, copy);
            }
            // Если в правой занято — пытаемся засунуть в инвентарь
            else {
                boolean added = false;
                for (int i = 0; i < 36; i++) {
                    if (player.getInventory().getItem(i).isEmpty()) {
                        player.getInventory().setItem(i, copy);
                        added = true;
                        break;
                    }
                }
                // Если места нет — дропаем
                if (!added) {
                    player.drop(copy, false);
                }
            }
        }
    }
}