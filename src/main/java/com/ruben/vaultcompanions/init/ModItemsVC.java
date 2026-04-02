package com.ruben.vaultcompanions.init;

import com.ruben.vaultcompanions.VaultCompanions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = VaultCompanions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItemsVC {

    public static final Item COMPANION_SCROLL = new Item(
            new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(64))
            .setRegistryName(new ResourceLocation(VaultCompanions.MOD_ID, "companion_scroll"));

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(COMPANION_SCROLL);
        VaultCompanions.LOGGER.info("Registered Companion Scroll item");
    }
}
