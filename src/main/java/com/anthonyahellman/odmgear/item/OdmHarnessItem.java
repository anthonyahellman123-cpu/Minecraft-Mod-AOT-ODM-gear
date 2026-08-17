package com.anthonyahellman.odmgear.item;

import com.anthonyahellman.odmgear.OdmGearMod;
import com.anthonyahellman.odmgear.client.model.OdmHarnessModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class OdmHarnessItem extends ArmorItem {
    public OdmHarnessItem() {
        super(ModArmorMaterials.ODM, Type.CHESTPLATE, new Properties().stacksTo(1));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return OdmGearMod.MOD_ID + ":textures/entity/odm_harness.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private OdmHarnessModel model;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                           EquipmentSlot slot, HumanoidModel<?> original) {
                if (model == null) {
                    model = new OdmHarnessModel(Minecraft.getInstance().getEntityModels()
                            .bakeLayer(OdmHarnessModel.LAYER_LOCATION));
                }

                original.copyPropertiesTo(model);
                model.setAllVisible(false);
                model.body.visible = true;
                return model;
            }
        });
    }
}
