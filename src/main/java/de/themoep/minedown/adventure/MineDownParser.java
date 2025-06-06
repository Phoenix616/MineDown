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
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.AbstractMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.themoep.minedown.adventure.MineDown.COLOR_PREFIX;
import static de.themoep.minedown.adventure.MineDown.FONT_PREFIX;
import static de.themoep.minedown.adventure.MineDown.FORMAT_PREFIX;
import static de.themoep.minedown.adventure.MineDown.HOVER_PREFIX;
import static de.themoep.minedown.adventure.MineDown.INSERTION_PREFIX;
import static de.themoep.minedown.adventure.MineDown.SHADOW_ALPHA;
import static de.themoep.minedown.adventure.MineDown.SHADOW_PREFIX;
import static de.themoep.minedown.adventure.MineDown.TRANSLATE_PREFIX;
import static de.themoep.minedown.adventure.MineDown.WITH_PREFIX;
import static net.kyori.adventure.text.format.TextColor.HEX_PREFIX;

public class MineDownParser {
    private static final String RAINBOW = "rainbow";

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

    public static final Pattern URL_PATTERN = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]+\\.[a-z]{2,18})(/\\S*)?$");

    private ComponentBuilder<?, ?> builder;
    private StringBuilder value;
    private String translationKey;
    private List<Component> translationArgs = new ArrayList<>();
    private String font;
    private String insertion;
    private Integer rainbowPhase;
    private List<Map.Entry<TextColor, Boolean>> colors;
    private ShadowColor shadow;
    private Map<TextDecoration, Boolean> format;
    private boolean formattingIsLegacy = false;
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
            int eventEndIndex = -1;
            String eventDefinition = null;
            if (!escaped && isEnabled(Option.ADVANCED_FORMATTING) && c == '[') {
                eventEndIndex = Util.getUnescapedEndIndex(message, '[', ']', i);
                if (eventEndIndex != -1 && message.length() > eventEndIndex + 1 && message.charAt(eventEndIndex + 1) == '(') {
                    int definitionClose = Util.getUnescapedEndIndex(message, '(', ')', eventEndIndex + 1);
                    if (definitionClose != -1) {
                        eventDefinition = message.substring(eventEndIndex + 2, definitionClose);
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
                Integer rainbowPhase = null;
                List<Map.Entry<TextFormat, Boolean>> encoded = null;
                Option filterOption = null;
                StringBuilder colorString = new StringBuilder();
                for (int j = i; j < message.length(); j++) {
                    char c1 = message.charAt(j);
                    if (c1 == c && colorString.length() > 1) {
                        String colorStr = colorString.toString();
                        rainbowPhase = parseRainbow(colorStr, "", lenient());
                        if (rainbowPhase == null && !colorStr.contains("=")) {
                            encoded = parseFormat(colorStr, "", true);
                            if (encoded.isEmpty()) {
                                encoded = null;
                            } else {
                                filterOption = Option.SIMPLE_FORMATTING;
                                i = j;
                            }
                        } else {
                            filterOption = Option.SIMPLE_FORMATTING;
                            i = j;
                        }
                        break;
                    }
                    if (c1 != '_' && c1 != '#' && c1 != '-' && c1 != ',' && c1 != ':' &&
                            (c1 < 'A' || c1 > 'Z') && (c1 < 'a' || c1 > 'z') && (c1 < '0' || c1 > '9')) {
                        break;
                    }
                    colorString.append(c1);
                }
                if (rainbowPhase == null && encoded == null) {
                    TextFormat format = Util.getFormatFromLegacy(code);
                    if (format != null) {
                        filterOption = Option.LEGACY_COLORS;
                        encoded = new ArrayList<>();
                        encoded.add(new AbstractMap.SimpleEntry<>(format, true));
                    }
                }

                if (rainbowPhase != null || encoded != null) {
                    if (!isFiltered(filterOption)) {
                        if (encoded != null && encoded.size() == 1) {
                            Map.Entry<TextFormat, Boolean> single = encoded.iterator().next();
                            if (single.getKey() == Util.TextControl.RESET) {
                                if (builder() == null && ((format() != null && !format().isEmpty()) || (colors() != null && !colors().isEmpty()))) {
                                    builder(Component.text());
                                }
                                appendValue();
                                colors(new ArrayList<>());
                                rainbowPhase(null);
                                format(new HashMap<>());
                            } else if (single.getKey() instanceof TextColor) {
                                if (value().length() > 0) {
                                    if (builder() == null && format() != null && !format().isEmpty()) {
                                        builder(Component.text());
                                    }
                                    appendValue();
                                }
                                colors(new ArrayList<>());
                                colors().add(new AbstractMap.SimpleImmutableEntry<>((TextColor) single.getKey(), single.getValue()));
                                rainbowPhase(null);
                                if (formattingIsLegacy()) {
                                    format(new HashMap<>());
                                }
                            } else if (single.getKey() instanceof TextDecoration) {
                                if (value.length() > 0) {
                                    appendValue();
                                }
                                formattingIsLegacy(true);
                                format().put((TextDecoration) single.getKey(), single.getValue());
                            }
                        } else {
                            if (value().length() > 0) {
                                appendValue();
                            }
                            rainbowPhase(rainbowPhase);
                            if (encoded != null) {
                                List<Map.Entry<TextColor, Boolean>> colors = new ArrayList<>();
                                for (Map.Entry<TextFormat, Boolean> e : encoded) {
                                    if (e.getKey() instanceof TextColor) {
                                        colors.add(new AbstractMap.SimpleImmutableEntry<>((TextColor) e.getKey(), e.getValue()));
                                    }
                                }
                                colors(colors);
                            } else {
                                colors(null);
                            }
                            if (formattingIsLegacy()) {
                                format(new HashMap<>());
                            }
                        }
                    }
                } else {
                    value().append(c).append(code);
                }
                continue;

                // Events
            } else if (eventEndIndex != -1 && eventDefinition != null) {
                appendValue();
                if (!isFiltered(Option.ADVANCED_FORMATTING) && !eventDefinition.isEmpty()) {
                    append(parseEvent(message.substring(i + 1, eventEndIndex), eventDefinition));
                } else {
                    append(copy(true).parse(message.substring(i + 1, eventEndIndex)));
                }
                i = eventEndIndex + 2 + eventDefinition.length();
                continue;

                // Simple formatting
            } else if (isFormatting) {
                int endIndex = message.indexOf(String.valueOf(c) + String.valueOf(c), i + 2);
                Map<TextDecoration, Boolean> formats = new HashMap<>(format());
                if (!isFiltered(Option.SIMPLE_FORMATTING)) {
                    formats.put(MineDown.getFormatFromChar(c), true);
                }
                formattingIsLegacy(false);
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
                    value(new StringBuilder(message.substring(i, urlEnd)));
                    appendValue();
                    i = urlEnd - 1;
                    continue;
                }
            }

            // It's normal text, just append the character
            value().append(message.charAt(i));
        }
        if (escaped) {
            value().append('\\');
        }
        appendValue();
        if (builder() == null) {
            builder(Component.text());
        }
        return builder();
    }

    private void append(ComponentBuilder<?, ?> builder) {
        if (builder() == null) {
            builder(Component.text().append(builder));
        } else {
            builder().append(builder);
        }
    }

    private void appendValue() {
        ComponentBuilder<?, ?> builder;
        List<TextColor> applicableColors;
        long valueCodepointLength = value().length();
        // If the value is empty don't add anything
        if (valueCodepointLength == 0) {
            return;
        }
        if (rainbowPhase() != null) {
            // Rainbow colors
            valueCodepointLength = value().codePoints().count();
            applicableColors = Util.createRainbow(valueCodepointLength, rainbowPhase());
        } else if (colors() != null) {
            if (colors().size() > 1) {
                valueCodepointLength = value().codePoints().count();
                applicableColors = Util.createGradient(
                        valueCodepointLength,
                        colors.stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toList())
                );
            } else {
                applicableColors = colors.stream().map(Map.Entry::getKey).collect(Collectors.toCollection(ArrayList::new));
            }
        } else {
            applicableColors = new ArrayList<>();
        }

        if (applicableColors.size() > 1 && translationKey() == null) {
            // Colors need to have a gradient/rainbow applied
            builder = Component.text();
        } else {
            // Single color mode
            if (translationKey() != null) {
                try {
                    builder = Component.translatable(translationKey(), value().toString(), translationArgs()).toBuilder();
                } catch (NoSuchMethodError e) {
                    // Adventure version without fallback
                    builder = Component.translatable(translationKey(), translationArgs()).toBuilder();
                }
                if (!applicableColors.isEmpty()) {
                    // translatable components can only have one color
                    builder.color(applicableColors.get(0));
                }
            } else {
                builder = Component.text(value().toString()).toBuilder();
                if (applicableColors.size() == 1) {
                    builder.color(applicableColors.get(0));
                }
            }
        }

        if (shadow() != null) {
            builder.shadowColor(shadow());
        }

        if (font() != null) {
            builder.font(Key.key(font()));
        }
        builder.insertion(insertion());
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
        if (clickEvent() != null) {
            builder.clickEvent(clickEvent());
        }
        if (hoverEvent() != null) {
            builder.hoverEvent(hoverEvent());
        }

        if (applicableColors.size() > 1) {
            int stepLength = (int) Math.round((double) valueCodepointLength / applicableColors.size());
            ComponentBuilder<?, ?> component = Component.empty().toBuilder();

            StringBuilder sb = new StringBuilder();
            int colorIndex = 0;
            int steps = 0;

            for (PrimitiveIterator.OfInt it = value().codePoints().iterator(); it.hasNext(); ) {
                sb.appendCodePoint(it.next());
                if (++steps == stepLength) {
                    steps = 0;
                    component.append(Component.text(sb.toString()).color(applicableColors.get(colorIndex++)));
                    sb = new StringBuilder();
                }
            }
            builder.append(component);
        }
        if (builder() == null) {
            builder(builder);
        } else {
            builder().append(builder);
        }
        value(new StringBuilder());
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
        Integer rainbowPhase = null;
        List<Map.Entry<TextColor, Boolean>> colors = null;
        ShadowColor shadowColor = null;
        String translationKey = null;
        List<Component> translationArgs = new ArrayList<>();
        String font = null;
        String insertion = null;
        Map<TextDecoration, Boolean> formats = new HashMap<>();
        ClickEvent clickEvent = null;
        HoverEvent hoverEvent = null;

        int formatEnd = -1;

        for (AtomicInteger i = new AtomicInteger(); i.get() < defParts.size(); i.incrementAndGet()) {
            String definition = defParts.get(i.get());
            Integer parsedRainbowPhase = parseRainbow(definition, "", lenient());
            if (parsedRainbowPhase != null) {
                rainbowPhase = parsedRainbowPhase;
                continue;
            } else if (!definition.contains("=")) {
                List<Map.Entry<TextFormat, Boolean>> parsed = parseFormat(definition, "", true);
                if (parsed != null && !parsed.isEmpty()) {
                    for (Map.Entry<TextFormat, Boolean> e : parsed) {
                        if (e.getKey() instanceof TextColor) {
                            if (colors == null) {
                                colors = new ArrayList<>();
                            }
                            colors.add(new AbstractMap.SimpleImmutableEntry<>((TextColor) e.getKey(), e.getValue()));
                        } else if (e.getKey() instanceof TextDecoration) {
                            formats.put((TextDecoration) e.getKey(), e.getValue());
                        }
                    }
                    formatEnd = i.get();
                    continue;
                }
            }

            if (definition.toLowerCase(Locale.ROOT).startsWith(TRANSLATE_PREFIX)) {
                translationKey = definition.substring(TRANSLATE_PREFIX.length());
                continue;
            }

            if (definition.toLowerCase(Locale.ROOT).startsWith(WITH_PREFIX)) {
                String[] args = getValue(i, definition.substring(WITH_PREFIX.length()), defParts, true).split("(?<!\\\\),");
                for (String arg : args) {
                    translationArgs.add(copy(false).urlDetection(false).parse(arg).build());
                }
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
                Integer colorRainbowPhase = parseRainbow(definition, COLOR_PREFIX, lenient());
                if (colorRainbowPhase == null) {
                    List<Map.Entry<TextFormat, Boolean>> parsed = parseFormat(definition, COLOR_PREFIX, lenient());
                    colors = new ArrayList<>();
                    for (Map.Entry<TextFormat, Boolean> e : parsed) {
                        if (e.getKey() instanceof TextColor) {
                            colors.add(new AbstractMap.SimpleImmutableEntry<>((TextColor) e.getKey(), e.getValue()));
                        } else if (!lenient()) {
                            throw new IllegalArgumentException(e + "  is a format and not a color!");
                        }
                    }
                } else {
                    rainbowPhase = colorRainbowPhase;
                }
                formatEnd = i.get();
                continue;
            }

            if (definition.toLowerCase(Locale.ROOT).startsWith(SHADOW_PREFIX)) {
                ShadowColor parsed = parseShadow(definition, SHADOW_PREFIX, lenient());
                if (parsed != null) {
                    shadowColor = parsed;
                } else if (!lenient()) {
                    throw new IllegalArgumentException("Invalid shadow definition: " + definition);
                }
                formatEnd = i.get();
                continue;
            }

            if (definition.toLowerCase(Locale.ROOT).startsWith(FORMAT_PREFIX)) {
                List<Map.Entry<TextFormat, Boolean>> parsed = parseFormat(definition, FORMAT_PREFIX, lenient());
                for (Map.Entry<TextFormat, Boolean> e : parsed) {
                    if (e.getKey() instanceof TextDecoration) {
                        formats.put((TextDecoration) e.getKey(), e.getValue());
                    } else if (!lenient()) {
                        throw new IllegalArgumentException(e + " is a color and not a format!");
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
            if (hoverAction == null) {
                hoverAction = HoverEvent.Action.NAMES.value(parts[0].toLowerCase(Locale.ROOT));
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
                            .append(Component.text(" " + clickEvent.value())).color(NamedTextColor.WHITE)
                            .build());
        }

        return copy()
                .urlDetection(false)
                .translationKey(translationKey)
                .translationArgs(translationArgs)
                .rainbowPhase(rainbowPhase)
                .colors(colors)
                .shadow(shadowColor)
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

    protected ComponentBuilder<?,?> builder() {
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

    public MineDownParser translationKey(String translationKey) {
        this.translationKey = translationKey;
        return this;
    }

    public String translationKey() {
        return translationKey;
    }

    public MineDownParser translationArgs(List<Component> translationArgs) {
        this.translationArgs = translationArgs;
        return this;
    }

    public List<Component> translationArgs() {
        return translationArgs;
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

    protected MineDownParser colors(List<Map.Entry<TextColor, Boolean>> colors) {
        this.colors = colors;
        return this;
    }

    protected MineDownParser rainbowPhase(Integer rainbowPhase) {
        this.rainbowPhase = rainbowPhase;
        return this;
    }

    protected Integer rainbowPhase() {
        return this.rainbowPhase;
    }

    protected List<Map.Entry<TextColor, Boolean>> colors() {
        return this.colors;
    }

    protected MineDownParser shadow(ShadowColor shadow) {
        this.shadow = shadow;
        return this;
    }

    protected ShadowColor shadow() {
        return this.shadow;
    }

    protected MineDownParser format(Map<TextDecoration, Boolean> format) {
        this.format = format;
        return this;
    }

    protected Map<TextDecoration, Boolean> format() {
        return this.format;
    }

    protected MineDownParser formattingIsLegacy(boolean formattingIsLegacy) {
        this.formattingIsLegacy = formattingIsLegacy;
        return this;
    }

    protected boolean formattingIsLegacy() {
        return formattingIsLegacy;
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

    private static Integer parseRainbow(String colorString, String prefix, boolean lenient) {
        if (colorString.substring(prefix.length()).toLowerCase(Locale.ROOT).startsWith(RAINBOW)) {
            if (colorString.length() > prefix.length() + RAINBOW.length() + 1) {
                try {
                    return Integer.parseInt(colorString.substring(prefix.length() + RAINBOW.length() + 1));
                } catch (NumberFormatException e) {
                    if (!lenient) throw e;
                }
            } else {
                return 0;
            }
        }
        return null;
    }

    /**
     * Parse a color/format definition
     * @param colorString The string to parse
     * @param prefix      The color prefix e.g. ampersand (&amp;)
     * @param lenient     Whether or not to accept malformed strings
     * @return The parsed color or <code>null</code> if lenient is true and no color was found
     */
    private static List<Map.Entry<TextFormat, Boolean>> parseFormat(String colorString, String prefix, boolean lenient) {
        List<Map.Entry<TextFormat, Boolean>> formats = new ArrayList<>();
        if (prefix.length() + 1 == colorString.length()) {
            TextFormat format = Util.getFormatFromLegacy(colorString.charAt(prefix.length()));
            if (format == null && !lenient) {
                throw new IllegalArgumentException(colorString.charAt(prefix.length()) + " is not a valid " + prefix + " char!");
            }
            formats.add(new AbstractMap.SimpleImmutableEntry<>(format, true));
        } else {
            for (String part : colorString.substring(prefix.length()).split("[\\-,]")) {
                if (part.isEmpty()) {
                    continue;
                }
                boolean negated = part.charAt(0) == '!';
                if (negated) {
                    part = part.substring(1);
                }
                try {
                    TextFormat format = Util.getFormatFromString(part);
                    if (format != null) {
                        formats.add(new AbstractMap.SimpleImmutableEntry<>(format, !negated));
                    }
                } catch (IllegalArgumentException e) {
                    if (!lenient) {
                        throw e;
                    }
                }
            }
        }
        return formats;
    }

    /**
     * Parse a color/format definition
     * @param shadowString The string to parse
     * @param prefix       The shadow prefix
     * @param lenient      Whether to accept malformed strings
     * @return The parsed shadow color or <code>null</code> if lenient is true and no color was found
     */
    private static ShadowColor parseShadow(String shadowString, String prefix, boolean lenient) {
        if (prefix.length() + 1 == shadowString.length()) {
            TextFormat format = Util.getFormatFromLegacy(shadowString.charAt(prefix.length()));
            if (format == null && !lenient) {
                throw new IllegalArgumentException(shadowString.charAt(prefix.length()) + " is not a valid " + prefix + " char!");
            }
            if (format instanceof TextColor) {
                return ShadowColor.shadowColor((TextColor) format, SHADOW_ALPHA);
            } else if (!lenient) {
                throw new IllegalArgumentException(shadowString.charAt(prefix.length()) + " is not a valid shadow color!");
            } else {
                return null;
            }
        }
        String shadowColor = shadowString.substring(prefix.length());
        if (shadowColor.isEmpty()) {
            if (!lenient) {
                throw new IllegalArgumentException("No value for the shadow specified!");
            } else {
                return null;
            }
        }

        try {
            TextFormat format = Util.getFormatFromString(shadowColor);
            if (format instanceof TextColor) {
                return ShadowColor.shadowColor((TextColor) format, SHADOW_ALPHA);
            } else if (format != null) {
                if (!lenient) {
                    throw new IllegalArgumentException(shadowColor + " is not a valid shadow color!");
                }
            }
        } catch (IllegalArgumentException ignored) {}
        String modShadowColor = shadowColor;
        if (shadowColor.startsWith(HEX_PREFIX) && shadowColor.length() == 5) {
            // support short form which only specifies a single hex for each channel
            modShadowColor = HEX_PREFIX + shadowColor.charAt(1) + shadowColor.charAt(1)
                    + shadowColor.charAt(2) + shadowColor.charAt(2)
                    + shadowColor.charAt(3) + shadowColor.charAt(3)
                    + shadowColor.charAt(4) + shadowColor.charAt(4);
        }
        ShadowColor shadow = ShadowColor.fromHexString(modShadowColor);
        if (shadow != null) {
            return shadow;
        }
        if (!lenient) {
            throw new IllegalArgumentException(shadowColor + " is not a valid shadow color!");
        }
        return null;
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
            formattingIsLegacy(from.formattingIsLegacy());
            rainbowPhase(from.rainbowPhase());
            colors(from.colors());
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
        translationKey = null;
        translationArgs.clear();
        font = null;
        insertion = null;
        rainbowPhase = null;
        colors = null;
        shadow = null;
        format = new HashMap<>();
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
