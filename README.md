# Minedown
A library that adds the ability to use a MarkDown inspired markup to write Minecraft chat components!

It lets you convert string messages into chat components by using a custom mark up syntax
which is loosely based on MarkDown while still supporting legacy formatting codes. It also
includes a way to directly replace placeholders in the messages.

This requires BungeeCord's chat API so it will only work on **Spigot** or **BungeeCord**!

## Syntax

### Inline Formatting
 Description   | Syntax       | More Info
 --------------|--------------|---------------------------------------------------------------------
 Color legacy  |` &6Text     `| [Formatting codes](https://minecraft.gamepedia.com/Formatting_codes)
 Color         |` &gold&Text `| [Formatting codes](https://minecraft.gamepedia.com/Formatting_codes)
 Bold          |` **Text**   `| 
 Italic        |` ##Text##   `| 
 Underlined    |` __Text__   `| 
 Strikethrough |` ~~Text~~   `| 
 Obfuscated    |` ??Text??   `| 

### Events ###
You can define click and hover events with the commonly used MarkDown link syntax.

#### Simple Syntax
 Description                    | Syntax
 -------------------------------|---------------------------------------------------------
 General syntax                 |` [Text](text-color text-formatting... link hover text) `
 Simple Link                    |` [Text](https://example.com)                           `
 Simple Command                 |` [Text](/command to run)                               `
 Link + Hover                   |` [Text](https://example.com Hover Text)                `
 Text formatting + Link + Hover |` [Text](blue underline https://example.com Hover Text) `
 
#### Advanced Syntax
 Description    | Syntax                                 | More Info
 ---------------|----------------------------------------|----
 General syntax |` [Text](action=value)                 `|[ClickEvent.Action](https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/net/md_5/bungee/api/chat/ClickEvent.Action.html), [HoverEvent.Action](https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/net/md_5/bungee/api/chat/HoverEvent.Action.html)
 Link           |` [Text](open_url=https://example.com) `|
 Color          |` [Text](color=red)                    `|
 Formatting     |` [Text](format=underline,bold)        `|
 Hover          |` [Text](hover=Hover Text)             `|
 Command        |` [Text](run_command=/command string)  `|
 
All advanced settings can be chained/included in a event definition.
You can't however add multiple different colors or click and hover actions!

## How to use it
The library's main API access is through the [MineDown.class](https://github.com/Phoenix616/MineDown/blob/master/src/main/java/de/themoep/minedown/MineDown.java) and its parse methods.

E.g. you can use it like this in your Spigot plugin:
```java
player.spigot().sendMessage(new MineDown(rawMessage).replace(replacements).toComponent());
```
or with a static approach:
```java
player.spigot().sendMessage(MineDown.parse(rawMessage, replacements));
```

Take a look at the [MineDown JavaDocs](https://docs.minebench.de/minedown/) for more
detailed info on the library and the included classes.

## Include it into your plugin
You can easily include this library into your plugin by using maven.
Make sure to relocate it into your plugin's package!

### Repository
```xml
<repositories>
    <repository>
        <id>minebench-repo</id>
        <url>http://repo.minebench.de/</url>
    </repository>
</repositories>
```

### Artifact
```xml
<dependencies>
    <dependency>
        <groupId>de.themoep</groupId>
        <artifactId>minedown</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### Relocation
```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.1.0</version>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>de.themoep.minedown</pattern>
                        <shadedPattern>your.package.path.libraries.minedown</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## License
MineDown is licensed under the MIT open source license:

```
Copyright (c) 2017 Max Lee (https://github.com/Phoenix616)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```