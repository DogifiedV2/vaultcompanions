package com.ruben.vaultcompanions;

import com.mojang.logging.LogUtils;
import com.ruben.vaultcompanions.command.VaultCompanionsCommand;
import com.ruben.vaultcompanions.handler.CompanionInteractionHandler;
import com.ruben.vaultcompanions.network.ModNetwork;
import com.ruben.vaultcompanions.registry.ModPets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(VaultCompanions.MOD_ID)
public class VaultCompanions {

    public static final String MOD_ID = "vaultcompanions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VaultCompanions() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.register(new CompanionInteractionHandler());
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        ModNetwork.register();
        event.enqueueWork(() -> {
            ModPets.register();
            LOGGER.info("VaultCompanions loaded {} custom pet types", ModPets.getRegisteredTypeCount());
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        VaultCompanionsCommand.register(event.getDispatcher());
    }
}
