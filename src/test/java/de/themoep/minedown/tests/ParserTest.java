package de.themoep.minedown.tests;

import de.themoep.minedown.MineDown;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {
    
    private void parse(String mineDownString, String target, String... replacements) {
        String parsed = ComponentSerializer.toString(MineDown.parse(mineDownString, replacements));
        System.out.print(mineDownString + "\n" + parsed + "\n");
        if (!parsed.equals(target)) {
            throw new IllegalArgumentException("Parsed string '" + mineDownString + "' does not equal target");
        }
    }

    private void parse(String mineDownString, String placeholder, BaseComponent[] replacement, String target) {
        String parsed = ComponentSerializer.toString(new MineDown(mineDownString).replace(placeholder, replacement).toComponent());
        System.out.print(mineDownString + "\n" + parsed + "\n");
        if (!parsed.equals(target)) {
            throw new IllegalArgumentException("Parsed string '" + mineDownString + "' does not equal target");
        }
    }

    private void parse(String mineDownString, String placeholder1, BaseComponent[] replacement1, String placeholder2, BaseComponent[] replacement2, String target) {
        String parsed = ComponentSerializer.toString(new MineDown(mineDownString)
                .replace(placeholder1, replacement1)
                .replace(placeholder2, replacement2)
                .toComponent());
        System.out.print(mineDownString + "\n" + parsed + "\n");
        if (!parsed.equals(target)) {
            throw new IllegalArgumentException("Parsed string '" + mineDownString + "' does not equal target");
        }
    }
    
    @Test
    public void testParsing() {
        System.out.println("testParsing");
        Assertions.assertAll(
                () -> parse("##&eTest## [&blue&b__this__](https://example.com **Hover ??text??**) ~~string~~!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\"},{\"text\":\" \"},{\"text\":\"b\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\"this\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\"\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
                ),
                () -> parse("##&eTest## [__this \\&6 \\that__](blue /example command hover=**Hover ??text??**) ~~string~~!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\"},{\"text\":\" \"},{\"text\":\"\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\"this \\u00266 that\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\"\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
                ),
                () -> parse("[TestLink](https://example.com) [Testcommand](/command test  )",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"TestLink\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]]}},{\"text\":\" \"},{\"text\":\"Testcommand\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/command test \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"run command\",\"color\":\"blue\"},{\"text\":\" /command test \",\"color\":\"white\"}]]}},{\"text\":\"\"}]}"
                ),
                () -> parse("&b&lTest [this](color=green format=bold,italic https://example.com Hover & text) string!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"aqua\",\"bold\":true},{\"text\":\"this\",\"color\":\"green\",\"bold\":true,\"italic\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Hover \\u0026 text\"}]]}},{\"text\":\" string!\",\"color\":\"aqua\",\"bold\":true}]}"),
                () -> parse("&bTest [this](color=green format=bold,italic suggest_command=/example command hover=Hover text) string!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"aqua\"},{\"text\":\"this\",\"color\":\"green\",\"bold\":true,\"italic\":true,\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Hover text\"}]]}},{\"text\":\" string!\",\"color\":\"aqua\"}]}"),
                () -> parse("&b[Test] [this](6 bold italic https://example.com) &as&bt&cr&di&en&5g&7!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"[Test] \",\"color\":\"aqua\"},{\"text\":\"this\",\"color\":\"gold\",\"bold\":true,\"italic\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]]}},{\"text\":\" \",\"color\":\"aqua\"},{\"text\":\"s\",\"color\":\"green\"},{\"text\":\"t\",\"color\":\"aqua\"},{\"text\":\"r\",\"color\":\"red\"},{\"text\":\"i\",\"color\":\"light_purple\"},{\"text\":\"n\",\"color\":\"yellow\"},{\"text\":\"g\",\"color\":\"dark_purple\"},{\"text\":\"!\",\"color\":\"gray\"}]}"
                ),
                () -> parse("&bTest [[this]](https://example.com)!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"aqua\"},{\"text\":\"[this]\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]]}},{\"text\":\"!\",\"color\":\"aqua\"}]}"
                ),
                () -> parse("&bTest [**[this]**](https://example.com)!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]]}},{\"text\":\"[this]\",\"color\":\"aqua\",\"bold\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]]}},{\"text\":\"\",\"color\":\"aqua\",\"bold\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]]}},{\"text\":\"!\",\"color\":\"aqua\"}]}"
                ),
                () -> parse("&lbold &oitalic &0not bold or italic but black!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"bold \",\"bold\":true},{\"text\":\"italic \",\"bold\":true,\"italic\":true},{\"text\":\"not bold or italic but black!\",\"color\":\"black\"}]}"
                ),
                () -> parse("&cRed &land bold!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Red \",\"color\":\"red\"},{\"text\":\"and bold!\",\"color\":\"red\",\"bold\":true}]}"
                ),
                () -> parse("&bTest \n&cexample.com &rstring!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \\n\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"red\"},{\"text\":\"example.com\",\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Click to open url\"}]]}},{\"text\":\" \",\"color\":\"red\"},{\"text\":\"string!\"}]}"
                ),
                () -> parse("&bTest \n&chttps://example.com &rstring!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \\n\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"red\"},{\"text\":\"https://example.com\",\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Click to open url\"}]]}},{\"text\":\" \",\"color\":\"red\"},{\"text\":\"string!\"}]}"
                ),
                () -> parse("&bTest &chttps://example.com/test?t=2&d002=da0s#d2q &rstring!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"red\"},{\"text\":\"https://example.com/test?t\\u003d2\\u0026d002\\u003dda0s#d2q\",\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com/test?t\\u003d2\\u0026d002\\u003dda0s#d2q\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Click to open url\"}]]}},{\"text\":\" \",\"color\":\"red\"},{\"text\":\"string!\"}]}"
                ),
                () -> parse(
                        "Test inner escaping [\\]](gray)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test inner escaping \"},{\"text\":\"]\",\"color\":\"gray\"},{\"text\":\"\"}]}"
                ),
                () -> parse(
                        "[Test insertion](insert={text to insert} color=red)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test insertion\",\"color\":\"red\",\"insertion\":\"text to insert\"},{\"text\":\"\"}]}"
                )
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> MineDown.parse("&bTest [this](color=green format=green,bold,italic https://example.com) shit!"));
    }

    @Test
    public void testParseHexColors() {
        System.out.println("testParseHexColors");
        Assertions.assertAll(
                () -> parse("##&eTest## [&#593&b__this__](Text) ~~string~~!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\"},{\"text\":\" \"},{\"text\":\"b\",\"color\":\"#559933\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Text\"}]]}},{\"text\":\"this\",\"color\":\"#559933\",\"underlined\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Text\"}]]}},{\"text\":\"\",\"color\":\"#559933\",\"underlined\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Text\"}]]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
                ),
                () -> parse("##&eTest## [&#593593&b__this__](Text) ~~string~~!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\"},{\"text\":\" \"},{\"text\":\"b\",\"color\":\"#593593\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Text\"}]]}},{\"text\":\"this\",\"color\":\"#593593\",\"underlined\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Text\"}]]}},{\"text\":\"\",\"color\":\"#593593\",\"underlined\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Text\"}]]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
                ),
                () -> parse("##&eTest## [__this \\&6 \\that__](#290329 /example command hover=**Hover ??text??**) ~~string~~!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\"},{\"text\":\" \"},{\"text\":\"\",\"color\":\"#290329\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\"this \\u00266 that\",\"color\":\"#290329\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\"\",\"color\":\"#290329\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
                ),
                () -> parse("##&eTest## [__this \\&6 \\that__](color=#290329 /example command hover=**Hover ??text??**) ~~string~~!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\"},{\"text\":\" \"},{\"text\":\"\",\"color\":\"#290329\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\"this \\u00266 that\",\"color\":\"#290329\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\"\",\"color\":\"#290329\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
                )
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> MineDown.parse("&bTest [this](color=green format=green,bold,italic https://example.com) shit!"));
    }

    @Test
    public void testParseLegacyHexColors() {
        System.out.println("testParseLegacyHexColors");
        Assertions.assertAll(
                () -> parse("§x§5§9§3§5§9§3__Test__",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\",\"color\":\"#593593\"},{\"text\":\"Test\",\"color\":\"#593593\",\"underlined\":true},{\"text\":\"\",\"color\":\"#593593\"}]}"
                )
        );
    }
    
    @Test
    public void testReplacing() {
        System.out.println("testReplacing");
        Assertions.assertAll(
                () -> parse("&6Test __%placeholder%__&r =D",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"gold\"},{\"text\":\"value\",\"color\":\"gold\",\"underlined\":true},{\"text\":\"\",\"color\":\"gold\"},{\"text\":\" \\u003dD\"}]}",
                        "placeholder", "value"),
                () -> parse("&6Test __%PlaceHolder%__&r =D",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"gold\"},{\"text\":\"**value**\",\"color\":\"gold\",\"underlined\":true},{\"text\":\"\",\"color\":\"gold\"},{\"text\":\" \\u003dD\"}]}",
                        "placeholder", "**value**"),
                () -> parse("&6Test __%placeholder%__&r =D",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"gold\"},{\"text\":\"\\u00265value\",\"color\":\"gold\",\"underlined\":true},{\"text\":\"\",\"color\":\"gold\"},{\"text\":\" \\u003dD\"}]}",
                        "PlaceHolder", "&5value"),
                () -> parse("&6Test __%placeholder%__&r =D",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"gold\"},{\"text\":\"[value](https://example.com)\",\"color\":\"gold\",\"underlined\":true},{\"text\":\"\",\"color\":\"gold\"},{\"text\":\" \\u003dD\"}]}", "" +
                                "placeholder", "[value](https://example.com)")
        );
    }

    @Test
    public void testComponentReplacing() {
        System.out.println("testComponentReplacing");
        Assertions.assertAll(
                () -> parse("&6Test No placeholder =D", "placeholder", new MineDown("value").toComponent(),
                        "{\"text\":\"Test No placeholder \\u003dD\",\"color\":\"gold\"}"
                ),
                () -> parse("&6Test __%placeholder%__&r =D", "placeholder", new MineDown("**value**").toComponent(),
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"gold\",\"underlined\":true,\"extra\":[{\"text\":\"\"},{\"text\":\"value\",\"bold\":true},{\"text\":\"\"}]},{\"text\":\"\",\"color\":\"gold\",\"underlined\":true},{\"text\":\"\",\"color\":\"gold\"},{\"text\":\" \\u003dD\"}]}"
                ),
                () -> parse("&6Test __%PlaceHolder%__&r %placeholder% =D", "placeholder", new MineDown("&5value").toComponent(),
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"gold\",\"underlined\":true,\"extra\":[{\"text\":\"value\",\"color\":\"dark_purple\"}]},{\"text\":\"\",\"color\":\"gold\",\"underlined\":true},{\"text\":\"\",\"color\":\"gold\"},{\"text\":\" \",\"extra\":[{\"text\":\"value\",\"color\":\"dark_purple\"}]},{\"text\":\" \\u003dD\"}]}"
                ),
                () -> parse("&6Test __%placeholder1%__&r %placeholder2%=D",
                        "PlaceHolder1",new MineDown("[replacement1](https://example.com)").toComponent(),
                        "placeholder2", new MineDown("[replacement2](https://example.com)").toComponent(),
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"gold\",\"underlined\":true,\"extra\":[{\"text\":\"\"},{\"text\":\"replacement1\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]]}},{\"text\":\"\"}]},{\"text\":\"\",\"color\":\"gold\",\"underlined\":true},{\"text\":\"\",\"color\":\"gold\"},{\"text\":\" \",\"extra\":[{\"text\":\"\"},{\"text\":\"replacement2\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]]}},{\"text\":\"\"}]},{\"text\":\"\\u003dD\"}]}"
                )
        );
    }

    @Test
    public void testParseContentHover() {
        System.out.println("testParsing");
        Assertions.assertAll(
                () -> parse("[this](show_text=&bHi)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"this\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[[{\"text\":\"Hi\",\"color\":\"aqua\"}]]}},{\"text\":\"\"}]}"
                ),
                () -> parse("[this](show_item=stone*3 {Name:\"Ein Stein\"}",
                        "{\"text\":\"[this](show_item\\u003dstone*3 {Name:\\\"Ein Stein\\\"}\"}"
                ),
                () -> parse("[this](show_item=minecraft:stone*3 {Name:\"Ein Stein\"}",
                        "{\"text\":\"[this](show_item\\u003dminecraft:stone*3 {Name:\\\"Ein Stein\\\"}\"}"
                ),
                () -> parse("[this](show_entity=coool-uuid-lol:cow &bEine Kuh)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"this\",\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":[{\"type\":\"minecraft:cow\",\"id\":\"coool-uuid-lol\",\"name\":{\"text\":\"§f§bEine Kuh\"}}]}},{\"text\":\"\"}]}"
                ),
                () -> parse("[this](show_entity=coool-uuid-lol:minecraft:cow &bEine Kuh)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"this\",\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":[{\"type\":\"minecraft:cow\",\"id\":\"coool-uuid-lol\",\"name\":{\"text\":\"§f§bEine Kuh\"}}]}},{\"text\":\"\"}]}"
                )
        );
    }
}
