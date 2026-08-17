package com.anthonyahellman.odmgear;

import com.anthonyahellman.odmgear.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;

@Mod(OdmGearMod.MOD_ID)
public final class OdmGearMod {
    public static final String MOD_ID = "odmgear";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OdmGearMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModItems.register(modEventBus);
        modEventBus.addListener(this::addCreativeTabContents);

        LOGGER.info("ODM Gear is loading");
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.ODM_HARNESS);
        }
    }
}
