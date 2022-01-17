package com.github.konaeakira.skypixel;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import javax.annotation.Nullable;

@Environment(value= EnvType.CLIENT)
public class ToastBuilder implements Toast {
    private final Text title;
    private final Text description;

    public ToastBuilder(Text title, Text description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public Toast.Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        manager.getGame().getTextureManager().bindTexture(TEXTURE);
        RenderSystem.color3f(1.0f, 1.0f, 1.0f);
        manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());
        manager.getGame().textRenderer.draw(matrices, title, 7.0f, 7.0f, -11534256);
        manager.getGame().textRenderer.draw(matrices, description, 7.0f, 18.0f, -16777216);
        return startTime >= 3000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}