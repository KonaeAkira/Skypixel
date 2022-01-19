package com.github.konaeakira.skypixel.mixin;

import com.github.konaeakira.skypixel.QuickNavButton;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        String title = super.getTitle().getString().trim();
        System.out.println(title);

        int left_x = (super.width - this.backgroundWidth) / 2 + 4;
        int right_x = (super.width + this.backgroundWidth) / 2 - 3;
        int top_y = (super.height - this.backgroundHeight) / 2 - 28;
        int bottom_y = (super.height + this.backgroundHeight) / 2 - 4;
        if (this.backgroundHeight > 166) --bottom_y; // why is this even a thing

        super.addButton(new QuickNavButton(left_x + 29 * 0, top_y, QuickNavButton.Type.TOP, title.contains("Your Skills"), "/skills", new ItemStack(Items.DIAMOND_SWORD)));
        super.addButton(new QuickNavButton(left_x + 29 * 1, top_y, QuickNavButton.Type.TOP, title.contains("Collection"), "/collection", new ItemStack(Items.PAINTING)));

        super.addButton(new QuickNavButton(right_x - 29 * 1, top_y, QuickNavButton.Type.TOP, title.contains("Craft Item"), "/craft", new ItemStack(Items.CRAFTING_TABLE)));
        super.addButton(new QuickNavButton(right_x - 29 * 2, top_y, QuickNavButton.Type.TOP, title.contains("Anvil"), "/av", new ItemStack(Items.ANVIL)));
        super.addButton(new QuickNavButton(right_x - 29 * 3, top_y, QuickNavButton.Type.TOP, title.contains("Enchant Item"), "/et", new ItemStack(Items.ENCHANTING_TABLE)));

        super.addButton(new QuickNavButton(right_x - 29 * 1, bottom_y, QuickNavButton.Type.BOTTOM, title.contains("Storage"), "/storage", new ItemStack(Items.ENDER_CHEST)));
        try {
            super.addButton(new QuickNavButton(right_x - 29 * 2, bottom_y, QuickNavButton.Type.BOTTOM, title.contains("Wardrobe"), "/wd", ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:leather_chestplate\", Count:1, tag:{display:{color:8991416}}}"))));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        super.addButton(new QuickNavButton(right_x - 29 * 3, bottom_y, QuickNavButton.Type.BOTTOM, title.contains("Pets"), "/pets", new ItemStack(Items.BONE)));
    }
    private void dummy() {}
}
