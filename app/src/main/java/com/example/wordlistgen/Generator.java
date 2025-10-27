package com.example.wordlistgen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Generator {

    public interface Callback {
        void onWord(String w);
        boolean isCancelled();
    }

    // Simple leet map
    private static char leet(char c) {
        switch (Character.toLowerCase(c)) {
            case 'a': return '4';
            case 'e': return '3';
            case 'i': return '1';
            case 'o': return '0';
            case 's': return '5';
            case 't': return '7';
            default: return c;
        }
    }

    // Apply leet conversion to a string
    public static String toLeet(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) sb.append(leet(c));
        return sb.toString();
    }

    // Apply simple transforms and return distinct variants
    public static List<String> transforms(String token, boolean leetEnabled, boolean reverseEnabled) {
        Set<String> set = new HashSet<>();
        if (token == null || token.trim().isEmpty()) return new ArrayList<>(set);
        String t = token.trim();
        set.add(t);
        set.add(t.toLowerCase());
        set.add(t.toUpperCase());
        // Capitalize first letter
        set.add(Character.toUpperCase(t.charAt(0)) + (t.length() > 1 ? t.substring(1) : ""));

        if (reverseEnabled) {
            String rev = new StringBuilder(t).reverse().toString();
            set.add(rev);
        }

        if (leetEnabled) {
            set.add(toLeet(t));
        }

        return new ArrayList<>(set);
    }

    // Recursively build combinations of tokens up to depth `combLen`
    private static void combineRecursive(List<String> tokens, int combLen, StringBuilder current, List<String> out) {
        if (combLen == 0) {
            out.add(current.toString());
            return;
        }
        for (int i = 0; i < tokens.size(); i++) {
            int lenBefore = current.length();
            current.append(tokens.get(i));
            combineRecursive(tokens, combLen - 1, current, out); // allow tokens repeat and re-order
            current.setLength(lenBefore);
        }
    }

    /**
     * Main generator method:
     * - tokens: base tokens (already normalized)
     * - years: optional strings to append
     * - symbols: optional strings to append
     * - maxComb: max tokens combined (1 means single tokens only)
     * - leetEnabled / reverseEnabled: transforms
     * - maxLines: stops after this many lines (to avoid explosion)
     * - callback: receives each generated word and can return isCancelled
     */
    public static long generate(
            List<String> tokens,
            List<String> years,
            List<String> symbols,
            int maxComb,
            boolean leetEnabled,
            boolean reverseEnabled,
            long maxLines,
            Callback callback
    ) {
        long count = 0;
        if (tokens == null || tokens.isEmpty()) return 0;

        // For combLen = 1..maxComb
        outer:
        for (int combLen = 1; combLen <= maxComb; combLen++) {
            // Build all base combinations for this combLen
            List<String> baseCombs = new ArrayList<>();
            combineRecursive(tokens, combLen, new StringBuilder(), baseCombs);

            for (String base : baseCombs) {
                // apply transforms
                List<String> variants = transforms(base, leetEnabled, reverseEnabled);

                for (String v : variants) {
                    if (callback.isCancelled()) break outer;
                    // 1) yield v
                    callback.onWord(v);
                    count++;
                    if (count >= maxLines) break outer;

                    // 2) v + year
                    if (years != null) for (String y : years) {
                        if (callback.isCancelled()) break outer;
                        callback.onWord(v + y);
                        count++;
                        if (count >= maxLines) break outer;
                    }
                    // 3) v + symbol
                    if (symbols != null) for (String s : symbols) {
                        if (callback.isCancelled()) break outer;
                        callback.onWord(v + s);
                        count++;
                        if (count >= maxLines) break outer;
                    }
                    // 4) v + year + symbol
                    if (years != null && symbols != null) {
                        for (String y : years) {
                            for (String s : symbols) {
                                if (callback.isCancelled()) break outer;
                                callback.onWord(v + y + s);
                                count++;
                                if (count >= maxLines) break outer;
                            }
                        }
                    }
                }
            }
        }

        return count;
    }
}