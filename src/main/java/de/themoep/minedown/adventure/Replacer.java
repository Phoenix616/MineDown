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
import net.kyori.adventure.text.BuildableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
    private final Map<String, Component> componentReplacements = new LinkedHashMap<>();

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
     * @param message      The Component to replace in
     * @param replacements The replacements, nth element is the placeholder, n+1th the value
     * @return A copy of the Component array with all the placeholders replaced
     */
    public static Component replaceIn(Component message, String... replacements) {
        return new Replacer().replace(replacements).replaceIn(message);
    }

    /**
     * Replace a certain placeholder with a component array in a component array.
     * This uses the % character as placeholder indicators (suffix and prefix)
     * @param message     The Component to replace in
     * @param placeholder The placeholder to replace
     * @param replacement The replacement components
     * @return A copy of the Component array with all the placeholders replaced
     */
    public static Component replaceIn(Component message, String placeholder, Component replacement) {
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
            Object any = replacements.values().stream().filter(Objects::nonNull).findAny().orElse(null);
            if (any instanceof String) {
                replacements().putAll((Map<String, String>) replacements);
            } else if (any instanceof Component) {
                componentReplacements().putAll((Map<String, Component>) replacements);
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
    public Replacer replace(String placeholder, Component replacement) {
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
     * Replace the placeholders in a component list
     * @param components The Component list to replace in
     * @return A copy of the array with the placeholders replaced
     */
    public List<Component> replaceIn(List<Component> components) {
        List<Component> replaced = new ArrayList<>();
        for (Component component : components) {
            replaced.add(replaceIn(component));
        }
        return replaced;
    }

    /**
     * Replace the placeholders in a component list
     * @param component The Component list to replace in
     * @return A copy of the array with the placeholders replaced
     */
    public Component replaceIn(Component component) {
        TextComponent.Builder builder = Component.text();

        if (component instanceof KeybindComponent) {
            component = ((KeybindComponent) component).keybind(replaceIn(((KeybindComponent) component).keybind()));
        }
        if (component instanceof TextComponent) {
            String replaced = replaceIn(((TextComponent) component).content());
            if (replaced.indexOf('§') != -1) {
                // replacement contain legacy code, parse to components and append them as children
                Component replacedComponent = LegacyComponentSerializer.legacySection().deserialize(replaced);
                component = ((TextComponent) component).content("");
                List<Component> children = new ArrayList<>();
                children.add(replacedComponent);
                children.addAll(component.children());
                component = component.children(children);
            } else {
                component = ((TextComponent) component).content(replaced);
            }
        }
        if (component instanceof TranslatableComponent) {
            component = ((TranslatableComponent) component).key(replaceIn(((TranslatableComponent) component).key()));
            component = ((TranslatableComponent) component).args(replaceIn(((TranslatableComponent) component).args()));
        }
        if (component.insertion() != null) {
            component = component.insertion(replaceIn(component.insertion()));
        }
        if (component.clickEvent() != null) {
            component = component.clickEvent(ClickEvent.clickEvent(
                    component.clickEvent().action(),
                    replaceIn(component.clickEvent().value())
            ));
        }
        if (component.hoverEvent() != null) {
            if (component.hoverEvent().action() == HoverEvent.Action.SHOW_TEXT) {
                component = component.hoverEvent(HoverEvent.showText(
                        replaceIn((Component) component.hoverEvent().value())
                ));
            } else if (component.hoverEvent().action() == HoverEvent.Action.SHOW_ENTITY) {
                HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity) component.hoverEvent().value();
                component = component.hoverEvent(HoverEvent.showEntity(
                        HoverEvent.ShowEntity.of(
                                Key.key(replaceIn(showEntity.type().asString())),
                                showEntity.id(),
                                replaceIn(showEntity.name())
                        )
                ));
            } else if (component.hoverEvent().action() == HoverEvent.Action.SHOW_ITEM) {
                HoverEvent.ShowItem showItem = (HoverEvent.ShowItem) component.hoverEvent().value();
                component = component.hoverEvent(HoverEvent.showItem(
                        HoverEvent.ShowItem.of(
                                Key.key(replaceIn(showItem.item().asString())),
                                showItem.count(),
                                showItem.nbt() != null ? BinaryTagHolder.of(replaceIn(showItem.nbt().string())) : null
                        )
                ));
            }
        }

        component = component.children(replaceIn(component.children()));

        // Component replacements
        List<Component> replacedComponents = new ArrayList<>();
        replacedComponents.add(component);

        for (Map.Entry<String, Component> replacement : componentReplacements().entrySet()) {
            List<Component> newReplacedComponents = new ArrayList<>();

            for (Component replaceComponent : replacedComponents) {
                if (replaceComponent instanceof TextComponent) {
                    TextComponent textComponent = (TextComponent) replaceComponent;
                    String placeHolder = placeholderPrefix()
                            + (ignorePlaceholderCase() ? replacement.getKey().toLowerCase(Locale.ROOT) : replacement.getKey())
                            + placeholderSuffix();
                    String text = ignorePlaceholderCase() ? textComponent.content().toLowerCase(Locale.ROOT) : textComponent.content();
                    int index = text.indexOf(placeHolder);
                    if (index > -1) {
                        while (true) {
                            ComponentBuilder<?, ?> startBuilder;
                            if (index > 0) {
                                startBuilder = Component.text().mergeStyle(textComponent);
                                ((TextComponent.Builder) startBuilder).content(textComponent.content().substring(0, index));
                                startBuilder.append(replacement.getValue());
                            } else if (replacement.getValue() instanceof BuildableComponent){
                                startBuilder = ((BuildableComponent<?, ?>) replacement.getValue()).toBuilder();
                                // Merge replacement style onto the component's to properly apply the replacement styles over the component ones
                                startBuilder.style(Style.style().merge(textComponent.style()).merge(replacement.getValue().style()).build());
                            } else {
                                startBuilder = Component.text().mergeStyle(textComponent);
                                startBuilder.append(replacement.getValue());
                            }
                            newReplacedComponents.add(startBuilder.build());

                            textComponent = textComponent.content(textComponent.content().substring(index + placeHolder.length()));
                            text = ignorePlaceholderCase() ? textComponent.content().toLowerCase(Locale.ROOT) : textComponent.content();

                            if (text.isEmpty() || (index = text.indexOf(placeHolder)) < 0) {
                                // No further placeholder in text, add rest to newReplacedComponents
                                newReplacedComponents.add(textComponent);
                                break;
                            }
                        }
                        continue;
                    }
                }

                // Nothing was replaced, just add it
                newReplacedComponents.add(replaceComponent);
            }
            replacedComponents = newReplacedComponents;
        }
        builder.append(replacedComponents);

        return builder.build();
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
    public Map<String, Component> componentReplacements() {
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
