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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MineDown - Mark Up for Minecraft!
 * <p>
 * TODO:
 * PeL: Write stuff here
 * CeL: Seriously, this should contain information about how the syntax works and stuff
 */
@Getter(AccessLevel.PROTECTED)
public class MineDown {
    private final String message;
    private final Replacer replacer = new Replacer();
    private final Parser parser = new Parser();
    private BaseComponent[] baseComponents = null;
    
    /**
     * Create a new MineDown builder with a certain message
     * @param message The message to parse
     */
    public MineDown(String message) {
        this.message = message;
    }
    
    /**
     * Parse a MineDown string to components
     * @param message       The message to translate
     * @param replacements  Optional placeholder replacements
     * @return              The parsed components
     */
    public static BaseComponent[] parse(String message, String... replacements) {
        return new MineDown(message).replace(replacements).toComponent();
    }
    
    /**
     * Convert components to a MineDown string
     * @param components    The components to convert
     * @return              The components represented as a MineDown string
     */
    public static String toString(BaseComponent[] components) {
        // TODO: Implement
        return "";
    }
    
    /**
     * Parse and convert the message to the component
     * @return
     */
    public BaseComponent[] toComponent() {
        if (baseComponents() == null) {
            baseComponents = new Parser().parse(replacer().replaceIn(message()));
        }
        return baseComponents();
    }
    
    /**
     * Remove a cached component and re-parse the next time {@link #toComponent} is called
     */
    private void reset() {
        baseComponents = null;
    }
    
    /**
     * Add an array with placeholders and values that should get replaced in the message
     * @param replacements The replacements, nth element is the placeholder, n+1th the value
     */
    public MineDown replace(String... replacements) {
        reset();
        replacer().replace(replacements);
        return this;
    }
    
    /**
     * Add a map with placeholders and values that should get replaced in the message
     * @param replacements The replacements mapped placeholder to value
     */
    public MineDown replace(Map<String, String> replacements) {
        reset();
        replacer().replace(replacements);
        return this;
    }
    
    /**
     * Set the placeholder indicator for both prefix and suffix
     * @param placeholderIndicator The character to use as a placeholder indicator
     * @return The MineDown instance
     */
    public MineDown placeholderIndicator(char placeholderIndicator) {
        placeholderPrefix(placeholderIndicator);
        placeholderSuffix(placeholderIndicator);
        return this;
    }
    
    /**
     * Set the placeholder indicator's prefix character
     * @param placeholderPrefix The character to use as the placeholder indicator's prefix
     * @return The MineDown instance
     */
    public MineDown placeholderPrefix(char placeholderPrefix) {
        reset();
        replacer().placeholderPrefix(placeholderPrefix);
        return this;
    }
    
    /**
     * Get the placeholder indicator's prefix character
     * @return The placeholder indicator's prefix character
     */
    public char placeholderPrefix() {
        return replacer().placeholderPrefix();
    }
    
    /**
     * Set the placeholder indicator's suffix character
     * @param placeholderSuffix The character to use as the placeholder indicator's suffix
     * @return The MineDown instance
     */
    public MineDown placeholderSuffix(char placeholderSuffix) {
        reset();
        replacer().placeholderSuffix(placeholderSuffix);
        return this;
    }
    
    /**
     * Get the placeholder indicator's suffix character
     * @return The placeholder indicator's suffix character
     */
    public char placeholderSuffix() {
        return replacer().placeholderSuffix();
    }
    
    /**
     * Enable or disable the translation of legacy color codes
     * @param translateLegacyColors Whether or not to translate legacy color codes (Default: true)
     * @return The MineDown instance
     */
    public MineDown translateLegacyColors(boolean translateLegacyColors) {
        reset();
        parser().translateLegacyColors(translateLegacyColors);
        return this;
    }
    
    /**
     * Set a special character to replace color codes by if translating legacy colors is enabled.
     * @param colorChar The character to use as a special color code. (Default: &)
     * @return The MineDown instance
     */
    public MineDown colorChar(char colorChar) {
        reset();
        parser().colorChar(colorChar);
        return this;
    }
    
    /**
     * The replacer, 'duh
     */
    @Getter
    @Setter
    public static class Replacer {
        /**
         * The map of placeholders with their values
         */
        private final Map<String, String> replacements = new LinkedHashMap<>();
        
        /**
         * The placeholder indicator's prefix character
         */
        private char placeholderPrefix;
        
        /**
         * The placeholder indicator's suffix character
         */
        private char placeholderSuffix;
    
        /**
         * The replacer, 'duh
         */
        public Replacer() {
            this.placeholderPrefix = '%';
            this.placeholderSuffix = '%';
        }
    
        public static String replace(String message, String... replacements) {
            return new Replacer().replace(replacements).replaceIn(message);
        }
    
        public static BaseComponent[] replace(BaseComponent[] message, String... replacements) {
            return new Replacer().replace(replacements).replaceIn(message);
        }
    
        /**
         * Add an array with placeholders and values that should get replaced in the message
         * @param replacements The replacements, nth element is the placeholder, n+1th the value
         */
        public Replacer replace(String... replacements) {
            Util.validate(replacements.length % 2 == 0, "The replacement length has to be even, " +
                    "mapping i % 2 == 0 to the placeholder and i % 2 = 1 to the placeholder's value");
            Map<String, String> replacementMap = new LinkedHashMap<>();
            for (int i = 0; i + 1 < replacements.length; i+=2) {
                replacementMap.put(replacements[i], replacements[i+1]);
            }
            return replace(replacementMap);
        }
    
        /**
         * Add a map with placeholders and values that should get replaced in the message
         * @param replacements The replacements mapped placeholder to value
         */
        public Replacer replace(Map<String, String> replacements) {
            replacements().putAll(replacements);
            return this;
        }
    
        /**
         * Set the placeholder indicator for both prefix and suffix
         * @param placeholderIndicator The character to use as a placeholder indicator
         * @return The Replacer instance
         */
        public Replacer placeholderIndicator(char placeholderIndicator) {
            placeholderPrefix(placeholderIndicator);
            placeholderSuffix(placeholderIndicator);
            return this;
        }
    
        public BaseComponent[] replaceIn(BaseComponent[] components) {
            return replaceIn(Arrays.asList(components));
        }
    
        public BaseComponent[] replaceIn(List<BaseComponent> components) {
            BaseComponent[] returnArray = new BaseComponent[components.size()];
            for (int i = 0; i < components.size(); i++) {
                BaseComponent component = components.get(i).duplicate();
                if (component instanceof KeybindComponent) {
                    ((KeybindComponent) component).setKeybind(replaceIn(((KeybindComponent) component).getKeybind()));
                }
                if (component instanceof TextComponent) {
                    ((TextComponent) component).setText(replaceIn(((TextComponent) component).getText()));
                }
                if (component instanceof TranslatableComponent) {
                    ((TranslatableComponent) component).setTranslate(replaceIn(((TranslatableComponent) component).getTranslate()));
                    ((TranslatableComponent) component).setWith(Arrays.asList(replaceIn(((TranslatableComponent) component).getWith())));
                }
                if (component.getClickEvent() != null) {
                    component.setClickEvent(new ClickEvent(
                            component.getClickEvent().getAction(),
                            replaceIn(component.getClickEvent().getValue())
                    ));
                }
                if (component.getHoverEvent() != null) {
                    component.setHoverEvent(new HoverEvent(
                            component.getHoverEvent().getAction(),
                            replaceIn(component.getHoverEvent().getValue())
                    ));
                }
                component.setExtra(Arrays.asList(replaceIn(component.getExtra())));
            
                returnArray[i] = component;
            }
            return returnArray;
        }
    
        public String replaceIn(String message) {
            for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                message = message.replace(placeholderPrefix() + replacement.getKey() + placeholderSuffix(), replacement.getValue());
            }
            return message;
        }
    }
    
    public static class Parser {
        private ComponentBuilder builder = null;
    
        /**
         * Whether or not to translate legacy color codes (Default: true)
         */
        @Getter @Setter
        private boolean translateLegacyColors = true;
    
        /**
         * The character to use as a special color code. (Default: &)
         */
        @Getter @Setter
        private char colorChar = '&';
    
        /**
         * Whether to accept malformed strings or not (Default: false)
         */
        @Getter @Setter
        private boolean lenient = false;
    
        /**
         * Detect urls in strings and add events to them? (Default: true)
         */
        @Getter @Setter
        private boolean urlDetection = true;
        
        public static final Pattern URL_PATTERN = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
        
        public static final String COLOR_PREFIX = "color=";
        public static final String FORMAT_PREFIX = "format=";
        public static final String HOVER_PREFIX = "hover=";
    
        private StringBuilder value = new StringBuilder();
        private ChatColor prevColor = null;
        private Set<ChatColor> prevFormat = new HashSet<>();
    
        public BaseComponent[] parse(String message) throws IllegalArgumentException {
            Matcher urlMatcher = urlDetection() ? URL_PATTERN.matcher(message) : null;
            for (int i = 0; i < message.length(); i++) {
                char c = message.charAt(i);
                
                // Escaping
                if (c == '\\' && i + 1 < message.length()) {
                    i++;
                    
                // Legacy color codes
                } else if (translateLegacyColors() && i + 1 < message.length() && c == ChatColor.COLOR_CHAR || c == colorChar()) {
                    i++;
                    char code = message.charAt(i);
                    if (code >= 'A' && code <= 'Z') {
                        code += 32;
                    }
                    ChatColor encoded = ChatColor.getByChar(code);
                    if (encoded == ChatColor.RESET) {
                        appendValue();
                        if (prevColor != null) {
                            builder.color(prevColor);
                        }
                        prevColor = null;
                        Util.applyFormat(builder, prevFormat);
                        prevFormat = new HashSet<>();
                    } else if (!isFormat(encoded)) {
                        if (value.length() > 0) {
                            appendValue();
                        }
                        if (prevColor != null) {
                            builder.color(prevColor);
                        }
                        prevColor = encoded;
                    } else {
                        if (value.length() > 0) {
                            appendValue();
                        }
                        Util.applyFormat(builder, prevFormat);
                        prevFormat = new HashSet<>();
                        prevFormat.add(encoded);
                    }
                    continue;
                    
                // Events
                } else if (c == '[') {
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
                        builder.append(parseEvent(message.substring(i + 1, index), message.substring(index + 2, endIndex)));
                        i = endIndex + 1;
                    }
                }
                
                // URLs
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
            appendValue();
            if (builder == null) {
                builder = new ComponentBuilder("");
            }
            return builder.create();
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
                builder.color(prevColor);
                Util.applyFormat(builder, prevFormat);
                if (URL_PATTERN.matcher(value).matches()) {
                    builder.event(new ClickEvent(ClickEvent.Action.OPEN_URL, value.toString()));
                    builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open url").create()));
                }
                value = new StringBuilder();
            }
        }
    
        // [text](white bold https://moep.tv Open Moep.tv)
        // [text](white https://moep.tv Open Moep.tv)
        // [text](https://moep.tv Open Moep.tv)
        // [text](https://moep.tv Open Moep.tv)
    
        public BaseComponent[] parseEvent(String text, String definitions) {
            BaseComponent[] components = new Parser().copy(this).urlDetection(false).parse(text);
            String[] defParts = definitions.split(" ");
            ChatColor color = null;
            Set<ChatColor> formats = new HashSet<>();
            ClickEvent clickEvent = null;
            HoverEvent hoverEvent = null;
            
            int formatEnd = -1;
            
            for (int i = 0; i < defParts.length; i++) {
                String definition = defParts[i];
                ChatColor parsed = parseColor(definition, "", true);
                if (parsed != null) {
                    if (isFormat(parsed)) {
                        formats.add(parsed);
                    } else {
                        color = parsed;
                    }
                    formatEnd = i;
                    continue;
                }
                
                if (definition.toLowerCase().startsWith(COLOR_PREFIX)) {
                    color = parseColor(definition, COLOR_PREFIX, lenient());
                    if (!lenient() && isFormat(color)) {
                        throw new IllegalArgumentException(color + " is a format and not a color!");
                    }
                    formatEnd = i;
                    continue;
                }
                
                if (definition.toLowerCase().startsWith(FORMAT_PREFIX)) {
                    for (String formatStr : definition.substring(FORMAT_PREFIX.length()).split(",")) {
                        ChatColor format = parseColor(formatStr, "", lenient());
                        if (!lenient() && !isFormat(format)) {
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
    
                ClickEvent.Action clickAction = null;
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
                
                boolean hasBracket = parts.length > 1 && parts[1].startsWith("{") && clickAction != null || hoverAction != null;
                
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
                
                for (i = i + 1; i < defParts.length; i++) {
                    if (!hasBracket && defParts[i].indexOf('=') != -1) {
                        i--;
                        break;
                    }
                    value.append(" ");
                    if (hasBracket && defParts[i].endsWith("}")) {
                        value.append(defParts[i].substring(0, defParts[i].length() - 1));
                        break;
                    }
                    value.append(defParts[i]);
                }
    
                if (clickAction != null) {
                    clickEvent = new ClickEvent(clickAction, value.toString());
                } else if (hoverAction == null) {
                    hoverAction = HoverEvent.Action.SHOW_TEXT;
                }
                if (hoverAction != null) {
                    hoverEvent = new HoverEvent(hoverAction, new Parser().copy(this).urlDetection(false).parse(value.toString()));
                }
            }
            
            if (clickEvent != null && hoverEvent == null) {
                hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(clickEvent.getAction().toString().toLowerCase().replace('_', ' ')).color(ChatColor.BLUE)
                                .append(clickEvent.getValue()).color(ChatColor.WHITE)
                                .create());
            }
            
            for (BaseComponent component : components) {
                if (color != null) {
                    component.setColor(color);
                }
                if (formats != null) {
                    Util.applyFormat(component, formats);
                }
                if (clickEvent != null) {
                    component.setClickEvent(clickEvent);
                }
                if (hoverEvent != null) {
                    component.setHoverEvent(hoverEvent);
                }
            }
            return components;
        }
    
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
    
        private Parser copy(Parser parser) {
            lenient(parser.lenient());
            translateLegacyColors(parser.translateLegacyColors());
            colorChar(colorChar());
            return this;
        }
    
        private static boolean isFormat(ChatColor color) {
            return color.ordinal() >= 16;
        }
    }
    
    public static class Util {
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
}
