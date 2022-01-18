package com.github.konaeakira.skypixel.mixin;

import com.github.konaeakira.skypixel.Skypixel;
import com.github.konaeakira.skypixel.StatusBars;
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
    @ModifyVariable(method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"), argsOnly = true)
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
        StatusBars.render(matrices);
        ci.cancel();
    }
}
