package de.themoep.minedown.tests;

import de.themoep.minedown.MineDown;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Color;

public class StringifyTest {
    
    private void stringify(BaseComponent[] components) {
        String stringified = MineDown.stringify(components);
        System.out.print(ComponentSerializer.toString(components) + "\n" + stringified + "\n" + ComponentSerializer.toString(MineDown.parse(stringified)) + "\n\n");
    }
    
    @Test
    public void testStringify() {
        Assertions.assertAll(
                () -> stringify(new ComponentBuilder("")
                        .append("Test ")
                        .append("link").underlined(true).color(ChatColor.BLUE)
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://example.com"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Hover text").color(ChatColor.BLUE).create()))
                        .append(". Test Text.").retain(ComponentBuilder.FormatRetention.NONE)
                        .create()),
                () -> stringify(new ComponentBuilder("")
                        .append("Test ").underlined(true).color(ChatColor.of(new Color(0, 255, 128)))
                        .append("link")
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://example.com"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Hover text").color(ChatColor.BLUE).create())))
                        .color(ChatColor.of(new Color(255, 0, 0)))
                        .append(". Test Text.").retain(ComponentBuilder.FormatRetention.NONE)
                        .create()),
                () -> stringify(new ComponentBuilder("")
                        .append("Entity")
                        .event(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new Entity("pig", "some-uuid", new TextComponent(new ComponentBuilder("Hover text").color(ChatColor.BLUE).create()))))
                        .color(ChatColor.of(new Color(255, 0, 0)))
                        .create()),
                () -> stringify(new ComponentBuilder("")
                        .append("Entity")
                        .event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item("stone", 3, ItemTag.ofNbt("{Name:\"Name\"}"))))
                        .color(ChatColor.of(new Color(255, 0, 0)))
                        .create())
        );
    }
}
