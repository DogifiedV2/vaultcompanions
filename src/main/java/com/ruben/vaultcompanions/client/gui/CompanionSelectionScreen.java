package com.ruben.vaultcompanions.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ruben.vaultcompanions.init.ModItemsVC;
import com.ruben.vaultcompanions.network.ModNetwork;
import com.ruben.vaultcompanions.network.SwapCompanionPacket;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.entity.entity.pet.PetHelper;
import iskallia.vault.entity.entity.pet.PetModelRegistry;
import iskallia.vault.entity.entity.pet.PetModelType;
import iskallia.vault.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class CompanionSelectionScreen extends Screen {

    private enum Tab { UNLOCKED, ALL, FAVORITES }

    private final Set<String> unlockedVariantIds;
    private final String currentVariantType;

    private Tab currentTab = Tab.UNLOCKED;
    private CompanionListWidget petList;
    private EditBox searchBox;
    private Button confirmButton;
    private Button tabUnlocked;
    private Button tabAll;
    private Button tabFavorites;

    private PetEntity previewEntity;
    private String selectedVariantId;
    private String selectedDisplayName;
    private final ItemStack scrollIconStack = new ItemStack(ModItemsVC.COMPANION_SCROLL);

    public CompanionSelectionScreen(Set<String> unlockedVariantIds, String currentVariantType) {
        super(new TextComponent("Companion Selection"));
        this.unlockedVariantIds = unlockedVariantIds;
        this.currentVariantType = currentVariantType;
        this.selectedVariantId = currentVariantType;
    }

    @Override
    protected void init() {
        super.init();
        CompanionFavorites.ensureLoaded();

        int listWidth = (int) (this.width * 0.45);
        int listTop = 40;
        int listBottom = this.height - 10;

        int tabWidth = (listWidth - 8) / 3;
        int tabY = 8;
        tabUnlocked = new Button(4, tabY, tabWidth, 16,
                new TextComponent("Unlocked"), b -> switchTab(Tab.UNLOCKED));
        tabAll = new Button(4 + tabWidth + 2, tabY, tabWidth, 16,
                new TextComponent("All"), b -> switchTab(Tab.ALL));
        tabFavorites = new Button(4 + (tabWidth + 2) * 2, tabY, tabWidth, 16,
                new TextComponent("Favorites"), b -> switchTab(Tab.FAVORITES));
        addRenderableWidget(tabUnlocked);
        addRenderableWidget(tabAll);
        addRenderableWidget(tabFavorites);

        searchBox = new EditBox(this.font, 6, 28, listWidth - 12, 12, new TextComponent("Search"));
        searchBox.setMaxLength(50);
        searchBox.setBordered(true);
        searchBox.setVisible(true);
        searchBox.setTextColor(0xFFFFFF);
        searchBox.setResponder(text -> populateList());
        addRenderableWidget(searchBox);

        petList = new CompanionListWidget(this, this.minecraft, listWidth, this.height, listTop, listBottom, 18);
        petList.setLeftPos(0);
        addWidget(petList);

        int previewCenterX = listWidth + (this.width - listWidth) / 2;
        int scrollCount = countScrollsInInventory();
        String confirmText = scrollCount > 0 ? "Confirm (x" + scrollCount + ")" : "Confirm";
        confirmButton = new Button(previewCenterX - 60, this.height - 30, 120, 20,
                new TextComponent(confirmText).withStyle(ChatFormatting.GREEN), b -> onConfirm());
        confirmButton.active = scrollCount > 0;
        addRenderableWidget(confirmButton);

        createPreviewEntity();
        updatePreview(currentVariantType);
        updateTabAppearance();
        populateList();
    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
        updateTabAppearance();
        populateList();
    }

    private void updateTabAppearance() {
        tabUnlocked.active = currentTab != Tab.UNLOCKED;
        tabAll.active = currentTab != Tab.ALL;
        tabFavorites.active = currentTab != Tab.FAVORITES;
    }

    private void populateList() {
        petList.clear();
        String filter = searchBox.getValue().toLowerCase().trim();

        for (PetModelType modelType : PetModelRegistry.getAll()) {
            PetHelper.PetVariant[] variants = modelType.getVariants();
            if (variants.length == 0) continue;

            boolean isMultiVariant = variants.length > 1;

            for (int i = 0; i < variants.length; i++) {
                PetHelper.PetVariant variant = variants[i];

                if (!filter.isEmpty() && !variant.displayName().toLowerCase().contains(filter)) {
                    continue;
                }

                boolean isUnlocked = isVariantUnlocked(variant);
                boolean isActive = variant.type().equalsIgnoreCase(currentVariantType);

                boolean showInTab = switch (currentTab) {
                    case UNLOCKED -> isUnlocked;
                    case ALL -> true;
                    case FAVORITES -> CompanionFavorites.isFavorite(variant.type()) && isUnlocked;
                };

                if (!showInTab) continue;

                boolean isSubVariant = isMultiVariant && i > 0;
                petList.addPetEntry(variant.type(), variant.displayName(), isSubVariant, isUnlocked, isActive);
            }
        }
    }

    private boolean isVariantUnlocked(PetHelper.PetVariant variant) {
        if (!variant.requiresUnlock() && !variant.requiresRewards()) {
            return true;
        }
        return unlockedVariantIds.contains(variant.type());
    }

    public void onPetSelected(CompanionListWidget.PetEntry entry) {
        this.selectedVariantId = entry.getVariantId();
        this.selectedDisplayName = entry.getDisplayName();
        updatePreview(selectedVariantId);
    }

    public void onFavoriteToggled() {
        if (currentTab == Tab.FAVORITES) {
            populateList();
        }
    }

    private void onConfirm() {
        if (countScrollsInInventory() <= 0) return;
        if (selectedVariantId == null) return;
        if (selectedVariantId.equalsIgnoreCase(currentVariantType)) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                        new TextComponent("Already using this companion.").withStyle(ChatFormatting.YELLOW), true);
            }
            return;
        }

        ModNetwork.sendToServer(new SwapCompanionPacket(selectedVariantId));
        this.onClose();
    }

    private void createPreviewEntity() {
        if (this.minecraft == null || this.minecraft.level == null) return;
        previewEntity = ModEntities.PET.create(this.minecraft.level);
    }

    private void updatePreview(String variantId) {
        if (previewEntity == null || variantId == null) return;

        PetHelper.getVariant(variantId).ifPresent(variant -> {
            ItemStack fakeStack = variant.displayItem().copy();
            previewEntity.setCompanionData(fakeStack);
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (previewEntity != null) {
            previewEntity.tickCount++;
        }
        searchBox.tick();

        int scrollCount = countScrollsInInventory();
        confirmButton.setMessage(new TextComponent(
                scrollCount > 0 ? "Confirm (x" + scrollCount + ")" : "Confirm"
        ).withStyle(ChatFormatting.GREEN));
        confirmButton.active = scrollCount > 0;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        int listWidth = (int) (this.width * 0.45);
        fill(poseStack, 0, 0, listWidth, this.height, 0x80000000);
        petList.render(poseStack, mouseX, mouseY, partialTick);

        int previewCenterX = listWidth + (this.width - listWidth) / 2;
        int previewCenterY = this.height / 2 - 10;
        renderPreview(poseStack, previewCenterX, previewCenterY, mouseX, mouseY);

        if (selectedDisplayName != null) {
            int nameWidth = this.font.width(selectedDisplayName);
            this.font.drawShadow(poseStack, selectedDisplayName, previewCenterX - nameWidth / 2.0f, 50, 0x55FFFF);
        }

        if (confirmButton != null) {
            this.itemRenderer.renderAndDecorateItem(scrollIconStack, confirmButton.x - 20, confirmButton.y + 2);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font, this.title, listWidth / 2, 2, 0xFFFFFF);
    }

    private void renderPreview(PoseStack poseStack, int centerX, int centerY, int mouseX, int mouseY) {
        if (previewEntity == null) return;

        PetHelper.PetDimensions dims = PetHelper.getDimensions(previewEntity);
        float entityHeight = Math.max(dims.height(), 0.5f);
        int scale = (int) Math.min(80, Math.max(15, 50.0f / entityHeight));

        InventoryScreen.renderEntityInInventory(
                centerX, centerY + scale + 10,
                scale,
                centerX - mouseX,
                centerY - scale / 2.0f - mouseY,
                previewEntity
        );
    }

    private int countScrollsInInventory() {
        if (this.minecraft == null || this.minecraft.player == null) return 0;
        int count = 0;
        for (int i = 0; i < this.minecraft.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.minecraft.player.getInventory().getItem(i);
            if (stack.is(ModItemsVC.COMPANION_SCROLL)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox.isFocused() && searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox.isFocused() && searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (petList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (petList.isMouseOver(mouseX, mouseY)) {
            return petList.mouseScrolled(mouseX, mouseY, delta);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
