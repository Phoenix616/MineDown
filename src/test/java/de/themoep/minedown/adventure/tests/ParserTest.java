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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {
    
    private void parse(String mineDownString, String... replacements) {
        System.out.print(mineDownString + "\n" + GsonComponentSerializer.gson().serialize(MineDown.parse(mineDownString, replacements)) + "\n");
    }

    private void parse(String mineDownString, String placeholder, Component replacement) {
        System.out.print(mineDownString + "\n" + GsonComponentSerializer.gson().serialize(new MineDown(mineDownString).replace(placeholder, replacement).toComponent()) + "\n");
    }

    private void parse(String mineDownString, String placeholder1, Component replacement1, String placeholder2, Component replacement2) {
        System.out.print(mineDownString + "\n" + GsonComponentSerializer.gson().serialize(new MineDown(mineDownString)
                .replace(placeholder1, replacement1)
                .replace(placeholder2, replacement2)
                .toComponent()) + "\n");
    }
    
    @Test
    public void testParsing() {
        System.out.println("testParsing");
        Assertions.assertAll(
                () -> parse("##&eTest## [&blue&b__this__](https://example.com **Hover ??text??**) ~~string~~!"),
                () -> parse("&e##Test## [__this \\&6 \\that__](blue /example command hover=**Hover ??text??**) ~~string~~!"),
                () -> parse("[TestLink](https://example.com) [Testcommand](/command test  )"),
                () -> parse("&b&lTest [this](color=green format=bold,italic,underlined https://example.com Hover & text) string!"),
                () -> parse("&bTest [this](color=green format=bold,italic,underline suggest_command=/example command hover=Hover text) string!"),
                () -> parse("&b[Test] [this](6 bold italic https://example.com) &as&bt&cr&di&en&5g&7!"),
                () -> parse("&bTest [[this]](https://example.com)!"),
                () -> parse("&bTest [**[this]**](https://example.com)!"),
                () -> parse("&lbold &oitalic &0not bold or italic but black!"),
                () -> parse("&cRed &land bold!"),
                () -> parse("&bTest \n&cexample.com &rstring!"),
                () -> parse("&bTest \n&chttps://example.com &rstring!"),
                () -> parse("&bTest &chttps://example.com/test?t=2&d002=da0s#d2q &rstring!"),
                () -> parse("Test inner escaping [\\]](gray)"),
                () -> parse("[Test insertion](insert={text to insert} color=red)")
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> MineDown.parse("&bTest [this](color=green format=green,bold,italic https://example.com) shit!"));
    }

    @Test
    public void testParseHexColors() {
        System.out.println("testParseHexColors");
        Assertions.assertAll(
                () -> parse("##&eTest## [&#593&b__this__](Text) ~~string~~!"),
                () -> parse("##&eTest## [&#593593&b__this__](Text) ~~string~~!"),
                () -> parse("##&eTest## [__this \\&6 \\that__](#290329 /example command hover=**Hover ??text??**) ~~string~~!"),
                () -> parse("##&eTest## [__this \\&6 \\that__](color=#290329 /example command hover=**Hover ??text??**) ~~string~~!")
        );
        Assertions.assertThrows(IllegalArgumentException.class, () -> MineDown.parse("&bTest [this](color=green format=green,bold,italic https://example.com) shit!"));
    }

    @Test
    public void testParseGradient() {
        System.out.println("testParseGradient");
        Assertions.assertAll(
                () -> parse("[Test Gradient](#fff-#000) &7:D"),
                () -> parse("[Test Gradient](#fff-#666666-#555555) &7:D"),
                () -> parse("[Test Gradient](#fff-#666666-#fff) &7:D"),
                () -> parse("[Test Gradient](#fff-#000 Hover message) &7:D"),
                () -> parse("[Test Gradient](color=#fff,#000 format=bold,italic Hover message) &7:D"),
                () -> parse("&#fff-#000&Test Gradient&7No Gradient")
        );
    }

    @Test
    public void testParseRainbow() {
        System.out.println("testParseRainbow");
        Assertions.assertAll(
                () -> parse("[Test Rainbow](color=rainbow)"),
                () -> parse("[Test Rainbow](rainbow)"),
                () -> parse("[Test Rainbow](rainbow:25)"),
                () -> parse("[Test Rainbow](rainbow:240)"),
                () -> parse("&Rainbow&Rainbow&7 Test")
        );
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
    public void testNegated() {
        Assertions.assertAll(
                () -> parse("&lBold [not bold](!bold) bold")
        );
    }

    @Test
    public void testParseNested() {
        Assertions.assertAll(
                () -> parse("[outer start [inner](green) outer end](aqua)"),
                () -> parse("[outer start \\[[inner](green)\\] outer end](aqua)"),
                () -> parse("[outer start [inner](green) outer end](aqua hover={[red hover](red)})")
        );
    }

    @Test
    public void testEmptyEvent() {
        Assertions.assertAll(
                () -> parse("[test]()")
        );
    }
}
