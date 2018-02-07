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
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Map;

/**
 * MineDown - A MarkDown inspired markup for Minecraft chat components
 * <p>
 * TODO:
 * PeL: Write stuff here
 * CeL: Seriously, this should contain information about how the syntax works and stuff
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
     * @return
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
    
}
