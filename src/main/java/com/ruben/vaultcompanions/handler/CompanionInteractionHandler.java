package com.ruben.vaultcompanions.handler;

import com.ruben.vaultcompanions.network.ModNetwork;
import com.ruben.vaultcompanions.network.OpenCompanionGuiPacket;
import iskallia.vault.entity.entity.pet.PetHelper;
import iskallia.vault.entity.entity.pet.PetModelRegistry;
import iskallia.vault.entity.entity.pet.PetModelType;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.util.IskalliaDevs;
import iskallia.vault.world.data.CompanionVariantUnlockData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

public class CompanionInteractionHandler {

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        if (!(event.getItemStack().getItem() instanceof CompanionItem)) return;

        ServerPlayer player = (ServerPlayer) event.getPlayer();
        String currentType = CompanionItem.getPetType(event.getItemStack());
        if (currentType == null || currentType.isEmpty()) currentType = "";

        Set<String> unlocked = new HashSet<>(CompanionVariantUnlockData.get(player.getLevel()).getUnlocked(player.getUUID()));

        if (IskalliaDevs.isDeveloper(player.getUUID())) {
            for (PetModelType modelType : PetModelRegistry.getAll()) {
                for (PetHelper.PetVariant variant : modelType.getVariants()) {
                    if (variant.requiresRewards()) {
                        unlocked.add(variant.type());
                    }
                }
            }
        }

        ModNetwork.sendToPlayer(player, new OpenCompanionGuiPacket(unlocked, currentType));
        event.setCanceled(true);
    }
}
