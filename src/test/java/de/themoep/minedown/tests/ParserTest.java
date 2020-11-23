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
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\",\"italic\":true},{\"text\":\" \"},{\"text\":\"b\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\"this\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\"\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
                ),
                () -> parse("&e##Test## [__this \\&6 \\that__](blue /example command hover=**Hover ??text??**) ~~string~~!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\",\"color\":\"yellow\"},{\"text\":\"Test\",\"color\":\"yellow\",\"italic\":true},{\"text\":\" \",\"color\":\"yellow\"},{\"text\":\"\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\"this \\u00266 that\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\"\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\" \",\"color\":\"yellow\"},{\"text\":\"string\",\"color\":\"yellow\",\"strikethrough\":true},{\"text\":\"!\",\"color\":\"yellow\"}]}"
                ),
                () -> parse("[TestLink](https://example.com) [Testcommand](/command test  )",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"TestLink\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]}},{\"text\":\" \"},{\"text\":\"Testcommand\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/command test \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"run command\",\"color\":\"blue\"},{\"text\":\" /command test \",\"color\":\"white\"}]}},{\"text\":\"\"}]}"
                ),
                () -> parse("&b&lTest [this](color=green format=bold,italic https://example.com Hover & text) string!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"aqua\",\"bold\":true},{\"text\":\"this\",\"color\":\"green\",\"bold\":true,\"italic\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Hover \\u0026 text\"}]}},{\"text\":\" string!\",\"color\":\"aqua\",\"bold\":true}]}"),
                () -> parse("&bTest [this](color=green format=bold,italic suggest_command=/example command hover=Hover text) string!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"aqua\"},{\"text\":\"this\",\"color\":\"green\",\"bold\":true,\"italic\":true,\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Hover text\"}]}},{\"text\":\" string!\",\"color\":\"aqua\"}]}"),
                () -> parse("&b[Test] [this](6 bold italic https://example.com) &as&bt&cr&di&en&5g&7!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"[Test] \",\"color\":\"aqua\"},{\"text\":\"this\",\"color\":\"gold\",\"bold\":true,\"italic\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]}},{\"text\":\" \",\"color\":\"aqua\"},{\"text\":\"s\",\"color\":\"green\"},{\"text\":\"t\",\"color\":\"aqua\"},{\"text\":\"r\",\"color\":\"red\"},{\"text\":\"i\",\"color\":\"light_purple\"},{\"text\":\"n\",\"color\":\"yellow\"},{\"text\":\"g\",\"color\":\"dark_purple\"},{\"text\":\"!\",\"color\":\"gray\"}]}"
                ),
                () -> parse("&bTest [[this]](https://example.com)!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"aqua\"},{\"text\":\"[this]\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]}},{\"text\":\"!\",\"color\":\"aqua\"}]}"
                ),
                () -> parse("&bTest [**[this]**](https://example.com)!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]}},{\"text\":\"[this]\",\"color\":\"aqua\",\"bold\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]}},{\"text\":\"\",\"color\":\"aqua\",\"bold\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]}},{\"text\":\"!\",\"color\":\"aqua\"}]}"
                ),
                () -> parse("&lbold &oitalic &0not bold or italic but black!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"bold \",\"bold\":true},{\"text\":\"italic \",\"bold\":true,\"italic\":true},{\"text\":\"not bold or italic but black!\",\"color\":\"black\"}]}"
                ),
                () -> parse("&cRed &land bold!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Red \",\"color\":\"red\"},{\"text\":\"and bold!\",\"color\":\"red\",\"bold\":true}]}"
                ),
                () -> parse("&bTest \n&cexample.com &rstring!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \\n\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"red\"},{\"text\":\"example.com\",\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Click to open url\"}]}},{\"text\":\" \",\"color\":\"red\"},{\"text\":\"string!\"}]}"
                ),
                () -> parse("&bTest \n&chttps://example.com &rstring!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \\n\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"red\"},{\"text\":\"https://example.com\",\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Click to open url\"}]}},{\"text\":\" \",\"color\":\"red\"},{\"text\":\"string!\"}]}"
                ),
                () -> parse("&bTest &chttps://example.com/test?t=2&d002=da0s#d2q &rstring!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"red\"},{\"text\":\"https://example.com/test?t\\u003d2\\u0026d002\\u003dda0s#d2q\",\"color\":\"red\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com/test?t\\u003d2\\u0026d002\\u003dda0s#d2q\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Click to open url\"}]}},{\"text\":\" \",\"color\":\"red\"},{\"text\":\"string!\"}]}"
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
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\",\"italic\":true},{\"text\":\" \"},{\"text\":\"b\",\"color\":\"#559933\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]}},{\"text\":\"this\",\"color\":\"#559933\",\"underlined\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]}},{\"text\":\"\",\"color\":\"#559933\",\"underlined\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
                ),
                () -> parse("##&eTest## [&#593593&b__this__](Text) ~~string~~!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\",\"italic\":true},{\"text\":\" \"},{\"text\":\"b\",\"color\":\"#593593\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]}},{\"text\":\"this\",\"color\":\"#593593\",\"underlined\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]}},{\"text\":\"\",\"color\":\"#593593\",\"underlined\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Text\"}]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
                ),
                () -> parse("##&eTest## [__this \\&6 \\that__](#290329 /example command hover=**Hover ??text??**) ~~string~~!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\",\"italic\":true},{\"text\":\" \"},{\"text\":\"\",\"color\":\"#290329\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\"this \\u00266 that\",\"color\":\"#290329\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\"\",\"color\":\"#290329\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
                ),
                () -> parse("##&eTest## [__this \\&6 \\that__](color=#290329 /example command hover=**Hover ??text??**) ~~string~~!",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"Test\",\"color\":\"yellow\",\"italic\":true},{\"text\":\" \"},{\"text\":\"\",\"color\":\"#290329\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\"this \\u00266 that\",\"color\":\"#290329\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\"\",\"color\":\"#290329\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/example command\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"\"},{\"text\":\"Hover \",\"bold\":true},{\"text\":\"text\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\",\"bold\":true,\"obfuscated\":true},{\"text\":\"\"}]}},{\"text\":\" \"},{\"text\":\"string\",\"strikethrough\":true},{\"text\":\"!\"}]}"
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
    public void testParseGradient() {
        System.out.println("testParseGradient");
        Assertions.assertAll(
                () -> parse("[Test Gradient](#fff-#000) &7:D",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"\",\"extra\":[{\"text\":\"T\",\"color\":\"#ffffff\"},{\"text\":\"e\",\"color\":\"#eaeaea\"},{\"text\":\"s\",\"color\":\"#d5d5d5\"},{\"text\":\"t\",\"color\":\"#bfbfbf\"},{\"text\":\" \",\"color\":\"#aaaaaa\"},{\"text\":\"G\",\"color\":\"#959595\"},{\"text\":\"r\",\"color\":\"#808080\"},{\"text\":\"a\",\"color\":\"#6a6a6a\"},{\"text\":\"d\",\"color\":\"#555555\"},{\"text\":\"i\",\"color\":\"#404040\"},{\"text\":\"e\",\"color\":\"#2a2a2a\"},{\"text\":\"n\",\"color\":\"#151515\"},{\"text\":\"t\",\"color\":\"#000000\"}]},{\"text\":\" \"},{\"text\":\":D\",\"color\":\"gray\"}]}"
                ),
                () -> parse("[Test Gradient](#fff-#666666-#555555) &7:D", "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"\",\"extra\":[{\"text\":\"T\",\"color\":\"#ffffff\"},{\"text\":\"e\",\"color\":\"#e6e6e6\"},{\"text\":\"s\",\"color\":\"#cccccc\"},{\"text\":\"t\",\"color\":\"#b3b3b3\"},{\"text\":\" \",\"color\":\"#999999\"},{\"text\":\"G\",\"color\":\"#7f7f7f\"},{\"text\":\"r\",\"color\":\"#666666\"},{\"text\":\"a\",\"color\":\"#666666\"},{\"text\":\"d\",\"color\":\"#636363\"},{\"text\":\"i\",\"color\":\"#606060\"},{\"text\":\"e\",\"color\":\"#5e5e5e\"},{\"text\":\"n\",\"color\":\"#5b5b5b\"},{\"text\":\"t\",\"color\":\"#585858\"}]},{\"text\":\" \"},{\"text\":\":D\",\"color\":\"gray\"}]}"
                ),
                () -> parse("[Test Gradient](#fff-#000 Hover message) &7:D",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"\",\"extra\":[{\"text\":\"T\",\"color\":\"#ffffff\"},{\"text\":\"e\",\"color\":\"#eaeaea\"},{\"text\":\"s\",\"color\":\"#d5d5d5\"},{\"text\":\"t\",\"color\":\"#bfbfbf\"},{\"text\":\" \",\"color\":\"#aaaaaa\"},{\"text\":\"G\",\"color\":\"#959595\"},{\"text\":\"r\",\"color\":\"#808080\"},{\"text\":\"a\",\"color\":\"#6a6a6a\"},{\"text\":\"d\",\"color\":\"#555555\"},{\"text\":\"i\",\"color\":\"#404040\"},{\"text\":\"e\",\"color\":\"#2a2a2a\"},{\"text\":\"n\",\"color\":\"#151515\"},{\"text\":\"t\",\"color\":\"#000000\"}],\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Hover message\"}]}},{\"text\":\" \"},{\"text\":\":D\",\"color\":\"gray\"}]}"
                ),
                () -> parse("[Test Gradient](color=#fff,#000 format=bold,italic Hover message) &7:D",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"\",\"bold\":true,\"italic\":true,\"extra\":[{\"text\":\"T\",\"color\":\"#ffffff\"},{\"text\":\"e\",\"color\":\"#eaeaea\"},{\"text\":\"s\",\"color\":\"#d5d5d5\"},{\"text\":\"t\",\"color\":\"#bfbfbf\"},{\"text\":\" \",\"color\":\"#aaaaaa\"},{\"text\":\"G\",\"color\":\"#959595\"},{\"text\":\"r\",\"color\":\"#808080\"},{\"text\":\"a\",\"color\":\"#6a6a6a\"},{\"text\":\"d\",\"color\":\"#555555\"},{\"text\":\"i\",\"color\":\"#404040\"},{\"text\":\"e\",\"color\":\"#2a2a2a\"},{\"text\":\"n\",\"color\":\"#151515\"},{\"text\":\"t\",\"color\":\"#000000\"}],\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Hover message\"}]}},{\"text\":\" \"},{\"text\":\":D\",\"color\":\"gray\"}]}"
                ),
                () -> parse("&#fff-#000&Test Gradient&7No Gradient",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\",\"extra\":[{\"text\":\"T\",\"color\":\"#ffffff\"},{\"text\":\"e\",\"color\":\"#eaeaea\"},{\"text\":\"s\",\"color\":\"#d5d5d5\"},{\"text\":\"t\",\"color\":\"#bfbfbf\"},{\"text\":\" \",\"color\":\"#aaaaaa\"},{\"text\":\"G\",\"color\":\"#959595\"},{\"text\":\"r\",\"color\":\"#808080\"},{\"text\":\"a\",\"color\":\"#6a6a6a\"},{\"text\":\"d\",\"color\":\"#555555\"},{\"text\":\"i\",\"color\":\"#404040\"},{\"text\":\"e\",\"color\":\"#2a2a2a\"},{\"text\":\"n\",\"color\":\"#151515\"},{\"text\":\"t\",\"color\":\"#000000\"}]},{\"text\":\"No Gradient\",\"color\":\"gray\"}]}"
                )
        );
    }

    @Test
    public void testParseRainbow() {
        System.out.println("testParseRainbow");
        Assertions.assertAll(
                () -> parse("[Test Rainbow](color=rainbow)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"\",\"extra\":[{\"text\":\"T\",\"color\":\"#f3801f\"},{\"text\":\"e\",\"color\":\"#c9bf03\"},{\"text\":\"s\",\"color\":\"#8bed08\"},{\"text\":\"t\",\"color\":\"#4bff2c\"},{\"text\":\" \",\"color\":\"#18ed68\"},{\"text\":\"R\",\"color\":\"#01bfa9\"},{\"text\":\"a\",\"color\":\"#0c80e0\"},{\"text\":\"i\",\"color\":\"#3640fc\"},{\"text\":\"n\",\"color\":\"#7412f7\"},{\"text\":\"b\",\"color\":\"#b401d3\"},{\"text\":\"o\",\"color\":\"#e71297\"},{\"text\":\"w\",\"color\":\"#fe4056\"}]},{\"text\":\"\"}]}"
                ),
                () -> parse("[Test Rainbow](rainbow)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"\",\"extra\":[{\"text\":\"T\",\"color\":\"#f3801f\"},{\"text\":\"e\",\"color\":\"#c9bf03\"},{\"text\":\"s\",\"color\":\"#8bed08\"},{\"text\":\"t\",\"color\":\"#4bff2c\"},{\"text\":\" \",\"color\":\"#18ed68\"},{\"text\":\"R\",\"color\":\"#01bfa9\"},{\"text\":\"a\",\"color\":\"#0c80e0\"},{\"text\":\"i\",\"color\":\"#3640fc\"},{\"text\":\"n\",\"color\":\"#7412f7\"},{\"text\":\"b\",\"color\":\"#b401d3\"},{\"text\":\"o\",\"color\":\"#e71297\"},{\"text\":\"w\",\"color\":\"#fe4056\"}]},{\"text\":\"\"}]}"
                ),
                () -> parse("[Test Rainbow](rainbow:25)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"\",\"extra\":[{\"text\":\"T\",\"color\":\"#03cc9b\"},{\"text\":\"e\",\"color\":\"#078ed5\"},{\"text\":\"s\",\"color\":\"#2a4df9\"},{\"text\":\"t\",\"color\":\"#651afc\"},{\"text\":\" \",\"color\":\"#a601dd\"},{\"text\":\"R\",\"color\":\"#de0ba6\"},{\"text\":\"a\",\"color\":\"#fc3364\"},{\"text\":\"i\",\"color\":\"#f8712a\"},{\"text\":\"n\",\"color\":\"#d5b206\"},{\"text\":\"b\",\"color\":\"#9ae503\"},{\"text\":\"o\",\"color\":\"#59fe22\"},{\"text\":\"w\",\"color\":\"#21f459\"}]},{\"text\":\"\"}]}"
                ),
                () -> parse("[Test Rainbow](rainbow:240)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"\",\"extra\":[{\"text\":\"T\",\"color\":\"#e00ca2\"},{\"text\":\"e\",\"color\":\"#fc3760\"},{\"text\":\"s\",\"color\":\"#f77527\"},{\"text\":\"t\",\"color\":\"#d2b505\"},{\"text\":\" \",\"color\":\"#96e804\"},{\"text\":\"R\",\"color\":\"#55fe25\"},{\"text\":\"a\",\"color\":\"#1ff35d\"},{\"text\":\"i\",\"color\":\"#03c89f\"},{\"text\":\"n\",\"color\":\"#088ad8\"},{\"text\":\"b\",\"color\":\"#2d4afa\"},{\"text\":\"o\",\"color\":\"#6917fb\"},{\"text\":\"w\",\"color\":\"#aa01da\"}]},{\"text\":\"\"}]}"
                ),
                () -> parse("&Rainbow&Rainbow&7 Test",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\",\"extra\":[{\"text\":\"R\",\"color\":\"#f3801f\"},{\"text\":\"a\",\"color\":\"#9ee303\"},{\"text\":\"i\",\"color\":\"#32fb44\"},{\"text\":\"n\",\"color\":\"#01b7b2\"},{\"text\":\"b\",\"color\":\"#2e48fa\"},{\"text\":\"o\",\"color\":\"#9904e6\"},{\"text\":\"w\",\"color\":\"#f11c84\"}]},{\"text\":\" Test\",\"color\":\"gray\"}]}"
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
                        "{\"text\":\"\",\"extra\":[{\"text\":\"Test \",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"gold\",\"underlined\":true,\"extra\":[{\"text\":\"\"},{\"text\":\"replacement1\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]}},{\"text\":\"\"}]},{\"text\":\"\",\"color\":\"gold\",\"underlined\":true},{\"text\":\"\",\"color\":\"gold\"},{\"text\":\" \",\"extra\":[{\"text\":\"\"},{\"text\":\"replacement2\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://example.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"open url\",\"color\":\"blue\"},{\"text\":\" https://example.com\",\"color\":\"white\"}]}},{\"text\":\"\"}]},{\"text\":\"\\u003dD\"}]}"
                )
        );
    }

    @Test
    public void testParseContentHover() {
        System.out.println("testParseContentHover");
        Assertions.assertAll(
                () -> parse("[this](show_text=&bHi)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"this\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Hi\",\"color\":\"aqua\"}]}},{\"text\":\"\"}]}"
                ),
                () -> parse("[this](show_item=stone*3 {Name:\"Ein Stein\"})",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"this\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"stone\",\"Count\":3,\"tag\":\"{Name:\\\"Ein Stein\\\"}\"}}},{\"text\":\"\"}]}"
                ),
                () -> parse("[this](show_item=minecraft:stone*3 {Name:\"Ein Stein\"})",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"this\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"Count\":3,\"tag\":\"{Name:\\\"Ein Stein\\\"}\"}}},{\"text\":\"\"}]}"
                ),
                () -> parse("[this](show_entity=coool-uuid-lol:cow &bEine Kuh)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"this\",\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":{\"type\":\"minecraft:cow\",\"id\":\"coool-uuid-lol\",\"name\":{\"text\":\"§f§bEine Kuh\"}}}},{\"text\":\"\"}]}"
                ),
                () -> parse("[this](show_entity=coool-uuid-lol:minecraft:cow &bEine Kuh)",
                        "{\"text\":\"\",\"extra\":[{\"text\":\"\"},{\"text\":\"this\",\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":{\"type\":\"minecraft:cow\",\"id\":\"coool-uuid-lol\",\"name\":{\"text\":\"§f§bEine Kuh\"}}}},{\"text\":\"\"}]}"
                )
        );
    }
}
