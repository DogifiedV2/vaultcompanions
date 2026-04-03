package com.ruben.vaultcompanions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CompanionListWidget extends ObjectSelectionList<CompanionListWidget.Entry> {

    private final CompanionSelectionScreen parentScreen;

    public CompanionListWidget(CompanionSelectionScreen parentScreen, Minecraft mc, int width, int height, int top, int bottom, int itemHeight) {
        super(mc, width, height, top, bottom, itemHeight);
        this.parentScreen = parentScreen;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width - 6;
    }

    public void clear() {
        this.clearEntries();
    }

    public void addPetEntry(String variantId, String displayName, boolean isVariant, boolean isUnlocked, boolean isActive) {
        this.addEntry(new PetEntry(variantId, displayName, isVariant, isUnlocked, isActive));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        int windowHeight = window.getHeight();
        RenderSystem.enableScissor(
                (int) (this.x0 * scale),
                (int) (windowHeight - this.y1 * scale),
                (int) ((this.x1 - this.x0) * scale),
                (int) ((this.y1 - this.y0) * scale)
        );
        super.render(poseStack, mouseX, mouseY, partialTick);
        RenderSystem.disableScissor();
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
    }

    public class PetEntry extends Entry {

        private final String variantId;
        private final String displayName;
        private final boolean isVariant;
        private final boolean isUnlocked;
        private final boolean isActive;

        public PetEntry(String variantId, String displayName, boolean isVariant, boolean isUnlocked, boolean isActive) {
            this.variantId = variantId;
            this.displayName = displayName;
            this.isVariant = isVariant;
            this.isUnlocked = isUnlocked;
            this.isActive = isActive;
        }

        public String getVariantId() {
            return variantId;
        }

        public boolean isUnlocked() {
            return isUnlocked;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            Font font = Minecraft.getInstance().font;
            int textX = left + (isVariant ? 14 : 4);
            int textY = top + (height - font.lineHeight) / 2;

            boolean selected = CompanionListWidget.this.getSelected() == this;

            if (selected) {
                fill(poseStack, left, top, left + width, top + height, 0x80336699);
            } else if (isHovered) {
                fill(poseStack, left, top, left + width, top + height, 0x40FFFFFF);
            }

            if (isActive) {
                font.draw(poseStack, "\u25CF", left + (isVariant ? 6 : 0) - 2, textY, 0x55FF55);
            }

            boolean isFavorite = CompanionFavorites.isFavorite(variantId);
            int heartX = left + width - 14;
            String heartChar = isFavorite ? "\u2764" : "\u2661";
            int heartColor = isFavorite ? 0xFF5555 : 0x888888;
            if (isUnlocked) {
                font.draw(poseStack, heartChar, heartX, textY, heartColor);
            }

            int textColor;
            if (!isUnlocked) {
                textColor = 0x808080;
            } else if (selected) {
                textColor = 0xFFFF55;
            } else if (isHovered) {
                textColor = 0xFFFFFF;
            } else {
                textColor = 0xDDDDDD;
            }

            String prefix = isVariant ? "  " : "";
            font.draw(poseStack, prefix + displayName, textX, textY, textColor);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;

            int heartX = CompanionListWidget.this.x0 + CompanionListWidget.this.getRowWidth() - 14;
            if (mouseX >= heartX && mouseX <= heartX + 12 && isUnlocked) {
                CompanionFavorites.toggleFavorite(variantId);
                parentScreen.onFavoriteToggled();
                return true;
            }

            CompanionListWidget.this.setSelected(this);
            parentScreen.onPetSelected(this);
            return true;
        }

        @Override
        public Component getNarration() {
            return new TextComponent(displayName);
        }
    }
}
