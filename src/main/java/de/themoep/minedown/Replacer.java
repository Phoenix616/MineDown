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
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The replacer, 'duh
 */
@Getter
@Setter
public class Replacer {
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
