package com.anthonyahellman.odmgear.network;

import com.anthonyahellman.odmgear.grapple.GrappleState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MovementInputPacket(boolean forward, boolean backward, boolean left,
                                  boolean right, boolean up, boolean down) {
    public static void encode(MovementInputPacket packet, FriendlyByteBuf buffer) {
        int flags = (packet.forward ? 1 : 0)
                | (packet.backward ? 2 : 0)
                | (packet.left ? 4 : 0)
                | (packet.right ? 8 : 0)
                | (packet.up ? 16 : 0)
                | (packet.down ? 32 : 0);
        buffer.writeByte(flags);
    }

    public static MovementInputPacket decode(FriendlyByteBuf buffer) {
        int flags = buffer.readUnsignedByte();
        return new MovementInputPacket((flags & 1) != 0, (flags & 2) != 0,
                (flags & 4) != 0, (flags & 8) != 0,
                (flags & 16) != 0, (flags & 32) != 0);
    }

    public static void handle(MovementInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            GrappleState.setMovementInput(player, packet);
        }
        context.setPacketHandled(true);
    }
}
