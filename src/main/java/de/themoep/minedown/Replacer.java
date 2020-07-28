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

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class offers the ability to replace placeholders with values in strings and components.
 * It also lets you define which placeholders indicators (prefix and suffix) should be used.
 * By default these are the % character.
 */
public class Replacer {

    private static final boolean HAS_HOVER_CONTENT_SUPPORT = Util.hasMethod(HoverEvent.class, "getContents");
    private static final Method HOVER_GET_VALUE = Util.getMethod(HoverEvent.class, "getValue");

    /**
     * A cache of compiled replacement patterns
     */
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    /**
     * The creator of the patterns for the pattern cache
     */
    private static final Function<String, Pattern> PATTERN_CREATOR = p -> Pattern.compile(p, Pattern.LITERAL);

    /**
     * The map of placeholders with their string replacements
     */
    private final Map<String, String> replacements = new LinkedHashMap<>();

    /**
     * The map of placeholders with their component array replacements
     */
    private final Map<String, BaseComponent[]> componentReplacements = new LinkedHashMap<>();

    /**
     * The placeholder indicator's prefix character
     */
    private String placeholderPrefix = "%";

    /**
     * The placeholder indicator's suffix character
     */
    private String placeholderSuffix = "%";

    /**
     * Replace the placeholder no matter what the case of it is
     */
    private boolean ignorePlaceholderCase = true;

    /**
     * Replace certain placeholders with values in string.
     * This uses the % character as placeholder indicators (suffix and prefix)
     * @param message      The string to replace in
     * @param replacements The replacements, nth element is the placeholder, n+1th the value
     * @return The string with all the placeholders replaced
     */
    public static String replaceIn(String message, String... replacements) {
        return new Replacer().replace(replacements).replaceIn(message);
    }

    /**
     * Replace certain placeholders with values in a component array.
     * This uses the % character as placeholder indicators (suffix and prefix)
     * @param message      The BaseComponent array to replace in
     * @param replacements The replacements, nth element is the placeholder, n+1th the value
     * @return A copy of the BaseComponent array with all the placeholders replaced
     */
    public static BaseComponent[] replaceIn(BaseComponent[] message, String... replacements) {
        return new Replacer().replace(replacements).replaceIn(message);
    }

    /**
     * Replace a certain placeholder with a component array in a component array.
     * This uses the % character as placeholder indicators (suffix and prefix)
     * @param message     The BaseComponent array to replace in
     * @param placeholder The placeholder to replace
     * @param replacement The replacement components
     * @return A copy of the BaseComponent array with all the placeholders replaced
     */
    public static BaseComponent[] replaceIn(BaseComponent[] message, String placeholder, BaseComponent... replacement) {
        return new Replacer().replace(placeholder, replacement).replaceIn(message);
    }

    /**
     * Add an array with placeholders and values that should get replaced in the message
     * @param replacements The replacements, nth element is the placeholder, n+1th the value
     * @return The Replacer instance
     */
    public Replacer replace(String... replacements) {
        Util.validate(replacements.length % 2 == 0, "The replacement length has to be even, " +
                "mapping i % 2 == 0 to the placeholder and i % 2 = 1 to the placeholder's value");
        Map<String, String> replacementMap = new LinkedHashMap<>();
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            replacementMap.put(replacements[i], replacements[i + 1]);
        }
        return replace(replacementMap);
    }

    /**
     * Add a map with placeholders and values that should get replaced in the message
     * @param replacements The replacements mapped placeholder to value
     * @return The Replacer instance
     */
    public Replacer replace(Map<String, ?> replacements) {
        if (replacements != null && !replacements.isEmpty()) {
            Object any = replacements.values().stream().findAny().orElse(null);
            if (any instanceof String) {
                replacements().putAll((Map<String, String>) replacements);
            } else if (any != null && any.getClass().isArray() && BaseComponent.class.isAssignableFrom(any.getClass().getComponentType())) {
                componentReplacements().putAll((Map<String, BaseComponent[]>) replacements);
            } else {
                for (Map.Entry<String, ?> entry : replacements.entrySet()) {
                    replacements().put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        return this;
    }

    /**
     * Add a placeholder to component mapping that should get replaced in the message
     * @param placeholder The placeholder to replace
     * @param replacement The replacement components
     * @return The Replacer instance
     */
    public Replacer replace(String placeholder, BaseComponent... replacement) {
        componentReplacements().put(placeholder, replacement);
        return this;
    }

    /**
     * Set the placeholder indicator for both prefix and suffix
     * @param placeholderIndicator The character to use as a placeholder indicator
     * @return The Replacer instance
     */
    public Replacer placeholderIndicator(String placeholderIndicator) {
        placeholderPrefix(placeholderIndicator);
        placeholderSuffix(placeholderIndicator);
        return this;
    }

    /**
     * Replace the placeholders in a component array
     * @param components The BaseComponent array to replace in
     * @return A copy of the array with the placeholders replaced
     */
    public BaseComponent[] replaceIn(BaseComponent... components) {
        return replaceIn(Arrays.asList(components));
    }

    /**
     * Replace the placeholders in a component list
     * @param components The BaseComponent list to replace in
     * @return A copy of the array with the placeholders replaced
     */
    public BaseComponent[] replaceIn(List<BaseComponent> components) {
        List<BaseComponent> returnList = new ArrayList<>();
        // String replacements:
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
                if (HAS_HOVER_CONTENT_SUPPORT) {
                    component.setHoverEvent(new HoverEvent(
                            component.getHoverEvent().getAction(),
                            replaceInContents(component.getHoverEvent().getContents())
                    ));
                } else if (HOVER_GET_VALUE != null) {
                    try {
                        component.setHoverEvent(new HoverEvent(
                                component.getHoverEvent().getAction(),
                                replaceIn((BaseComponent[]) HOVER_GET_VALUE.invoke(component.getHoverEvent()))
                        ));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (component.getExtra() != null) {
                component.setExtra(Arrays.asList(replaceIn(component.getExtra())));
            }

            // Component replacements
            List<BaseComponent> replacedComponents = new ArrayList<>();
            replacedComponents.add(component);

            for (Map.Entry<String, BaseComponent[]> replacement : componentReplacements().entrySet()) {
                List<BaseComponent> newReplacedComponents = new ArrayList<>();

                for (BaseComponent replaceComponent : replacedComponents) {
                    if (replaceComponent instanceof TextComponent) {
                        TextComponent textComponent = (TextComponent) replaceComponent;
                        String placeHolder = placeholderPrefix()
                                + (ignorePlaceholderCase() ? replacement.getKey().toLowerCase(Locale.ROOT) : replacement.getKey())
                                + placeholderSuffix();
                        String text = ignorePlaceholderCase() ? textComponent.getText().toLowerCase(Locale.ROOT) : textComponent.getText();
                        int index = text.indexOf(placeHolder);
                        if (index > -1) {
                            do {
                                TextComponent startComponent = new TextComponent(textComponent);
                                if (index > 0) {
                                    startComponent.setText(textComponent.getText().substring(0, index));
                                } else {
                                    startComponent.setText("");
                                }
                                startComponent.setExtra(Arrays.asList(replacement.getValue()));
                                newReplacedComponents.add(startComponent);

                                if (index + placeHolder.length() < textComponent.getText().length()) {
                                    textComponent.setText(textComponent.getText().substring(index + placeHolder.length()));
                                } else {
                                    textComponent.setText("");
                                }
                                text = ignorePlaceholderCase() ? textComponent.getText().toLowerCase(Locale.ROOT) : textComponent.getText();
                                newReplacedComponents.add(textComponent);
                            } while (!text.isEmpty() && (index = text.indexOf(placeHolder)) > -1);
                            continue;
                        }
                    }

                    // Nothing was replaced, just add it
                    newReplacedComponents.add(replaceComponent);
                }
                replacedComponents = newReplacedComponents;
            }
            returnList.addAll(replacedComponents);
        }
        return returnList.toArray(new BaseComponent[0]);
    }

    private List<Content> replaceInContents(List<Content> contents) {
        List<Content> replacedContents = new ArrayList<>();
        for (Content content : contents) {
            if (content instanceof Text) {
                Object value = ((Text) content).getValue();
                if (value instanceof BaseComponent[]) {
                    replacedContents.add(new Text(replaceIn((BaseComponent[]) value)));
                } else if (value instanceof String) {
                    replacedContents.add(new Text(replaceIn((String) value)));
                } else {
                    throw new UnsupportedOperationException("Cannot replace in " + value.getClass() + "!");
                }
            } else if (content instanceof Entity) {
                Entity entity = (Entity) content;
                String id = replaceIn(entity.getId());
                String type;
                if (entity.getType() != null) {
                    type = replaceIn(entity.getType());
                } else {
                    type = "minecraft:pig"; // Meh
                }
                BaseComponent name = null;
                if (entity.getName() != null) {
                    name = new TextComponent(replaceIn(TextComponent.toLegacyText(entity.getName())));
                }
                replacedContents.add(new Entity(type, id, name));
            } else if (content instanceof Item) {
                Item item = (Item) content;
                String id = replaceIn(item.getId());
                ItemTag itemTag = item.getTag() != null ? ItemTag.ofNbt(replaceIn(item.getTag().getNbt())) : null;
                replacedContents.add(new Item(id, item.getCount(), itemTag));
            } else {
                replacedContents.add(content); // TODO: Find a good way to clone this
            }
        }
        return replacedContents;
    }

    /**
     * Replace the placeholders in a string. Does not replace component replacements!
     * @param string The String list to replace in
     * @return The string with the placeholders replaced
     */
    public String replaceIn(String string) {
        for (Map.Entry<String, String> replacement : replacements().entrySet()) {
            String replValue = replacement.getValue() != null ? replacement.getValue() : "null";
            if (ignorePlaceholderCase()) {
                String placeholder = placeholderPrefix() + replacement.getKey().toLowerCase(Locale.ROOT) + placeholderSuffix();
                int nextStart = 0;
                int startIndex;
                while (nextStart < string.length() && (startIndex = string.toLowerCase(Locale.ROOT).indexOf(placeholder, nextStart)) > -1) {
                    nextStart = startIndex + replValue.length();
                    string = string.substring(0, startIndex) + replValue + string.substring(startIndex + placeholder.length());
                }
            } else {
                String placeholder = placeholderPrefix() + replacement.getKey() + placeholderSuffix();
                Pattern pattern = PATTERN_CACHE.computeIfAbsent(placeholder, PATTERN_CREATOR);
                string = pattern.matcher(string).replaceAll(Matcher.quoteReplacement(replValue));
            }
        }
        return string;
    }

    /**
     * Create a copy of this Replacer
     * @return A copy of this Replacer
     */
    public Replacer copy() {
        return new Replacer().copy(this);
    }

    /**
     * Copy all the values of another Replacer
     * @param from The replacer to copy
     * @return The Replacer instance
     */
    public Replacer copy(Replacer from) {
        replacements().clear();
        replacements().putAll(from.replacements());
        componentReplacements().clear();
        componentReplacements().putAll(from.componentReplacements());
        placeholderPrefix(from.placeholderPrefix());
        placeholderSuffix(from.placeholderSuffix());
        return this;
    }

    /**
     * Get the map of placeholders with their string replacements
     * @return the replacement map
     */
    public Map<String, String> replacements() {
        return this.replacements;
    }

    /**
     * Get the map of placeholders with their component array replacements
     * @return the replacement map
     */
    public Map<String, BaseComponent[]> componentReplacements() {
        return this.componentReplacements;
    }

    /**
     * Get the placeholder indicator's prefix string
     * @return the prefix characters
     */
    public String placeholderPrefix() {
        return this.placeholderPrefix;
    }

    /**
     * Set the placeholder indicator's prefix string
     * @param placeholderPrefix The placeholder prefix string
     * @return the instance of this Replacer
     */
    public Replacer placeholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
        return this;
    }

    /**
     * Get the placeholder indicator's suffix string
     * @return the suffix characters
     */
    public String placeholderSuffix() {
        return this.placeholderSuffix;
    }

    /**
     * Set the placeholder indicator's suffix string
     * @param placeholderSuffix The placeholder suffix string
     * @return the instance of this Replacer
     */
    public Replacer placeholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
        return this;
    }

    /**
     * Replace the placeholder no matter what the case of it is
     * @return whether or not to ignore the placeholder case (Default: true)
     */
    public boolean ignorePlaceholderCase() {
        return this.ignorePlaceholderCase;
    }

    /**
     * Set whether or not the placeholder should be replaced no matter what the case of it is
     * @param ignorePlaceholderCase Whether or not to ignore the case in placeholders (Default: true)
     * @return the instance of this Replacer
     */
    public Replacer ignorePlaceholderCase(boolean ignorePlaceholderCase) {
        this.ignorePlaceholderCase = ignorePlaceholderCase;
        return this;
    }
}
