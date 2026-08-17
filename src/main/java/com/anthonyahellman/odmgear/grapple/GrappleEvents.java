package com.anthonyahellman.odmgear.grapple;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.anthonyahellman.odmgear.OdmGearMod;

@Mod.EventBusSubscriber(modid = OdmGearMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GrappleEvents {
    private GrappleEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            GrappleState.tick(player);
        }
    }
}
