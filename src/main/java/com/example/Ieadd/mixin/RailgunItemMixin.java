package com.example.Ieadd.mixin;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Внутри вашего RailgunItemMixin:
// ВОТ ЭТА СТРОКА ОБЯЗАТЕЛЬНА! Без неё Mixin не работает.
@Mixin(RailgunItem.class)
public abstract class RailgunItemMixin extends Item {

    // Конструктор (нужен, так как мы наследуемся от Item)
    public RailgunItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = {"onUseTick", "m_5929_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void onUseTickCopper(Level level, LivingEntity user, ItemStack stack, int count, CallbackInfo ci) {
        if (user instanceof Player player) {
            // Проверяем, какой снаряд сейчас выбран
            ItemStack ammo = RailgunItem.findAmmo(stack, player);

            if (ammo.getItem() == Items.COPPER_INGOT) {
                int inUse = stack.getUseDuration() - count;
                int originalChargeTime = RailgunItem.getChargeTime(stack);

                // Зарядка на 75% быстрее (умножаем на 0.25)
                int newChargeTime = (int) (originalChargeTime * 0.25f);

                // Если зарядка достигла нужного значения — стреляем автоматически
                if (inUse >= newChargeTime) {
                    if (!level.isClientSide) {
                        int consumption = IEServerConfig.TOOLS.railgun_consumption.get();
                        IEnergyStorage energy = CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);

                        // Тратим в 2 раза меньше энергии (умножаем на 0.5)
                        int cheaperConsumption = (int) (consumption * 0.5f);

                        if (energy.extractEnergy(cheaperConsumption, true) == cheaperConsumption) {
                            ItemStack ammoConsumed = ammo.split(1);
                            RailgunItem.fireProjectile(stack, level, user, ammoConsumed);
                            energy.extractEnergy(cheaperConsumption, false);

                            // Звук выстрела
                            level.playSound(null, user.getX(), user.getY(), user.getZ(), IESounds.railgunFire.get(), SoundSource.PLAYERS, 1.0F, 0.5F + 0.5F * user.getRandom().nextFloat());
                        }
                    }
                    // Останавливаем использование, так как выстрел уже произошел
                    user.stopUsingItem();
                    player.getCooldowns().addCooldown(stack.getItem(), 0);
                    ci.cancel(); // Отменяем стандартную логику (звуки зарядки)
                }
            }
        }
    }
}