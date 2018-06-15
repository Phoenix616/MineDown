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

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class MineDownParser {
    private ComponentBuilder builder = null;

    /**
     * Whether or not to translate legacy color codes (Default: true)
     */
    private boolean translateLegacyColors = true;

    /**
     * The character to use as a special color code. (Default: ampersand &amp;)
     */
    private char colorChar = '&';

    /**
     * Whether to accept malformed strings or not (Default: false)
     */
    private boolean lenient = false;

    /**
     * Detect urls in strings and add events to them? (Default: true)
     */
    private boolean urlDetection = true;
    
    public static final Pattern URL_PATTERN = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    
    public static final String COLOR_PREFIX = "color=";
    public static final String FORMAT_PREFIX = "format=";
    public static final String HOVER_PREFIX = "hover=";

    private StringBuilder value = new StringBuilder();
    private ChatColor color = null;
    private Set<ChatColor> format = new HashSet<>();
    private ClickEvent clickEvent = null;
    private HoverEvent hoverEvent = null;
    
    /**
     * Create a ComponentBuilder by parsing a {@link MineDown} message
     * @param message   The message to parse
     * @return          The parsed ComponentBuilder
     * @throws IllegalArgumentException Thrown when a parsing error occurs and lenient is set to false
     */
    public ComponentBuilder parse(String message) throws IllegalArgumentException {
        Matcher urlMatcher = urlDetection() ? URL_PATTERN.matcher(message) : null;
        boolean escaped = false;
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
    
            boolean isEscape = c == '\\' && i + 1 < message.length();
            boolean isColorCode = translateLegacyColors && i + 1 < message.length() && (c == ChatColor.COLOR_CHAR || c == colorChar);
            boolean isEvent = c == '[';
            boolean isFormatting = (c == '_' || c == '*' || c == '~' || c == '?' || c == '#') && Util.isDouble(message, i);
    
            if (escaped) {
                escaped = false;
                if (!isEscape && !isColorCode && !isEvent && !isFormatting) {
                    value.append('\\');
                }
                
            // Escaping
            } else if (isEscape) {
                escaped = true;
                continue;
                
            // Legacy color codes
            } else if (isColorCode) {
                i++;
                char code = message.charAt(i);
                if (code >= 'A' && code <= 'Z') {
                    code += 32;
                }
                ChatColor encoded = null;
                StringBuilder colorString = new StringBuilder();
                for (int j = i; j < message.length(); j++) {
                    char c1 = message.charAt(j);
                    if (c1 == c) {
                        try {
                            encoded = ChatColor.valueOf(colorString.toString().toUpperCase());
                            i = j;
                        } catch (IllegalArgumentException ignored) {}
                        break;
                    }
                    if (c1 != '_' && (c1 < 'A' || c1 > 'Z') && (c1 < 'a' || c1 > 'z')) {
                        break;
                    }
                    colorString.append(c1);
                }
                if (encoded == null) {
                    encoded = ChatColor.getByChar(code);
                }
                
                if (encoded != null) {
                    if (encoded == ChatColor.RESET) {
                        appendValue();
                        color = null;
                        Util.applyFormat(builder, format);
                        format = new HashSet<>();
                    } else if (!Util.isFormat(encoded)) {
                        if (value.length() > 0) {
                            appendValue();
                        }
                        color = encoded;
                        format = new HashSet<>();
                    } else {
                        if (value.length() > 0) {
                            appendValue();
                        }
                        format.add(encoded);
                    }
                } else {
                    value.append(c).append(code);
                }
                continue;
                
            // Events
            } else if (isEvent) {
                int index = -1;
                int endIndex = -1;
                Event:
                for (int j = i + 1; j + 1 < message.length(); j++) {
                    index = message.indexOf(']', j);
                    if (index == -1) {
                        break;
                    } else if (index + 2 < message.length()
                            && message.charAt(index - 1) != '\\'
                            && message.charAt(index + 1) == '(') {
                        for (int k = index + 1; k < message.length(); k++) {
                            endIndex = message.indexOf(')', k);
                            if (endIndex == -1) {
                                break;
                            } else if (message.charAt(endIndex - 1) != '\\') {
                                break Event;
                            }
                        }
                    }
                    index = -1;
                }
                if (index > i && endIndex > index) {
                    appendValue();
                    append(parseEvent(message.substring(i + 1, index), message.substring(index + 2, endIndex)));
                    i = endIndex;
                    continue;
                }
                
            } else if (isFormatting) {
                int endIndex = message.indexOf(String.valueOf(c) + String.valueOf(c), i + 2);
                // Found formatting
                if (endIndex != -1) {
                    Set<ChatColor> formats = new HashSet<>(format);
                    formats.add(MineDown.getFormatFromChar(c));
                    appendValue();
                    append(copy().format(formats).parse(message.substring(i + 2, endIndex)));
                    i = endIndex + 1;
                    continue;
                }
            }
    
            // URL
            if (urlDetection()) {
                int urlEnd = message.indexOf(' ', i);
                if (urlEnd == -1) {
                    urlEnd = message.length();
                }
                if (urlMatcher.region(i, urlEnd).find()) {
                    appendValue();
                    value = new StringBuilder(message.substring(i, urlEnd));
                    appendValue();
                    i = urlEnd - 1;
                    continue;
                }
            }
            
            // It's normal text, just append the character
            value.append(message.charAt(i));
        }
        if (escaped) {
            value.append('\\');
        }
        appendValue();
        if (builder == null) {
            builder = new ComponentBuilder("");
        }
        return builder;
    }
    
    private void append(ComponentBuilder builder) {
        if (this.builder == null) {
            this.builder = new ComponentBuilder(builder);
        } else {
            BaseComponent[] components = builder.create();
            try {
                this.builder.append(components);
            } catch (NoSuchMethodError e) {
                // Older versions didn't have ComponentBuilder#append(BaseComponent[])
                // Recreating it with reflections. That might be slower but they should just update anyways...
                if (components.length > 0) {
                    try {
                        Field fCurrent = this.builder.getClass().getDeclaredField("current");
                        fCurrent.setAccessible(true);
                        BaseComponent previous = (BaseComponent) fCurrent.get(this.builder);
                        Field fParts = this.builder.getClass().getDeclaredField("parts");
                        fParts.setAccessible(true);
                        List<BaseComponent> parts = (List<BaseComponent>) fParts.get(this.builder);
    
                        for (BaseComponent component : components) {
                            parts.add(previous);
                            fCurrent.set(this.builder, component.duplicate());
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void appendValue() {
        appendValue(ComponentBuilder.FormatRetention.NONE);
    }

    private void appendValue(ComponentBuilder.FormatRetention retention) {
        if (value.length() > 0) {
            if (builder == null) {
                builder = new ComponentBuilder(value.toString());
            } else {
                builder.append(value.toString(), retention);
            }
            builder.color(color);
            Util.applyFormat(builder, format);
            if (URL_PATTERN.matcher(value).matches()) {
                builder.event(new ClickEvent(ClickEvent.Action.OPEN_URL, value.toString()));
                builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open url").create()));
            }
            if (clickEvent != null) {
                builder.event(clickEvent);
            }
            if (hoverEvent != null) {
                builder.event(hoverEvent);
            }
            value = new StringBuilder();
        }
    }
    
    /**
     * Parse a {@link MineDown} event string
     * @param text          The display text
     * @param definitions   The event definition string
     * @return              The parsed ComponentBuilder for this string
     */
    public ComponentBuilder parseEvent(String text, String definitions) {
        List<String> defParts = new ArrayList<>();
        if (definitions.startsWith(" ")) {
            defParts.add("");
        }
        Collections.addAll(defParts, definitions.split(" "));
        if (definitions.endsWith(" ")) {
            defParts.add("");
        }
        ChatColor color = null;
        Set<ChatColor> formats = new HashSet<>();
        ClickEvent clickEvent = null;
        HoverEvent hoverEvent = null;
        
        int formatEnd = -1;
        
        for (int i = 0; i < defParts.size(); i++) {
            String definition = defParts.get(i);
            ChatColor parsed = parseColor(definition, "", true);
            if (parsed != null) {
                if (Util.isFormat(parsed)) {
                    formats.add(parsed);
                } else {
                    color = parsed;
                }
                formatEnd = i;
                continue;
            }
            
            if (definition.toLowerCase().startsWith(COLOR_PREFIX)) {
                color = parseColor(definition, COLOR_PREFIX, lenient());
                if (!lenient() && Util.isFormat(color)) {
                    throw new IllegalArgumentException(color + " is a format and not a color!");
                }
                formatEnd = i;
                continue;
            }
            
            if (definition.toLowerCase().startsWith(FORMAT_PREFIX)) {
                for (String formatStr : definition.substring(FORMAT_PREFIX.length()).split(",")) {
                    ChatColor format = parseColor(formatStr, "", lenient());
                    if (!lenient() && !Util.isFormat(format)) {
                        throw new IllegalArgumentException(formats + " is a color and not a format!");
                    }
                    formats.add(format);
                }
                formatEnd = i;
                continue;
            }
            
            if (i == formatEnd + 1 && URL_PATTERN.matcher(definition).matches()) {
                clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, definition);
                continue;
            }

            ClickEvent.Action clickAction = definition.startsWith("/") ? ClickEvent.Action.RUN_COMMAND : null;
            HoverEvent.Action hoverAction = null;
            if (definition.toLowerCase().startsWith(HOVER_PREFIX)) {
                hoverAction = HoverEvent.Action.SHOW_TEXT;
            }
            String[] parts = definition.split("=", 2);
            try {
                hoverAction = HoverEvent.Action.valueOf(parts[0].toUpperCase());
            } catch (IllegalArgumentException ignored) {}
            try {
                clickAction = ClickEvent.Action.valueOf(parts[0].toUpperCase());
            } catch (IllegalArgumentException ignored) {}
            
            boolean hasBracket = parts.length > 1 && parts[1].startsWith("{") && (clickAction != null || hoverAction != null);
            
            StringBuilder value = new StringBuilder();
            if (parts.length > 1 && clickAction != null || hoverAction != null) {
                if (hasBracket) {
                    value.append(parts[1].substring(1));
                } else {
                    value.append(parts[1]);
                }
            } else {
                value.append(definition);
            }
            
            for (i = i + 1; i < defParts.size(); i++) {
                if (!hasBracket && defParts.get(i).indexOf('=') != -1) {
                    i--;
                    break;
                }
                value.append(" ");
                if (hasBracket && defParts.get(i).endsWith("}")) {
                    value.append(defParts.get(i), 0, defParts.get(i).length() - 1);
                    break;
                }
                value.append(defParts.get(i));
            }

            if (clickAction != null) {
                clickEvent = new ClickEvent(clickAction, value.toString());
            } else if (hoverAction == null) {
                hoverAction = HoverEvent.Action.SHOW_TEXT;
            }
            if (hoverAction != null) {
                hoverEvent = new HoverEvent(hoverAction, copy().clickEvent(null).hoverEvent(null).urlDetection(false).parse(value.toString()).create());
            }
        }
        
        if (clickEvent != null && hoverEvent == null) {
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(clickEvent.getAction().toString().toLowerCase().replace('_', ' ')).color(ChatColor.BLUE)
                            .append(" " + clickEvent.getValue()).color(ChatColor.WHITE)
                            .create());
        }
        
        return copy()
                .urlDetection(false)
                .color(color)
                .format(formats)
                .clickEvent(clickEvent)
                .hoverEvent(hoverEvent)
                .parse(text);
    }
    
    /**
     * Parse a color definition
     * @param colorString   The string to parse
     * @param prefix        The color prefix e.g. ampersand (&amp;)
     * @param lenient       Whether or not to accept malformed strings
     * @return              The parsed color or <tt>null</tt> if lenient is true and no color was found
     */
    public static ChatColor parseColor(String colorString, String prefix, boolean lenient) {
        ChatColor color = null;
        if (prefix.length() + 1 == colorString.length()) {
            color = ChatColor.getByChar(colorString.charAt(prefix.length()));
            if (color == null && !lenient) {
                throw new IllegalArgumentException(colorString.charAt(prefix.length()) + " is not a valid " + prefix + " char!");
            }
        } else {
            try {
                color = ChatColor.valueOf(colorString.substring(prefix.length()).toUpperCase());
            } catch (IllegalArgumentException e) {
                if (!lenient) {
                    throw e;
                }
            }
        }
        return color;
    }
    
    /**
     * Copy all the parser's setting to a new instance
     * @return The new parser instance with all settings copied
     */
    public MineDownParser copy() {
        MineDownParser copy = new MineDownParser();
        copy.lenient(lenient);
        copy.translateLegacyColors(translateLegacyColors);
        copy.colorChar(colorChar);
        copy.clickEvent(clickEvent);
        copy.hoverEvent(hoverEvent);
        return copy;
    }
    
}
