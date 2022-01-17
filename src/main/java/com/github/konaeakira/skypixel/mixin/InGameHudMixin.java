package com.github.konaeakira.skypixel.mixin;

import com.github.konaeakira.skypixel.Skypixel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final
    MinecraftClient client;
    @Shadow @Final
    private int scaledWidth;
    @Shadow @Final
    private int scaledHeight;

    @ModifyVariable(method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"))
    private Text setOverlayMessage(Text message) {
        String msg = message.getString();
        if (msg != null) {
            Matcher health = Pattern.compile("(§.)*(\\d+)/(\\d+)❤").matcher(msg);
            if (health.find()) {
                Skypixel.Attribute.HEALTH.set(Integer.parseInt(health.group(2)));
                Skypixel.Attribute.MAX_HEALTH.set(Integer.parseInt(health.group(3)));
                msg = health.replaceAll("").trim();
            }
            Matcher mana = Pattern.compile("(§.)*(\\d+)/(\\d+)✎\\sMana").matcher(msg);
            if (mana.find()) {
                Skypixel.Attribute.MANA.set(Integer.parseInt(mana.group(2)));
                Skypixel.Attribute.MAX_MANA.set(Integer.parseInt(mana.group(3)));
                msg = mana.replaceAll("").trim();
            }
            Matcher defense = Pattern.compile("(§.)*(\\d+)(§.)*❈\\sDefense").matcher(msg);
            if (defense.find()) {
                Skypixel.Attribute.DEFENSE.set(Integer.parseInt(defense.group(2)));
                msg = defense.replaceAll("").trim();
            }
            if (!msg.isEmpty()) {
                System.out.println(msg);
            }
        }
        return Text.of(msg);
    }

    @Inject(method = "renderStatusBars(Lnet/minecraft/client/util/math/MatrixStack;)V", at = @At("HEAD"), cancellable = true)
    private void renderStatusBars(MatrixStack matrices, CallbackInfo ci) {
        int health = min(20, Skypixel.Attribute.HEALTH.get() * 20 / Skypixel.Attribute.MAX_HEALTH.get());
        for (int i = 0; i < 10; ++i) {
            int x = scaledWidth / 2 - 91 + 8 * i;
            int y = scaledHeight - 39;
            ((InGameHud)(Object)this).drawTexture(matrices, x, y, 16, 0, 9, 9);
            if (health > i * 2 + 1) {
                ((InGameHud)(Object)this).drawTexture(matrices, x, y, 52, 0, 9, 9);
            } else if (health == i * 2 + 1) {
                ((InGameHud)(Object)this).drawTexture(matrices, x, y, 61, 0, 9, 9);
            }
        }

        int mana = min(10, Skypixel.Attribute.MANA.get() * 10 / Skypixel.Attribute.MAX_MANA.get());
        for (int i = 0; i < 10; ++i) {
            int x = scaledWidth / 2 + 83 - 8 * i;
            int y = scaledHeight - 39;
            if (mana > i) {
                ((InGameHud)(Object)this).drawTexture(matrices, x, y, 16, 18, 9, 9);
            }
        }

        TextRenderer renderer = client.textRenderer;

        String health_txt = String.format("%d", Skypixel.Attribute.HEALTH.get());
        int health_x = scaledWidth / 2 - 90;
        int health_y = scaledHeight - 48;
        renderer.draw(matrices, health_txt, health_x - 1, health_y, 0x000000);
        renderer.draw(matrices, health_txt, health_x + 1, health_y, 0x000000);
        renderer.draw(matrices, health_txt, health_x, health_y - 1, 0x000000);
        renderer.draw(matrices, health_txt, health_x, health_y + 1, 0x000000);
        renderer.draw(matrices, health_txt, health_x, health_y, 0xffffff);

        String mana_txt = String.format("%d", Skypixel.Attribute.MANA.get());
        int mana_x = scaledWidth / 2 + 91 - 6 * mana_txt.length();
        int mana_y = scaledHeight - 48;
        renderer.draw(matrices, mana_txt, mana_x - 1, mana_y, 0x000000);
        renderer.draw(matrices, mana_txt, mana_x + 1, mana_y, 0x000000);
        renderer.draw(matrices, mana_txt, mana_x, mana_y - 1, 0x000000);
        renderer.draw(matrices, mana_txt, mana_x, mana_y + 1, 0x000000);
        renderer.draw(matrices, mana_txt, mana_x, mana_y, 0xffffff);

        ci.cancel();
    }
}
