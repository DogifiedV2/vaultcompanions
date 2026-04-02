package com.ruben.vaultcompanions.network;

import com.ruben.vaultcompanions.client.gui.CompanionSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class OpenCompanionGuiPacket {

    private final Set<String> unlockedVariantIds;
    private final String currentVariantType;

    public OpenCompanionGuiPacket(Set<String> unlockedVariantIds, String currentVariantType) {
        this.unlockedVariantIds = unlockedVariantIds;
        this.currentVariantType = currentVariantType;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(unlockedVariantIds.size());
        for (String id : unlockedVariantIds) {
            buf.writeUtf(id);
        }
        buf.writeUtf(currentVariantType);
    }

    public static OpenCompanionGuiPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Set<String> unlocked = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            unlocked.add(buf.readUtf(256));
        }
        String currentType = buf.readUtf(256);
        return new OpenCompanionGuiPacket(unlocked, currentType);
    }

    public static void handle(OpenCompanionGuiPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> openScreen(packet));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(OpenCompanionGuiPacket packet) {
        Minecraft.getInstance().setScreen(
                new CompanionSelectionScreen(packet.unlockedVariantIds, packet.currentVariantType));
    }
}
