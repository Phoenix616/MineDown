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
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MineDownStringifier {

    private static final boolean HAS_FONT_SUPPORT = Util.hasMethod(BaseComponent.class, "getFontRaw");
    private static final boolean HAS_HOVER_CONTENT_SUPPORT = Util.hasMethod(HoverEvent.class, "getContents");
    private static final Method HOVER_GET_VALUE = Util.getMethod(HoverEvent.class, "getValue");

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
     * Whether or not to put colors in event definitions (Default: true)
     */
    private boolean colorInEventDefinition = true;

    /**
     * The character to use as a special color code. (Default: ampersand &amp;)
     */
    private char colorChar = '&';

    public static final String FONT_PREFIX = "font=";
    public static final String COLOR_PREFIX = "color=";
    public static final String FORMAT_PREFIX = "format=";
    public static final String HOVER_PREFIX = "hover=";

    private StringBuilder value = new StringBuilder();

    private ChatColor color = null;
    private ClickEvent clickEvent = null;
    private HoverEvent hoverEvent = null;
    private Set<ChatColor> formats = new LinkedHashSet<>();

    /**
     * Create a {@link MineDown} string from a component message
     * @param components The components to generate a MineDown string from
     * @return The MineDown string
     */
    public String stringify(BaseComponent... components) {
        StringBuilder sb = new StringBuilder();
        for (BaseComponent component : components) {
            if (!component.hasFormatting()) {
                appendText(sb, component);
                continue;
            }
            if (component.getClickEvent() != null || component.getHoverEvent() != null) {
                sb.append('[');
                if (!formattingInEventDefinition()) {
                    appendFormat(sb, component);
                }
                if (!colorInEventDefinition()) {
                    appendColor(sb, component.getColor());
                }
            } else if (component.getColorRaw() != null) {
                appendFormat(sb, component);
                appendColor(sb, component.getColor());
            } else {
                appendFormat(sb, component);
            }

            appendText(sb, component);

            if (component.getExtra() != null && !component.getExtra().isEmpty()) {
                sb.append(copy().stringify(component.getExtra().toArray(new BaseComponent[0])));
            }

            if (component.getClickEvent() != clickEvent || component.getHoverEvent() != hoverEvent) {
                clickEvent = component.getClickEvent();
                hoverEvent = component.getHoverEvent();
                if (!formattingInEventDefinition()) {
                    appendFormatSuffix(sb, component);
                }
                sb.append("](");
                List<String> definitions = new ArrayList<>();
                if (colorInEventDefinition()) {
                    StringBuilder sbi = new StringBuilder();
                    if (!preferSimpleEvents()) {
                        sbi.append(COLOR_PREFIX);
                    }
                    sbi.append(component.getColor().getName().toLowerCase());
                    definitions.add(sbi.toString());
                }
                if (formattingInEventDefinition()) {
                    StringBuilder sbi = new StringBuilder();
                    if (!preferSimpleEvents) {
                        sbi.append(FORMAT_PREFIX);
                    }
                    sbi.append(Util.getFormats(component, true).stream().map(c -> c.getName().toLowerCase()).collect(Collectors.joining(" ")));
                    definitions.add(sbi.toString());
                }
                if (HAS_FONT_SUPPORT && component.getFontRaw() != null) {
                    definitions.add(FONT_PREFIX + component.getFontRaw());
                }
                if (component.getClickEvent() != null) {
                    if (preferSimpleEvents() && component.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                        definitions.add(component.getClickEvent().getValue());
                    } else {
                        definitions.add(component.getClickEvent().getAction().toString().toLowerCase() + "=" + component.getClickEvent().getValue());
                    }
                }
                if (component.getHoverEvent() != null) {
                    StringBuilder sbi = new StringBuilder();
                    if (preferSimpleEvents() && component.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT &&
                            (component.getClickEvent() == null || component.getClickEvent().getAction() != ClickEvent.Action.OPEN_URL)) {
                        sbi.append(HOVER_PREFIX);
                    } else {
                        sbi.append(component.getHoverEvent().getAction().toString().toLowerCase()).append('=');
                    }
                    if (HAS_HOVER_CONTENT_SUPPORT) {
                        sbi.append(copy().stringify(component.getHoverEvent().getContents()));
                    } else if (HOVER_GET_VALUE != null) {
                        try {
                            sbi.append(copy().stringify((BaseComponent[]) HOVER_GET_VALUE.invoke(component.getHoverEvent())));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                    definitions.add(sbi.toString());
                }
                sb.append(definitions.stream().collect(Collectors.joining(" ")));
                sb.append(')');
            } else {
                appendFormatSuffix(sb, component);
            }
        }
        return sb.toString();
    }

    private StringBuilder stringify(List<Content> contents) {
        StringBuilder sb = new StringBuilder();
        for (Content content : contents) {
            if (content instanceof Text) {
                Object value = ((Text) content).getValue();
                if (value instanceof BaseComponent[]) {
                    sb.append(stringify((BaseComponent[]) value));
                } else {
                    sb.append(value);
                }
            } else if (content instanceof Entity) {
                Entity contentEntity = (Entity) content;
                sb.append(contentEntity.getId());
                if (contentEntity.getType() != null) {
                    sb.append(":").append(contentEntity.getType());
                }
                if (contentEntity.getName() != null) {
                    sb.append(" ").append(stringify(contentEntity.getName()));
                }
            } else if (content instanceof Item) {
                Item contentItem = (Item) content;
                sb.append(contentItem.getId());
                if (contentItem.getCount() > 0) {
                    sb.append("*").append(contentItem.getCount());
                }
                if (contentItem.getTag() != null) {
                    sb.append(" ").append(contentItem.getTag().getNbt());
                }
            }
        }
        return sb;
    }

    private void appendText(StringBuilder sb, BaseComponent component) {
        if (component instanceof TextComponent) {
            sb.append(((TextComponent) component).getText());
        } else {
            throw new UnsupportedOperationException("Cannot stringify " + component.getClass().getTypeName() + " yet! Only TextComponents are supported right now. Sorry. :(");
        }
    }

    private void appendColor(StringBuilder sb, ChatColor color) {
        if (this.color != color) {
            this.color = color;
            if (useLegacyColors()) {
                sb.append(colorChar()).append(color.toString().substring(1));
            } else {
                sb.append(colorChar()).append(color.getName()).append(colorChar());
            }
        }
    }

    private void appendFormat(StringBuilder sb, BaseComponent component) {
        Set<ChatColor> formats = Util.getFormats(component, true);
        if (!formats.containsAll(this.formats)) {
            if (useLegacyFormatting()) {
                sb.append(colorChar()).append(ChatColor.RESET.toString().charAt(1));
            } else {
                Deque<ChatColor> formatDeque = new ArrayDeque<>(this.formats);
                while (!formatDeque.isEmpty()) {
                    ChatColor format = formatDeque.pollLast();
                    if (!formats.contains(format)) {
                        sb.append(MineDown.getFormatString(format));
                    }
                }
            }
        } else {
            formats.removeAll(this.formats);
        }
        for (ChatColor format : formats) {
            if (useLegacyFormatting()) {
                sb.append(colorChar()).append(format.toString().charAt(1));
            } else {
                sb.append(MineDown.getFormatString(format));
            }
        }
        this.formats.clear();
        this.formats.addAll(formats);
    }

    private void appendFormatSuffix(StringBuilder sb, BaseComponent component) {
        if (!useLegacyFormatting()) {
            Set<ChatColor> formats = Util.getFormats(component, true);
            for (ChatColor format : formats) {
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
        MineDownStringifier copy = new MineDownStringifier();
        useLegacyColors(from.useLegacyColors());
        useLegacyFormatting(from.useLegacyFormatting());
        preferSimpleEvents(from.preferSimpleEvents());
        formattingInEventDefinition(from.formattingInEventDefinition());
        colorInEventDefinition(from.colorInEventDefinition());
        colorChar(from.colorChar());
        return copy;
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
