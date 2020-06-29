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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MineDownParser {

    private static final boolean HAS_RGB_SUPPORT = Util.hasMethod(ChatColor.class, "of", String.class);
    private static final boolean HAS_FONT_SUPPORT = Util.hasMethod(ComponentBuilder.class, "font", String.class);

    /**
     * The character to use as a special color code. (Default: ampersand &amp;)
     */
    private char colorChar = '&';

    /**
     * All enabled options
     */
    private Set<Option> enabledOptions = EnumSet.of(
            Option.LEGACY_COLORS,
            Option.SIMPLE_FORMATTING,
            Option.ADVANCED_FORMATTING
    );

    /**
     * All filters
     */
    private Set<Option> filteredOptions = EnumSet.noneOf(Option.class);

    /**
     * Whether to accept malformed strings or not (Default: false)
     */
    private boolean lenient = false;

    /**
     * Should the parser try to detect if RGB/font support is available?
     */
    private boolean backwardsCompatibility = true;

    /**
     * Detect urls in strings and add events to them? (Default: true)
     */
    private boolean urlDetection = true;

    /**
     * The text to display when hovering over an URL. Has a %url% placeholder.
     */
    private String urlHoverText = "Click to open url";

    /**
     * Automatically add http to values of open_url when there doesn't exist any? (Default: true)
     */
    private boolean autoAddUrlPrefix = true;

    /**
     * The max width the hover text should have.
     * Minecraft itself will wrap after 60 characters.
     * Won't apply if the text already includes new lines.
     */
    private int hoverTextWidth = 60;

    public static final Pattern URL_PATTERN = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");

    public static final String FONT_PREFIX = "font=";
    public static final String COLOR_PREFIX = "color=";
    public static final String FORMAT_PREFIX = "format=";
    public static final String HOVER_PREFIX = "hover=";

    private ComponentBuilder builder;
    private StringBuilder value;
    private String font;
    private ChatColor color;
    private Set<ChatColor> format;
    private ClickEvent clickEvent;
    private HoverEvent hoverEvent;

    public MineDownParser() {
        reset();
    }

    /**
     * Create a ComponentBuilder by parsing a {@link MineDown} message
     * @param message The message to parse
     * @return The parsed ComponentBuilder
     * @throws IllegalArgumentException Thrown when a parsing error occurs and lenient is set to false
     */
    public ComponentBuilder parse(String message) throws IllegalArgumentException {
        Matcher urlMatcher = urlDetection() ? URL_PATTERN.matcher(message) : null;
        boolean escaped = false;
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            boolean isEscape = c == '\\' && i + 1 < message.length();
            boolean isColorCode = isEnabled(Option.LEGACY_COLORS)
                    && i + 1 < message.length() && (c == ChatColor.COLOR_CHAR || c == colorChar());
            boolean isEvent = false;
            if (isEnabled(Option.ADVANCED_FORMATTING) && c == '[') {
                int nextEventClose = Util.indexOfNotEscaped(message, "](", i + 1);
                if (nextEventClose != -1 && nextEventClose + 2 < message.length()) {
                    int nextDefClose = Util.indexOfNotEscaped(message, ")", i + 2);
                    if (nextDefClose != -1) {
                        int depth = 1;
                        isEvent = true;
                        boolean innerEscaped = false;
                        for (int j = i + 1; j < nextEventClose; j++) {
                            if (innerEscaped) {
                                innerEscaped = false;
                            } else if (message.charAt(j) == '\\') {
                                innerEscaped = true;
                            } else if (message.charAt(j) == '[') {
                                depth++;
                            } else if (message.charAt(j) == ']') {
                                depth--;
                            }
                            if (depth == 0) {
                                isEvent = false;
                                break;
                            }
                        }
                    }
                }
            }
            boolean isFormatting = isEnabled(Option.SIMPLE_FORMATTING)
                    && (c == '_' || c == '*' || c == '~' || c == '?' || c == '#') && Util.isDouble(message, i)
                    && message.indexOf(String.valueOf(c) + String.valueOf(c), i + 2) != -1;

            if (escaped) {
                escaped = false;

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
                Option filterOption = null;
                StringBuilder colorString = new StringBuilder();
                for (int j = i; j < message.length(); j++) {
                    char c1 = message.charAt(j);
                    // Check if we have reached another indicator char and have a color string that isn't just one char
                    if (c1 == c && colorString.length() > 1) {
                        try {
                            encoded = parseColor(colorString.toString(), "", lenient(), backwardsCompatibility());
                            filterOption = Option.SIMPLE_FORMATTING;
                            i = j;
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    }
                    if (c1 != '_' && c1 != '#' && (c1 < 'A' || c1 > 'Z') && (c1 < 'a' || c1 > 'z') && (c1 < '0' || c1 > '9')) {
                        break;
                    }
                    colorString.append(c1);
                }
                if (encoded == null) {
                    encoded = ChatColor.getByChar(code);
                    if (encoded != null) {
                        filterOption = Option.LEGACY_COLORS;
                    }
                }

                if (encoded != null) {
                    if (!isFiltered(filterOption)) {
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
                    }
                } else {
                    value.append(c).append(code);
                }
                continue;

                // Events
            } else if (isEvent) {
                int index = Util.indexOfNotEscaped(message, "](", i + 1);
                int endIndex = Util.indexOfNotEscaped(message, ")", index + 2);
                appendValue();
                if (!isFiltered(Option.ADVANCED_FORMATTING)) {
                    append(parseEvent(message.substring(i + 1, index), message.substring(index + 2, endIndex)));
                } else {
                    append(copy(true).parse(message.substring(i + 1, index)));
                }
                i = endIndex;
                continue;

                // Simple formatting
            } else if (isFormatting) {
                int endIndex = message.indexOf(String.valueOf(c) + String.valueOf(c), i + 2);
                Set<ChatColor> formats = new HashSet<>(format);
                if (!isFiltered(Option.SIMPLE_FORMATTING)) {
                    formats.add(MineDown.getFormatFromChar(c));
                }
                appendValue();
                append(copy(true).format(formats).parse(message.substring(i + 2, endIndex)));
                i = endIndex + 1;
                continue;
            }

            // URL
            if (urlDetection() && urlMatcher != null) {
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
        append(builder.create());
    }

    private void append(BaseComponent[] components) {
        if (this.builder == null) {
            if (components.length > 0) {
                this.builder = new ComponentBuilder(components[0]);
                for (int i = 1; i < components.length; i++) {
                    builder.append(components[i]);
                }
            } else {
                this.builder = new ComponentBuilder("");
            }
        } else {
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
            if (!backwardsCompatibility || HAS_FONT_SUPPORT) {
                builder.font(font);
            }
            builder.color(color);
            Util.applyFormat(builder, format);
            if (urlDetection() && URL_PATTERN.matcher(value).matches()) {
                String v = value.toString();
                if (!v.startsWith("http://") && !v.startsWith("https://")) {
                    v = "http://" + v;
                }
                builder.event(new ClickEvent(ClickEvent.Action.OPEN_URL, v));
                if (urlHoverText() != null && !urlHoverText().isEmpty()) {
                    builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new MineDown(urlHoverText()).replace("url", value.toString()).toComponent()
                    ));
                }
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
     * @param text        The display text
     * @param definitions The event definition string
     * @return The parsed ComponentBuilder for this string
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
        String font = null;
        ChatColor color = null;
        Set<ChatColor> formats = new HashSet<>();
        ClickEvent clickEvent = null;
        HoverEvent hoverEvent = null;

        int formatEnd = -1;

        for (int i = 0; i < defParts.size(); i++) {
            String definition = defParts.get(i);
            ChatColor parsed = parseColor(definition, "", true, backwardsCompatibility());
            if (parsed != null) {
                if (Util.isFormat(parsed)) {
                    formats.add(parsed);
                } else {
                    color = parsed;
                }
                formatEnd = i;
                continue;
            }

            if (definition.toLowerCase().startsWith(FONT_PREFIX)) {
                font = definition.substring(FONT_PREFIX.length());
            }

            if (definition.toLowerCase().startsWith(COLOR_PREFIX)) {
                color = parseColor(definition, COLOR_PREFIX, lenient(), backwardsCompatibility());
                if (!lenient() && Util.isFormat(color)) {
                    throw new IllegalArgumentException(color + " is a format and not a color!");
                }
                formatEnd = i;
                continue;
            }

            if (definition.toLowerCase().startsWith(FORMAT_PREFIX)) {
                for (String formatStr : definition.substring(FORMAT_PREFIX.length()).split(",")) {
                    ChatColor format = parseColor(formatStr, "", lenient(), backwardsCompatibility());
                    if (!lenient() && !Util.isFormat(format)) {
                        throw new IllegalArgumentException(formats + " is a color and not a format!");
                    }
                    formats.add(format);
                }
                formatEnd = i;
                continue;
            }

            if (i == formatEnd + 1 && URL_PATTERN.matcher(definition).matches()) {
                if (!definition.startsWith("http://") && !definition.startsWith("https://")) {
                    definition = "http://" + definition;
                }
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
            } catch (IllegalArgumentException ignored) {
            }
            try {
                clickAction = ClickEvent.Action.valueOf(parts[0].toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }

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
                String v = value.toString();
                if (autoAddUrlPrefix() && clickAction == ClickEvent.Action.OPEN_URL && !v.startsWith("http://") && !v.startsWith("https://")) {
                    v = "http://" + v;
                }
                clickEvent = new ClickEvent(clickAction, v);
            } else if (hoverAction == null) {
                hoverAction = HoverEvent.Action.SHOW_TEXT;
            }
            if (hoverAction != null) {
                String valueStr = value.toString();
                hoverEvent = new HoverEvent(hoverAction, copy(false).urlDetection(false).parse(
                        hoverAction == HoverEvent.Action.SHOW_TEXT ? Util.wrap(valueStr, hoverTextWidth()) : valueStr
                ).create());
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
                .font(font)
                .color(color)
                .format(formats)
                .clickEvent(clickEvent)
                .hoverEvent(hoverEvent)
                .parse(text);
    }

    protected ComponentBuilder builder() {
        return this.builder;
    }

    protected MineDownParser builder(ComponentBuilder builder) {
        this.builder = builder;
        return this;
    }

    protected MineDownParser value(StringBuilder value) {
        this.value = value;
        return this;
    }

    protected StringBuilder value() {
        return this.value;
    }

    private MineDownParser font(String font) {
        this.font = font;
        return this;
    }

    protected String font() {
        return this.font;
    }

    protected MineDownParser color(ChatColor color) {
        this.color = color;
        return this;
    }

    protected ChatColor color() {
        return this.color;
    }

    protected MineDownParser format(Set<ChatColor> format) {
        this.format = format;
        return this;
    }

    protected Set<ChatColor> format() {
        return this.format;
    }

    protected MineDownParser clickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    protected ClickEvent clickEvent() {
        return this.clickEvent;
    }

    protected MineDownParser hoverEvent(HoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
        return this;
    }

    protected HoverEvent hoverEvent() {
        return this.hoverEvent;
    }

    /**
     * Parse a color definition
     * @param colorString The string to parse
     * @param prefix      The color prefix e.g. ampersand (&amp;)
     * @param lenient     Whether or not to accept malformed strings
     * @return The parsed color or <tt>null</tt> if lenient is true and no color was found
     * @deprecated This does not need to be exposed publicly and will bre private in the next version
     */
    @Deprecated
    public static ChatColor parseColor(String colorString, String prefix, boolean lenient) {
        return parseColor(colorString, prefix, lenient, true);
    }

    private static ChatColor parseColor(String colorString, String prefix, boolean lenient, boolean backwardsCompatibility) {
        ChatColor color = null;
        if (prefix.length() + 1 == colorString.length()) {
            color = ChatColor.getByChar(colorString.charAt(prefix.length()));
            if (color == null && !lenient) {
                throw new IllegalArgumentException(colorString.charAt(prefix.length()) + " is not a valid " + prefix + " char!");
            }
        } else {
            try {
                colorString = colorString.substring(prefix.length());
                if (colorString.charAt(0) == '#') {
                    if (colorString.length() == 4){
                        StringBuilder sb = new StringBuilder("#");
                        for (int i = 1; i < 4; i++) {
                            sb.append(colorString.charAt(i)).append(colorString.charAt(i));
                        }
                        colorString = sb.toString();
                    }
                    if (!backwardsCompatibility || HAS_RGB_SUPPORT) {
                        color = ChatColor.of(colorString);
                    } else {
                        color = Util.getClosestLegacy(new Color(Integer.parseInt(colorString.substring(1), 16)));
                    }
                } else {
                    color = ChatColor.valueOf(colorString.toUpperCase());
                }
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
        return copy(false);
    }

    /**
     * Copy all the parser's setting to a new instance
     * @param formatting Should the formatting be copied too?
     * @return The new parser instance with all settings copied
     */
    public MineDownParser copy(boolean formatting) {
        return new MineDownParser().copy(this, formatting);
    }

    /**
     * Copy all the parser's settings from another parser.
     * @param from The parser to copy from
     * @return This parser's instance
     */
    public MineDownParser copy(MineDownParser from) {
        return copy(from, false);
    }

    /**
     * Copy all the parser's settings from another parser.
     * @param from       The parser to copy from
     * @param formatting Should the formatting be copied too?
     * @return This parser's instance
     */
    public MineDownParser copy(MineDownParser from, boolean formatting) {
        lenient(from.lenient());
        urlDetection(from.urlDetection());
        urlHoverText(from.urlHoverText());
        autoAddUrlPrefix(from.autoAddUrlPrefix());
        hoverTextWidth(from.hoverTextWidth());
        enabledOptions(from.enabledOptions());
        filteredOptions(from.filteredOptions());
        colorChar(from.colorChar());
        if (formatting) {
            format(from.format());
            color(from.color());
            font(from.font());
            clickEvent(from.clickEvent());
            hoverEvent(from.hoverEvent());
        }
        return this;
    }

    /**
     * Reset the parser state to the start
     * @return The parser's instance
     */
    public MineDownParser reset() {
        builder = null;
        value = new StringBuilder();
        font = null;
        color = null;
        format = new HashSet<>();
        clickEvent = null;
        hoverEvent = null;
        return this;
    }

    /**
     * Whether or not to translate legacy color codes (Default: true)
     * @return Whether or not to translate legacy color codes (Default: true)
     * @deprecated Use {@link #isEnabled(Option)} instead
     */
    @Deprecated
    public boolean translateLegacyColors() {
        return isEnabled(Option.LEGACY_COLORS);
    }

    /**
     * Whether or not to translate legacy color codes
     * @return The parser
     * @deprecated Use {@link #enable(Option)} and {@link #disable(Option)} instead
     */
    @Deprecated
    public MineDownParser translateLegacyColors(boolean enabled) {
        return enabled ? enable(Option.LEGACY_COLORS) : disable(Option.LEGACY_COLORS);
    }

    /**
     * Check whether or not an option is enabled
     * @param option The option to check for
     * @return <tt>true</tt> if it's enabled; <tt>false</tt> if not
     */
    public boolean isEnabled(Option option) {
        return enabledOptions().contains(option);
    }

    /**
     * Enable an option.
     * @param option The option to enable
     * @return The parser instace
     */
    public MineDownParser enable(Option option) {
        enabledOptions().add(option);
        return this;
    }

    /**
     * Disable an option. Disabling an option will stop the parser from replacing
     * this option's chars in the string. Use {@link #filter(Option)} to completely
     * remove the characters used by this option from the message instead.
     * @param option The option to disable
     * @return The parser instace
     */
    public MineDownParser disable(Option option) {
        enabledOptions().remove(option);
        return this;
    }

    /**
     * Check whether or not an option is filtered
     * @param option The option to check for
     * @return <tt>true</tt> if it's enabled; <tt>false</tt> if not
     */
    public boolean isFiltered(Option option) {
        return filteredOptions().contains(option);
    }

    /**
     * Filter an option. This enables the parsing of an option and completely
     * removes the characters of this option from the string.
     * @param option The option to add to the filter
     * @return The parser instance
     */
    public MineDownParser filter(Option option) {
        filteredOptions().add(option);
        enabledOptions().add(option);
        return this;
    }

    /**
     * Unfilter an option. Does not enable it!
     * @param option The option to remove from the filter
     * @return The parser instance
     */
    public MineDownParser unfilter(Option option) {
        filteredOptions().remove(option);
        return this;
    }

    /**
     * Escape formatting in the string depending on this parser's options. This will escape backslashes too!
     * @param string The string to escape
     * @return The string with all formatting of this parser escaped
     */
    public String escape(String string) {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            boolean isEscape = c == '\\';
            boolean isColorCode = isEnabled(Option.LEGACY_COLORS)
                    && i + 1 < string.length() && (c == ChatColor.COLOR_CHAR || c == colorChar());
            boolean isEvent = isEnabled(Option.ADVANCED_FORMATTING)
                    && c == '[';
            boolean isFormatting = isEnabled(Option.SIMPLE_FORMATTING)
                    && (c == '_' || c == '*' || c == '~' || c == '?' || c == '#') && Util.isDouble(string, i);

            if (isEscape || isColorCode || isEvent || isFormatting) {
                value.append('\\');
            }
            value.append(c);
        }
        return value.toString();
    }

    public enum Option {
        /**
         * Translate simple, in-line MineDown formatting in strings? (Default: true)
         */
        SIMPLE_FORMATTING,
        /**
         * Translate advanced MineDown formatting (e.g. events) in strings? (Default: true)
         */
        ADVANCED_FORMATTING,
        /**
         * Whether or not to translate legacy color codes (Default: true)
         */
        LEGACY_COLORS
    }

    /**
     * Get The character to use as a special color code.
     * @return The color character (Default: ampersand &amp;)
     */
    public char colorChar() {
        return this.colorChar;
    }

    /**
     * Set the character to use as a special color code.
     * @param colorChar The color char (Default: ampersand &amp;)
     * @return The MineDownParser instance
     */
    public MineDownParser colorChar(char colorChar) {
        this.colorChar = colorChar;
        return this;
    }

    /**
     * Get all enabled options that will be used when parsing
     * @return a modifiable set of options
     */
    public Set<Option> enabledOptions() {
        return this.enabledOptions;
    }

    /**
     * Set all enabled options that will be used when parsing at once, replaces any existing options
     * @param enabledOptions The enabled options
     * @return The MineDownParser instance
     */
    public MineDownParser enabledOptions(Set<Option> enabledOptions) {
        this.enabledOptions = enabledOptions;
        return this;
    }

    /**
     * Get all filtered options that will be parsed and then removed from the string
     * @return a modifiable set of options
     */
    public Set<Option> filteredOptions() {
        return this.filteredOptions;
    }

    /**
     * Set all filtered options that will be parsed and then removed from the string at once,
     * replaces any existing options
     * @param filteredOptions The filtered options
     * @return The MineDownParser instance
     */
    public MineDownParser filteredOptions(Set<Option> filteredOptions) {
        this.filteredOptions = filteredOptions;
        return this;
    }

    /**
     * Get whether to accept malformed strings or not
     * @return whether or not the accept malformed strings (Default: false)
     */
    public boolean lenient() {
        return this.lenient;
    }

    /**
     * Set whether to accept malformed strings or not
     * @param lenient Set whether or not to accept malformed string (Default: false)
     * @return The MineDownParser instance
     */
    public MineDownParser lenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    /**
     * Get whether the parser should try to detect if RGB/font support is available
     * @return whether the parser should try to detect if RGB/font support is available (Default: true)
     */
    public boolean backwardsCompatibility() {
        return this.backwardsCompatibility;
    }

    /**
     * Set whether the parser should try to detect if RGB/font support is available
     * @param backwardsCompatibility Set whether the parser should try to detect if RGB/font support is available (Default: true)
     * @return The MineDownParser instance
     */
    public MineDownParser backwardsCompatibility(boolean backwardsCompatibility) {
        this.backwardsCompatibility = backwardsCompatibility;
        return this;
    }

    /**
     * Get whether or not urls in strings are detected and get events added to them?
     * @return whether or not urls are detected (Default: true)
     */
    public boolean urlDetection() {
        return this.urlDetection;
    }

    /**
     * Set whether or not to detect urls in strings and add events to them?
     * @param urlDetection Whether or not to detect urls in strings  (Default: true)
     * @return The MineDownParser instance
     */
    public MineDownParser urlDetection(boolean urlDetection) {
        this.urlDetection = urlDetection;
        return this;
    }

    /**
     * Get the text to display when hovering over an URL. Has a %url% placeholder.
     */
    public String urlHoverText() {
        return this.urlHoverText;
    }

    /**
     * Set the text to display when hovering over an URL. Has a %url% placeholder.
     * @param urlHoverText The url hover text
     * @return The MineDownParser instance
     */
    public MineDownParser urlHoverText(String urlHoverText) {
        this.urlHoverText = urlHoverText;
        return this;
    }

    /**
     * Get whether or not to automatically add http to values of open_url when there doesn't exist any?
     * @return whether or not to automatically add http to values of open_url when there doesn't exist any? (Default: true)
     */
    public boolean autoAddUrlPrefix() {
        return this.autoAddUrlPrefix;
    }

    /**
     * Set whether or not to automatically add http to values of open_url when there doesn't exist any?
     * @param autoAddUrlPrefix Whether or not automatically add http to values of open_url when there doesn't exist any? (Default: true)
     * @return The MineDownParser instance
     */
    public MineDownParser autoAddUrlPrefix(boolean autoAddUrlPrefix) {
        this.autoAddUrlPrefix = autoAddUrlPrefix;
        return this;
    }

    /**
     * Get the max width the hover text should have.
     * Minecraft itself will wrap after 60 characters.
     * Won't apply if the text already includes new lines.
     */
    public int hoverTextWidth() {
        return this.hoverTextWidth;
    }

    /**
     * Set the max width the hover text should have.
     * Minecraft itself will wrap after 60 characters.
     * Won't apply if the text already includes new lines.
     * @param hoverTextWidth The url hover text length
     * @return The MineDownParser instance
     */
    public MineDownParser hoverTextWidth(int hoverTextWidth) {
        this.hoverTextWidth = hoverTextWidth;
        return this;
    }

}
