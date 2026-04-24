package com.mumu17.kineticgrip.register;

import com.mumu17.kineticgrip.KineticGrip;
import com.simibubi.create.foundation.data.CreateRegistrate;
import dev.simulated_team.simulated.registrate.simulated_tab.CreativeTabItemTransforms;
import dev.simulated_team.simulated.registrate.simulated_tab.SimulatedCreativeTab;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;

public class ModRegistrate {

    public static final CreateRegistrate REGISTRATE =
            CreateRegistrate.create(KineticGrip.MODID).defaultCreativeTab(ModCreativeTabs.MAIN_TAB.getKey());

    public static void register(IEventBus bus) {
        REGISTRATE.registerEventListeners(bus);
    }

}
