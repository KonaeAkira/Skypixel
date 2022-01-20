package com.github.konaeakira.skypixel;

import com.github.konaeakira.skypixel.utils.ItemFixer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemList  {
    private static final String ITEM_REPO_DIR = "config/skypixel/items-repo";
    private static final String ITEM_LIST_DIR = ITEM_REPO_DIR + "/items";

    private static int page = 0;

    private static ArrayList<ItemStack> items = new ArrayList<>();

    public static void init() {
        updateItemRepo();
        readItemFiles();
    }

    private static void updateItemRepo() {
        if (!Files.isDirectory(Paths.get(ITEM_REPO_DIR))) {
            try {
                Git.cloneRepository()
                        .setURI("https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO.git")
                        .setDirectory(new File(ITEM_REPO_DIR))
                        .setBranchesToClone(Arrays.asList("refs/heads/master"))
                        .setBranch("refs/heads/master")
                        .call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Git.open(new File(ITEM_REPO_DIR)).pull().call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void readItemFiles() {
        File dir = new File(ITEM_LIST_DIR);
        File[] files = dir.listFiles();
        for (File file : files) {
            String path = ITEM_LIST_DIR + "/" + file.getName();
            try {
                String content = Files.readString(Paths.get(path));
                registerItem(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void registerItem(String json) {
        JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
        String internalName = obj.get("internalname").getAsString();

        NbtCompound root = new NbtCompound();
        root.put("Count", NbtByte.of((byte)1));

        String id = obj.get("itemid").getAsString();
        int damage = obj.get("damage").getAsInt();
        root.put("id", NbtString.of(ItemFixer.convert(id, damage)));

        NbtCompound tag = new NbtCompound();
        root.put("tag", tag);

        NbtCompound display = new NbtCompound();
        tag.put("display", display);

        String name = obj.get("displayname").getAsString();
        display.put("Name", NbtString.of(Text.Serializer.toJson(Text.of(name))));
        NbtList lore = new NbtList();
        display.put("Lore", lore);
        obj.get("lore").getAsJsonArray().forEach(
                el -> lore.add(NbtString.of(Text.Serializer.toJson(Text.of(el.getAsString()))))
        );

        String nbttag = obj.get("nbttag").getAsString();
        Matcher matcher = Pattern.compile("SkullOwner:\\{Id:\"(.{36})\",Properties:\\{textures:\\[0:\\{Value:\"(.+)\"\\}\\]\\}\\}").matcher(nbttag);
        if (matcher.find()) {
            NbtCompound skullOwner = new NbtCompound();
            tag.put("SkullOwner", skullOwner);
            UUID uuid = UUID.fromString(matcher.group(1));
            skullOwner.put("Id", NbtHelper.fromUuid(uuid));
            skullOwner.put("Name", NbtString.of(internalName));

            NbtCompound properties = new NbtCompound();
            skullOwner.put("Properties", properties);
            NbtList textures = new NbtList();
            properties.put("textures", textures);
            NbtCompound texture = new NbtCompound();
            textures.add(texture);
            texture.put("Value", NbtString.of(matcher.group(2)));
        }

        ItemStack itemStack = ItemStack.fromNbt(root);
        if (itemStack.getItem().equals(Items.AIR)) {
            System.err.println("ItemList: cannot register: " + internalName + " (" + id + ")");
        }
        items.add(itemStack);
    }

    public static void render(MatrixStack matrices, int mouseX, int mouseY, HandledScreen screen) {
        MinecraftClient client = MinecraftClient.getInstance();

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        int rows = scaledHeight / 16;
        int cols = 10;

        int gridX = scaledWidth - cols * 16;
        int gridY = 0;

        RenderSystem.disableDepthTest();

        ItemRenderer itemRenderer = client.getItemRenderer();
        for (int i = 0; i < rows; ++i)
            for (int j = 0; j < cols; ++j) {
                int index = page * rows * cols + i * cols + j;
                if (index < items.size()) {
                    int x = gridX + j * 16;
                    int y = gridY + i * 16;
                    itemRenderer.renderInGui(items.get(index), x, y);
                }
            }

        if (gridX <= mouseX && gridX + cols * 16 > mouseX && gridY <= mouseY && gridY + rows * 16 > mouseY) {
            int i = (mouseY - gridY) / 16;
            int j = (mouseX - gridX) / 16;
            int index = page * rows * cols + i * cols + j;
            List<Text> tooltip = screen.getTooltipFromItem(items.get(index));
            screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
        }
        RenderSystem.enableDepthTest();
    }
}

