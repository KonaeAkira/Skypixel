package com.github.konaeakira.skypixel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

import static java.lang.Math.min;

public class StatusBars {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final InGameHud inGameHud = client.inGameHud;
    private static final TextRenderer textRenderer = client.textRenderer;
    private static int scaledWidth;
    private static int scaledHeight;

    public static void render(MatrixStack matrices) {
        scaledWidth = client.getWindow().getScaledWidth();
        scaledHeight = client.getWindow().getScaledHeight();
        renderHealth(matrices);
        renderMana(matrices);
    }

    private static void renderHealth(MatrixStack matrices) {
        client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
        int health = min(20, Skypixel.Attribute.HEALTH.get() * 20 / Skypixel.Attribute.MAX_HEALTH.get());
        for (int i = 0; i < 10; ++i) {
            int x = scaledWidth / 2 - 91 + 8 * i;
            int y = scaledHeight - 39;
            inGameHud.drawTexture(matrices, x, y, 16, 0, 9, 9);
            if (health > i * 2 + 1) {
                inGameHud.drawTexture(matrices, x, y, 52, 0, 9, 9);
            } else if (health == i * 2 + 1) {
                inGameHud.drawTexture(matrices, x, y, 61, 0, 9, 9);
            }
        }
        String txt = String.format("%d", Skypixel.Attribute.HEALTH.get());
        int x = scaledWidth / 2 - 90;
        int y = scaledHeight - 48;
        textRenderer.draw(matrices, txt, x - 1, y, 0x000000);
        textRenderer.draw(matrices, txt, x + 1, y, 0x000000);
        textRenderer.draw(matrices, txt, x, y - 1, 0x000000);
        textRenderer.draw(matrices, txt, x, y + 1, 0x000000);
        textRenderer.draw(matrices, txt, x, y, 0xffffff);
    }

    private static void renderMana(MatrixStack matrices) {
        client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
        int mana = min(10, Skypixel.Attribute.MANA.get() * 10 / Skypixel.Attribute.MAX_MANA.get());
        for (int i = 0; i < 10; ++i) {
            int x = scaledWidth / 2 + 83 - 8 * i;
            int y = scaledHeight - 39;
            if (mana > i) {
                inGameHud.drawTexture(matrices, x, y, 16, 18, 9, 9);
            }
        }
        String txt = String.format("%d", Skypixel.Attribute.MANA.get());
        int x = scaledWidth / 2 + 91 - 6 * txt.length();
        int y = scaledHeight - 48;
        textRenderer.draw(matrices, txt, x - 1, y, 0x000000);
        textRenderer.draw(matrices, txt, x + 1, y, 0x000000);
        textRenderer.draw(matrices, txt, x, y - 1, 0x000000);
        textRenderer.draw(matrices, txt, x, y + 1, 0x000000);
        textRenderer.draw(matrices, txt, x, y, 0xffffff);
    }
}
