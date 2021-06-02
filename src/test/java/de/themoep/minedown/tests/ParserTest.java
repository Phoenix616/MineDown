package de.themoep.minedown.tests;

import de.themoep.minedown.MineDown;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {
    
    private void parse(String mineDownString, String target, String... replacements) {
        String parsed = ComponentSerializer.toString(MineDown.parse(mineDownString, replacements));
        if (!parsed.equals(target)) {
            throw new IllegalArgumentException("Parsed string '" + mineDownString + "' does not equal target\nGot:\n" + parsed + "\n Expected:\n" + target);
        }
    }

    private void parse(String mineDownString, String placeholder, BaseComponent[] replacement, String target) {
        String parsed = ComponentSerializer.toString(new MineDown(mineDownString).replace(placeholder, replacement).toComponent());
        if (!parsed.equals(target)) {
            throw new IllegalArgumentException("Parsed string '" + mineDownString + "' does not equal target\nGot:\n" + parsed + "\n Expected:\n" + target);
        }
    }

    private void parse(String mineDownString, String placeholder1, BaseComponent[] replacement1, String placeholder2, BaseComponent[] replacement2, String target) {
        String parsed = ComponentSerializer.toString(new MineDown(mineDownString)
                .replace(placeholder1, replacement1)
                .replace(placeholder2, replacement2)
                .toComponent());
        if (!parsed.equals(target)) {
            throw new IllegalArgumentException("Parsed string '" + mineDownString + "' does not equal target\nGot:\n" + parsed + "\n Expected:\n" + target);
        }
    }
    
    @Test
    public void testParsing() {
        System.out.println("testParsing");
        Assertions.assertAll(
                () -> parse("##&eTest## [&blue&b__this__](https://example.com **Hover ??text??**) ~~string~~!",
                        "{\"extra\":[{\"text\":\"\"},{\"italic\":true,\"color\":\"yellow\",\"text\":\"Test\"},{\"text\":\" \"},{\"color\":\"blue\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"b\"},{\"underlined\":true,\"color\":\"blue\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"this\"},{\"underlined\":true,\"color\":\"blue\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"\"},{\"text\":\" \"},{\"strikethrough\":true,\"text\":\"string\"},{\"text\":\"!\"}],\"text\":\"\"}"
                ),
                () -> parse("&e##Test## [__this \\&6 \\that__](blue /example command hover=**Hover ??text??**) ~~string~~!",
                        "{\"extra\":[{\"color\":\"yellow\",\"text\":\"\"},{\"italic\":true,\"color\":\"yellow\",\"text\":\"Test\"},{\"color\":\"yellow\",\"text\":\" \"},{\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"\"},{\"underlined\":true,\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"this \\u00266 that\"},{\"underlined\":true,\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"\"},{\"color\":\"yellow\",\"text\":\" \"},{\"strikethrough\":true,\"color\":\"yellow\",\"text\":\"string\"},{\"color\":\"yellow\",\"text\":\"!\"}],\"text\":\"\"}"
                ),
                () -> parse("[TestLink](https://example.com) [Testcommand](/command test  )",
                        "{\"extra\":[{\"text\":\"\"},{\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"color\":\"blue\",\"text\":\"open url\"},{\"color\":\"white\",\"text\":\" https://example.com\"}]},\"text\":\"TestLink\"},{\"text\":\" \"},{\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/command test \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"color\":\"blue\",\"text\":\"run command\"},{\"color\":\"white\",\"text\":\" /command test \"}]},\"text\":\"Testcommand\"},{\"text\":\"\"}],\"text\":\"\"}"
                ),
                () -> parse("&b&lTest [this](color=green format=bold,italic https://example.com Hover & text) string!",
                        "{\"extra\":[{\"bold\":true,\"color\":\"aqua\",\"text\":\"Test \"},{\"bold\":true,\"italic\":true,\"color\":\"green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Hover \\u0026 text\"}]},\"text\":\"this\"},{\"bold\":true,\"color\":\"aqua\",\"text\":\" string!\"}],\"text\":\"\"}"),
                () -> parse("&bTest [this](color=green format=bold,italic suggest_command=/example command hover=Hover text) string!",
                        "{\"extra\":[{\"color\":\"aqua\",\"text\":\"Test \"},{\"bold\":true,\"italic\":true,\"color\":\"green\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Hover text\"}]},\"text\":\"this\"},{\"color\":\"aqua\",\"text\":\" string!\"}],\"text\":\"\"}"),
                () -> parse("&b[Test] [this](6 bold italic https://example.com) &as&bt&cr&di&en&5g&7!",
                        "{\"extra\":[{\"color\":\"aqua\",\"text\":\"[Test] \"},{\"bold\":true,\"italic\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"color\":\"blue\",\"text\":\"open url\"},{\"color\":\"white\",\"text\":\" https://example.com\"}]},\"text\":\"this\"},{\"color\":\"aqua\",\"text\":\" \"},{\"color\":\"green\",\"text\":\"s\"},{\"color\":\"aqua\",\"text\":\"t\"},{\"color\":\"red\",\"text\":\"r\"},{\"color\":\"light_purple\",\"text\":\"i\"},{\"color\":\"yellow\",\"text\":\"n\"},{\"color\":\"dark_purple\",\"text\":\"g\"},{\"color\":\"gray\",\"text\":\"!\"}],\"text\":\"\"}"
                ),
                () -> parse("&bTest [[this]](https://example.com)!",
                        "{\"extra\":[{\"color\":\"aqua\",\"text\":\"Test \"},{\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"color\":\"blue\",\"text\":\"open url\"},{\"color\":\"white\",\"text\":\" https://example.com\"}]},\"text\":\"[this]\"},{\"color\":\"aqua\",\"text\":\"!\"}],\"text\":\"\"}"
                ),
                () -> parse("&bTest [**[this]**](https://example.com)!",
                        "{\"extra\":[{\"color\":\"aqua\",\"text\":\"Test \"},{\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"color\":\"blue\",\"text\":\"open url\"},{\"color\":\"white\",\"text\":\" https://example.com\"}]},\"text\":\"\"},{\"bold\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"color\":\"blue\",\"text\":\"open url\"},{\"color\":\"white\",\"text\":\" https://example.com\"}]},\"text\":\"[this]\"},{\"bold\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"color\":\"blue\",\"text\":\"open url\"},{\"color\":\"white\",\"text\":\" https://example.com\"}]},\"text\":\"\"},{\"color\":\"aqua\",\"text\":\"!\"}],\"text\":\"\"}"
                ),
                () -> parse("&lbold &oitalic &0not bold or italic but black!",
                        "{\"extra\":[{\"bold\":true,\"text\":\"bold \"},{\"bold\":true,\"italic\":true,\"text\":\"italic \"},{\"color\":\"black\",\"text\":\"not bold or italic but black!\"}],\"text\":\"\"}"
                ),
                () -> parse("&cRed &land bold!",
                        "{\"extra\":[{\"color\":\"red\",\"text\":\"Red \"},{\"bold\":true,\"color\":\"red\",\"text\":\"and bold!\"}],\"text\":\"\"}"
                ),
                () -> parse("&bTest \n&cexample.com &rstring!",
                        "{\"extra\":[{\"color\":\"aqua\",\"text\":\"Test \\n\"},{\"color\":\"red\",\"text\":\"\"},{\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Click to open url\"}]},\"text\":\"example.com\"},{\"color\":\"red\",\"text\":\" \"},{\"text\":\"string!\"}],\"text\":\"\"}"
                ),
                () -> parse("&bTest \n&chttps://example.com &rstring!",
                        "{\"extra\":[{\"color\":\"aqua\",\"text\":\"Test \\n\"},{\"color\":\"red\",\"text\":\"\"},{\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Click to open url\"}]},\"text\":\"https://example.com\"},{\"color\":\"red\",\"text\":\" \"},{\"text\":\"string!\"}],\"text\":\"\"}"
                ),
                () -> parse("&bTest &chttps://example.com/test?t=2&d002=da0s#d2q &rstring!",
                        "{\"extra\":[{\"color\":\"aqua\",\"text\":\"Test \"},{\"color\":\"red\",\"text\":\"\"},{\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com/test?t\\u003d2\\u0026d002\\u003dda0s#d2q\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Click to open url\"}]},\"text\":\"https://example.com/test?t\\u003d2\\u0026d002\\u003dda0s#d2q\"},{\"color\":\"red\",\"text\":\" \"},{\"text\":\"string!\"}],\"text\":\"\"}"
                ),
                () -> parse(
                        "Test inner escaping [\\]](gray)",
                        "{\"extra\":[{\"text\":\"Test inner escaping \"},{\"color\":\"gray\",\"text\":\"]\"},{\"text\":\"\"}],\"text\":\"\"}"
                ),
                () -> parse(
                        "[Test insertion](insert={text to insert} color=red)",
                        "{\"extra\":[{\"text\":\"\"},{\"color\":\"red\",\"insertion\":\"text to insert\",\"text\":\"Test insertion\"},{\"text\":\"\"}],\"text\":\"\"}"
                )
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> MineDown.parse("&bTest [this](color=green format=green,bold,italic https://example.com) shit!"));
    }

    @Test
    public void testParseHexColors() {
        System.out.println("testParseHexColors");
        Assertions.assertAll(
                () -> parse("##&eTest## [&#593&b__this__](Text) ~~string~~!",
                        "{\"extra\":[{\"text\":\"\"},{\"italic\":true,\"color\":\"yellow\",\"text\":\"Test\"},{\"text\":\" \"},{\"color\":\"#559933\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]},\"text\":\"b\"},{\"underlined\":true,\"color\":\"#559933\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]},\"text\":\"this\"},{\"underlined\":true,\"color\":\"#559933\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]},\"text\":\"\"},{\"text\":\" \"},{\"strikethrough\":true,\"text\":\"string\"},{\"text\":\"!\"}],\"text\":\"\"}"
                ),
                () -> parse("##&eTest## [&#593593&b__this__](Text) ~~string~~!",
                        "{\"extra\":[{\"text\":\"\"},{\"italic\":true,\"color\":\"yellow\",\"text\":\"Test\"},{\"text\":\" \"},{\"color\":\"#593593\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]},\"text\":\"b\"},{\"underlined\":true,\"color\":\"#593593\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]},\"text\":\"this\"},{\"underlined\":true,\"color\":\"#593593\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]},\"text\":\"\"},{\"text\":\" \"},{\"strikethrough\":true,\"text\":\"string\"},{\"text\":\"!\"}],\"text\":\"\"}"
                ),
                () -> parse("##&eTest## [__this \\&6 \\that__](#290329 /example command hover=**Hover ??text??**) ~~string~~!",
                        "{\"extra\":[{\"text\":\"\"},{\"italic\":true,\"color\":\"yellow\",\"text\":\"Test\"},{\"text\":\" \"},{\"color\":\"#290329\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"\"},{\"underlined\":true,\"color\":\"#290329\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"this \\u00266 that\"},{\"underlined\":true,\"color\":\"#290329\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"\"},{\"text\":\" \"},{\"strikethrough\":true,\"text\":\"string\"},{\"text\":\"!\"}],\"text\":\"\"}"
                ),
                () -> parse("##&eTest## [__this \\&6 \\that__](color=#290329 /example command hover=**Hover ??text??**) ~~string~~!",
                        "{\"extra\":[{\"text\":\"\"},{\"italic\":true,\"color\":\"yellow\",\"text\":\"Test\"},{\"text\":\" \"},{\"color\":\"#290329\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"\"},{\"underlined\":true,\"color\":\"#290329\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"this \\u00266 that\"},{\"underlined\":true,\"color\":\"#290329\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"Hover \"},{\"bold\":true,\"obfuscated\":true,\"text\":\"text\"},{\"bold\":true,\"obfuscated\":true,\"text\":\"\"},{\"text\":\"\"}]},\"text\":\"\"},{\"text\":\" \"},{\"strikethrough\":true,\"text\":\"string\"},{\"text\":\"!\"}],\"text\":\"\"}"
                )
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> MineDown.parse("&bTest [this](color=green format=green,bold,italic https://example.com) shit!"));
    }

    @Test
    public void testParseLegacyHexColors() {
        System.out.println("testParseLegacyHexColors");
        Assertions.assertAll(
                () -> parse("§x§5§9§3§5§9§3__Test__",
                        "{\"extra\":[{\"color\":\"#593593\",\"text\":\"\"},{\"underlined\":true,\"color\":\"#593593\",\"text\":\"Test\"},{\"color\":\"#593593\",\"text\":\"\"}],\"text\":\"\"}"
                )
        );
    }

    @Test
    public void testParseGradient() {
        System.out.println("testParseGradient");
        Assertions.assertAll(
                () -> parse("[Test Gradient](#fff-#000) &7:D",
                        "{\"extra\":[{\"text\":\"\"},{\"extra\":[{\"color\":\"#ffffff\",\"text\":\"T\"},{\"color\":\"#eaeaea\",\"text\":\"e\"},{\"color\":\"#d5d5d5\",\"text\":\"s\"},{\"color\":\"#bfbfbf\",\"text\":\"t\"},{\"color\":\"#aaaaaa\",\"text\":\" \"},{\"color\":\"#959595\",\"text\":\"G\"},{\"color\":\"#808080\",\"text\":\"r\"},{\"color\":\"#6a6a6a\",\"text\":\"a\"},{\"color\":\"#555555\",\"text\":\"d\"},{\"color\":\"#404040\",\"text\":\"i\"},{\"color\":\"#2a2a2a\",\"text\":\"e\"},{\"color\":\"#151515\",\"text\":\"n\"},{\"color\":\"#000000\",\"text\":\"t\"}],\"text\":\"\"},{\"text\":\" \"},{\"color\":\"gray\",\"text\":\":D\"}],\"text\":\"\"}"
                ),
                () -> parse("[Test Gradient](#fff-#666666-#555555) &7:D", "{\"extra\":[{\"text\":\"\"},{\"extra\":[{\"color\":\"#ffffff\",\"text\":\"T\"},{\"color\":\"#e6e6e6\",\"text\":\"e\"},{\"color\":\"#cccccc\",\"text\":\"s\"},{\"color\":\"#b3b3b3\",\"text\":\"t\"},{\"color\":\"#999999\",\"text\":\" \"},{\"color\":\"#7f7f7f\",\"text\":\"G\"},{\"color\":\"#666666\",\"text\":\"r\"},{\"color\":\"#666666\",\"text\":\"a\"},{\"color\":\"#636363\",\"text\":\"d\"},{\"color\":\"#606060\",\"text\":\"i\"},{\"color\":\"#5e5e5e\",\"text\":\"e\"},{\"color\":\"#5b5b5b\",\"text\":\"n\"},{\"color\":\"#585858\",\"text\":\"t\"}],\"text\":\"\"},{\"text\":\" \"},{\"color\":\"gray\",\"text\":\":D\"}],\"text\":\"\"}"
                ),
                () -> parse("[Test Gradient](#fff-#000 Hover message) &7:D",
                        "{\"extra\":[{\"text\":\"\"},{\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Hover message\"}]},\"extra\":[{\"color\":\"#ffffff\",\"text\":\"T\"},{\"color\":\"#eaeaea\",\"text\":\"e\"},{\"color\":\"#d5d5d5\",\"text\":\"s\"},{\"color\":\"#bfbfbf\",\"text\":\"t\"},{\"color\":\"#aaaaaa\",\"text\":\" \"},{\"color\":\"#959595\",\"text\":\"G\"},{\"color\":\"#808080\",\"text\":\"r\"},{\"color\":\"#6a6a6a\",\"text\":\"a\"},{\"color\":\"#555555\",\"text\":\"d\"},{\"color\":\"#404040\",\"text\":\"i\"},{\"color\":\"#2a2a2a\",\"text\":\"e\"},{\"color\":\"#151515\",\"text\":\"n\"},{\"color\":\"#000000\",\"text\":\"t\"}],\"text\":\"\"},{\"text\":\" \"},{\"color\":\"gray\",\"text\":\":D\"}],\"text\":\"\"}"
                ),
                () -> parse("[Test Gradient](color=#fff,#000 format=bold,italic Hover message) &7:D",
                        "{\"extra\":[{\"text\":\"\"},{\"bold\":true,\"italic\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Hover message\"}]},\"extra\":[{\"color\":\"#ffffff\",\"text\":\"T\"},{\"color\":\"#eaeaea\",\"text\":\"e\"},{\"color\":\"#d5d5d5\",\"text\":\"s\"},{\"color\":\"#bfbfbf\",\"text\":\"t\"},{\"color\":\"#aaaaaa\",\"text\":\" \"},{\"color\":\"#959595\",\"text\":\"G\"},{\"color\":\"#808080\",\"text\":\"r\"},{\"color\":\"#6a6a6a\",\"text\":\"a\"},{\"color\":\"#555555\",\"text\":\"d\"},{\"color\":\"#404040\",\"text\":\"i\"},{\"color\":\"#2a2a2a\",\"text\":\"e\"},{\"color\":\"#151515\",\"text\":\"n\"},{\"color\":\"#000000\",\"text\":\"t\"}],\"text\":\"\"},{\"text\":\" \"},{\"color\":\"gray\",\"text\":\":D\"}],\"text\":\"\"}"
                ),
                () -> parse("&#fff-#000&Test Gradient&7No Gradient",
                        "{\"extra\":[{\"extra\":[{\"color\":\"#ffffff\",\"text\":\"T\"},{\"color\":\"#eaeaea\",\"text\":\"e\"},{\"color\":\"#d5d5d5\",\"text\":\"s\"},{\"color\":\"#bfbfbf\",\"text\":\"t\"},{\"color\":\"#aaaaaa\",\"text\":\" \"},{\"color\":\"#959595\",\"text\":\"G\"},{\"color\":\"#808080\",\"text\":\"r\"},{\"color\":\"#6a6a6a\",\"text\":\"a\"},{\"color\":\"#555555\",\"text\":\"d\"},{\"color\":\"#404040\",\"text\":\"i\"},{\"color\":\"#2a2a2a\",\"text\":\"e\"},{\"color\":\"#151515\",\"text\":\"n\"},{\"color\":\"#000000\",\"text\":\"t\"}],\"text\":\"\"},{\"color\":\"gray\",\"text\":\"No Gradient\"}],\"text\":\"\"}"
                )
        );
    }

    @Test
    public void testParseRainbow() {
        System.out.println("testParseRainbow");
        Assertions.assertAll(
                () -> parse("[Test Rainbow](color=rainbow)",
                        "{\"extra\":[{\"text\":\"\"},{\"extra\":[{\"color\":\"#f3801f\",\"text\":\"T\"},{\"color\":\"#c9bf03\",\"text\":\"e\"},{\"color\":\"#8bed08\",\"text\":\"s\"},{\"color\":\"#4bff2c\",\"text\":\"t\"},{\"color\":\"#18ed68\",\"text\":\" \"},{\"color\":\"#01bfa9\",\"text\":\"R\"},{\"color\":\"#0c80e0\",\"text\":\"a\"},{\"color\":\"#3640fc\",\"text\":\"i\"},{\"color\":\"#7412f7\",\"text\":\"n\"},{\"color\":\"#b401d3\",\"text\":\"b\"},{\"color\":\"#e71297\",\"text\":\"o\"},{\"color\":\"#fe4056\",\"text\":\"w\"}],\"text\":\"\"},{\"text\":\"\"}],\"text\":\"\"}"
                ),
                () -> parse("[Test Rainbow](rainbow)",
                        "{\"extra\":[{\"text\":\"\"},{\"extra\":[{\"color\":\"#f3801f\",\"text\":\"T\"},{\"color\":\"#c9bf03\",\"text\":\"e\"},{\"color\":\"#8bed08\",\"text\":\"s\"},{\"color\":\"#4bff2c\",\"text\":\"t\"},{\"color\":\"#18ed68\",\"text\":\" \"},{\"color\":\"#01bfa9\",\"text\":\"R\"},{\"color\":\"#0c80e0\",\"text\":\"a\"},{\"color\":\"#3640fc\",\"text\":\"i\"},{\"color\":\"#7412f7\",\"text\":\"n\"},{\"color\":\"#b401d3\",\"text\":\"b\"},{\"color\":\"#e71297\",\"text\":\"o\"},{\"color\":\"#fe4056\",\"text\":\"w\"}],\"text\":\"\"},{\"text\":\"\"}],\"text\":\"\"}"
                ),
                () -> parse("[Test Rainbow](rainbow:25)",
                        "{\"extra\":[{\"text\":\"\"},{\"extra\":[{\"color\":\"#03cc9b\",\"text\":\"T\"},{\"color\":\"#078ed5\",\"text\":\"e\"},{\"color\":\"#2a4df9\",\"text\":\"s\"},{\"color\":\"#651afc\",\"text\":\"t\"},{\"color\":\"#a601dd\",\"text\":\" \"},{\"color\":\"#de0ba6\",\"text\":\"R\"},{\"color\":\"#fc3364\",\"text\":\"a\"},{\"color\":\"#f8712a\",\"text\":\"i\"},{\"color\":\"#d5b206\",\"text\":\"n\"},{\"color\":\"#9ae503\",\"text\":\"b\"},{\"color\":\"#59fe22\",\"text\":\"o\"},{\"color\":\"#21f459\",\"text\":\"w\"}],\"text\":\"\"},{\"text\":\"\"}],\"text\":\"\"}"
                ),
                () -> parse("[Test Rainbow](rainbow:240)",
                        "{\"extra\":[{\"text\":\"\"},{\"extra\":[{\"color\":\"#e00ca2\",\"text\":\"T\"},{\"color\":\"#fc3760\",\"text\":\"e\"},{\"color\":\"#f77527\",\"text\":\"s\"},{\"color\":\"#d2b505\",\"text\":\"t\"},{\"color\":\"#96e804\",\"text\":\" \"},{\"color\":\"#55fe25\",\"text\":\"R\"},{\"color\":\"#1ff35d\",\"text\":\"a\"},{\"color\":\"#03c89f\",\"text\":\"i\"},{\"color\":\"#088ad8\",\"text\":\"n\"},{\"color\":\"#2d4afa\",\"text\":\"b\"},{\"color\":\"#6917fb\",\"text\":\"o\"},{\"color\":\"#aa01da\",\"text\":\"w\"}],\"text\":\"\"},{\"text\":\"\"}],\"text\":\"\"}"
                ),
                () -> parse("&Rainbow&Rainbow&7 Test",
                        "{\"extra\":[{\"extra\":[{\"color\":\"#f3801f\",\"text\":\"R\"},{\"color\":\"#9ee303\",\"text\":\"a\"},{\"color\":\"#32fb44\",\"text\":\"i\"},{\"color\":\"#01b7b2\",\"text\":\"n\"},{\"color\":\"#2e48fa\",\"text\":\"b\"},{\"color\":\"#9904e6\",\"text\":\"o\"},{\"color\":\"#f11c84\",\"text\":\"w\"}],\"text\":\"\"},{\"color\":\"gray\",\"text\":\" Test\"}],\"text\":\"\"}"
                )
        );
    }
    
    @Test
    public void testReplacing() {
        System.out.println("testReplacing");
        Assertions.assertAll(
                () -> parse("&6Test __%placeholder%__&r =D",
                        "{\"extra\":[{\"color\":\"gold\",\"text\":\"Test \"},{\"underlined\":true,\"color\":\"gold\",\"text\":\"value\"},{\"color\":\"gold\",\"text\":\"\"},{\"text\":\" \\u003dD\"}],\"text\":\"\"}",
                        "placeholder", "value"),
                () -> parse("&6Test __%PlaceHolder%__&r =D",
                        "{\"extra\":[{\"color\":\"gold\",\"text\":\"Test \"},{\"underlined\":true,\"color\":\"gold\",\"text\":\"**value**\"},{\"color\":\"gold\",\"text\":\"\"},{\"text\":\" \\u003dD\"}],\"text\":\"\"}",
                        "placeholder", "**value**"),
                () -> parse("&6Test __%placeholder%__&r =D",
                        "{\"extra\":[{\"color\":\"gold\",\"text\":\"Test \"},{\"underlined\":true,\"color\":\"gold\",\"text\":\"\\u00265value\"},{\"color\":\"gold\",\"text\":\"\"},{\"text\":\" \\u003dD\"}],\"text\":\"\"}",
                        "PlaceHolder", "&5value"),
                () -> parse("&6Test __%placeholder%__&r =D",
                        "{\"extra\":[{\"color\":\"gold\",\"text\":\"Test \"},{\"underlined\":true,\"color\":\"gold\",\"text\":\"[value](https://example.com)\"},{\"color\":\"gold\",\"text\":\"\"},{\"text\":\" \\u003dD\"}],\"text\":\"\"}", "" +
                                "placeholder", "[value](https://example.com)")
        );
    }

    @Test
    public void testComponentReplacing() {
        System.out.println("testComponentReplacing");
        Assertions.assertAll(
                () -> parse("&6Test No placeholder =D", "placeholder", new MineDown("value").toComponent(),
                        "{\"color\":\"gold\",\"text\":\"Test No placeholder \\u003dD\"}"
                ),
                () -> parse("&6Test __%placeholder%__&r =D", "placeholder", new MineDown("**value**").toComponent(),
                        "{\"extra\":[{\"color\":\"gold\",\"text\":\"Test \"},{\"underlined\":true,\"color\":\"gold\",\"extra\":[{\"text\":\"\"},{\"bold\":true,\"text\":\"value\"},{\"text\":\"\"}],\"text\":\"\"},{\"underlined\":true,\"color\":\"gold\",\"text\":\"\"},{\"color\":\"gold\",\"text\":\"\"},{\"text\":\" \\u003dD\"}],\"text\":\"\"}"
                ),
                () -> parse("&6Test __%PlaceHolder%__&r %placeholder% =D", "placeholder", new MineDown("&5value").toComponent(),
                        "{\"extra\":[{\"color\":\"gold\",\"text\":\"Test \"},{\"underlined\":true,\"color\":\"gold\",\"extra\":[{\"color\":\"dark_purple\",\"text\":\"value\"}],\"text\":\"\"},{\"underlined\":true,\"color\":\"gold\",\"text\":\"\"},{\"color\":\"gold\",\"text\":\"\"},{\"extra\":[{\"color\":\"dark_purple\",\"text\":\"value\"}],\"text\":\" \"},{\"text\":\" \\u003dD\"}],\"text\":\"\"}"
                ),
                () -> parse("&6Test __%placeholder1%__&r %placeholder2%=D",
                        "PlaceHolder1",new MineDown("[replacement1](https://example.com)").toComponent(),
                        "placeholder2", new MineDown("[replacement2](https://example.com)").toComponent(),
                        "{\"extra\":[{\"color\":\"gold\",\"text\":\"Test \"},{\"underlined\":true,\"color\":\"gold\",\"extra\":[{\"text\":\"\"},{\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"color\":\"blue\",\"text\":\"open url\"},{\"color\":\"white\",\"text\":\" https://example.com\"}]},\"text\":\"replacement1\"},{\"text\":\"\"}],\"text\":\"\"},{\"underlined\":true,\"color\":\"gold\",\"text\":\"\"},{\"color\":\"gold\",\"text\":\"\"},{\"extra\":[{\"text\":\"\"},{\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"color\":\"blue\",\"text\":\"open url\"},{\"color\":\"white\",\"text\":\" https://example.com\"}]},\"text\":\"replacement2\"},{\"text\":\"\"}],\"text\":\" \"},{\"text\":\"\\u003dD\"}],\"text\":\"\"}"
                )
        );
    }

    @Test
    public void testParseContentHover() {
        System.out.println("testParseContentHover");
        Assertions.assertAll(
                () -> parse("[this](show_text=&bHi)",
                        "{\"extra\":[{\"text\":\"\"},{\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"color\":\"aqua\",\"text\":\"Hi\"}]},\"text\":\"this\"},{\"text\":\"\"}],\"text\":\"\"}"
                ),
                () -> parse("[this](show_item=stone*3 {Name:\"Ein Stein\"})",
                        "{\"extra\":[{\"text\":\"\"},{\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"stone\",\"Count\":3,\"tag\":\"{Name:\\\"Ein Stein\\\"}\"}},\"text\":\"this\"},{\"text\":\"\"}],\"text\":\"\"}"
                ),
                () -> parse("[this](show_item=minecraft:stone*3 {Name:\"Ein Stein\"})",
                        "{\"extra\":[{\"text\":\"\"},{\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"Count\":3,\"tag\":\"{Name:\\\"Ein Stein\\\"}\"}},\"text\":\"this\"},{\"text\":\"\"}],\"text\":\"\"}"
                ),
                () -> parse("[this](show_entity=6eee24fd-f55c-4684-976a-f5da291bbb3b:cow &bEine Kuh)",
                        "{\"extra\":[{\"text\":\"\"},{\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":{\"type\":\"minecraft:cow\",\"id\":\"6eee24fd-f55c-4684-976a-f5da291bbb3b\",\"name\":{\"text\":\"§f§bEine Kuh\"}}},\"text\":\"this\"},{\"text\":\"\"}],\"text\":\"\"}"
                ),
                () -> parse("[this](show_entity=6eee24fd-f55c-4684-976a-f5da291bbb3b:minecraft:cow &bEine Kuh)",
                        "{\"extra\":[{\"text\":\"\"},{\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":{\"type\":\"minecraft:cow\",\"id\":\"6eee24fd-f55c-4684-976a-f5da291bbb3b\",\"name\":{\"text\":\"§f§bEine Kuh\"}}},\"text\":\"this\"},{\"text\":\"\"}],\"text\":\"\"}"
                )
        );
    }

    @Test
    public void testNegated() {
        Assertions.assertAll(
                () -> parse("&lBold [not bold](!bold) bold", "{\"extra\":[{\"bold\":true,\"text\":\"Bold \"},{\"bold\":false,\"text\":\"not bold\"},{\"bold\":true,\"text\":\" bold\"}],\"text\":\"\"}")
        );
    }
}
