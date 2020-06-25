package de.themoep.minedown.adventure;

/*
 * Copyright (c) 2020 Max Lee (https://github.com/Phoenix616)
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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * @param formats   The collection of TextColor formats to apply
     * @return The component that was modified
     */
    public static Component applyFormat(Component component, Collection<TextDecoration> formats) {
        for (TextDecoration format : formats) {
            component.decoration(format, true);
        }
        if (!component.children().isEmpty()) {
            for (Component extra : component.children()) {
                applyFormat(extra, formats);
            }
        }
        return component;
    }

    /**
     * Apply a collection of colors/formats to a component builder
     * @param builder The ComponentBuilder
     * @param formats The collection of TextColor formats to apply
     * @return The component builder that was modified
     */
    public static ComponentBuilder applyFormat(ComponentBuilder builder, Collection<TextDecoration> formats) {
        for (TextDecoration format : formats) {
            builder.decoration(format, true);
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
     * Check whether a certain TextColor is formatting or not
     * @param format The TextColor to check
     * @return <tt>true</tt> if it's a format, <tt>false</tt> if it's a color
     */
    public static boolean isFormat(TextColor format) {
        return false;
    }

    /**
     * Get a set of TextColor formats all formats that a component includes
     * @param component    The component to get the formats from
     * @param ignoreParent Whether or not to include the parent's format (TODO: Does kyori-text not handle this?)
     * @return A set of all the format TextColors that the component includes
     */
    public static Set<TextDecoration> getFormats(Component component, boolean ignoreParent) {
        return component.decorations().entrySet().stream()
                .filter(e -> e.getValue() == TextDecoration.State.TRUE)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
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

    /**
     * Utility method to remove RGB colors from components. This modifies the input array!
     * @param components    The components to remove the rgb colors from
     * @return The modified components (same as input).
     */
    public static Component rgbColorsToLegacy(Component components) {
        return TextComponent.builder().append(components).mapChildrenDeep(buildableComponent
                -> buildableComponent.color() != null
                        ? TextComponent.builder().append(buildableComponent.color(getClosestLegacy(new Color(buildableComponent.color().value())))).build()
                        : buildableComponent
        ).build();
    }

    /**
     * Get the legacy color closest to a certain RGB color
     * @param color The color to get teh closest legacy color for
     * @return The closest legacy color
     */
    public static NamedTextColor getClosestLegacy(Color color) {
        NamedTextColor closest = null;
        double smallestDistance = Double.MAX_VALUE;
        for (NamedTextColor legacy : NamedTextColor.values()) {
            double distance = distance(color, new Color(legacy.value()));
            if (distance < smallestDistance) {
                closest = legacy;
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

    /**
     * Get a TextFormat from its legacy color code as kyori-text-api does not support that
     * @param code  The legacy char
     * @return      The TextFormat or null if none found with that char
     */
    public static TextFormat getFormatFromLegacy(char code) {
        switch (code) {
            case '0': return NamedTextColor.BLACK;
            case '1': return NamedTextColor.DARK_BLUE;
            case '2': return NamedTextColor.DARK_GREEN;
            case '3': return NamedTextColor.DARK_AQUA;
            case '4': return NamedTextColor.DARK_RED;
            case '5': return NamedTextColor.DARK_PURPLE;
            case '6': return NamedTextColor.GOLD;
            case '7': return NamedTextColor.GRAY;
            case '8': return NamedTextColor.DARK_GRAY;
            case '9': return NamedTextColor.BLUE;
            case 'a': return NamedTextColor.GREEN;
            case 'b': return NamedTextColor.AQUA;
            case 'c': return NamedTextColor.RED;
            case 'd': return NamedTextColor.LIGHT_PURPLE;
            case 'e': return NamedTextColor.YELLOW;
            case 'f': return NamedTextColor.WHITE;
            case 'k': return TextDecoration.OBFUSCATED;
            case 'l': return TextDecoration.BOLD;
            case 'm': return TextDecoration.STRIKETHROUGH;
            case 'n': return TextDecoration.UNDERLINED;
            case 'o': return TextDecoration.ITALIC;
            case 'r': return TextControl.RESET;
        }
        return null;
    }

    /**
     * Get the legacy color code from its format as kyori-text-api does not support that
     * @param format    The format
     * @return          The legacy color code or null if none found with that char
     */
    public static char getLegacyFormatChar(TextFormat format) {
        if (format == TextControl.RESET) {
            return 'r';
        } else if (format instanceof NamedTextColor) {
                if (format == NamedTextColor.BLACK) return '0';
                if (format == NamedTextColor.DARK_BLUE) return '1';
                if (format == NamedTextColor.DARK_GREEN) return '2';
                if (format == NamedTextColor.DARK_AQUA) return '3';
                if (format == NamedTextColor.DARK_RED) return '4';
                if (format == NamedTextColor.DARK_PURPLE) return '5';
                if (format == NamedTextColor.GOLD) return '6';
                if (format == NamedTextColor.GRAY) return '7';
                if (format == NamedTextColor.DARK_GRAY) return '8';
                if (format == NamedTextColor.BLUE) return '9';
                if (format == NamedTextColor.GREEN) return 'a';
                if (format == NamedTextColor.AQUA) return 'b';
                if (format == NamedTextColor.RED) return 'c';
                if (format == NamedTextColor.LIGHT_PURPLE) return 'd';
                if (format == NamedTextColor.YELLOW) return 'e';
                if (format == NamedTextColor.WHITE) return 'f';
        } else if (format instanceof TextDecoration) {
            switch ((TextDecoration) format) {
                case OBFUSCATED: return 'k';
                case BOLD: return 'l';
                case STRIKETHROUGH: return 'm';
                case UNDERLINED: return 'n';
                case ITALIC: return 'o';
            }
        } else if (format instanceof TextColor) {
            return getLegacyFormatChar(getClosestLegacy(new Color(((TextColor) format).value())));
        }
        throw new IllegalArgumentException(format + " is not supported!");
    }

    public enum TextControl implements TextFormat {
        RESET('r');

        private char c;

        TextControl(char c) {
            this.c = c;
        }

        public char getChar() {
            return c;
        }
    }
}
