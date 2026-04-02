package com.ruben.vaultcompanions.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ruben.vaultcompanions.VaultCompanions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class CompanionFavorites {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type SET_TYPE = new TypeToken<Set<String>>() {}.getType();
    private static final Path FAVORITES_PATH = Path.of("config", "vaultcompanions-favorites.json");

    private static Set<String> favorites;

    public static void ensureLoaded() {
        load();
    }

    public static boolean isFavorite(String variantId) {
        return load().contains(variantId);
    }

    public static void toggleFavorite(String variantId) {
        Set<String> set = load();
        if (!set.remove(variantId)) {
            set.add(variantId);
        }
        save();
    }

    public static Set<String> getFavorites() {
        return Collections.unmodifiableSet(load());
    }

    private static Set<String> load() {
        if (favorites != null) return favorites;

        try {
            if (Files.exists(FAVORITES_PATH)) {
                String json = Files.readString(FAVORITES_PATH);
                favorites = GSON.fromJson(json, SET_TYPE);
                if (favorites == null) favorites = new HashSet<>();
            } else {
                favorites = new HashSet<>();
            }
        } catch (IOException e) {
            VaultCompanions.LOGGER.warn("Failed to load companion favorites", e);
            favorites = new HashSet<>();
        }
        return favorites;
    }

    private static void save() {
        try {
            Files.createDirectories(FAVORITES_PATH.getParent());
            Files.writeString(FAVORITES_PATH, GSON.toJson(favorites));
        } catch (IOException e) {
            VaultCompanions.LOGGER.warn("Failed to save companion favorites", e);
        }
    }
}
