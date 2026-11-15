package com.example.Ieadd.common;

import com.example.Ieadd.Ieadd;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    // Создаем DeferredRegister для типов сущностей
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Ieadd.MODID);

    // Регистрируем нашу кастомную сущность
    public static final RegistryObject<EntityType<CustomRailgunShotEntity>> CUSTOM_RAILGUN_SHOT =
            ENTITY_TYPES.register("custom_railgun_shot",
                    () -> EntityType.Builder.<CustomRailgunShotEntity>of(CustomRailgunShotEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F) // Размер хитбокса (ширина, высота)
                            .clientTrackingRange(4) // Радиус, в котором клиенты видят сущность
                            .updateInterval(20) // Как часто обновляется позиция
                            .build("custom_railgun_shot"));

}