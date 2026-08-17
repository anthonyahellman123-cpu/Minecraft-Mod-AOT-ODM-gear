package com.anthonyahellman.odmgear.network;

import com.anthonyahellman.odmgear.grapple.GrappleState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record GrappleInputPacket(Action action) {
    public enum Action {
        TOGGLE_LEFT,
        TOGGLE_RIGHT,
        TOGGLE_AUTO_DETACH,
        BOOST_ON,
        BOOST_OFF
    }

    public static void encode(GrappleInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
    }

    public static GrappleInputPacket decode(FriendlyByteBuf buffer) {
        return new GrappleInputPacket(buffer.readEnum(Action.class));
    }

    public static void handle(GrappleInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            GrappleState.handleInput(player, packet.action);
        }
        context.setPacketHandled(true);
    }
}
