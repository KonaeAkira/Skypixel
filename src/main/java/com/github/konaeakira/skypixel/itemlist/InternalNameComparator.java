package com.github.konaeakira.skypixel.itemlist;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InternalNameComparator implements Comparator<String> {
    private static String name(String s) {
        return s.replaceAll(".(\\d+)$", "");
    }

    private static int tier(String s) {
        Matcher m = Pattern.compile(".(\\d+)$").matcher(s);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 0;
    }

    @Override
    public int compare(String s1, String s2) {
        if (name(s1).equals(name(s2))) return tier(s1) - tier(s2);
        return s1.compareTo(s2);
    }
}
