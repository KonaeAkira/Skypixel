package com.github.konaeakira.skypixel.itemlist;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Environment(value= EnvType.CLIENT)
public class ItemListScreen extends Screen implements Drawable {
    protected static ItemListScreen instance;
    public static ItemListScreen getInstance() {
        return instance;
    }

    private final int gridX;
    private final int gridY;
    private final int rows;
    private final int cols;

    private double scroll;
    private final double maxScroll;

    private final List<ItemStack> itemList = new ArrayList<>();

    public ItemListScreen(HandledScreen parent) {
        super(Text.of("Item List"));
        init(MinecraftClient.getInstance(), parent.width, parent.height);
        instance = this;
        setSearch("");

        this.cols = (this.width - 200) / 2 / 16;
        int searchWidth = this.cols * 16 - 4;
        int searchHeight = 16;
        this.rows = (this.height - searchHeight - 4) / 16;
        this.gridX = this.width - this.cols * 16 - 2;
        this.gridY = (searchHeight + 4 + this.height - this.rows * 16) / 2;
        int searchX = this.gridX + 2;
        int searchY = 4;

        this.scroll = 0;
        this.maxScroll = this.itemList.size() / this.cols - this.rows + 1;

        TextFieldWidget search = new TextFieldWidget(this.textRenderer, searchX, searchY, searchWidth, searchHeight, Text.of("Search"));
        search.setChangedListener(this::setSearch);
        addButton(search);
    }

    private void setSearch(String search) {
        itemList.clear();
        search = search.toLowerCase();
        for (ItemStack itemStack : ItemRegistry.registry.values()) {
            String name = itemStack.getName().toString().toLowerCase();
            String lore = itemStack.getTag().toString().toLowerCase();
            if (name.contains(search) || lore.contains(search))
                itemList.add(itemStack);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        RenderSystem.disableDepthTest();
        ItemRenderer itemRenderer = client.getItemRenderer();
        // item list
        for (int i = 0; i < rows; ++i)
            for (int j = 0; j < cols; ++j) {
                int index = (i + (int)scroll) * cols + j;
                if (index < this.itemList.size()) {
                    int x = gridX + j * 16;
                    int y = gridY + i * 16;
                    itemRenderer.renderInGui(this.itemList.get(index), x, y);
                }
            }
        // tooltip
        if (this.isMouseOverList(mouseX, mouseY)) {
            int i = (mouseY - gridY) / 16;
            int j = (mouseX - gridX) / 16;
            int index = (i + (int)scroll) * cols + j;
            if (index < this.itemList.size()) {
                List<Text> tooltip = getTooltipFromItem(this.itemList.get(index));
                renderTooltip(matrices, tooltip, mouseX, mouseY);
            }
        }
        RenderSystem.enableDepthTest();
    }

    public boolean isMouseOverList(double mouseX, double mouseY) {
        return gridX <= mouseX && mouseX < gridX + cols * 16 && gridY <= mouseY && mouseY < gridY + rows * 16;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (super.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        } else if (this.isMouseOverList(mouseX, mouseY)) {
            scroll = Math.min(this.maxScroll, Math.max(0.0, scroll - amount));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (this.getFocused() instanceof TextFieldWidget && ((TextFieldWidget) this.getFocused()).isFocused() && keyCode != 256) return true;
        return false;
    }
}
