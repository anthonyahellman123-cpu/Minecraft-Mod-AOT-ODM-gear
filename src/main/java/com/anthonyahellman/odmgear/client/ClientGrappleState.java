package com.anthonyahellman.odmgear.client;

import com.anthonyahellman.odmgear.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.anthonyahellman.odmgear.OdmGearMod;

@Mod.EventBusSubscriber(modid = OdmGearMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class ClientGrappleState {
    private static final double RANGE = 64.0D;
    private static Vec3 leftAnchor;
    private static Vec3 rightAnchor;
    private static boolean autoDetach = true;

    private ClientGrappleState() {
    }

    public static void toggleLeft() {
        leftAnchor = leftAnchor == null ? findAnchor() : null;
    }

    public static void toggleRight() {
        rightAnchor = rightAnchor == null ? findAnchor() : null;
    }

    public static void toggleAutoDetach() {
        autoDetach = !autoDetach;
    }

    private static Vec3 findAnchor() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ODM_HARNESS.get())) {
            return null;
        }

        HitResult hit = player.pick(RANGE, 0.0F, false);
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : null;
    }

    @SubscribeEvent
    public static void renderCables(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ODM_HARNESS.get())) {
            leftAnchor = null;
            rightAnchor = null;
            return;
        }
        if (leftAnchor == null && rightAnchor == null) {
            return;
        }

        float partialTick = event.getPartialTick();
        Vec3 playerPosition = player.getPosition(partialTick).add(0.0D, player.getBbHeight() * 0.62D, 0.0D);
        if (autoDetach) {
            if (leftAnchor != null && playerPosition.distanceTo(leftAnchor) <= 2.75D) {
                leftAnchor = null;
            }
            if (rightAnchor != null && playerPosition.distanceTo(rightAnchor) <= 2.75D) {
                rightAnchor = null;
            }
        }
        if (leftAnchor == null && rightAnchor == null) {
            return;
        }
        Vec3 side = player.getLookAngle().cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize().scale(0.28D);

        Camera camera = event.getCamera();
        Vec3 cameraPosition = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        if (leftAnchor != null) {
            drawCable(poseStack, lines, playerPosition.subtract(side), leftAnchor, 94, 104, 110);
        }
        if (rightAnchor != null) {
            drawCable(poseStack, lines, playerPosition.add(side), rightAnchor, 128, 138, 143);
        }
        buffers.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private static void drawCable(PoseStack poseStack, VertexConsumer consumer, Vec3 start, Vec3 end,
                                  int red, int green, int blue) {
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        Vec3 direction = end.subtract(start).normalize();

        consumer.vertex(pose, (float) start.x, (float) start.y, (float) start.z)
                .color(red, green, blue, 255)
                .normal(normal, (float) direction.x, (float) direction.y, (float) direction.z)
                .endVertex();
        consumer.vertex(pose, (float) end.x, (float) end.y, (float) end.z)
                .color(red, green, blue, 255)
                .normal(normal, (float) direction.x, (float) direction.y, (float) direction.z)
                .endVertex();
    }
}
