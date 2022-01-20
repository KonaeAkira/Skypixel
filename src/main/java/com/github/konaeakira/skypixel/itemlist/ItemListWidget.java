package com.github.konaeakira.skypixel.itemlist;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public class ItemListWidget extends AbstractParentElement implements Drawable {
    private final HandledScreen screen;
    private final MinecraftClient client;
    private final List<Element> children = Lists.newArrayList();

    private final int gridX;
    private final int gridY;
    private final int rows;
    private final int cols;

    private double scroll;
    private final double maxScroll;

    public ItemListWidget(HandledScreen screen) {
        ItemList.instance = this;
        this.screen = screen;
        this.client = MinecraftClient.getInstance();
        int scaledWidth = this.client.getWindow().getScaledWidth();
        int scaledHeight = this.client.getWindow().getScaledHeight();
        this.rows = scaledHeight / 16;
        this.cols = (scaledWidth - 200) / 2 / 16;
        this.gridX = scaledWidth - this.cols * 16;
        this.gridY = 0;
        this.scroll = 0;
        this.maxScroll = ItemList.items.size() / this.cols - this.rows + 1;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.disableDepthTest();
        ItemRenderer itemRenderer = client.getItemRenderer();
        for (int i = 0; i < rows; ++i)
            for (int j = 0; j < cols; ++j) {
                int index = (i + (int)scroll) * cols + j;
                if (index < ItemList.items.size()) {
                    int x = gridX + j * 16;
                    int y = gridY + i * 16;
                    itemRenderer.renderInGui(ItemList.items.get(index), x, y);
                }
            }
        if (this.isMouseOverList(mouseX, mouseY)) {
            int i = (mouseY - gridY) / 16;
            int j = (mouseX - gridX) / 16;
            int index = (i + (int)scroll) * cols + j;
            if (index < ItemList.items.size()) {
                List<Text> tooltip = this.screen.getTooltipFromItem(ItemList.items.get(index));
                this.screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
            }
        }
        RenderSystem.enableDepthTest();
    }

    @Override
    public List<? extends Element> children() {
        return this.children;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, amount)).isPresent())
            return true;
        else if (this.isMouseOverList(mouseX, mouseY)) {
            scroll = Math.min(this.maxScroll, Math.max(0.0, scroll - amount));
            return true;
        }
        return false;
    }

    public boolean isMouseOverList(double mouseX, double mouseY) {
        return gridX <= mouseX && mouseX < gridX + cols * 16 && gridY <= mouseY && mouseY < gridY + rows * 16;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isMouseOverList(mouseX, mouseY);
    }
}
