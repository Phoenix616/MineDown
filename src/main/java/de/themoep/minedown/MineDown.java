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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

import java.util.Map;

/**
 * <h1>MineDown</h1>
 * A MarkDown inspired markup for Minecraft chat components
 * <p>
 * This lets you convert string messages into chat components by using a custom mark up syntax
 * which is loosely based on MarkDown while still supporting legacy formatting codes.
 *
 * <h2>Inline Formatting</h2>
 * <table>
 * <tr> Color legacy  <td></td><td> &6Text     </td><td> -> {@link ChatColor} codes </td></tr>
 * <tr> Color         <td></td><td> &gold&Text </td><td> -> {@link ChatColor} codes </td</tr>
 * <tr> Bold          <td></td><td> **Text**   </td></tr>
 * <tr> Italic        <td></td><td> ##Text##   </td></tr>
 * <tr> Underlined    <td></td><td> __Text__   </td></tr>
 * <tr> Strikethrough <td></td><td> ~~Text~~   </td></tr>
 * <tr> Obfuscated    <td></td><td> ??Text??   </td></tr>
 * </table>
 *
 * <h2>Events</h2>
 * You can define click and hover events with the commonly used MarkDown link syntax.
 *
 * <h3>Simple Syntax</h3>
 * <table>
 * <tr> General syntax                 <td></td><td> [Text](text-color text-formatting... link hover text) </td></tr>
 * <tr> Simple Link                    <td></td><td> [Text](https://example.com)                           </td></tr>
 * <tr> Link + Hover                   <td></td><td> [Text](https://example.com Hover Text)                </td></tr>
 * <tr> Text formatting + Link + Hover <td></td><td> [Text](blue underline https://example.com Hover Text) </td></tr>
 * </table>
 *
 * <h3>Advanced Syntax</h3>
 * <table>
 * <tr> General syntax <td></td><td> [Text](action=value)                 </td><td> -> {@link ClickEvent.Action}, {@link HoverEvent.Action} </td></tr>
 * <tr> Link           <td></td><td> [Text](open_url=https://example.com) </td></tr>
 * <tr> Color          <td></td><td> [Text](color=red)                    </td></tr>
 * <tr> Formatting     <td></td><td> [Text](format=underline,bold)        </td></tr>
 * <tr> Hover          <td></td><td> [Text](hover=Hover Text)             </td></tr>
 * <tr> Command        <td></td><td> [Text](run_command=/command string)  </td></tr>
 * </table>
 * All advanced settings can be chained/included in a event definition.
 * You can't however add multiple different colors or click and hover actions!
 */
@Getter(AccessLevel.PROTECTED)
public class MineDown {
    private final String message;
    private final Replacer replacer = new Replacer();
    private final MineDownParser parser = new MineDownParser();
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
     * @return The parsed component message
     */
    public BaseComponent[] toComponent() {
        if (baseComponents() == null) {
            baseComponents = new MineDownParser().parse(replacer().replaceIn(message())).create();
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
    public MineDown replace(Map<String, String> replacements) {
        reset();
        replacer().replace(replacements);
        return this;
    }
    
    /**
     * Set the placeholder indicator for both prefix and suffix
     * @param placeholderIndicator  The character to use as a placeholder indicator
     * @return                      The MineDown instance
     */
    public MineDown placeholderIndicator(char placeholderIndicator) {
        placeholderPrefix(placeholderIndicator);
        placeholderSuffix(placeholderIndicator);
        return this;
    }
    
    /**
     * Set the placeholder indicator's prefix character
     * @param placeholderPrefix     The character to use as the placeholder indicator's prefix
     * @return                      The MineDown instance
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
     * @param placeholderSuffix     The character to use as the placeholder indicator's suffix
     * @return                      The MineDown instance
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
     * @return                      The MineDown instance
     */
    public MineDown translateLegacyColors(boolean translateLegacyColors) {
        reset();
        parser().translateLegacyColors(translateLegacyColors);
        return this;
    }
    
    /**
     * Set a special character to replace color codes by if translating legacy colors is enabled.
     * @param colorChar The character to use as a special color code. (Default: &)
    * @return           The MineDown instance
     */
    public MineDown colorChar(char colorChar) {
        reset();
        parser().colorChar(colorChar);
        return this;
    }
    
}
