package com.github.konaeakira.skypixel.itemlist;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemRegistry {
    private static final String ITEM_REPO_URI = "https://github.com/KonaeAkira/NotEnoughUpdates-REPO.git";

    private static final String ITEM_REPO_DIR = "./config/skypixel/items-repo/";
    private static final String ITEM_LIST_DIR = ITEM_REPO_DIR + "items/";
    private static final String CONSTANTS_DIR = ITEM_REPO_DIR + "constants/";
    private static final String PETNUMS_FILE = CONSTANTS_DIR + "petnums.json";

    private static final JsonParser JSON_PARSER = new JsonParser();

    protected static ArrayList<ItemStack> items = new ArrayList<>();
    private static JsonObject petNums;

    // TODO: make async
    public static void init() {
        updateItemRepo();
        try {
            petNums = JSON_PARSER.parse(Files.readString(Paths.get(PETNUMS_FILE))).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        readItemFiles();
    }

    private static void updateItemRepo() {
        if (!Files.isDirectory(Paths.get(ITEM_REPO_DIR))) {
            try {
                Git.cloneRepository()
                        .setURI(ITEM_REPO_URI)
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
        JsonObject obj = JSON_PARSER.parse(json).getAsJsonObject();
        List<Pair<String, String>> injectors = new ArrayList<>();

        String internalName = obj.get("internalname").getAsString();
        injectors.addAll(petData(internalName));

        NbtCompound root = new NbtCompound();
        root.put("Count", NbtByte.of((byte)1));

        String id = obj.get("itemid").getAsString();
        int damage = obj.get("damage").getAsInt();
        root.put("id", NbtString.of(ItemFixerUpper.convert(id, damage)));

        NbtCompound tag = new NbtCompound();
        root.put("tag", tag);

        NbtCompound display = new NbtCompound();
        tag.put("display", display);

        String name = injectData(obj.get("displayname").getAsString(), injectors);
        display.put("Name", NbtString.of(Text.Serializer.toJson(Text.of(name))));

        NbtList lore = new NbtList();
        display.put("Lore", lore);
        obj.get("lore").getAsJsonArray().forEach(el ->
                lore.add(NbtString.of(Text.Serializer.toJson(Text.of(injectData(el.getAsString(), injectors)))))
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

    private static String injectData(String string, List<Pair<String, String>> injectors) {
        for (Pair<String, String> injector : injectors)
            string = string.replaceAll(injector.getLeft(), injector.getRight());
        return string;
    }

    // TODO: fix stats for GOLDEN_DRAGON (lv1 -> lv200)
    private static List<Pair<String, String>> petData(String internalName) {
        List<Pair<String, String>> list = new ArrayList<>();

        String petName = internalName.split(";")[0];
        if (!internalName.contains(";") || !petNums.has(petName)) return list;

        list.add(new Pair("\\{LVL\\}", "1 ➡ 100"));

        final String[] rarities = {
                "COMMON",
                "UNCOMMON",
                "RARE",
                "EPIC",
                "LEGENDARY",
                "MYTHIC"
        };
        String rarity = rarities[Integer.parseInt(internalName.split(";")[1])];
        JsonObject data = petNums.get(petName).getAsJsonObject().get(rarity).getAsJsonObject();

        JsonObject statNumsMin = data.get("1").getAsJsonObject().get("statNums").getAsJsonObject();
        JsonObject statNumsMax = data.get("100").getAsJsonObject().get("statNums").getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entrySet = statNumsMin.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey();
            String left = "\\{" + key+ "\\}";
            String right = statNumsMin.get(key).getAsString() + " ➡ " + statNumsMax.get(key).getAsString();
            list.add(new Pair(left, right));
        }

        JsonArray otherNumsMin = data.get("1").getAsJsonObject().get("otherNums").getAsJsonArray();
        JsonArray otherNumsMax = data.get("100").getAsJsonObject().get("otherNums").getAsJsonArray();
        for (int i = 0; i < otherNumsMin.size(); ++i) {
            String left = "\\{" + Integer.toString(i) + "\\}";
            String right = otherNumsMin.get(i).getAsString() + " ➡ " + otherNumsMax.get(i).getAsString();
            list.add(new Pair(left, right));
        }

        return list;
    }
}

