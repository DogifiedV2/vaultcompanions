package com.ruben.vaultcompanions.network;

import com.ruben.vaultcompanions.VaultCompanions;
import com.ruben.vaultcompanions.init.ModItemsVC;
import iskallia.vault.entity.entity.pet.PetHelper;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.item.CompanionPetManager;
import iskallia.vault.item.CompanionSeries;
import iskallia.vault.util.IskalliaDevs;
import iskallia.vault.world.data.CompanionVariantUnlockData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class SwapCompanionPacket {

    private final String targetVariantType;

    public SwapCompanionPacket(String targetVariantType) {
        this.targetVariantType = targetVariantType;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(targetVariantType);
    }

    public static SwapCompanionPacket decode(FriendlyByteBuf buf) {
        return new SwapCompanionPacket(buf.readUtf(256));
    }

    public static void handle(SwapCompanionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Optional<PetHelper.PetVariant> variantOpt = PetHelper.getVariant(packet.targetVariantType);
            if (variantOpt.isEmpty()) {
                player.sendMessage(new TextComponent("Unknown companion variant.").withStyle(ChatFormatting.RED), player.getUUID());
                return;
            }

            PetHelper.PetVariant variant = variantOpt.get();

            boolean isDev = IskalliaDevs.isDeveloper(player.getUUID());
            boolean needsUnlockCheck = variant.requiresUnlock() || (variant.requiresRewards() && !isDev);

            if (needsUnlockCheck) {
                Set<String> unlocked = CompanionVariantUnlockData.get(player.getLevel()).getUnlocked(player.getUUID());
                if (!unlocked.contains(variant.type())) {
                    player.sendMessage(new TextComponent("You haven't unlocked this variant.").withStyle(ChatFormatting.RED), player.getUUID());
                    return;
                }
            }

            ItemStack heldItem = player.getMainHandItem();
            if (!(heldItem.getItem() instanceof CompanionItem)) {
                player.sendMessage(new TextComponent("You must be holding a companion.").withStyle(ChatFormatting.RED), player.getUUID());
                return;
            }

            String currentType = CompanionItem.getPetType(heldItem);
            if (currentType == null) currentType = "";
            if (currentType.equalsIgnoreCase(variant.type())) {
                player.sendMessage(new TextComponent("Already using this companion.").withStyle(ChatFormatting.YELLOW), player.getUUID());
                return;
            }

            if (!consumeCompanionScroll(player)) {
                player.sendMessage(new TextComponent("You need a Companion Scroll to swap.").withStyle(ChatFormatting.RED), player.getUUID());
                return;
            }

            CompanionItem.setPetType(heldItem, variant.type());
            CompanionItem.setPetName(heldItem, variant.displayName());
            CompanionItem.setPetSeries(heldItem, CompanionSeries.PET);

            CompanionPetManager.despawnPet(player);

            player.sendMessage(
                    new TextComponent("Companion changed to ").withStyle(ChatFormatting.GREEN)
                            .append(new TextComponent(variant.displayName()).withStyle(ChatFormatting.AQUA)),
                    player.getUUID());

            VaultCompanions.LOGGER.debug("Player {} swapped companion to {}", player.getGameProfile().getName(), variant.type());
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean consumeCompanionScroll(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItemsVC.COMPANION_SCROLL)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
