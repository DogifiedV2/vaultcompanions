package com.ruben.vaultcompanions.registry;

import com.ruben.vaultcompanions.VaultCompanions;
import iskallia.vault.entity.entity.pet.PetHelper;
import iskallia.vault.entity.entity.pet.PetModelRegistry;
import iskallia.vault.entity.entity.pet.PetModelType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModPets {

    private static final List<PetModelType> REGISTERED_TYPES = new ArrayList<>();

    public static void register() {
        // Register custom pet types here.
        // Example:
        // registerType("my_pet",
        //     new PetHelper.PetVariant("my_pet_red", "Red Pet", false, false,
        //         PetHelper.PetTrait.Builder.defaultPet().addSleep().build(),
        //         new PetHelper.PetRenderData(
        //             new ResourceLocation(VaultCompanions.MOD_ID, "geo/my_pet.geo.json"),
        //             new ResourceLocation(VaultCompanions.MOD_ID, "textures/entity/my_pet_red.png"),
        //             new ResourceLocation(VaultCompanions.MOD_ID, "animations/my_pet.animation.json")))
        // );
    }

    private static void registerType(String typeId, PetHelper.PetVariant... variants) {
        if (PetModelRegistry.get(typeId) != null) {
            VaultCompanions.LOGGER.error("Cannot register pet type '{}' — ID already exists in VH registry", typeId);
            return;
        }

        try {
            PetModelType type = PetModelRegistry.register(typeId, variants);
            REGISTERED_TYPES.add(type);
            for (PetHelper.PetVariant variant : variants) {
                VaultCompanions.LOGGER.debug("  Registered variant: {} ({})", variant.type(), variant.displayName());
            }
            VaultCompanions.LOGGER.info("Registered custom pet type: {} ({} variants)", typeId, variants.length);
        } catch (Exception e) {
            VaultCompanions.LOGGER.error("Failed to register pet type '{}': {}", typeId, e.getMessage());
        }
    }

    public static List<String> getRegisteredVariantIds() {
        return REGISTERED_TYPES.stream()
                .flatMap(type -> Arrays.stream(type.getVariants()))
                .map(PetHelper.PetVariant::type)
                .toList();
    }

    public static int getRegisteredTypeCount() {
        return REGISTERED_TYPES.size();
    }
}
