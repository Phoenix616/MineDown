package de.themoep.minedown.adventure.tests;

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

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringifyTest {
    
    private void stringify(Component component) {
        String stringified = MineDown.stringify(component);
        System.out.print(GsonComponentSerializer.gson().serialize(component)
                + "\n" + stringified);
        System.out.print("\n" + GsonComponentSerializer.gson().serialize(MineDown.parse(stringified))
                + "\n\n");
    }
    
    @Test
    public void testStringify() {
        Assertions.assertAll(
                () -> stringify(Component.text()
                        .append(Component.text("Test "))
                        .append(Component.text("link", NamedTextColor.BLUE, TextDecoration.UNDERLINED))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://example.com"))
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Hover text", NamedTextColor.BLUE)))
                        .append(Component.text(". Test Text."))
                        .build()),
                () -> stringify(Component.text()
                        .append(Component.text("Test ", NamedTextColor.BLUE, TextDecoration.UNDERLINED))
                        .append(Component.text("link")
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://example.com"))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Hover text", NamedTextColor.BLUE)))
                        )
                        .append(Component.text(". Test Text."))
                        .build()),
                () -> stringify(Component.text("Page Click").clickEvent(ClickEvent.changePage(42))),
                () -> stringify(Component.text("Binary Click").clickEvent(ClickEvent.custom(Key.key("test"), BinaryTagHolder.binaryTagHolder("test data")))),
                () -> stringify(Component.text("test").color(TextColor.color(0x11FF00))),
                () -> stringify(Component.text("Test shadow").shadowColor(ShadowColor.shadowColor(0x11FF0044))),
                () -> stringify(Component.text("Test named shadow").shadowColor(ShadowColor.shadowColor(1694455125))),
                () -> stringify(Component.text("Test short shadow").shadowColor(ShadowColor.shadowColor(1694433280))),
                () -> stringify(Component.text("Test insertion").insertion("Insert text")),
                () -> stringify(Component.translatable("test.translation")),
                () -> stringify(Component.translatable("test.translation", "fallback text")),
                () -> stringify(Component.translatable("test.translation", "fallback text",
                        Component.text("replacement text 1").color(NamedTextColor.BLUE),
                        Component.text("replacement text 2").color(NamedTextColor.YELLOW)
                ))
        );
    }
}
