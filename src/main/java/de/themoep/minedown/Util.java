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

public class Util {
    /**
     * Utility method to throw an IllegalArgumentException if the value is false
     * @param value     The value to validate
     * @param message   The message for the exception
     * @throws IllegalArgumentException Thrown if the value is false
     */
    public static void validate(boolean value, String message) throws IllegalArgumentException {
        if (!value) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void applyFormat(BaseComponent component, Collection<ChatColor> formats) {
        for(ChatColor format : formats) {
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
    }

    public static void applyFormat(ComponentBuilder builder, Collection<ChatColor> formats) {
        for(ChatColor format : formats) {
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
                    format = ChatColor.WHITE;
                default:
                    builder.color(format);
            }
        }
    }
}
