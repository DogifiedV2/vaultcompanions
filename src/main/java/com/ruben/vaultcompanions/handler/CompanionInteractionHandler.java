package com.ruben.vaultcompanions.handler;

import com.ruben.vaultcompanions.network.ModNetwork;
import com.ruben.vaultcompanions.network.OpenCompanionGuiPacket;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.world.data.CompanionVariantUnlockData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

        Set<String> unlocked = CompanionVariantUnlockData.get(player.getLevel()).getUnlocked(player.getUUID());
        ModNetwork.sendToPlayer(player, new OpenCompanionGuiPacket(unlocked, currentType));
        event.setCanceled(true);
    }
}
