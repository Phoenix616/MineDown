package de.themoep.minedown.tests;

import de.themoep.minedown.MineDown;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {
    
    private void parse(String mineDownString, String... replacements) {
        System.out.print(mineDownString + "\n" + ComponentSerializer.toString(MineDown.parse(mineDownString, replacements)) + "\n");
    }

    private void parse(String mineDownString, String placeholder, BaseComponent[] replacement) {
        System.out.print(mineDownString + "\n" + ComponentSerializer.toString(new MineDown(mineDownString).replace(placeholder, replacement).toComponent()) + "\n");
    }

    private void parse(String mineDownString, String placeholder1, BaseComponent[] replacement1, String placeholder2, BaseComponent[] replacement2) {
        System.out.print(mineDownString + "\n" + ComponentSerializer.toString(new MineDown(mineDownString)
                .replace(placeholder1, replacement1)
                .replace(placeholder2, replacement2)
                .toComponent()) + "\n");
    }
    
    @Test
    public void testParsing() {
        System.out.println("testParsing");
        Assertions.assertAll(
                () -> parse("##&eTest## [&blue&b__this__](https://example.com **Hover ??text??**) ~~string~~!"),
                () -> parse("##&eTest## [__this \\&6 \\that__](blue /example command hover=**Hover ??text??**) ~~string~~!"),
                () -> parse("[TestLink](https://example.com) [Testcommand](/command test  )"),
                () -> parse("&b&lTest [this](color=green format=bold,italic https://example.com Hover & text) string!"),
                () -> parse("&bTest [this](color=green format=bold,italic suggest_command=/example command hover=Hover text) string!"),
                () -> parse("&b[Test] [this](6 bold italic https://example.com) &as&bt&cr&di&en&5g&7!"),
                () -> parse("&bTest [[this]](https://example.com)!"),
                () -> parse("&bTest [**[this]**](https://example.com)!"),
                () -> parse("&lbold &oitalic &0not bold or italic but black!"),
                () -> parse("&cRed &land bold!"),
                () -> parse("&bTest \n&cexample.com &rstring!"),
                () -> parse("&bTest \n&chttps://example.com &rstring!"),
                () -> parse("&bTest &chttps://example.com/test?t=2&d002=da0s#d2q &rstring!"),
                () -> parse("Test inner escaping [\\]](gray)")
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> MineDown.parse("&bTest [this](color=green format=green,bold,italic https://example.com) shit!"));
    }

    @Test
    public void testParseHexColors() {
        System.out.println("testParsing");
        Assertions.assertAll(
                () -> parse("##&eTest## [&#593&b__this__](Text) ~~string~~!"),
                () -> parse("##&eTest## [&#593593&b__this__](Text) ~~string~~!"),
                () -> parse("##&eTest## [__this \\&6 \\that__](#290329 /example command hover=**Hover ??text??**) ~~string~~!"),
                () -> parse("##&eTest## [__this \\&6 \\that__](color=#290329 /example command hover=**Hover ??text??**) ~~string~~!")
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> MineDown.parse("&bTest [this](color=green format=green,bold,italic https://example.com) shit!"));
    }
    
    @Test
    public void testReplacing() {
        System.out.println("testReplacing");
        Assertions.assertAll(
                () -> parse("&6Test __%placeholder%__&r =D", "placeholder", "value"),
                () -> parse("&6Test __%PlaceHolder%__&r =D", "placeholder", "**value**"),
                () -> parse("&6Test __%placeholder%__&r =D", "PlaceHolder", "&5value"),
                () -> parse("&6Test __%placeholder%__&r =D", "placeholder", "[value](https://example.com)")
        );
    }

    @Test
    public void testComponentReplacing() {
        System.out.println("testComponentReplacing");
        Assertions.assertAll(
                () -> parse("&6Test No placeholder =D", "placeholder", new MineDown("value").toComponent()),
                () -> parse("&6Test __%placeholder%__&r =D", "placeholder", new MineDown("**value**").toComponent()),
                () -> parse("&6Test __%PlaceHolder%__&r %placeholder% =D", "placeholder", new MineDown("&5value").toComponent()),
                () -> parse("&6Test __%placeholder1%__&r %placeholder2%=D",
                        "PlaceHolder1", new MineDown("[replacement1](https://example.com)").toComponent(),
                        "placeholder2", new MineDown("[replacement2](https://example.com)").toComponent()
                )
        );
    }

    @Test
    public void testParseContentHover() {
        System.out.println("testParsing");
        Assertions.assertAll(
                () -> parse("[this](show_text=&bHi)"),
                () -> parse("[this](show_item=stone*3 {Name:\"Ein Stein\"}"),
                () -> parse("[this](show_item=minecraft:stone*3 {Name:\"Ein Stein\"}"),
                () -> parse("[this](show_entity=coool-uuid-lol:cow &bEine Kuh)"),
                () -> parse("[this](show_entity=coool-uuid-lol:minecraft:cow &bEine Kuh)")
        );
    }
}
