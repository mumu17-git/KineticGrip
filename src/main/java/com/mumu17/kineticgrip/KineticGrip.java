package com.mumu17.kineticgrip;

import com.mojang.logging.LogUtils;
import com.mumu17.kineticgrip.register.ModBlockEntityTypes;
import com.mumu17.kineticgrip.register.ModBlocks;
import com.mumu17.kineticgrip.register.ModCreativeTabs;
import com.mumu17.kineticgrip.register.ModRegistrate;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(KineticGrip.MODID)
public class KineticGrip {
    public static final String MODID = "kineticgrip";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KineticGrip(IEventBus bus) {
        ModBlocks.register();
        ModBlockEntityTypes.register();
        ModRegistrate.register(bus);
        ModCreativeTabs.register(bus);
    }
}
