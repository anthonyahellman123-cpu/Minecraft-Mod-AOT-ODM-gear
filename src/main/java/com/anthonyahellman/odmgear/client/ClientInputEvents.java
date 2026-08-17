package com.anthonyahellman.odmgear.client;

import com.anthonyahellman.odmgear.OdmGearMod;
import com.anthonyahellman.odmgear.network.GrappleInputPacket;
import com.anthonyahellman.odmgear.network.ModNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ClientInputEvents {
    private static final String CATEGORY = "key.categories.odmgear";
    private static final KeyMapping LEFT_GRAPPLE = new KeyMapping(
            "key.odmgear.left_grapple", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);
    private static final KeyMapping RIGHT_GRAPPLE = new KeyMapping(
            "key.odmgear.right_grapple", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY);
    private static final KeyMapping GAS_BOOST = new KeyMapping(
            "key.odmgear.gas_boost", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    private static boolean boostWasDown;

    private ClientInputEvents() {
    }

    @Mod.EventBusSubscriber(modid = OdmGearMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT)
    public static final class ModBusEvents {
        private ModBusEvents() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(LEFT_GRAPPLE);
            event.register(RIGHT_GRAPPLE);
            event.register(GAS_BOOST);
        }
    }

    @Mod.EventBusSubscriber(modid = OdmGearMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT)
    public static final class ForgeBusEvents {
        private ForgeBusEvents() {
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            while (LEFT_GRAPPLE.consumeClick()) {
                ModNetwork.sendInput(GrappleInputPacket.Action.TOGGLE_LEFT);
            }
            while (RIGHT_GRAPPLE.consumeClick()) {
                ModNetwork.sendInput(GrappleInputPacket.Action.TOGGLE_RIGHT);
            }

            boolean boostDown = GAS_BOOST.isDown();
            if (boostDown != boostWasDown) {
                ModNetwork.sendInput(boostDown
                        ? GrappleInputPacket.Action.BOOST_ON
                        : GrappleInputPacket.Action.BOOST_OFF);
                boostWasDown = boostDown;
            }
        }
    }
}
