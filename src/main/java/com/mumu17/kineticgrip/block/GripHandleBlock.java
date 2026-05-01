package com.mumu17.kineticgrip.block;

import com.mumu17.kineticgrip.register.ModBlockEntityTypes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import dev.simulated_team.simulated.content.blocks.handle.HandleBlock;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class GripHandleBlock extends HandleBlock implements IWrenchable {

    public GripHandleBlock(Properties properties, @Nullable DyeColor dyeColor, Variant variant) {
        super(properties, dyeColor, variant);
    }

    @Override
    public BlockEntityType<? extends GripHandleBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.GRIP_HANDLE.get();
    }
}


