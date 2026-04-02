package com.ruben.vaultcompanions.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.ruben.vaultcompanions.init.ModItemsVC;
import com.ruben.vaultcompanions.registry.ModPets;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.entity.entity.pet.PetHelper;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.item.CompanionSeries;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class VaultCompanionsCommand {

    private static final SuggestionProvider<CommandSourceStack> VARIANT_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(ModPets.getRegisteredVariantIds(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vaultcompanions")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("variant", StringArgumentType.string())
                                        .suggests(VARIANT_SUGGESTIONS)
                                        .executes(VaultCompanionsCommand::executeGive))))
                .then(Commands.literal("give_scroll")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeGiveScroll(ctx, 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> executeGiveScroll(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("list")
                        .executes(VaultCompanionsCommand::executeList))
        );
    }

    private static int executeGive(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String variantId = StringArgumentType.getString(context, "variant");

        Optional<PetHelper.PetVariant> variantOpt = PetHelper.getVariant(variantId);
        if (variantOpt.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Unknown variant: " + variantId));
            return 0;
        }

        PetHelper.PetVariant variant = variantOpt.get();
        ItemStack companionStack = new ItemStack(ModItems.COMPANION);

        CompanionItem.generateCompanionDataForSeries(companionStack, JavaRandom.ofNanoTime(), CompanionSeries.PET);
        CompanionItem.setPetType(companionStack, variant.type());
        CompanionItem.setPetName(companionStack, variant.displayName());
        CompanionItem.setOwner(companionStack, player.getUUID());
        CompanionItem.setOwnerName(companionStack, player.getGameProfile().getName());

        if (!player.getInventory().add(companionStack)) {
            player.drop(companionStack, false);
        }

        context.getSource().sendSuccess(
                new TextComponent("Gave ").withStyle(ChatFormatting.GREEN)
                        .append(new TextComponent(variant.displayName()).withStyle(ChatFormatting.AQUA))
                        .append(new TextComponent(" to ").withStyle(ChatFormatting.GREEN))
                        .append(new TextComponent(player.getGameProfile().getName()).withStyle(ChatFormatting.AQUA)),
                true);
        return 1;
    }

    private static int executeGiveScroll(CommandContext<CommandSourceStack> context, int amount) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ItemStack scrollStack = new ItemStack(ModItemsVC.COMPANION_SCROLL, amount);

        if (!player.getInventory().add(scrollStack)) {
            player.drop(scrollStack, false);
        }

        context.getSource().sendSuccess(
                new TextComponent("Gave " + amount + "x Companion Scroll to ").withStyle(ChatFormatting.GREEN)
                        .append(new TextComponent(player.getGameProfile().getName()).withStyle(ChatFormatting.AQUA)),
                true);
        return 1;
    }

    private static int executeList(CommandContext<CommandSourceStack> context) {
        List<String> variants = ModPets.getRegisteredVariantIds();

        if (variants.isEmpty()) {
            context.getSource().sendSuccess(
                    new TextComponent("No custom pets registered.").withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        context.getSource().sendSuccess(
                new TextComponent("Custom pets (" + variants.size() + "):").withStyle(ChatFormatting.GREEN), false);

        for (String variantId : variants) {
            PetHelper.getVariant(variantId).ifPresent(variant ->
                    context.getSource().sendSuccess(
                            new TextComponent(" - ").withStyle(ChatFormatting.GRAY)
                                    .append(new TextComponent(variant.type()).withStyle(ChatFormatting.AQUA))
                                    .append(new TextComponent(" (" + variant.displayName() + ")").withStyle(ChatFormatting.GRAY)),
                            false));
        }
        return 1;
    }
}
