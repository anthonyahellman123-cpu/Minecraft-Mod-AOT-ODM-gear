package com.anthonyahellman.odmgear.registry;

import com.anthonyahellman.odmgear.OdmGearMod;
import com.anthonyahellman.odmgear.item.OdmHarnessItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OdmGearMod.MOD_ID);

    public static final RegistryObject<Item> ODM_HARNESS =
            ITEMS.register("odm_harness", OdmHarnessItem::new);

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
