package com.mumu17.kineticgrip.register;

import com.mumu17.kineticgrip.KineticGrip;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KineticGrip.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            TABS.register("main", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + KineticGrip.MODID))
                            .icon(() -> new ItemStack((ItemLike) ModBlocks.GRIP_HANDLE.get()))
                            .build()
            );

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }

}
