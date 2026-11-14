package com.example.Ieadd.event;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import com.example.Ieadd.Ieadd;
import com.example.Ieadd.common.CustomRailgunShotEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// Эта аннотация говорит Forge: "Вызывай методы в этом классе на нужных этапах загрузки"
@Mod.EventBusSubscriber(modid = Ieadd.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RailgunEvents {

    public static void reregisterDamage(Item item , double damage){
        if (item != null) {
            // Удаляем стандартный обработчик IE
            RailgunHandler.projectilePropertyMap.removeIf(pair ->
                    pair.getFirst().get().test(new ItemStack(item)));
            // Создаем ингредиент
            Ingredient ingredient = Ingredient.of(item);
            // Явно указываем типы <Supplier<Ingredient>, IRailgunProjectile>
            RailgunHandler.projectilePropertyMap.add(0, Pair.of(
                    () -> ingredient,
                    new CustomRailgunProjectile(damage)
            ));
        }
    }

    // Этот метод вызывается, когда мод загружается (на этапе CommonSetup)
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {

        //железный стержень
       Item ironRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "stick_iron"));
        reregisterDamage(ironRod , 28.0);
        Item steelRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "stick_steel"));
        reregisterDamage(steelRod , 45.0);

        Item aluminumRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "stick_aluminum"));
        reregisterDamage(aluminumRod , 22.0);

        Item graphite_electrodeRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "graphite_electrode"));
        reregisterDamage(graphite_electrodeRod , 96.0);

        Item sawbladeRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "sawblade"));
        reregisterDamage(sawbladeRod , 70.0);

        Item blaze_rodRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "blaze_rod"));
        reregisterDamage(blaze_rodRod , 40.0);
        });
    }

    // Внутренний класс, который реализует логику урона
    public static class CustomRailgunProjectile implements RailgunHandler.IRailgunProjectile {

        // 1. Создаем поле для хранения урона конкретного снаряда
        private final double damage;

        // 2. Создаем общую константу для гравитации (одинаковая для всех)
        private static final double GRAVITY = 0.2;

        // 3. Создаем конструктор, через который будем передавать урон при регистрации
        public CustomRailgunProjectile(double damage) {
            this.damage = damage;
        }

        @Override
        public double getDamage(Level world, Entity target, @Nullable UUID shooter, Entity projectile) {
            return this.damage;}

        @Override
        public double getGravity() {
            // 5. Возвращаем общую гравитацию
            return GRAVITY;}

        @Override
        public double getBreakChance(@Nullable UUID shooter, ItemStack ammo) {
            return 0.0; // 0.0 = 0% шанс исчезновения, 1.0 = 100%
        }

        @Override
        public Entity getProjectile(@Nullable Player shooter, ItemStack ammo, Entity defaultProjectile) {
            // Если shooter равен null (например, выстрел из раздатчика), возвращаем стандартный снаряд

            if (shooter == null) {
                return defaultProjectile;
            }

            // Создаем наш кастомный снаряд.
            // 3.0F и 1.0F — это стандартные скорость и точность, можете оставить их.
            return new CustomRailgunShotEntity(shooter.level(), shooter, 3.0F, 1.0F, ammo);
        }
    }
}