package com.ruben.vaultcompanions.network;

import com.ruben.vaultcompanions.VaultCompanions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VaultCompanions.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(OpenCompanionGuiPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenCompanionGuiPacket::encode)
                .decoder(OpenCompanionGuiPacket::decode)
                .consumer(OpenCompanionGuiPacket::handle)
                .add();

        CHANNEL.messageBuilder(SwapCompanionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SwapCompanionPacket::encode)
                .decoder(SwapCompanionPacket::decode)
                .consumer(SwapCompanionPacket::handle)
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
