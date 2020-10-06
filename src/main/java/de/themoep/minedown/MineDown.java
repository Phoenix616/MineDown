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

import java.util.Map;

/**
 * <h2>MineDown</h2>
 * A MarkDown inspired markup for Minecraft chat components
 * <p>
 * This lets you convert string messages into chat components by using a custom mark up syntax
 * which is loosely based on MarkDown while still supporting legacy formatting codes.
 *
 * <table>
 * <caption><strong> Inline Formatting </strong></caption>
 * <tr><td> Color legacy  </td><td><code> &amp;6Text           </code></td><td> {@link ChatColor} codes </td></tr>
 * <tr><td> Color         </td><td><code> &amp;gold&amp;Text   </code></td><td> {@link ChatColor} codes </td></tr>
 * <tr><td> RGB Hex Color </td><td><code> &amp;ff00ff&amp;Text </code></td><td> Full hexadecimal format  </td></tr>
 * <tr><td> RGB Hex Color </td><td><code> &amp;f0f&amp;Text    </code></td><td> Short format (equivalent to long one)  </td></tr>
 * <tr><td> Bold          </td><td><code> **Text**             </code></td></tr>
 * <tr><td> Italic        </td><td><code> ##Text##             </code></td></tr>
 * <tr><td> Underlined    </td><td><code> __Text__             </code></td></tr>
 * <tr><td> Strikethrough </td><td><code> ~~Text~~             </code></td></tr>
 * <tr><td> Obfuscated    </td><td><code> ??Text??             </code></td></tr>
 * </table>
 *
 * <h3>Events</h3>
 * You can define click and hover events with the commonly used MarkDown link syntax.
 * <p>
 * <table>
 * <caption><strong> Simple Syntax </strong></caption>
 * <tr><td> General syntax                 </td><td><code> [Text](text-color text-formatting... link hover text) </code></td></tr>
 * <tr><td> Simple Link                    </td><td><code> [Text](https://example.com)                           </code></td></tr>
 * <tr><td> Simple Command                 </td><td><code> [Text](/command to run)                               </code></td></tr>
 * <tr><td> Link + Hover                   </td><td><code> [Text](https://example.com Hover Text)                </code></td></tr>
 * <tr><td> Text formatting + Link + Hover </td><td><code> [Text](blue underline https://example.com Hover Text) </code></td></tr>
 * </table>
 * <p>
 * <table>
 * <caption><strong> Advanced Syntax </strong></caption>
 * <tr><td> General syntax      </td><td><code> [Text](action=value)                 </code></td><td> {@link ClickEvent.Action}, {@link HoverEvent.Action} </td></tr>
 * <tr><td> Link                </td><td><code> [Text](open_url=https://example.com) </code></td></tr>
 * <tr><td> Color               </td><td><code> [Text](color=red)                    </code></td></tr>
 * <tr><td> RGB Hex Color       </td><td><code> [Text](color=#ff00ff)                </code></td><td> Full hexadecimal format </td></tr>
 * <tr><td> RGB Hex Color       </td><td><code> [Text](color=#f0f)                   </code></td><td> Short format (equivalent to long one) </td></tr>
 * <tr><td> Formatting          </td><td><code> [Text](format=underline,bold)        </code></td></tr>
 * <tr><td> Font                </td><td><code> [Text](format=underline,bold)        </code></td></tr>
 * <tr><td> Run Command         </td><td><code> [Text](run_command=/command string)  </code></td></tr>
 * <tr><td> Suggest Command     </td><td><code> [Text](suggest_command=/command)     </code></td></tr>
 * <tr><td> Simple Hover        </td><td><code> [Text](hover=Hover Text)             </code></td></tr>
 * <tr><td> Hover Text          </td><td><code> [Text](show_text=Hover Text)         </code></td></tr>
 * <tr><td> Hover Entity Info   </td><td><code> [Text](show_entity=uuid:pig Name)    </code></td></tr>
 * <tr><td> Hover Item Info     </td><td><code> [Text](show_item=stone*2 nbt...)     </code></td></tr>
 * </table>
 * <p>
 * All advanced settings can be chained/included in a event definition.
 * You can't however add multiple different colors or click and hover actions!
 */
public class MineDown {
    public static final String FONT_PREFIX = "font=";
    public static final String COLOR_PREFIX = "color=";
    public static final String FORMAT_PREFIX = "format=";
    public static final String HOVER_PREFIX = "hover=";
    public static final String INSERTION_PREFIX = "insert=";

    private String message;
    private final Replacer replacer = new Replacer();
    private final MineDownParser parser = new MineDownParser();
    private BaseComponent[] baseComponents = null;
    private boolean replaceFirst = false;
    
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
    public static String stringify(BaseComponent[] components) {
        return new MineDownStringifier().stringify(components);
    }
    
    /**
     * Parse and convert the message to the component
     * @return The parsed component message
     */
    public BaseComponent[] toComponent() {
        if (baseComponents() == null) {
            if (replaceFirst()) {
                baseComponents = parser().parse(replacer().replaceIn(message())).create();
            } else {
                baseComponents = replacer().replaceIn(parser().parse(message()).create());
            }
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
     * Set whether or not replacements should be replaced before or after the components are created.
     * When replacing first it will not replace any placeholders with component replacement values!
     * Default is after. (replaceFirst = false)
     * @param replaceFirst  Whether or not to replace first or parse first
     * @return              The MineDown instance
     */
    public MineDown replaceFirst(boolean replaceFirst) {
        reset();
        this.replaceFirst = replaceFirst;
        return this;
    }

    /**
     * Get whether or not replacements should be replaced before or after the components are created.
     * When replacing first it will not replace any placeholders with component replacement values!
     * Default is after. (replaceFirst = false)
     * @return Whether or not to replace first or parse first
     */
    public boolean replaceFirst() {
        return replaceFirst;
    }

    /**
     * Add an array with placeholders and values that should get replaced in the message
     * @param replacements  The replacements, nth element is the placeholder, n+1th the value
     * @return              The MineDown instance
     */
    public MineDown replace(String... replacements) {
        reset();
        replacer().replace(replacements);
        return this;
    }
    
    /**
     * Add a map with placeholders and values that should get replaced in the message
     * @param replacements  The replacements mapped placeholder to value
     * @return              The MineDown instance
     */
    public MineDown replace(Map<String, ?> replacements) {
        reset();
        replacer().replace(replacements);
        return this;
    }

    /**
     * Add a placeholder to component mapping that should get replaced in the message
     * @param placeholder   The placeholder to replace
     * @param replacement   The replacement components
     * @return              The Replacer instance
     */
    public MineDown replace(String placeholder, BaseComponent... replacement) {
        reset();
        replacer().replace(placeholder,replacement);
        return this;
    }
    
    /**
     * Set the placeholder indicator for both prefix and suffix
     * @param placeholderIndicator  The character to use as a placeholder indicator
     * @return                      The MineDown instance
     */
    public MineDown placeholderIndicator(String placeholderIndicator) {
        placeholderPrefix(placeholderIndicator);
        placeholderSuffix(placeholderIndicator);
        return this;
    }
    
    /**
     * Set the placeholder indicator's prefix character
     * @param placeholderPrefix     The character to use as the placeholder indicator's prefix
     * @return                      The MineDown instance
     */
    public MineDown placeholderPrefix(String placeholderPrefix) {
        reset();
        replacer().placeholderPrefix(placeholderPrefix);
        return this;
    }
    
    /**
     * Get the placeholder indicator's prefix character
     * @return The placeholder indicator's prefix character
     */
    public String placeholderPrefix() {
        return replacer().placeholderPrefix();
    }
    
    /**
     * Set the placeholder indicator's suffix character
     * @param placeholderSuffix     The character to use as the placeholder indicator's suffix
     * @return                      The MineDown instance
     */
    public MineDown placeholderSuffix(String placeholderSuffix) {
        reset();
        replacer().placeholderSuffix(placeholderSuffix);
        return this;
    }
    
    /**
     * Get the placeholder indicator's suffix character
     * @return The placeholder indicator's suffix character
     */
    public String placeholderSuffix() {
        return replacer().placeholderSuffix();
    }

    /**
     * Set whether or not the case of the placeholder should be ignored when replacing
     * @param ignorePlaceholderCase Whether or not to ignore the case of the placeholders
     * @return                      The MineDown instance
     */
    public MineDown ignorePlaceholderCase(boolean ignorePlaceholderCase) {
        reset();
        replacer().ignorePlaceholderCase(ignorePlaceholderCase);
        return this;
    }

    /**
     * Get whether or not the case of the placeholder should be ignored when replacing
     * @return Whether or not to ignore the case of the placeholders
     */
    public boolean ignorePlaceholderCase() {
        return replacer().ignorePlaceholderCase();
    }

    /**
     * Enable or disable the translation of legacy color codes
     * @param translateLegacyColors Whether or not to translate legacy color codes (Default: true)
     * @return                      The MineDown instance
     * @deprecated Use {@link #enable(MineDownParser.Option)} and {@link #disable(MineDownParser.Option)}
     */
    @Deprecated
    public MineDown translateLegacyColors(boolean translateLegacyColors) {
        reset();
        parser().translateLegacyColors(translateLegacyColors);
        return this;
    }

    /**
     * Detect urls in strings and add events to them? (Default: true)
     * @param enabled   Whether or not to detect URLs and add events to them
     * @return          The MineDown instance
     */
    public MineDown urlDetection(boolean enabled) {
        reset();
        parser().urlDetection(enabled);
        return this;
    }

    /**
     * Automatically add http to values of open_url when there doesn't exist any? (Default: true)
     * @param enabled   Whether or not to automatically add http when missing
     * @return          The MineDown instance
     */
    public MineDown autoAddUrlPrefix(boolean enabled) {
        reset();
        parser().autoAddUrlPrefix(enabled);
        return this;
    }

    /**
     * The text to display when hovering over an URL
     * @param text  The text to display when hovering over an URL
     * @return      The MineDown instance
     */
    public MineDown urlHoverText(String text) {
        reset();
        parser().urlHoverText(text);
        return this;
    }

    /**
     * Set the max width the hover text should have.
     * Minecraft itself will wrap after 60 characters.
     * Won't apply if the text already includes new lines.
     * @param hoverTextWidth   The url hover text length
     * @return                  The MineDown instance
     */
    public MineDown hoverTextWidth(int hoverTextWidth) {
        reset();
        parser().hoverTextWidth(hoverTextWidth);
        return this;
    }

    /**
     * Enable an option. Unfilter it if you filtered it before.
     * @param option    The option to enable
     * @return          The MineDown instance
     */
    public MineDown enable(MineDownParser.Option option) {
        reset();
        parser().enable(option);
        return this;
    }

    /**
     * Disable an option. Disabling an option will stop the parser from replacing
     * this option's chars in the string. Use {@link #filter(MineDownParser.Option)} to completely
     * remove the characters used by this option from the message instead.
     * @param option    The option to disable
     * @return          The MineDown instance
     */
    public MineDown disable(MineDownParser.Option option) {
        reset();
        parser().disable(option);
        return this;
    }

    /**
     * Filter an option. This completely removes the characters of this option from
     * the string ignoring whether the option is enabled or not.
     * @param option    The option to add to the filter
     * @return          The MineDown instance
     */
    public MineDown filter(MineDownParser.Option option) {
        reset();
        parser().filter(option);
        return this;
    }

    /**
     * Unfilter an option. Does not enable it!
     * @param option    The option to remove from the filter
     * @return          The MineDown instance
     */
    public MineDown unfilter(MineDownParser.Option option) {
        reset();
        parser().unfilter(option);
        return this;
    }
    
    /**
     * Set a special character to replace color codes by if translating legacy colors is enabled.
     * @param colorChar The character to use as a special color code. (Default: ampersand &amp;)
    * @return           The MineDown instance
     */
    public MineDown colorChar(char colorChar) {
        reset();
        parser().colorChar(colorChar);
        return this;
    }

    /**
     * Get the set message that is to be parsed
     * @return The to be parsed message
     */
    public String message() {
        return this.message;
    }

    /**
     * Set the message that is to be parsed
     * @param message The message to be parsed
     * @return The MineDown instance
     */
    public MineDown message(String message) {
        this.message = message;
        reset();
        return this;
    }

    /**
     * Get the replacer instance that is currently used
     * @return The currently used replacer instance
     */
    public Replacer replacer() {
        return this.replacer;
    }

    /**
     * Get the parser instance that is currently used
     * @return The currently used parser instance
     */
    public MineDownParser parser() {
        return this.parser;
    }

    protected BaseComponent[] baseComponents() {
        return this.baseComponents;
    }

    /**
     * Copy all MineDown settings to a new instance
     * @return The new MineDown instance with all settings copied
     */
    public MineDown copy() {
        return new MineDown(message()).copy(this);
    }

    /**
     * Copy all MineDown settings from another one
     * @param from  The MineDown to copy from
     * @return      This MineDown instance
     */
    public MineDown copy(MineDown from) {
        replacer().copy(from.replacer());
        parser().copy(from.parser());
        return this;
    }

    /**
     * Get the string that represents the format in MineDown
     * @param format    The format
     * @return          The MineDown string or an empty one if it's not a format
     */
    public static String getFormatString(ChatColor format) {
        if (format == ChatColor.BOLD) {
            return "**";
        } else if (format == ChatColor.ITALIC) {
            return "##";
        } else if (format == ChatColor.UNDERLINE) {
            return "__";
        } else if (format == ChatColor.STRIKETHROUGH) {
            return "~~";
        } else if (format == ChatColor.MAGIC) {
            return "??";
        }
        return "";
    }
    
    /**
     * Get the ChatColor format from a MineDown string
     * @param c The character
     * @return  The ChatColor of that format or <code>null</code> it none was found
     */
    public static ChatColor getFormatFromChar(char c) {
        switch (c) {
            case '~':
                return ChatColor.STRIKETHROUGH;
            case '_':
                return ChatColor.UNDERLINE;
            case '*':
                return ChatColor.BOLD;
            case '#':
                return ChatColor.ITALIC;
            case '?':
                return ChatColor.MAGIC;
        }
        return null;
    }

    /**
     * Escape all MineDown formatting in a string. This will escape backslashes too!
     * @param string    The string to escape in
     * @return          The string with formatting escaped
     */
    public static String escape(String string) {
        return new MineDown(string).parser().escape(string);
    }
}
