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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.network.chat.Component;
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
    private static boolean autoAim = true;
    private static Vec3 leftCandidate;
    private static Vec3 rightCandidate;
    private static int candidateCooldown;

    private ClientGrappleState() {
    }

    public static Vec3 toggleLeft() {
        leftAnchor = leftAnchor == null ? findAnchor(-1.0D) : null;
        return leftAnchor;
    }

    public static Vec3 toggleRight() {
        rightAnchor = rightAnchor == null ? findAnchor(1.0D) : null;
        return rightAnchor;
    }

    public static void toggleAutoDetach() {
        autoDetach = !autoDetach;
    }

    public static void toggleAutoAim() {
        autoAim = !autoAim;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.literal("ODM auto-aim: " + (autoAim ? "ON" : "OFF")), true);
        }
        leftCandidate = null;
        rightCandidate = null;
    }

    public static void tick() {
        if (!autoAim || --candidateCooldown > 0) {
            return;
        }
        candidateCooldown = 4;
        leftCandidate = scanForAnchor(-1.0D);
        rightCandidate = scanForAnchor(1.0D);
    }

    private static Vec3 findAnchor(double sideBias) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ODM_HARNESS.get())) {
            return null;
        }

        if (autoAim) {
            Vec3 candidate = sideBias < 0.0D ? leftCandidate : rightCandidate;
            if (candidate != null) {
                return candidate;
            }
        }
        HitResult hit = player.pick(RANGE, 0.0F, false);
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : null;
    }

    private static Vec3 scanForAnchor(double sideBias) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null
                || !player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ODM_HARNESS.get())) {
            return null;
        }

        Vec3 start = player.getEyePosition();
        Vec3 right = player.getLookAngle().cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        Vec3 best = null;
        double bestScore = -Double.MAX_VALUE;
        float[] yawOffsets = {-18.0F, -10.0F, -4.0F, 0.0F, 4.0F, 10.0F, 18.0F};
        float[] pitchOffsets = {-20.0F, -12.0F, -5.0F, 2.0F, 9.0F};

        for (float yawOffset : yawOffsets) {
            for (float pitchOffset : pitchOffsets) {
                Vec3 direction = Vec3.directionFromRotation(
                        player.getXRot() + pitchOffset, player.getYRot() + yawOffset);
                BlockHitResult hit = minecraft.level.clip(new ClipContext(start,
                        start.add(direction.scale(RANGE)), ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, player));
                if (hit.getType() != HitResult.Type.BLOCK) {
                    continue;
                }

                Vec3 point = hit.getLocation();
                Vec3 offset = point.subtract(start);
                double sideValue = offset.normalize().dot(right) * sideBias;
                double heightValue = Math.max(-4.0D, point.y - player.getY());
                double score = heightValue * 1.45D + sideValue * 8.0D
                        - offset.length() * 0.07D - Math.abs(pitchOffset) * 0.05D;
                if (score > bestScore) {
                    bestScore = score;
                    best = point;
                }
            }
        }
        return best;
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
            if (!autoAim || (leftCandidate == null && rightCandidate == null)) {
                return;
            }
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
        if (autoAim && leftAnchor == null && leftCandidate != null) {
            drawMarker(poseStack, lines, leftCandidate, 70, 165, 255);
        }
        if (autoAim && rightAnchor == null && rightCandidate != null) {
            drawMarker(poseStack, lines, rightCandidate, 255, 185, 65);
        }
        if (leftAnchor != null) {
            drawCable(poseStack, lines, playerPosition.subtract(side), leftAnchor, 94, 104, 110);
        }
        if (rightAnchor != null) {
            drawCable(poseStack, lines, playerPosition.add(side), rightAnchor, 128, 138, 143);
        }
        buffers.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private static void drawMarker(PoseStack poseStack, VertexConsumer consumer, Vec3 center,
                                   int red, int green, int blue) {
        double size = 0.22D;
        drawCable(poseStack, consumer, center.add(-size, 0.0D, 0.0D), center.add(size, 0.0D, 0.0D), red, green, blue);
        drawCable(poseStack, consumer, center.add(0.0D, -size, 0.0D), center.add(0.0D, size, 0.0D), red, green, blue);
        drawCable(poseStack, consumer, center.add(0.0D, 0.0D, -size), center.add(0.0D, 0.0D, size), red, green, blue);
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
