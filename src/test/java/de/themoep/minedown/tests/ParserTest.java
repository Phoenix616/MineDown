package de.themoep.minedown.tests;

import de.themoep.minedown.MineDown;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {
    
    private void parse(String mineDownString, String... replacements) {
        System.out.print(mineDownString + "\n" + ComponentSerializer.toString(MineDown.parse(mineDownString, replacements)) + "\n");
    }
    
    @Test
    public void testParsing() {
        Assertions.assertAll(
                () -> parse("##&eTest## [&blue&b__this__](https://example.com **Hover ??text??**) ~~string~~!"),
                () -> parse("##&eTest## [__this \\&6 \\that__](blue /example command hover=**Hover ??text??**) ~~string~~!"),
                () -> parse("[TestLink](https://example.com) [Testcommand](/command test  )"),
                () -> parse("&bTest [this](color=green format=bold,italic https://example.com Hover & text) string!"),
                () -> parse("&bTest [this](color=green format=bold,italic suggest_command=/example command hover=Hover text) string!"),
                () -> parse("&bTest [this](6 bold italic https://example.com) &as&bt&cr&di&en&5g&7!"),
                () -> parse("&bTest [[this]](https://example.com)!"),
                () -> parse("&bTest [**[this]**](https://example.com)!"),
                () -> parse("&lbold &oitalic &0not bold or italic but black!"),
                () -> parse("&cRed &land bold!"),
                () -> parse("&bTest &chttps://example.com &rstring!"),
                () -> parse("&bTest &chttps://example.com/test?t=2&d002=da0s#d2q &rstring!")
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> MineDown.parse("&bTest [this](color=green format=green,bold,italic https://example.com) shit!"));
    }
    
    @Test
    public void testReplacing() {
        Assertions.assertAll(
                () -> parse("&6Test __%placeholder%__&r =D", "placeholder", "value"),
                () -> parse("&6Test __%placeholder%__&r =D", "placeholder", "**value**"),
                () -> parse("&6Test __%placeholder%__&r =D", "placeholder", "&5value"),
                () -> parse("&6Test __%placeholder%__&r =D", "placeholder", "[value](https://example.com)")
        );
    }
}
