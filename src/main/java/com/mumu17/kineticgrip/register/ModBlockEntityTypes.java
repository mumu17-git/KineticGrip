package com.mumu17.kineticgrip.register;

import com.mumu17.kineticgrip.block.GripHandleBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.mumu17.kineticgrip.register.ModRegistrate.REGISTRATE;

public class ModBlockEntityTypes {
    public static final BlockEntityEntry<GripHandleBlockEntity> GRIP_HANDLE = REGISTRATE.blockEntity("grip_handle", GripHandleBlockEntity::new).validBlock(ModBlocks.GRIP_HANDLE).register();

    public static void register() {}
}
