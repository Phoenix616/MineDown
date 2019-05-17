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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class Util {
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
            switch (format) {
                case BOLD:
                    component.setBold(true);
                    break;
                case ITALIC:
                    component.setItalic(true);
                    break;
                case UNDERLINE:
                    component.setUnderlined(true);
                    break;
                case STRIKETHROUGH:
                    component.setStrikethrough(true);
                    break;
                case MAGIC:
                    component.setObfuscated(true);
                    break;
                case RESET:
                    component.setBold(false);
                    component.setItalic(false);
                    component.setUnderlined(false);
                    component.setStrikethrough(false);
                    component.setObfuscated(false);
                    format = ChatColor.WHITE;
                default:
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
     */
    public static ComponentBuilder applyFormat(ComponentBuilder builder, Collection<ChatColor> formats) {
        for (ChatColor format : formats) {
            switch (format) {
                case BOLD:
                    builder.bold(true);
                    break;
                case ITALIC:
                    builder.italic(true);
                    break;
                case UNDERLINE:
                    builder.underlined(true);
                    break;
                case STRIKETHROUGH:
                    builder.strikethrough(true);
                    break;
                case MAGIC:
                    builder.obfuscated(true);
                    break;
                case RESET:
                    builder.bold(false);
                    builder.italic(false);
                    builder.underlined(false);
                    builder.strikethrough(false);
                    builder.obfuscated(false);
                    format = null;
                default:
                    builder.color(format);
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
     * @return <tt>true</tt> if it's a format, <tt>false</tt> if it's a color
     */
    public static boolean isFormat(ChatColor format) {
        switch (format) {
            case BOLD:
            case ITALIC:
            case UNDERLINE:
            case STRIKETHROUGH:
            case MAGIC:
                return true;
        }
        return false;
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
}
