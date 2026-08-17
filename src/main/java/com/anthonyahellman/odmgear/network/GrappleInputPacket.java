package com.anthonyahellman.odmgear.network;

import com.anthonyahellman.odmgear.grapple.GrappleState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record GrappleInputPacket(Action action, Vec3 target) {
    public enum Action {
        TOGGLE_LEFT,
        TOGGLE_RIGHT,
        TOGGLE_AUTO_DETACH,
        BOOST_ON,
        BOOST_OFF
    }

    public static void encode(GrappleInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
        buffer.writeBoolean(packet.target != null);
        if (packet.target != null) {
            buffer.writeDouble(packet.target.x);
            buffer.writeDouble(packet.target.y);
            buffer.writeDouble(packet.target.z);
        }
    }

    public static GrappleInputPacket decode(FriendlyByteBuf buffer) {
        Action action = buffer.readEnum(Action.class);
        Vec3 target = buffer.readBoolean()
                ? new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
                : null;
        return new GrappleInputPacket(action, target);
    }

    public static void handle(GrappleInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            GrappleState.handleInput(player, packet.action, packet.target);
        }
        context.setPacketHandled(true);
    }
}
