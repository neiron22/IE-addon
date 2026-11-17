package com.example.Ieadd.event;

import blusunrize.immersiveengineering.common.register.IEPotions;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.example.Ieadd.Ieadd;

@Mod.EventBusSubscriber(modid = Ieadd.MODID)
public class DamageEvents {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 1. Проверяем, что урон наносится огнём
        if (!event.getSource().is(DamageTypes.ON_FIRE) && !event.getSource().is(DamageTypes.IN_FIRE)) {
            return;
        }

        LivingEntity entity = event.getEntity();
        
        // 2. Проверяем наличие эффекта «Горючий» (Flammable)
        MobEffectInstance flammable = event.getEntity().getEffect(IEPotions.FLAMMABLE.get());
        if (flammable == null) {
            return;
        }

        int maxFireTicks = 100;
        if (entity.getRemainingFireTicks() > maxFireTicks) {
            entity.setRemainingFireTicks(maxFireTicks);
        }

        // 3. Вычисляем текущий множитель IE (1.5 + amplifier^2 * 0.5)
        double currentMultiplier = 1.5 + (flammable.getAmplifier() * flammable.getAmplifier() * 0.5);

        // 4. Целевой урон — 5 HP (2.5 сердца) за такт
        // Базовый урон огня в Minecraft — 1.0 HP, поэтому целевой множитель — 5.0
        double targetMultiplier = 5.0;

        // 5. Дополнительный множитель, который нужно применить поверх текущего
        double extraMultiplier = targetMultiplier / currentMultiplier;

        // 6. Применяем
        event.setAmount((float) (event.getAmount() * extraMultiplier));
    }
}