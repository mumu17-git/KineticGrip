package com.mumu17.kineticgrip.block;

import com.mumu17.kineticgrip.KineticGrip;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import dev.ryanhcode.sable.api.physics.constraint.fixed.FixedConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.fixed.FixedConstraintHandle;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.simulated_team.simulated.content.blocks.handle.HandleBlockEntity;
import dev.simulated_team.simulated.content.blocks.util.AbstractDirectionalAxisBlock;
import dev.simulated_team.simulated.service.SimConfigService;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;


public class GripHandleBlockEntity extends HandleBlockEntity {

    private final Map<UUID, GripHandleConstraint> players = new Object2ObjectOpenHashMap<>();

    public GripHandleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private void checkPlayers() {
        assert this.level != null;

        Iterator<Map.Entry<UUID, GripHandleConstraint>> it = this.players.entrySet().iterator();

        while(it.hasNext()) {
            Map.Entry<UUID, GripHandleConstraint> entry = it.next();
            Player player = this.level.getPlayerByUUID((UUID)entry.getKey());
            GripHandleConstraint constraint = (GripHandleConstraint)entry.getValue();
            if (player != null && !player.isDeadOrDying()) {
                if (constraint == null || !constraint.hasJoint()) {
                    player.resetFallDistance();
                }
            } else {
                if (constraint != null) {
                    constraint.removeJoint();
                }

                it.remove();
                this.setChanged();
            }
        }

    }

    @Override
    public void sable$physicsTick(final ServerSubLevel subLevel, final RigidBodyHandle handle, final double timeStep) {
        this.checkPlayers();

        for(GripHandleConstraint constraint : this.players.values()) {
            constraint.physicsTick(subLevel, handle);
        }

    }

    @Override
    public void startGrabbingServer(UUID player, float desiredRange) {
        if (this.players.containsKey(player)) {
            ((GripHandleConstraint)this.players.get(player)).setScrollDistance(desiredRange);
        } else {
            GripHandleConstraint handle = new GripHandleConstraint(player, desiredRange, (PhysicsConstraintHandle)null);
            this.players.put(player, handle);
            this.setChanged();
        }
    }

    @Override
    public void stopGrabbingServer(UUID player) {
        GripHandleConstraint constraint = (GripHandleConstraint)this.players.remove(player);
        this.setChanged();
        if (constraint != null) {
            constraint.removeJoint();
        }

    }

    @Override
    public void remove() {
        super.remove();
        this.players.values().forEach(GripHandleConstraint::removeJoint);
        this.players.clear();
        this.setChanged();
    }

    private class GripHandleConstraint {
        private static final double CONSTRAINT_DAMPING = (double)30.0F;
        private static final double CONSTRAINT_STIFFNESS = (double)240.0F;
        private final UUID playerId;
        private float scrollDistance;
        private @Nullable PhysicsConstraintHandle constraintHandle;

        public GripHandleConstraint(final UUID playerId, final float scrollDistance, final PhysicsConstraintHandle constraintHandle) {
            super();
            this.playerId = playerId;
            this.scrollDistance = scrollDistance;
            this.constraintHandle = constraintHandle;
        }

        public void physicsTick(ServerSubLevel subLevel, RigidBodyHandle handle) {
            this.removeJoint();
            Player player = GripHandleBlockEntity.this.level != null ? GripHandleBlockEntity.this.level.getPlayerByUUID(this.playerId) : null;
            if (player != null) {
                boolean isJumping = (player.getDeltaMovement().y > 0 || player.getDeltaMovement().y < 0) && !player.onGround();
                if (player.onGround() || isJumping || player.isInWater() || player.getAbilities().flying || player.onClimbable()) {
                    SubLevel standingSubLevel = Sable.HELPER.getTrackingSubLevel(player);
                    if (standingSubLevel != subLevel) {
                        Vector3d constraintGoal = JOMLConversion.toJOML(player.getEyePosition().add(player.getLookAngle().scale(Math.max((double)2.0F, (double)this.scrollDistance))));
                        Vector3d constraintPosition = GripHandleBlockEntity.this.getGrabCenter();
                        Vec3 look = player.getLookAngle();
                        double yawRad = Math.atan2(look.x, look.z);

                        BlockState state = GripHandleBlockEntity.this.getBlockState();
                        Direction facing = state.getValue(DirectionalBlock.FACING);
                        boolean axisAlongFirst = state.getValue(AbstractDirectionalAxisBlock.AXIS_ALONG_FIRST_COORDINATE);
                        Vector3d forward = new Vector3d(
                                facing.getStepX(),
                                facing.getStepY(),
                                facing.getStepZ()
                        );
                        Vector3d up = Math.abs(forward.y) > 0.999D
                                ? new Vector3d(0, 0, 1)
                                : new Vector3d(0, 1, 0);
                        Quaterniond initialRot = new Quaterniond()
                                .lookAlong(forward, up)
                                .rotateY(Math.PI);
                        if (axisAlongFirst) {
                            // facing方向に応じた回転軸の選択
                            switch (facing) {
                                case NORTH, SOUTH ->
                                    initialRot.rotateZ((Math.PI / 2.0D) * (facing.equals(Direction.NORTH) ? 1 : -1));
                                case UP, DOWN ->
                                    initialRot.rotateY(Math.PI / 2.0D * (facing.equals(Direction.UP) ? 1 : -1));
                            }
                        } else {
                            switch (facing) {
                                case EAST, WEST ->
                                initialRot.rotateX(Math.PI / 2.0D * (facing.equals(Direction.EAST) ? 1 : -1));
                            }
                        }

                        // Y座標の健全性チェックと制限
                        final double MAX_Y_COORDINATE = 1000.0D;
                        boolean validConstraintGoal = !Double.isNaN(constraintGoal.y) && !Double.isInfinite(constraintGoal.y) && Math.abs(constraintGoal.y) <= MAX_Y_COORDINATE;
                        boolean validConstraintPosition = !Double.isNaN(constraintPosition.y) && !Double.isInfinite(constraintPosition.y) && Math.abs(constraintPosition.y) <= MAX_Y_COORDINATE;

                        if (validConstraintGoal && validConstraintPosition) {
                            double validRange = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue() + (double)2.0F;
                            double currentDistance = Sable.HELPER.distanceSquaredWithSubLevels(GripHandleBlockEntity.this.level, constraintGoal, constraintPosition);
                            if (!Mth.equal(-1.0F, this.scrollDistance) && !(currentDistance > validRange * validRange)) {
                                ServerSubLevelContainer container = SubLevelContainer.getContainer(subLevel.getLevel());

                                assert container != null;

                                SubLevelPhysicsSystem physicsSystem = container.physicsSystem();

                                Quaterniond quaterniond = new Quaterniond().rotateY(-yawRad).invert().mul(initialRot).rotateY(Math.PI);

                                PhysicsConstraintConfiguration<FixedConstraintHandle> configuration = new FixedConstraintConfiguration(constraintGoal, constraintPosition, quaterniond);
                                this.constraintHandle = physicsSystem.getPipeline().addConstraint((ServerSubLevel) null, subLevel, configuration);
                                if (this.constraintHandle != null) {
                                    double maxForce = (double) SimConfigService.INSTANCE.server().physics.handleMaxForce.getF();

                                    for (ConstraintJointAxis axis : ConstraintJointAxis.LINEAR) {
                                        this.constraintHandle.setMotor(axis, (double) 0.0F, (double) 240.0F / 8, (double) 30.0F, true, maxForce);
                                    }

                                    for (ConstraintJointAxis axis : ConstraintJointAxis.ANGULAR) {
                                        this.constraintHandle.setMotor(axis, (double) 0.0F, (double) 0.0F, (double) 4.5F, true, maxForce);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        public boolean hasJoint() {
            return this.constraintHandle != null;
        }

        public void removeJoint() {
            if (this.constraintHandle != null) {
                this.constraintHandle.remove();
                this.constraintHandle = null;
            }

        }

        public void setScrollDistance(float desiredRange) {
            this.scrollDistance = (float)Math.min((double)desiredRange, (double)2.5F);
        }
    }
}
