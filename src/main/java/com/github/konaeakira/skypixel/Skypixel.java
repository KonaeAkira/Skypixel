package com.github.konaeakira.skypixel;

import com.github.konaeakira.skypixel.itemlist.ItemList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Environment(EnvType.CLIENT)
public class Skypixel implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (!Files.isDirectory(Paths.get("config/skypixel"))) {
            (new File("config/skypixel")).mkdir();
        }
        ItemList.init();
    }

    public enum Attribute {
        HEALTH(100),
        MAX_HEALTH(100),
        MANA(100),
        MAX_MANA(100),
        DEFENSE(0);

        private int value;

        Attribute(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }

        public void set(int value) {
            this.value = value;
        }
    }
}
