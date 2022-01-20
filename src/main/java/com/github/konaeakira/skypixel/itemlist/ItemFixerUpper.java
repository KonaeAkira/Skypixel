package com.github.konaeakira.skypixel.itemlist;

import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public class ItemFixerUpper {
    private final static Map<String, String> MAPPING = Map.ofEntries(
            Map.entry("minecraft:golden_rail", "minecraft:powered_rail"),
            Map.entry("minecraft:lit_pumpkin", "minecraft:jack_o_lantern"),
            Map.entry("minecraft:snow_layer", "minecraft:snow"),
            Map.entry("minecraft:hardened_clay", "minecraft:terracotta"),
            Map.entry("minecraft:speckled_melon", "minecraft:glistering_melon_slice"),
            Map.entry("minecraft:mob_spawner", "minecraft:spawner"),
            Map.entry("minecraft:brick_block", "minecraft:bricks"),
            Map.entry("minecraft:deadbush", "minecraft:dead_bush"),
            Map.entry("minecraft:slime", "minecraft:slime_block"),
            Map.entry("minecraft:melon_block", "minecraft:melon"),
            Map.entry("minecraft:reeds", "minecraft:sugar_cane"),
            Map.entry("minecraft:yellow_flower", "minecraft:dandelion"),
            Map.entry("minecraft:firework_charge", "minecraft:firework_star"),
            Map.entry("minecraft:noteblock", "minecraft:note_block"),
            Map.entry("minecraft:web", "minecraft:cobweb"),
            Map.entry("minecraft:fireworks", "minecraft:firework_rocket"),
            Map.entry("minecraft:netherbrick", "minecraft:nether_brick"),
            Map.entry("minecraft:stained_hardened_clay", "minecraft:terracotta"),
            Map.entry("minecraft:quartz_ore", "minecraft:nether_quartz_ore"),
            Map.entry("minecraft:skull", "minecraft:player_head"),
            Map.entry("minecraft:fish", "minecraft:cod"),
            Map.entry("minecraft:cooked_fish", "minecraft:cooked_cod"),
            Map.entry("minecraft:red_flower", "minecraft:poppy"),
            Map.entry("minecraft:waterlily", "minecraft:lily_pad")
    );

    private final static String[] DYE_COLORS = {
            "minecraft:ink_sac",
            "minecraft:red_dye",
            "minecraft:green_dye",
            "minecraft:cocoa_beans",
            "minecraft:lapis_lazuli",
            "minecraft:purple_dye",
            "minecraft:cyan_dye",
            "minecraft:light_gray_dye",
            "minecraft:gray_dye",
            "minecraft:pink_dye",
            "minecraft:lime_dye",
            "minecraft:yellow_dye",
            "minecraft:light_blue_dye",
            "minecraft:magenta_dye",
            "minecraft:orange_dye",
            "minecraft:bone_meal"
    };

    private final static String[] BLOCK_COLORS = {
            "white_",
            "orange_",
            "magenta_",
            "light_blue_",
            "yellow_",
            "lime_",
            "pink_",
            "gray_",
            "light_gray_",
            "cyan_",
            "purple_",
            "blue_",
            "brown_",
            "green_",
            "red_",
            "black_"
    };

    private final static String[] TREE_VARIANTS = {
            "oak_",
            "spruce_",
            "birch_",
            "jungle_",
            "acacia_",
            "dark_oak_"
    };

    private final static String[] STONE_BRICK_VARIANTS = {
            "minecraft:stone_bricks",
            "minecraft:mossy_stone_bricks",
            "minecraft:cracked_stone_bricks",
            "minecraft:chiseled_stone_bricks"
    };

    private final static Map<Integer, String> SPAWN_EGG_VARIANTS = Map.ofEntries(
            Map.entry(50, "minecraft:creeper_spawn_egg"),
            Map.entry(51, "minecraft:skeleton_spawn_egg"),
            Map.entry(52, "minecraft:spider_spawn_egg"),
            Map.entry(54, "minecraft:zombie_spawn_egg"),
            Map.entry(55, "minecraft:slime_spawn_egg"),
            Map.entry(56, "minecraft:ghast_spawn_egg"),
            Map.entry(57, "minecraft:zombified_piglin_spawn_egg"),
            Map.entry(58, "minecraft:enderman_spawn_egg"),
            Map.entry(59, "minecraft:cave_spider_spawn_egg"),
            Map.entry(60, "minecraft:silverfish_spawn_egg"),
            Map.entry(61, "minecraft:blaze_spawn_egg"),
            Map.entry(62, "minecraft:magma_cube_spawn_egg"),
            Map.entry(65, "minecraft:bat_spawn_egg"),
            Map.entry(66, "minecraft:witch_spawn_egg"),
            Map.entry(67, "minecraft:endermite_spawn_egg"),
            Map.entry(68, "minecraft:guardian_spawn_egg"),
            Map.entry(90, "minecraft:pig_spawn_egg"),
            Map.entry(91, "minecraft:sheep_spawn_egg"),
            Map.entry(92, "minecraft:cow_spawn_egg"),
            Map.entry(93, "minecraft:chicken_spawn_egg"),
            Map.entry(94, "minecraft:squid_spawn_egg"),
            Map.entry(95, "minecraft:wolf_spawn_egg"),
            Map.entry(96, "minecraft:mooshroom_spawn_egg"),
            Map.entry(98, "minecraft:ocelot_spawn_egg"),
            Map.entry(100, "minecraft:horse_spawn_egg"),
            Map.entry(101, "minecraft:rabbit_spawn_egg"),
            Map.entry(120, "minecraft:villager_spawn_egg")
    );

    public static String convert(String id, int damage) {
        if (id.equals("minecraft:dye")) return DYE_COLORS[damage];
        if (id.equals("minecraft:stonebrick")) return STONE_BRICK_VARIANTS[damage];
        if (id.equals("minecraft:spawn_egg")) return SPAWN_EGG_VARIANTS.getOrDefault(damage, "minecraft:ghast_spawn_egg");
        id = MAPPING.getOrDefault(id, id);
        if (Registry.ITEM.get(new Identifier(id)).equals(Items.AIR)) {
            String shortId = id.split(":")[1];
            if (damage < BLOCK_COLORS.length && !Registry.ITEM.get(new Identifier("minecraft:" + BLOCK_COLORS[damage] + shortId)).equals(Items.AIR))
                return "minecraft:" + BLOCK_COLORS[damage] + shortId;
            if (damage < TREE_VARIANTS.length && !Registry.ITEM.get(new Identifier("minecraft:" + TREE_VARIANTS[damage] + shortId)).equals(Items.AIR))
                return "minecraft:" + TREE_VARIANTS[damage] + shortId;
            if (id.contains("wooden_")) return id.replaceFirst("wooden_", TREE_VARIANTS[damage]);
            if (id.contains("minecraft:record")) return id.replaceFirst("minecraft:record", "minecraft:music_disc");
        }
        return id;
    }
}
