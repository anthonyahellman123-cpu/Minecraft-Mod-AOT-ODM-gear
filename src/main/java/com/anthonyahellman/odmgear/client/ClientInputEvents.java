package com.anthonyahellman.odmgear.client;

import com.anthonyahellman.odmgear.OdmGearMod;
import com.anthonyahellman.odmgear.network.GrappleInputPacket;
import com.anthonyahellman.odmgear.network.ModNetwork;
import com.anthonyahellman.odmgear.network.MovementInputPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
    private static final KeyMapping AUTO_DETACH = new KeyMapping(
            "key.odmgear.auto_detach", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);
    private static final KeyMapping AUTO_AIM = new KeyMapping(
            "key.odmgear.auto_aim", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);
    private static final KeyMapping ODM_DOWN = new KeyMapping(
            "key.odmgear.down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, CATEGORY);
    private static final KeyMapping HOOK_MODE = new KeyMapping(
            "key.odmgear.hook_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    private static boolean boostWasDown;
    private static boolean leftWasDown;
    private static boolean rightWasDown;
    private static HookMode hookMode = HookMode.HOLD;

    private enum HookMode {
        HOLD,
        TOGGLE
    }

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
            event.register(AUTO_DETACH);
            event.register(AUTO_AIM);
            event.register(ODM_DOWN);
            event.register(HOOK_MODE);
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

            handleHookInputs();
            while (AUTO_DETACH.consumeClick()) {
                ClientGrappleState.toggleAutoDetach();
                ModNetwork.sendInput(GrappleInputPacket.Action.TOGGLE_AUTO_DETACH);
            }
            while (AUTO_AIM.consumeClick()) {
                ClientGrappleState.toggleAutoAim();
            }
            while (HOOK_MODE.consumeClick()) {
                hookMode = hookMode == HookMode.HOLD ? HookMode.TOGGLE : HookMode.HOLD;
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(
                            Component.literal("ODM hook mode: " + hookMode), true);
                }
            }

            ClientGrappleState.tick();

            boolean boostDown = GAS_BOOST.isDown();
            if (boostDown != boostWasDown) {
                ModNetwork.sendInput(boostDown
                        ? GrappleInputPacket.Action.BOOST_ON
                        : GrappleInputPacket.Action.BOOST_OFF);
                boostWasDown = boostDown;
            }

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                ModNetwork.sendMovement(new MovementInputPacket(
                        minecraft.options.keyUp.isDown(), minecraft.options.keyDown.isDown(),
                        minecraft.options.keyLeft.isDown(), minecraft.options.keyRight.isDown(),
                        minecraft.options.keyJump.isDown(), ODM_DOWN.isDown()));
            }
        }

        private static void handleHookInputs() {
            if (hookMode == HookMode.TOGGLE) {
                while (LEFT_GRAPPLE.consumeClick()) {
                    sendLeftToggle();
                }
                while (RIGHT_GRAPPLE.consumeClick()) {
                    sendRightToggle();
                }
                leftWasDown = LEFT_GRAPPLE.isDown();
                rightWasDown = RIGHT_GRAPPLE.isDown();
                return;
            }

            boolean leftDown = LEFT_GRAPPLE.isDown();
            if (leftDown != leftWasDown) {
                if (leftDown || ClientGrappleState.isLeftAttached()) {
                    sendLeftToggle();
                }
                leftWasDown = leftDown;
            }

            boolean rightDown = RIGHT_GRAPPLE.isDown();
            if (rightDown != rightWasDown) {
                if (rightDown || ClientGrappleState.isRightAttached()) {
                    sendRightToggle();
                }
                rightWasDown = rightDown;
            }
        }

        private static void sendLeftToggle() {
            ModNetwork.sendGrapple(GrappleInputPacket.Action.TOGGLE_LEFT,
                    ClientGrappleState.toggleLeft());
        }

        private static void sendRightToggle() {
            ModNetwork.sendGrapple(GrappleInputPacket.Action.TOGGLE_RIGHT,
                    ClientGrappleState.toggleRight());
        }
    }
}
