package com.anthonyahellman.odmgear.network;

import com.anthonyahellman.odmgear.OdmGearMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(OdmGearMod.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private ModNetwork() {
    }

    public static void initialize() {
        CHANNEL.messageBuilder(GrappleInputPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(GrappleInputPacket::encode)
                .decoder(GrappleInputPacket::decode)
                .consumerMainThread(GrappleInputPacket::handle)
                .add();
    }

    public static void sendInput(GrappleInputPacket.Action action) {
        CHANNEL.sendToServer(new GrappleInputPacket(action));
    }
}
