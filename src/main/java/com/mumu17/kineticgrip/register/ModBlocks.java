package com.mumu17.kineticgrip.register;

import com.mumu17.kineticgrip.KineticGrip;
import com.mumu17.kineticgrip.block.GripHandleBlock;
import com.mumu17.kineticgrip.block.GripHandleBlockEntity;
import com.simibubi.create.foundation.item.ItemDescription;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.service.SimBlockStateService;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;

public class ModBlocks {

    public static final BlockEntry<GripHandleBlock> GRIP_HANDLE;

    static {
        GRIP_HANDLE = ModRegistrate.REGISTRATE
                .block("grip_handle", (p) -> new GripHandleBlock(p, null, GripHandleBlock.Variant.IRON))
                .properties(p -> BlockBehaviour.Properties.ofFullCopy(SimBlocks.IRON_HANDLE.get()))
                .blockEntity(GripHandleBlockEntity::new).build()
                .blockstate((ctx, prov) -> SimBlockStateService.INSTANCE.directionalAxisBlock(ctx, prov, (blockState, vertical) -> {
                    String suffix = vertical ? "vertical" : "horizontal";
                    return prov.models().getExistingFile(prov.modLoc("block/grip_handle/block_" + suffix));
                }))
                .loot(RegistrateBlockLootTables::dropSelf)
                .onRegisterAfter(Registries.ITEM, (v) -> ItemDescription.useKey(v, "block."+ KineticGrip.MODID +".grip_handle"))
                .item().model((ctx, prov) -> ((ItemModelBuilder)prov.withExistingParent(ctx.getName(), prov.modLoc("block/grip_handle/item"))).texture("0", prov.modLoc("block/grip_handle/iron_handle"))).build()
                .register();
    }

    public static void register() {}

}
