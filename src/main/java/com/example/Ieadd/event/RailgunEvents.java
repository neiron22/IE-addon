package com.example.Ieadd.event;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import com.example.Ieadd.Ieadd;
import com.example.Ieadd.common.CustomRailgunShotEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

    public static void reRegisterDamage(Item item , double damage){
        if (item != null) {
            // Удаляем стандартный обработчик IE
            RailgunHandler.projectilePropertyMap.removeIf(pair ->
                    pair.getFirst().get().test(new ItemStack(item)));
            // Создаем ингредиент
            Ingredient ingredient = Ingredient.of(item);
            // Явно указываем типы <Supplier<Ingredient>, IRailgunProjectile>
            RailgunHandler.projectilePropertyMap.add(0, Pair.of(
                    () -> ingredient,
                    new CustomRailgunProjectile(item,damage)
            ));
        }
    }

    // Этот метод вызывается, когда мод загружается (на этапе CommonSetup)
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {

        //железный стержень
       Item ironRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "stick_iron"));
        reRegisterDamage(ironRod , 28.0);
        Item steelRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "stick_steel"));
        reRegisterDamage(steelRod , 45.0);

        Item aluminumRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "stick_aluminum"));
        reRegisterDamage(aluminumRod , 20.0);

        Item graphite_electrodeRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "graphite_electrode"));
        reRegisterDamage(graphite_electrodeRod , 96.0);

        Item sawbladeRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "sawblade"));
        reRegisterDamage(sawbladeRod , 70.0);
        // вот тут например
        
        
        Item blaze_rodRod = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "blaze_rod"));
        reRegisterDamage(blaze_rodRod , 38.0);
        
        });
    }

    // Внутренний класс, который реализует логику урона
    public static class CustomRailgunProjectile implements RailgunHandler.IRailgunProjectile {


        private final double damage;
        private final Item ammoItem;
        private static final double GRAVITY = 0.8;

        public CustomRailgunProjectile(Item ammoItem, double damage) {
            this.ammoItem = ammoItem;
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
            if (shooter == null) {
                return defaultProjectile;
            }

            // 1. Создаем наш снаряд
            CustomRailgunShotEntity projectile = new CustomRailgunShotEntity(shooter.level(), shooter, 3.0F, 1.0F, ammo);

            // 2. Получаем точку глаз игрока (откуда смотрит камера)
            Vec3 eyePos = shooter.getEyePosition(1.0F);

            // 3. Получаем вектор взгляда (куда смотрит игрок)
            Vec3 lookVec = shooter.getViewVector(1.0F);

            // 4. Сдвигаем точку появления чуть вперед, чтобы снаряд не задел самого стрелка
            Vec3 spawnPos = eyePos.add(lookVec.scale(0.5));

            // 5. Устанавливаем снаряду точную позицию и направление полета
            projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            projectile.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0F, 0.0F); // 3.0F - скорость, 0.0F - без разброса

            return projectile;
        }

        @Override
        public void onHitTarget(Level world, HitResult target, @Nullable UUID shooter, Entity projectile) {
            // Проверяем, что мы на сервере и попали в сущность
            if (world.isClientSide || !(target instanceof EntityHitResult entityHit)) {
                return;
            }

            Entity hitEntity = entityHit.getEntity();

            // --- СПЕЦИАЛЬНЫЕ ЭФФЕКТЫ ДЛЯ РАЗНЫХ СНАРЯДОВ ---

            // 1. Пильный диск: ломает блоки (если попал в блок)
            if (this.ammoItem == ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "sawblade"))) {
                if (target instanceof BlockHitResult blockHit) {
                    // Пример логики: ломаем блок
                    world.destroyBlock(blockHit.getBlockPos(), true);
                }
            }

            // 2. Огненный стержень: поджигает цель
            if (this.ammoItem == Items.BLAZE_ROD) {
                if (hitEntity != null) {
                    hitEntity.setSecondsOnFire(12); // Поджигаем на 12 секунд
                }
            }

            // 3. Графитовый электрод: накладывает эффект или наносит доп. урон
            if (this.ammoItem == ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "graphite_electrode"))) {
                if (hitEntity instanceof LivingEntity living) {
                    // Пример: накладываем эффект "Слабость" на 3 секунды
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
                }
            }
        }
    }
}