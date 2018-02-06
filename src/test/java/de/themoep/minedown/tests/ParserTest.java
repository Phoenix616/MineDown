package de.themoep.minedown.tests;

import de.themoep.minedown.MineDown;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {
    
    @Test
    public void testParsing() {
        Assertions.assertAll(() -> {
            parse("&bTest [this](color=green format=bold,italic https://example.com Hover text) string!");
            parse("&bTest [this](color=green format=bold,italic suggest_command=/example command hover=Hover text) string!");
            parse("&bTest [this](6 bold italic https://example.com) &as&bt&cr&di&en&5g&7!");
            parse("&bTest &chttps://example.com &rstring!");
            parse("&bTest &chttps://example.com/test?t=2&d002=da0s#d2q &rstring!");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> MineDown.parse("&bTest [this](color=green format=green,bold,italic https://example.com) shit!"));
    }
    
    private void parse(String mineDownString) {
        System.out.print(mineDownString + "\n" + ComponentSerializer.toString(MineDown.parse(mineDownString)) + "\n");
    }
}
