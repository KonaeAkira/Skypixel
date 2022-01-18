package com.github.konaeakira.skypixel.mixin;

import com.github.konaeakira.skypixel.ToastBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.item.Items;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    private static final String[] filters = {
            "RARE DROP!",
            "CRAZY RARE DROP!",
            "INSANE DROP!",
            "PET DROP!"
    };

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onMessage(MessageType messageType, Text message, UUID senderUuid, CallbackInfo ci) {
        if (message.getSiblings().size() > 1) {
            String first = message.getSiblings().get(0).getString().trim();
            for (String filter : filters) {
                if (first.equals(filter)) {
                    Text title = message.getSiblings().get(0);
                    Text description = message.getSiblings().get(1);
                    client.getToastManager().add(new ToastBuilder(title, description));
                    ci.cancel();
                    break;
                }
            }
        }
        System.out.println(message);
    }

}