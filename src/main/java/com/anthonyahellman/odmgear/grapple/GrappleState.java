package com.anthonyahellman.odmgear.grapple;

import com.anthonyahellman.odmgear.network.GrappleInputPacket;
import com.anthonyahellman.odmgear.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class GrappleState {
    private static final String ROOT_TAG = "odmgear_grapple";
    private static final double RANGE = 64.0D;
    private static final double MIN_ROPE_LENGTH = 4.0D;
    private static final double MAX_ACCELERATION = 0.18D;
    private static final double BOOST_ACCELERATION = 0.065D;
    private static final double MAX_SPEED = 3.2D;

    private GrappleState() {
    }

    public static boolean hasHarness(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ODM_HARNESS.get());
    }

    public static void handleInput(ServerPlayer player, GrappleInputPacket.Action action) {
        CompoundTag state = getState(player);
        if (!hasHarness(player)) {
            clear(state);
            return;
        }

        switch (action) {
            case TOGGLE_LEFT -> toggleCable(player, state, "left");
            case TOGGLE_RIGHT -> toggleCable(player, state, "right");
            case BOOST_ON -> state.putBoolean("boost", true);
            case BOOST_OFF -> state.putBoolean("boost", false);
        }
    }

    public static void tick(ServerPlayer player) {
        CompoundTag state = getState(player);
        if (!hasHarness(player)) {
            clear(state);
            return;
        }

        boolean left = state.getBoolean("left_active");
        boolean right = state.getBoolean("right_active");
        if (!left && !right) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        if (left) {
            velocity = applyCableForce(player.position(), velocity, readAnchor(state, "left"),
                    state.getDouble("left_length"));
        }
        if (right) {
            velocity = applyCableForce(player.position(), velocity, readAnchor(state, "right"),
                    state.getDouble("right_length"));
        }

        if (state.getBoolean("boost")) {
            velocity = velocity.add(player.getLookAngle().scale(BOOST_ACCELERATION));
        }

        double speed = velocity.length();
        if (speed > MAX_SPEED) {
            velocity = velocity.scale(MAX_SPEED / speed);
        }

        player.setDeltaMovement(velocity);
        player.hurtMarked = true;
        player.fallDistance = Math.min(player.fallDistance, 3.0F);
    }

    private static void toggleCable(ServerPlayer player, CompoundTag state, String side) {
        String activeKey = side + "_active";
        if (state.getBoolean(activeKey)) {
            state.putBoolean(activeKey, false);
            return;
        }

        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(RANGE));
        BlockHitResult hit = player.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        Vec3 anchor = hit.getLocation();
        writeAnchor(state, side, anchor);
        state.putDouble(side + "_length", Math.max(MIN_ROPE_LENGTH,
                player.position().distanceTo(anchor) * 0.82D));
        state.putBoolean(activeKey, true);
    }

    private static Vec3 applyCableForce(Vec3 playerPosition, Vec3 velocity, Vec3 anchor, double ropeLength) {
        Vec3 toAnchor = anchor.subtract(playerPosition);
        double distance = toAnchor.length();
        if (distance < 0.001D || distance <= ropeLength) {
            return velocity;
        }

        Vec3 direction = toAnchor.scale(1.0D / distance);
        double stretch = distance - ropeLength;
        Vec3 result = velocity.add(direction.scale(Math.min(MAX_ACCELERATION, stretch * 0.035D)));

        double radialVelocity = result.dot(direction);
        if (radialVelocity < 0.0D) {
            result = result.add(direction.scale(-radialVelocity * 0.28D));
        }
        return result;
    }

    private static CompoundTag getState(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_TAG)) {
            persistent.put(ROOT_TAG, new CompoundTag());
        }
        return persistent.getCompound(ROOT_TAG);
    }

    private static void writeAnchor(CompoundTag state, String side, Vec3 anchor) {
        state.putDouble(side + "_x", anchor.x);
        state.putDouble(side + "_y", anchor.y);
        state.putDouble(side + "_z", anchor.z);
    }

    private static Vec3 readAnchor(CompoundTag state, String side) {
        return new Vec3(state.getDouble(side + "_x"), state.getDouble(side + "_y"),
                state.getDouble(side + "_z"));
    }

    private static void clear(CompoundTag state) {
        state.putBoolean("left_active", false);
        state.putBoolean("right_active", false);
        state.putBoolean("boost", false);
    }
}
