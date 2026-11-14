package com.example.Ieadd.mixin;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.example.Ieadd.client.RailgunClientExtensions;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.item.Item.Properties;
import java.util.function.Consumer;

@Mixin(RailgunItem.class)
public abstract class RailgunItemMixin extends Item {

    // Конструктор нужен, так как мы наследуемся от Item для доступа к методу
    public RailgunItemMixin(Properties properties) {
        super(properties);
    }

    // Внедряемся в метод инициализации клиентских свойств предмета
    @Inject(method = "initializeClient", at = @At("HEAD"), remap = false)
    public void onInitializeClient(Consumer<IClientItemExtensions> consumer, CallbackInfo ci) {
        consumer.accept(new RailgunClientExtensions());
    }
}