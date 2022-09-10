package de.themoep.minedown;

/*
 * Copyright (c) 2017 Max Lee (https://github.com/Phoenix616)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Util {

    private static final Pattern WRAP_PATTERN = Pattern.compile(" ", Pattern.LITERAL);

    /**
     * Utility method to throw an IllegalArgumentException if the value is false
     * @param value   The value to validate
     * @param message The message for the exception
     * @throws IllegalArgumentException Thrown if the value is false
     */
    public static void validate(boolean value, String message) throws IllegalArgumentException {
        if (!value) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Apply a collection of colors/formats to a component
     * @param component The BaseComponent
     * @param formats   The collection of ChatColor formats to apply
     * @return The component that was modified
     */
    public static BaseComponent applyFormat(BaseComponent component, Collection<ChatColor> formats) {
        for (ChatColor format : formats) {
            if (format == ChatColor.BOLD) {
                component.setBold(true);
            } else if (format == ChatColor.ITALIC) {
                component.setItalic(true);
            } else if (format == ChatColor.UNDERLINE) {
                component.setUnderlined(true);
            } else if (format == ChatColor.STRIKETHROUGH) {
                component.setStrikethrough(true);
            } else if (format == ChatColor.MAGIC) {
                component.setObfuscated(true);
            } else if (format == ChatColor.RESET) {
                component.setBold(false);
                component.setItalic(false);
                component.setUnderlined(false);
                component.setStrikethrough(false);
                component.setObfuscated(false);
                component.setColor(ChatColor.WHITE);
            } else {
                component.setColor(format);
            }
        }
        if (component.getExtra() != null) {
            for (BaseComponent extra : component.getExtra()) {
                applyFormat(extra, formats);
            }
        }
        return component;
    }

    /**
     * Apply a collection of colors/formats to a component builder
     * @param builder The ComponentBuilder
     * @param formats The collection of ChatColor formats to apply
     * @return The component builder that was modified
     * @deprecated Use {@link #applyFormat(BaseComponent, Collection)}
     */
    @Deprecated
    public static ComponentBuilder applyFormat(ComponentBuilder builder, Set<ChatColor> formats) {
        Map<ChatColor, Boolean> formatMap = new HashMap<>();
        for (ChatColor format : formats) {
            formatMap.put(format, true);
        }
        return applyFormat(builder, formatMap);
    }

    /**
     * Apply a collection of colors/formats to a component builder
     * @param builder The ComponentBuilder
     * @param formats The collection of ChatColor formats to apply
     * @return The component builder that was modified
     */
    public static ComponentBuilder applyFormat(ComponentBuilder builder, Map<ChatColor, Boolean> formats) {
        for (Map.Entry<ChatColor, Boolean> e : formats.entrySet()) {
            if (e.getKey() == ChatColor.BOLD) {
                builder.bold(e.getValue());
            } else if (e.getKey() == ChatColor.ITALIC) {
                builder.italic(e.getValue());
            } else if (e.getKey() == ChatColor.UNDERLINE) {
                builder.underlined(e.getValue());
            } else if (e.getKey() == ChatColor.STRIKETHROUGH) {
                builder.strikethrough(e.getValue());
            } else if (e.getKey() == ChatColor.MAGIC) {
                builder.obfuscated(e.getValue());
            } else if (e.getKey() == ChatColor.RESET) {
                builder.bold(!e.getValue());
                builder.italic(!e.getValue());
                builder.underlined(!e.getValue());
                builder.strikethrough(!e.getValue());
                builder.obfuscated(!e.getValue());
                builder.color(ChatColor.WHITE);
            } else if (e.getValue()) {
                builder.color(e.getKey());
            } else if (builder.getCurrentComponent().getColor() == e.getKey()) {
                builder.color(null);
            }
        }
        return builder;
    }

    /**
     * Check whether or not a character at a certain index of a string repeats itself
     * @param string The string to check
     * @param index  The index at which to check the character
     * @return Whether or not the character at that index repeated itself
     */
    public static boolean isDouble(String string, int index) {
        return index + 1 < string.length() && string.charAt(index) == string.charAt(index + 1);
    }

    /**
     * Check whether a certain ChatColor is formatting or not
     * @param format The ChatColor to check
     * @return <code>true</code> if it's a format, <code>false</code> if it's a color
     */
    public static boolean isFormat(ChatColor format) {
        return !MineDown.getFormatString(format).isEmpty();
    }

    /**
     * Get a set of ChatColor formats all formats that a component includes
     * @param component    The component to get the formats from
     * @param ignoreParent Whether or not to include the parent's format
     * @return A set of all the format ChatColors that the component includes
     */
    public static Set<ChatColor> getFormats(BaseComponent component, boolean ignoreParent) {
        Set<ChatColor> formats = new LinkedHashSet<>();
        if ((!ignoreParent && component.isBold()) || (component.isBoldRaw() != null && component.isBoldRaw())) {
            formats.add(ChatColor.BOLD);
        }
        if ((!ignoreParent && component.isItalic()) || (component.isItalicRaw() != null && component.isItalicRaw())) {
            formats.add(ChatColor.ITALIC);
        }
        if ((!ignoreParent && component.isUnderlined()) || (component.isUnderlinedRaw() != null && component.isUnderlinedRaw())) {
            formats.add(ChatColor.UNDERLINE);
        }
        if ((!ignoreParent && component.isStrikethrough()) || (component.isStrikethroughRaw() != null && component.isStrikethroughRaw())) {
            formats.add(ChatColor.STRIKETHROUGH);
        }
        if ((!ignoreParent && component.isObfuscated()) || (component.isObfuscatedRaw() != null && component.isObfuscatedRaw())) {
            formats.add(ChatColor.MAGIC);
        }
        return formats;
    }

    /**
     * Get the index of the first occurrences of a not escaped character
     * @param string The string to search
     * @param chars  The characters to search for
     * @return The first unescaped index or -1 if not found
     */
    public static int indexOfNotEscaped(String string, String chars) {
        return indexOfNotEscaped(string, chars, 0);
    }

    /**
     * Get the index of the first occurrences of a not escaped character
     * @param string    The string to search
     * @param chars     The characters to search for
     * @param fromIndex Start searching from that index
     * @return The first unescaped index or {@code -1} if not found
     */
    public static int indexOfNotEscaped(String string, String chars, int fromIndex) {
        for (int i = fromIndex; i < string.length(); i++) {
            int index = string.indexOf(chars, i);
            if (index == -1) {
                return -1;
            }
            if (!isEscaped(string, index)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Check if a character at a certain index is escaped
     * @param string The string to check
     * @param index  The index of the character in the string to check
     * @return Whether or not the character is escaped (uneven number of backslashes in front of char mains it is escaped)
     * @throws IndexOutOfBoundsException if the {@code index} argument is not less than the length of this string.
     */
    public static boolean isEscaped(String string, int index) {
        if (index - 1 > string.length()) {
            return false;
        }
        int e = 0;
        while (index > e && string.charAt(index - e - 1) == '\\') {
            e++;
        }
        return e % 2 != 0;
    }

    /**
     * Gets the proper end index of a certain definition on the same depth while ignoring escaped chars.
     * @param string    The string to search
     * @param startChar The start cahracter of the definition
     * @param endChar   The end character of the definition
     * @param fromIndex The index to start searching from (should be at the start char)
     * @return The first end index of that group  or {@code -1} if not found
     */
    public static int getUnescapedEndIndex(String string, char startChar, char endChar, int fromIndex) {
        int depth = 0;
        boolean innerEscaped = false;
        for (int i = fromIndex; i < string.length(); i++) {
            if (innerEscaped) {
                innerEscaped = false;
            } else if (string.charAt(i) == '\\') {
                innerEscaped = true;
            } else if (string.charAt(i) == startChar) {
                depth++;
            } else if (string.charAt(i) == endChar) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Wrap a string if it is longer than the line length and contains no new line.
     * Will try to wrap at spaces between words.
     * @param string        The string to wrap
     * @param lineLength    The max length of a line
     * @return The wrapped string
     */
    public static String wrap(String string, int lineLength) {
        if (string.length() <= lineLength || string.contains("\n")) {
            return string;
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        for (String s : WRAP_PATTERN.split(string)) {
            if (currentLine.length() + s.length() + 1 > lineLength) {
                int rest = lineLength - currentLine.length() - 1;
                if (rest > lineLength / 4 && s.length() > Math.min(rest * 2, lineLength / 4)) {
                    currentLine.append(" ").append(s, 0, rest);
                } else {
                    rest = 0;
                }
                lines.add(currentLine.toString());
                String restString = s.substring(rest);
                while (restString.length() >= lineLength) {
                    lines.add(restString.substring(0, lineLength));
                    restString = restString.substring(lineLength);
                }
                currentLine = new StringBuilder(restString);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(s);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return String.join("\n", lines);
    }

    private static Map<ChatColor, Color> legacyColors = new LinkedHashMap<>();

    static {
        legacyColors.put(ChatColor.BLACK, new Color(0x000000));
        legacyColors.put(ChatColor.DARK_BLUE, new Color(0x0000AA));
        legacyColors.put(ChatColor.DARK_GREEN, new Color(0x00AA00));
        legacyColors.put(ChatColor.DARK_AQUA, new Color(0x00AAAA));
        legacyColors.put(ChatColor.DARK_RED, new Color(0xAA0000));
        legacyColors.put(ChatColor.DARK_PURPLE, new Color(0xAA00AA));
        legacyColors.put(ChatColor.GOLD, new Color(0xFFAA00));
        legacyColors.put(ChatColor.GRAY, new Color(0xAAAAAA));
        legacyColors.put(ChatColor.DARK_GRAY, new Color(0x555555));
        legacyColors.put(ChatColor.BLUE, new Color(0x05555FF));
        legacyColors.put(ChatColor.GREEN, new Color(0x55FF55));
        legacyColors.put(ChatColor.AQUA, new Color(0x55FFFF));
        legacyColors.put(ChatColor.RED, new Color(0xFF5555));
        legacyColors.put(ChatColor.LIGHT_PURPLE, new Color(0xFF55FF));
        legacyColors.put(ChatColor.YELLOW, new Color(0xFFFF55));
        legacyColors.put(ChatColor.WHITE, new Color(0xFFFFFF));
    }

    /**
     * Utility method to remove RGB colors from components. This modifies the input array!
     * @param components    The components to remove the rgb colors from
     * @return The modified components (same as input).
     */
    public static BaseComponent[] rgbColorsToLegacy(BaseComponent[] components) {
        for (BaseComponent component : components) {
            if (component.getColorRaw() != null && component.getColorRaw().getName().startsWith("#")) {
                component.setColor(getClosestLegacy(new Color(Integer.parseInt(component.getColorRaw().getName().substring(1), 16))));
            }
            if (component.getExtra() != null) {
                rgbColorsToLegacy(component.getExtra().toArray(new BaseComponent[0]));
            }
        }
        return components;
    }

    /**
     * Get the legacy color closest to a certain RGB color
     * @param color The color to get the closest legacy color for
     * @return The closest legacy color
     */
    public static ChatColor getClosestLegacy(Color color) {
        ChatColor closest = null;
        double smallestDistance = Double.MAX_VALUE;
        for (Map.Entry<ChatColor, Color> legacy : legacyColors.entrySet()) {
            double distance = distance(color, legacy.getValue());
            if (distance < smallestDistance) {
                smallestDistance = distance;
                closest = legacy.getKey();
            }
        }
        return closest;
    }

    /**
     * Get the distance between two colors
     * @param c1 Color A
     * @param c2 Color B
     * @return The distance or 0 if they are equal
     */
    public static double distance(Color c1, Color c2) {
        if (c1.getRGB() == c2.getRGB()) {
            return 0;
        }
        return Math.sqrt(Math.pow(c1.getRed() - c2.getRed(), 2) + Math.pow(c1.getGreen() - c2.getGreen(), 2) + Math.pow(c1.getBlue() - c2.getBlue(), 2));
    }

    /*
     * createRainbow is adapted from the net.kyori.adventure.text.minimessage.fancy.Rainbow class
     * in adventure-text-minimessage, licensed under the MIT License.
     *
     * Copyright (c) 2018-2020 KyoriPowered
     *
     * Permission is hereby granted, free of charge, to any person obtaining a copy
     * of this software and associated documentation files (the "Software"), to deal
     * in the Software without restriction, including without limitation the rights
     * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     * copies of the Software, and to permit persons to whom the Software is
     * furnished to do so, subject to the following conditions:
     *
     * The above copyright notice and this permission notice shall be included in all
     * copies or substantial portions of the Software.
     *
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
     * SOFTWARE.
     */
    /**
     * Generate a rainbow with a certain length and phase
     * @param length    The length of the rainbow
     * @param phase     The phase of the rainbow.
     * @param rgb       Whether or not to use RGB colors
     * @return the colors in the rainbow
     * @deprecated Use {@link #createRainbow(long, int, boolean)}
     */
    @Deprecated
    public static List<ChatColor> createRainbow(int length, int phase, boolean rgb) {
        return createRainbow((long) length, phase, rgb);
    }

    /**
     * Generate a rainbow with a certain length and phase
     * @param length    The length of the rainbow
     * @param phase     The phase of the rainbow.
     * @param rgb       Whether or not to use RGB colors
     * @return the colors in the rainbow
     */
    public static List<ChatColor> createRainbow(long length, int phase, boolean rgb) {
        List<ChatColor> colors = new ArrayList<>();

        float fPhase = phase / 10f;

        float center = 128;
        float width = 127;
        double frequency = Math.PI * 2 / length;

        for (int i = 0; i < length; i++) {
            Color color = new Color(
                    (int) (Math.sin(frequency * i + 2 + fPhase) * width + center),
                    (int) (Math.sin(frequency * i + 0 + fPhase) * width + center),
                    (int) (Math.sin(frequency * i + 4 + fPhase) * width + center)
            );
            if (rgb) {
                colors.add(ChatColor.of(color));
            } else {
                ChatColor chatColor = getClosestLegacy(color);
                if (colors.isEmpty() || chatColor != colors.get(colors.size() - 1)) {
                    colors.add(chatColor);
                }
            }
        }
        return colors;
    }

    /*
     * createGradient is adapted from the net.kyori.adventure.text.minimessage.fancy.Gradient class
     * in adventure-text-minimessage, licensed under the MIT License.
     *
     * Copyright (c) 2018-2020 KyoriPowered
     *
     * Permission is hereby granted, free of charge, to any person obtaining a copy
     * of this software and associated documentation files (the "Software"), to deal
     * in the Software without restriction, including without limitation the rights
     * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     * copies of the Software, and to permit persons to whom the Software is
     * furnished to do so, subject to the following conditions:
     *
     * The above copyright notice and this permission notice shall be included in all
     * copies or substantial portions of the Software.
     *
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
     * SOFTWARE.
     */
    /**
     * Generate a gradient with certain colors
     * @param length    The length of the gradient
     * @param gradient    The colors of the gradient.
     * @param rgb       Whether or not to use RGB colors
     * @return the colors in the gradient
     * @deprecated Use {@link #createRainbow(long, int, boolean)}
     */
    @Deprecated
    public static List<ChatColor> createGradient(int length, List<ChatColor> gradient, boolean rgb) {
        return createGradient((long) length, gradient, rgb);
    }

    /**
     * Generate a gradient with certain colors
     * @param length    The length of the gradient
     * @param gradient    The colors of the gradient.
     * @param rgb       Whether or not to use RGB colors
     * @return the colors in the gradient
     */
    public static List<ChatColor> createGradient(long length, List<ChatColor> gradient, boolean rgb) {
        List<ChatColor> colors = new ArrayList<>();
        if (gradient.size() < 2 || length < 2) {
            if (gradient.isEmpty()) {
                return gradient;
            }
            return Collections.singletonList(gradient.get(0));
        }

        float fPhase = 0;

        float sectorLength = (float) (length - 1) / (gradient.size() - 1);
        float factorStep = 1.0f / (sectorLength);

        long index = 0;

        int colorIndex = 0;

        for (long i = 0; i < length; i++) {

            if (factorStep * index > 1) {
                colorIndex++;
                index = 0;
            }

            float factor = factorStep * (index++ + fPhase);
            // loop around if needed
            if (factor > 1) {
                factor = 1 - (factor - 1);
            }

            Color color = interpolate(
                    getColor(gradient.get(colorIndex), rgb),
                    getColor(gradient.get(Math.min(gradient.size() - 1, colorIndex + 1)), rgb),
                    factor
            );

            if (color != null) {
                if (rgb) {
                    colors.add(ChatColor.of(color));
                } else {
                    ChatColor chatColor = getClosestLegacy(color);
                    if (colors.isEmpty() || chatColor != colors.get(colors.size() - 1)) {
                        colors.add(chatColor);
                    }
                }
            }
        }

        return colors;
    }

    private static Color getColor(ChatColor color, boolean rgb) {
        if (legacyColors.containsKey(color)) {
            return legacyColors.get(color);
        }

        if (color.getName().startsWith("#")) {
            Color c = new Color(Integer.parseInt(color.getName().substring(1), 16));
            if (rgb) {
                return c;
            } else {
                return legacyColors.get(getClosestLegacy(c));
            }
        } else if (rgb) {
            return color.getColor();
        }

        return null;
    }

    private static Color interpolate(Color color1, Color color2, float factor) {
        if (color1 == null || color2 == null) {
            return null;
        }
        return new Color(
                Math.round(color1.getRed() + factor * (color2.getRed() - color1.getRed())),
                Math.round(color1.getGreen() + factor * (color2.getGreen() - color1.getGreen())),
                Math.round(color1.getBlue() + factor * (color2.getBlue() - color1.getBlue()))
        );
    }

    /**
     * Check if a certain class exists. See {@link Class#forName(String)}
     * @param className The class name to check
     * @return <code>true</code> if the class exists, <code>false</code> if not
     */
    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException classDoesntExist) {
            return false;
        }
    }

    /**
     * Check if a class has a certain method. See {@link Class#getMethod(String, Class[])}
     * @param clazz     The class to check
     * @param method    The method to check for
     * @param parameter Method parameter types
     * @return <code>true</code> if the class has the method, <code>false</code> if not
     */
    public static boolean hasMethod(Class<?> clazz, String method, Class<?>... parameter) {
        try {
            clazz.getMethod(method, parameter);
            return true;
        } catch (NoSuchMethodException methodDoesntExist) {
            return false;
        }
    }

    /**
     * Get a method from a class if it exists. See {@link Class#getMethod(String, Class[])}
     * @param clazz     The class
     * @param method    The method name to get
     * @param parameter Method parameter types
     * @return the method, null if it doesn't exist
     */
    public static Method getMethod(Class<?> clazz, String method, Class<?>... parameter) {
        try {
            return clazz.getMethod(method, parameter);
        } catch (NoSuchMethodException methodDoesntExist) {
            return null;
        }
    }
}
