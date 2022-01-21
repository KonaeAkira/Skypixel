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
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Environment(value= EnvType.CLIENT)
public class ItemListScreen extends Screen implements Drawable {
    protected static ItemListScreen instance = null;
    private static final List<Entry> entries = new ArrayList<>();
    private static String searchString = "";
    private static double scroll = 0.0;

    public static ItemListScreen getInstance() {
        return instance;
    }

    private final int gridX;
    private final int gridY;
    private final int rows;
    private final int cols;

    private double maxScroll;

    private final TextFieldWidget search;

    public ItemListScreen(HandledScreen parent) {
        super(Text.of("Item List"));
        init(MinecraftClient.getInstance(), parent.width, parent.height);

        int marginTop = 40;
        int marginRight = 8;
        this.cols = (this.width - 200) / 2 / 16;
        this.rows = (this.height - marginTop) / 16;
        this.gridX = this.width - this.cols * 16 - 2;
        this.gridY = marginTop;

        this.maxScroll = Math.max(0, entries.size() / this.cols - this.rows + 1);

        int searchX = this.gridX + 1;
        int searchY = 18;
        int searchWidth = this.cols * 16 - 2;
        int searchHeight = 16;
        this.search = new TextFieldWidget(this.textRenderer, searchX, searchY, searchWidth, searchHeight, Text.of("Search"));
        this.search.setText(searchString);
        this.search.setChangedListener(this::setSearch);
        addButton(search);

        if (instance == null) setSearch(searchString);
        instance = this;
    }

    private void setSearch(String search) {
        searchString = search;
        entries.clear();
        search = search.toLowerCase();
        for (Entry entry : ItemRegistry.registry) {
            String name = entry.itemStack.getName().toString().toLowerCase();
            String lore = entry.itemStack.getTag().toString().toLowerCase();
            if (name.contains(search) || lore.contains(search))
                entries.add(entry);
        }
        this.maxScroll = Math.max(0, entries.size() / this.cols - this.rows + 1);
        scroll = Math.min(scroll, this.maxScroll);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        ItemRenderer itemRenderer = client.getItemRenderer();
        RenderSystem.disableDepthTest();
        // search box title
        client.textRenderer.drawWithShadow(matrices, "Search", this.gridX, 6, 0xff9e9e9e);
        // slot hover
        int mouseOverIndex = getMouseOverIndex(mouseX, mouseY);
        if (mouseOverIndex != -1) {
            int x = this.gridX + mouseOverIndex % this.cols * 16;
            int y = this.gridY + (mouseOverIndex / this.cols - (int)scroll) * 16;
            fill(matrices, x, y, x + 16, y + 16, 0x20ffffff);
        }
        // item list
        for (int i = 0; i < rows; ++i)
            for (int j = 0; j < cols; ++j) {
                int index = (i + (int)scroll) * cols + j;
                if (index < entries.size()) {
                    int x = gridX + j * 16;
                    int y = gridY + i * 16;
                    itemRenderer.renderInGui(entries.get(index).itemStack, x, y);
                }
            }
        // item tooltip
        if (mouseOverIndex != -1) {
            List<Text> tooltip = getTooltipFromItem(entries.get(mouseOverIndex).itemStack);
            renderTooltip(matrices, tooltip, mouseX, mouseY);
        }
        RenderSystem.enableDepthTest();
    }

    private boolean isMouseOverList(double mouseX, double mouseY) {
        return gridX <= mouseX && mouseX < gridX + cols * 16 && gridY <= mouseY && mouseY < gridY + rows * 16;
    }

    private int getMouseOverIndex(double mouseX, double mouseY) {
        if (isMouseOverList(mouseX, mouseY)) {
            int i = (int)(mouseY - this.gridY) / 16;
            int j = (int)(mouseX - this.gridX) / 16;
            int index = (i + (int)scroll) * this.cols + j;
            if (index < entries.size()) return index;
        }
        return -1;
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
        return this.getFocused() instanceof TextFieldWidget && ((TextFieldWidget) this.getFocused()).isFocused() && keyCode != 256;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        int index = getMouseOverIndex(mouseX, mouseY);
        if (index != -1 && entries.get(index).clickCommand != null) {
            this.client.player.sendChatMessage(entries.get(index).clickCommand);
            return true;
        }
        return false;
    }
}
