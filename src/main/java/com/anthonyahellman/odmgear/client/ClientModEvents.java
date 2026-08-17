package com.anthonyahellman.odmgear.client;

import com.anthonyahellman.odmgear.OdmGearMod;
import com.anthonyahellman.odmgear.client.model.OdmHarnessModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OdmGearMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(OdmHarnessModel.LAYER_LOCATION, OdmHarnessModel::createBodyLayer);
    }
}
