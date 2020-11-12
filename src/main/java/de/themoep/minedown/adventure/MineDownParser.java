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

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.themoep.minedown.adventure.MineDown.COLOR_PREFIX;
import static de.themoep.minedown.adventure.MineDown.FONT_PREFIX;
import static de.themoep.minedown.adventure.MineDown.FORMAT_PREFIX;
import static de.themoep.minedown.adventure.MineDown.HOVER_PREFIX;
import static de.themoep.minedown.adventure.MineDown.INSERTION_PREFIX;

public class MineDownParser {

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

    private ComponentBuilder<?, ?> builder;
    private StringBuilder value;
    private String font;
    private String insertion;
    private TextColor color;
    private Set<TextDecoration> format;
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
                    && i + 1 < message.length() && (c == '§' || c == colorChar());
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
                TextFormat encoded = null;
                Option filterOption = null;
                StringBuilder colorString = new StringBuilder();
                for (int j = i; j < message.length(); j++) {
                    char c1 = message.charAt(j);
                    if (c1 == c && colorString.length() > 1) {
                        try {
                            encoded = parse(colorString.toString(), "", lenient());
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
                    encoded = Util.getFormatFromLegacy(code);
                    if (encoded != null) {
                        filterOption = Option.LEGACY_COLORS;
                    }
                }

                if (encoded != null) {
                    if (!isFiltered(filterOption)) {
                        if (encoded == Util.TextControl.RESET) {
                            appendValue();
                            color = null;
                            Util.applyFormat(builder, format);
                            format = new HashSet<>();
                        } else if (encoded instanceof TextColor) {
                            if (value.length() > 0) {
                                appendValue();
                            }
                            color = (TextColor) encoded;
                            format = new HashSet<>();
                        } else {
                            if (value.length() > 0) {
                                appendValue();
                            }
                            format.add((TextDecoration) encoded);
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
                Set<TextDecoration> formats = new HashSet<>(format);
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
            builder = Component.text();
        }
        return builder;
    }

    private void append(ComponentBuilder builder) {
        if (this.builder == null) {
            this.builder = Component.text().append(builder);
        } else {
            this.builder.append(builder);
        }
    }

    private void appendValue() {
        appendValue(Style.empty());
    }

    private void appendValue(Style style) {
        if (builder == null) {
            builder = Component.text().append(Component.text(value.toString()));
        } else {
            builder.append(Component.text(value.toString())).style(style);
        }
        if (font != null) {
            builder.font(Key.key(font));
        }
        builder.insertion(insertion);
        builder.color(color);
        Util.applyFormat(builder, format);
        if (urlDetection() && URL_PATTERN.matcher(value).matches()) {
            String v = value.toString();
            if (!v.startsWith("http://") && !v.startsWith("https://")) {
                v = "http://" + v;
            }
            builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, v));
            if (urlHoverText() != null && !urlHoverText().isEmpty()) {
                builder.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new MineDown(urlHoverText()).replace("url", value.toString()).toComponent()
                ));
            }
        }
        if (clickEvent != null) {
            builder.clickEvent(clickEvent);
        }
        if (hoverEvent != null) {
            builder.hoverEvent(hoverEvent);
        }
        value = new StringBuilder();
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
        TextColor color = null;
        String font = null;
        String insertion = null;
        Set<TextDecoration> formats = new HashSet<>();
        ClickEvent clickEvent = null;
        HoverEvent hoverEvent = null;

        int formatEnd = -1;

        for (AtomicInteger i = new AtomicInteger(); i.get() < defParts.size(); i.incrementAndGet()) {
            String definition = defParts.get(i.get());
            TextFormat parsed = parse(definition, "", true);
            if (parsed != null) {
                if (parsed == Util.TextControl.RESET) {
                    color = null;
                } else if (parsed instanceof TextDecoration) {
                    formats.add((TextDecoration) parsed);
                } else if (parsed instanceof TextColor) {
                    color = (TextColor) parsed;
                }
                formatEnd = i.get();
                continue;
            }

            if (definition.toLowerCase(Locale.ROOT).startsWith(FONT_PREFIX)) {
                font = definition.substring(FONT_PREFIX.length());
                continue;
            }

            if (definition.toLowerCase(Locale.ROOT).startsWith(INSERTION_PREFIX)) {
                insertion = getValue(i, definition.substring(INSERTION_PREFIX.length()), defParts, true);
                continue;
            }

            if (definition.toLowerCase(Locale.ROOT).startsWith(COLOR_PREFIX)) {
                parsed = parse(definition, COLOR_PREFIX, lenient());
                if (parsed instanceof TextColor) {
                    color = (TextColor) parsed;
                } else if (!lenient()) {
                    if (parsed == null) {
                        throw new IllegalArgumentException("Unable to parse " + definition);
                    } else {
                        throw new IllegalArgumentException(definition + " -> " + color + " is a format and not a color!");
                    }
                }
                formatEnd = i.get();
                continue;
            }

            if (definition.toLowerCase(Locale.ROOT).startsWith(FORMAT_PREFIX)) {
                for (String formatStr : definition.substring(FORMAT_PREFIX.length()).split(",")) {
                    parsed = parse(formatStr, "", lenient());
                    if (parsed instanceof TextDecoration) {
                        formats.add((TextDecoration) parsed);
                    } else if (!lenient()) {
                        if (parsed == null) {
                            throw new IllegalArgumentException("Unable to parse " + formatStr);
                        } else {
                            throw new IllegalArgumentException(formatStr + " -> " + formats + " is a color and not a format!");
                        }
                    }
                }
                formatEnd = i.get();
                continue;
            }

            if (i.get() == formatEnd + 1 && URL_PATTERN.matcher(definition).matches()) {
                if (!definition.startsWith("http://") && !definition.startsWith("https://")) {
                    definition = "http://" + definition;
                }
                clickEvent = ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, definition);
                continue;
            }

            ClickEvent.Action clickAction = definition.startsWith("/") ? ClickEvent.Action.RUN_COMMAND : null;
            HoverEvent.Action hoverAction = null;
            if (definition.toLowerCase(Locale.ROOT).startsWith(HOVER_PREFIX)) {
                hoverAction = HoverEvent.Action.SHOW_TEXT;
            }
            String[] parts = definition.split("=", 2);
            try {
                hoverAction = HoverEvent.Action.NAMES.value(parts[0].toLowerCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
            }
            try {
                clickAction = ClickEvent.Action.valueOf(parts[0].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
            }

            String valueStr = getValue(i, parts.length > 1 ? parts[1] : "", defParts, clickAction != null || hoverAction != null);

            if (clickAction != null) {
                if (autoAddUrlPrefix() && clickAction == ClickEvent.Action.OPEN_URL && !valueStr.startsWith("http://") && !valueStr.startsWith("https://")) {
                    valueStr = "http://" + valueStr;
                }
                clickEvent = ClickEvent.clickEvent(clickAction, valueStr);
            } else if (hoverAction == null) {
                hoverAction = HoverEvent.Action.SHOW_TEXT;
            }
            if (hoverAction != null) {
                if (hoverAction == HoverEvent.Action.SHOW_TEXT) {
                    hoverEvent = HoverEvent.hoverEvent(hoverAction, copy(false).urlDetection(false).parse(Util.wrap(valueStr, hoverTextWidth())).build());
                } else if (hoverAction == HoverEvent.Action.SHOW_ENTITY) {
                    String[] valueParts = valueStr.split(":", 2);
                    try {
                        String[] additionalParts = valueParts[1].split(" ", 2);
                        if (!additionalParts[0].contains(":")) {
                            additionalParts[0] = "minecraft:" + additionalParts[0];
                        }
                        hoverEvent = HoverEvent.showEntity(HoverEvent.ShowEntity.of(
                                Key.key(additionalParts[0]), UUID.fromString(valueParts[0]),
                                additionalParts.length > 1 && additionalParts[1] != null ?
                                        copy(false).urlDetection(false).parse(additionalParts[1]).build() : null
                        ));
                    } catch (Exception e) {
                        if (!lenient()) {
                            if (valueParts.length < 2) {
                                throw new IllegalArgumentException("Invalid entity definition. Needs to be of format uuid:id or uuid:namespace:id!");
                            }
                            throw new IllegalArgumentException(e.getMessage());
                        }
                    }
                } else if (hoverAction == HoverEvent.Action.SHOW_ITEM) {
                    String[] valueParts = valueStr.split(" ", 2);
                    String id = valueParts[0];
                    if (!id.contains(":")) {
                        id = "minecraft:" + id;
                    }
                    int count = 1;
                    int countIndex = valueParts[0].indexOf('*');
                    if (countIndex > 0 && countIndex + 1 < valueParts[0].length()) {
                        try {
                            count = Integer.parseInt(valueParts[0].substring(countIndex + 1));
                            id = valueParts[0].substring(0, countIndex);
                        } catch (NumberFormatException e) {
                            if (!lenient()) {
                                throw new IllegalArgumentException(e.getMessage());
                            }
                        }
                    }
                    BinaryTagHolder tag = null;
                    if (valueParts.length > 1 && valueParts[1] != null) {
                        tag = BinaryTagHolder.of(valueParts[1]);
                    }

                    try {
                        hoverEvent = HoverEvent.showItem(HoverEvent.ShowItem.of(
                                Key.key(id), count, tag
                        ));
                    } catch (Exception e) {
                        if (!lenient()) {
                            throw new IllegalArgumentException(e.getMessage());
                        }
                    }
                }
            }
        }

        if (clickEvent != null && hoverEvent == null) {
            hoverEvent = HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Component.text()
                            .append(Component.text(clickEvent.action().toString().toLowerCase(Locale.ROOT).replace('_', ' ')))
                            .color(NamedTextColor.BLUE)
                            .append(Component.text(" " + clickEvent.action())).color(NamedTextColor.WHITE)
                            .build());
        }

        return copy()
                .urlDetection(false)
                .color(color)
                .font(font)
                .insertion(insertion)
                .format(formats)
                .clickEvent(clickEvent)
                .hoverEvent(hoverEvent)
                .parse(text);
    }

    private String getValue(AtomicInteger i, String firstPart, List<String> defParts, boolean hasAction) {
        int bracketDepth = !firstPart.isEmpty() && firstPart.startsWith("{") && hasAction ? 1 : 0;

        StringBuilder value = new StringBuilder();
        if (!firstPart.isEmpty() && hasAction) {
            if (bracketDepth > 0) {
                value.append(firstPart.substring(1));
            } else {
                value.append(firstPart);
            }
        } else {
            value.append(defParts.get(i.get()));
        }

        for (i.incrementAndGet(); i.get() < defParts.size(); i.incrementAndGet()) {
            String part = defParts.get(i.get());
            if (bracketDepth == 0) {
                int equalsIndex = part.indexOf('=');
                if (equalsIndex > 0 && !Util.isEscaped(part, equalsIndex)) {
                    i.decrementAndGet();
                    break;
                }
            }
            value.append(" ");
            if (bracketDepth > 0) {
                int startBracketIndex = part.indexOf("={");
                if (startBracketIndex > 0 && !Util.isEscaped(part, startBracketIndex) && !Util.isEscaped(part, startBracketIndex + 1)) {
                    bracketDepth++;
                }
                if (part.endsWith("}") && !Util.isEscaped(part, part.length() - 1)) {
                    bracketDepth--;
                    if (bracketDepth == 0) {
                        value.append(part, 0, part.length() - 1);
                        break;
                    }
                }
            }
            value.append(part);
        }

        return value.toString();
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

    private MineDownParser insertion(String insertion) {
        this.insertion = insertion;
        return this;
    }

    protected String insertion() {
        return this.insertion;
    }

    protected MineDownParser color(TextColor color) {
        this.color = color;
        return this;
    }

    protected TextColor color() {
        return this.color;
    }

    protected MineDownParser format(Set<TextDecoration> format) {
        this.format = format;
        return this;
    }

    protected Set<TextDecoration> format() {
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
     * Parse a color/format definition
     * @param colorString The string to parse
     * @param prefix      The color prefix e.g. ampersand (&amp;)
     * @param lenient     Whether or not to accept malformed strings
     * @return The parsed color or <code>null</code> if lenient is true and no color was found
     */
    public static TextFormat parse(String colorString, String prefix, boolean lenient) {
        TextFormat format = null;
        if (prefix.length() + 1 == colorString.length()) {
            format = Util.getFormatFromLegacy(colorString.charAt(prefix.length()));
            if (format == null && !lenient) {
                throw new IllegalArgumentException(colorString.charAt(prefix.length()) + " is not a valid " + prefix + " char!");
            }
        } else {
            if (colorString.charAt(prefix.length()) == '#') {
                format = TextColor.fromCSSHexString(colorString.substring(prefix.length()));
            } else {
                format = NamedTextColor.NAMES.value(colorString.substring(prefix.length()).toLowerCase(Locale.ROOT));
                if (format == null) {
                    try {
                        format = TextDecoration.valueOf(colorString.substring(prefix.length()).toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e2) {
                        if (!lenient) {
                            throw e2;
                        }
                    }
                }
            }
        }
        return format;
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
        insertion = null;
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
     * @return <code>true</code> if it's enabled; <code>false</code> if not
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
     * @return <code>true</code> if it's enabled; <code>false</code> if not
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
                    && i + 1 < string.length() && (c == '§' || c == colorChar());
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
