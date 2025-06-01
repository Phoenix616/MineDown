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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

public class MineDownStringifier {

    /**
     * Whether or not to use legacy color codes (Default: false)
     */
    private boolean useLegacyColors = false;

    /**
     * Whether or not to translate legacy formatting codes over Minedown ones (Default: false)
     */
    private boolean useLegacyFormatting = false;

    /**
     * Whether or not to use simple event definitions or specific ones (Default: true)
     */
    private boolean preferSimpleEvents = true;

    /**
     * Whether or not to put formatting in event definitions (Default: false)
     */
    private boolean formattingInEventDefinition = false;

    /**
     * Whether or not to put colors in event definitions (Default: false)
     */
    private boolean colorInEventDefinition = false;

    /**
     * The character to use as a special color code. (Default: ampersand &amp;)
     */
    private char colorChar = '&';

    private StringBuilder value = new StringBuilder();

    private TextColor color = null;
    private ClickEvent clickEvent = null;
    private HoverEvent hoverEvent = null;
    private Set<TextDecoration> formats = new LinkedHashSet<>();

    /**
     * Create a {@link MineDown} string from a component message
     * @param components The components to generate a MineDown string from
     * @return The MineDown string
     */
    public String stringify(List<Component> components) {
        StringBuilder sb = new StringBuilder();
        for (Component component : components) {
            sb.append(stringify(component));
        }
        return sb.toString();
    }

    /**
     * Create a {@link MineDown} string from a component message
     * @param component The component to generate a MineDown string from
     * @return The MineDown string
     */
    public String stringify(Component component) {
        StringBuilder sb = new StringBuilder();
        if (!component.hasStyling() && component.children().isEmpty() &&  component instanceof TextComponent) {
            appendText(sb, component);
            return sb.toString();
        }
        boolean hasEvent = (component.style().font() != null && component.style().font() != Style.DEFAULT_FONT)
                || (component.shadowColor() != null && component.shadowColor().alpha() != 0)
                || component instanceof TranslatableComponent || component.insertion() != null
                || component.clickEvent() != clickEvent || component.hoverEvent() != hoverEvent;
        if (hasEvent) {
            sb.append('[');
            if (!formattingInEventDefinition()) {
                appendFormat(sb, component);
            }
            if (!colorInEventDefinition()) {
                appendColor(sb, component.color());
            }
        } else if (component.color() != null) {
            appendFormat(sb, component);
            appendColor(sb, component.color());
        } else {
            appendFormat(sb, component);
        }

        appendText(sb, component);

        if (!component.children().isEmpty()) {
            sb.append(copy().stringify(component.children()));
        }

        if (hasEvent) {
            clickEvent = component.clickEvent();
            hoverEvent = component.hoverEvent();
            if (!formattingInEventDefinition()) {
                appendFormatSuffix(sb, component);
            }
            sb.append("](");
            List<String> definitions = new ArrayList<>();
            if (component instanceof TranslatableComponent) {
                TranslatableComponent translatable = (TranslatableComponent) component;
                definitions.add(TRANSLATE_PREFIX + translatable.key());
                if (!translatable.args().isEmpty()) {
                    definitions.add(new StringBuilder()
                            .append(WITH_PREFIX)
                            .append("{")
                            .append(translatable.args().stream().map(this::stringify)
                                    .collect(Collectors.joining(",")))
                            .append("}").toString());
                }
            }
            if (colorInEventDefinition() && component.color() != null) {
                StringBuilder sbi = new StringBuilder();
                if (!preferSimpleEvents()) {
                    sbi.append(COLOR_PREFIX);
                }
                if (component.color() instanceof NamedTextColor) {
                    sbi.append(component.color());
                } else {
                    sbi.append(component.color().asHexString().toLowerCase(Locale.ROOT));
                }
                definitions.add(sbi.toString());
            }
            if (formattingInEventDefinition()) {
                StringBuilder sbi = new StringBuilder();
                if (!preferSimpleEvents) {
                    sbi.append(FORMAT_PREFIX);
                }
                sbi.append(component.decorations().entrySet().stream()
                        .filter(e -> e.getValue() == TextDecoration.State.TRUE)
                        .map(e -> e.getKey().name().toLowerCase(Locale.ROOT))
                        .collect(Collectors.joining(" ")));
                definitions.add(sbi.toString());
            }
            if (component.shadowColor() != null && component.shadowColor().alpha() != 0) {
                String hexString = component.shadowColor().asHexString().toLowerCase(Locale.ROOT);
                if (component.shadowColor().alpha() == SHADOW_ALPHA) {
                    String shortHex = hexString.substring(0, 7);
                    TextColor color = TextColor.fromHexString(shortHex);
                    NamedTextColor namedColor = NamedTextColor.namedColor(color.value());
                    if (namedColor != null) {
                        definitions.add(SHADOW_PREFIX + namedColor);
                    } else {
                        definitions.add(SHADOW_PREFIX + shortHex);
                    }
                } else {
                    definitions.add(SHADOW_PREFIX + hexString);
                }
            }
            if (component.style().font() != null && component.style().font() != Style.DEFAULT_FONT) {
                Key font = component.style().font();
                if (font.namespace().equals("minecraft")) {
                    definitions.add(FONT_PREFIX + font.value());
                } else {
                    definitions.add(FONT_PREFIX + font);
                }
            }
            if (component.insertion() != null) {
                if (component.insertion().contains(" ")) {
                    definitions.add(INSERTION_PREFIX + "{" + component.insertion() + "}");
                } else {
                    definitions.add(INSERTION_PREFIX + component.insertion());
                }
            }
            if (component.clickEvent() != null) {
                if (preferSimpleEvents() && component.clickEvent().action() == ClickEvent.Action.OPEN_URL) {
                    definitions.add(component.clickEvent().value());
                } else {
                    definitions.add(component.clickEvent().action().toString().toLowerCase(Locale.ROOT) + "=" + component.clickEvent().value());
                }
            }
            if (component.hoverEvent() != null) {
                StringBuilder sbi = new StringBuilder();
                if (preferSimpleEvents()) {
                    if (component.hoverEvent().action() == HoverEvent.Action.SHOW_TEXT &&
                            (component.clickEvent() == null || component.clickEvent().action() != ClickEvent.Action.OPEN_URL)) {
                        sbi.append(HOVER_PREFIX);
                    }
                } else {
                    sbi.append(component.hoverEvent().action().toString().toLowerCase(Locale.ROOT)).append('=');
                }
                HoverEvent<?> hoverEvent = component.hoverEvent();
                if (hoverEvent.value() instanceof Component) {
                    sbi.append(copy().stringify((Component) hoverEvent.value()));
                } else if (hoverEvent.value() instanceof HoverEvent.ShowEntity) {
                    HoverEvent.ShowEntity contentEntity = (HoverEvent.ShowEntity) hoverEvent.value();
                    sb.append(contentEntity.id()).append(":").append(contentEntity.type());
                    if (contentEntity.name() != null) {
                        sb.append(" ").append(stringify(contentEntity.name()));
                    }
                } else if (hoverEvent.value() instanceof HoverEvent.ShowItem) {
                    HoverEvent.ShowItem contentItem = (HoverEvent.ShowItem) hoverEvent.value();
                    sb.append(contentItem.item());
                    if (contentItem.count() > 0) {
                        sb.append("*").append(contentItem.count());
                    }
                    if (contentItem.nbt() != null) {
                        sb.append(" ").append(contentItem.nbt().string());
                    }
                }
                definitions.add(sbi.toString());
            }
            sb.append(String.join(" ", definitions));
            sb.append(')');
        } else {
            appendFormatSuffix(sb, component);
        }
        return sb.toString();
    }

    private void appendText(StringBuilder sb, Component component) {
        if (component instanceof TextComponent) {
            sb.append(((TextComponent) component).content());
            return;
        } else if (component instanceof TranslatableComponent) {
            try {
                sb.append(((TranslatableComponent) component).fallback());
            } catch (NoSuchMethodError ignored) {
                // version without fallback
            }
        } else {
            throw new UnsupportedOperationException("Cannot stringify " + component.getClass().getTypeName() + " yet! Only TextComponents are supported right now. Sorry. :(");
        }
    }

    private void appendColor(StringBuilder sb, TextColor color) {
        if (this.color != color) {
            this.color = color;
            if (useLegacyColors()) {
                if (color == null) {
                    sb.append(colorChar()).append(Util.TextControl.RESET.getChar());
                } else {
                    try {
                        char colorChar = Util.getLegacyFormatChar(color);
                        sb.append(colorChar()).append(colorChar);
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                }
            } else if (color instanceof NamedTextColor) {
                sb.append(colorChar()).append(((NamedTextColor) color).toString()).append(colorChar());
            } else if (color != null) {
                sb.append(colorChar()).append(color.asHexString()).append(colorChar());
            } else {
                sb.append(colorChar()).append(Util.TextControl.RESET.name()).append(colorChar());
            }
        }
    }

    private void appendFormat(StringBuilder sb, Component component) {
        Set<TextDecoration> formats = Util.getFormats(component, true);
        if (!formats.containsAll(this.formats)) {
            if (useLegacyFormatting()) {
                sb.append(colorChar()).append(Util.TextControl.RESET.getChar());
            } else {
                Deque<TextDecoration> formatDeque = new ArrayDeque<>(this.formats);
                while (!formatDeque.isEmpty()) {
                    TextDecoration format = formatDeque.pollLast();
                    if (!formats.contains(format)) {
                        sb.append(MineDown.getFormatString(format));
                    }
                }
            }
        } else {
            formats.removeAll(this.formats);
        }
        for (TextDecoration format : formats) {
            if (useLegacyFormatting()) {
                try {
                    char colorChar = Util.getLegacyFormatChar(format);
                    sb.append(colorChar()).append(colorChar);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                sb.append(MineDown.getFormatString(format));
            }
        }
        this.formats.clear();
        this.formats.addAll(formats);
    }

    private void appendFormatSuffix(StringBuilder sb, Component component) {
        if (!useLegacyFormatting()) {
            Set<TextDecoration> formats = Util.getFormats(component, true);
            for (TextDecoration format : formats) {
                sb.append(MineDown.getFormatString(format));
            }
            this.formats.removeAll(formats);
        }
    }

    /**
     * Copy all the parser's setting to a new instance
     * @return The new parser instance with all settings copied
     */
    public MineDownStringifier copy() {
        return new MineDownStringifier().copy(this);
    }

    /**
     * Copy all the parser's settings from another parser
     * @param from The stringifier to copy from
     * @return This stringifier's instance
     */
    public MineDownStringifier copy(MineDownStringifier from) {
        useLegacyColors(from.useLegacyColors());
        useLegacyFormatting(from.useLegacyFormatting());
        preferSimpleEvents(from.preferSimpleEvents());
        formattingInEventDefinition(from.formattingInEventDefinition());
        colorInEventDefinition(from.colorInEventDefinition());
        colorChar(from.colorChar());
        return this;
    }

    /**
     * Get whether or not to use legacy color codes
     * @return whether or not to use legacy color codes when possible (Default: true)
     */
    public boolean useLegacyColors() {
        return this.useLegacyColors;
    }

    /**
     * Set whether or not to use legacy color codes
     * @param useLegacyColors Whether or not to use legacy colors (Default: true)
     * @return The MineDownStringifier instance
     */
    public MineDownStringifier useLegacyColors(boolean useLegacyColors) {
        this.useLegacyColors = useLegacyColors;
        return this;
    }

    /**
     * Get whether or not to translate legacy formatting codes over MineDown ones
     * @return whether or not to use legacy formatting codes (Default: false)
     */
    public boolean useLegacyFormatting() {
        return this.useLegacyFormatting;
    }

    /**
     * Set whether or not to translate legacy formatting codes over MineDown ones
     * @param useLegacyFormatting Whether or not to translate legacy formatting codes (Default: false)
     * @return The MineDownStringifier instance
     */
    public MineDownStringifier useLegacyFormatting(boolean useLegacyFormatting) {
        this.useLegacyFormatting = useLegacyFormatting;
        return this;
    }

    /**
     * Get whether or not to use simple event definitions or specific ones (Default: true)
     * @return whether or not to use simple events
     */
    public boolean preferSimpleEvents() {
        return this.preferSimpleEvents;
    }

    /**
     * Set whether or not to use simple event definitions or specific ones
     * @param preferSimpleEvents Whether or not to prefer simple events (Default: true)
     * @return The MineDownStringifier instance
     */
    public MineDownStringifier preferSimpleEvents(boolean preferSimpleEvents) {
        this.preferSimpleEvents = preferSimpleEvents;
        return this;
    }

    /**
     * Get whether or not to put colors in event definitions or use inline color definitions
     * @return whether or not to put colors in event definitions (Default: false)
     */
    public boolean colorInEventDefinition() {
        return this.colorInEventDefinition;
    }

    /**
     * Set whether or not to put colors in event definitions or use inline color definitions
     * @param colorInEventDefinition Whether or not to put colors in event definitions (Default: false)
     * @return The MineDownStringifier instance
     */
    public MineDownStringifier colorInEventDefinition(boolean colorInEventDefinition) {
        this.colorInEventDefinition = colorInEventDefinition;
        return this;
    }

    /**
     * Get whether or not to put formatting in event definitions or use inline formatting definitions
     * @return whether or not to put formatting in event definitions (Default: false)
     */
    public boolean formattingInEventDefinition() {
        return this.formattingInEventDefinition;
    }

    /**
     * Set whether or not to put formatting in event definitions or use inline formatting definitions
     * @param formattingInEventDefinition Whether or not to put formatting in event definitions (Default: false)
     * @return The MineDownStringifier instance
     */
    public MineDownStringifier formattingInEventDefinition(boolean formattingInEventDefinition) {
        this.formattingInEventDefinition = formattingInEventDefinition;
        return this;
    }

    /**
     * Get the character to use as a special color code. (Default: ampersand &amp;)
     * @return the color character
     */
    public char colorChar() {
        return this.colorChar;
    }

    /**
     * Set the character to use as a special color code.
     * @param colorChar The character to be used as the color char (for legacy and MineDown colors, default: ampersand &amp;)
     * @return The MineDownStringifier instance
     */
    public MineDownStringifier colorChar(char colorChar) {
        this.colorChar = colorChar;
        return this;
    }

}
