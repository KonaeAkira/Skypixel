package com.github.konaeakira.skypixel.mixin;

import com.github.konaeakira.skypixel.itemlist.ItemListScreen;
import com.github.konaeakira.skypixel.quicknav.QuickNav;
import com.github.konaeakira.skypixel.quicknav.QuickNavButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        // quicknav
        String title = super.getTitle().getString().trim();
        int left_x = (super.width - this.backgroundWidth) / 2 + 4;
        int right_x = (super.width + this.backgroundWidth) / 2 - 3;
        int top_y = (super.height - this.backgroundHeight) / 2 - 28;
        int bottom_y = (super.height + this.backgroundHeight) / 2 - 4;
        if (this.backgroundHeight > 166) --bottom_y; // why is this even a thing
        List<QuickNavButton> buttons = QuickNav.init(title, left_x, right_x, top_y, bottom_y);
        for (QuickNavButton button : buttons) super.addButton(button);

        // itemlist
        super.addChild(new ItemListScreen((HandledScreen)(Object)this));
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", at = @At("TAIL"))
    private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ItemListScreen.getInstance().render(matrices, mouseX, mouseY, delta);
    }
}
