# MineDown-adventure
A library that adds the ability to use a MarkDown inspired markup to write Minecraft chat components!

It provides a custom mark up syntax which is loosely based on MarkDown that adds the ability to use the full power of 
component messages with the same simplicity as legacy formatting codes. (Which it can still support!)
It also includes a way to directly replace placeholders in the messages, both string based and component based ones!

This requires the [kyori-adventure](https://github.com/KyoriPowered/adventure) chat API so you need to provide one of 
the [platform libraries](https://github.com/KyoriPowered/adventure-platform) in your project in order to use these
messages! See [this plugin](https://github.com/Phoenix616/MineDownPlugin/) for a simple implementation.

## Syntax

### Inline Formatting
 Description   | Syntax             | More Info                                                     | Preview
 --------------|--------------------|---------------------------------------------------------------|-------------------------------------------
 Color legacy  |` &6Text           `| [Formatting codes](https://minecraft.wiki/w/Formatting_codes) | ![](https://i.phoenix616.dev/gbJYVeql.png)
 Color         |` &gold&Text       `| [Color names](https://minecraft.wiki/w/Formatting_codes)      | ![](https://i.phoenix616.dev/gfgApGrn.png)
 RGB Hex Color |` &#ff00ff&Text    `| Full hexadecimal format                                       | ![](https://i.phoenix616.dev/glFV9zgw.png)
 RGB Hex Color |` &#f0f&Text       `| Short format (equivalent to long one)                         | ![](https://i.phoenix616.dev/gojZlSaN.png)
 Gradient      |` &#f0f-#fff&Text  `| Inline gradients                                              | ![](https://i.phoenix616.dev/giOQuXV6.png)
 Rainbow       |` &rainbow&Text    `| Inline Rainbow                                                | ![](https://i.phoenix616.dev/ggYaEWZt.png)
 Rainbow Phase |` &rainbow:20&Text `| Inline Rainbow with a phase                                   | ![](https://i.phoenix616.dev/gojDf1ZM.png)
 Bold          |` **Text**         `|                                                               | ![](https://i.phoenix616.dev/fYDs0soW.png)
 Italic        |` ##Text##         `|                                                               | ![](https://i.phoenix616.dev/gaLmjWZA.png)
 Underlined    |` __Text__         `|                                                               | ![](https://i.phoenix616.dev/gk6lbR0B.png)
 Strikethrough |` ~~Text~~         `|                                                               | ![](https://i.phoenix616.dev/gpc5zBr4.png)
 Obfuscated    |` ??Text??         `|                                                               | ![](https://i.phoenix616.dev/giRU4C9u.gif)

### Events ###
You can define click and hover events with the commonly used MarkDown link syntax
as well as specify formatting, font and colors that way.

#### Simple Syntax
 Description                    | Syntax
 -------------------------------|------------------------------------------------------------
 General syntax                 |` [Text](text-color text-formatting... link hover text)    `
 Simple Link                    |` [Text](https://example.com)                              `
 Simple Command                 |` [Text](/command to run)                                  `
 Link + Hover                   |` [Text](https://example.com Hover Text)                   `
 Text formatting                |` [Text](blue underline !bold)                             `
 Gradient                       |` [Text](#fff-#000)                                        `
 Rainbow                        |` [Text](rainbow)                                          `
 Phased Rainbow                 |` [Text](rainbow:20)                                       `
 Text formatting + Link + Hover |` [Text](#0000ff underline https://example.com Hover Text) `

 
#### Advanced Syntax
 Description        | Syntax                                                          | More Info                                                                                                                                                                                                                                                                                                    
 --------------------|-----------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 General syntax     | ` [Text](action=value) `                                        | [ClickEvent.Action](https://github.com/KyoriPowered/adventure/blob/master/api/src/main/java/net/kyori/adventure/text/event/ClickEvent.java#L196-L222), [HoverEvent.Action](https://github.com/KyoriPowered/adventure/blob/master/api/src/main/java/net/kyori/adventure/text/event/HoverEvent.java#L311-L339) 
 Link               | ` [Text](open_url=https://example.com) `                        |
 Color              | ` [Text](color=red) `                                           | [Color names](https://minecraft.wiki/w/Formatting_codes)                                                                                                                                                                                                                                                     
 RGB Hex Color      | ` [Text](color=#ff00ff) `                                       | Full hexadecimal format                                                                                                                                                                                                                                                                                      
 RGB Hex Color      | ` [Text](color=#f0f) `                                          | Short format (equivalent to long one)                                                                                                                                                                                                                                                                        
 RGB Color Gradient | ` [Text](color=#fff-#000) `                                     | Gradient of two colors. (Supports all color forms)                                                                                                                                                                                                                                                           
 RGB Color Gradient | ` [Text](color=#fff-#333-#222) `                                | Gradient of three colors.                                                                                                                                                                                                                                                                                    
 RGB Rainbow        | ` [Text](color=rainbow) `                                       | An RGB rainbow.                                                                                                                                                                                                                                                                                              
 Phased RGB Rainbow | ` [Text](color=rainbow:50) `                                    | An RGB rainbow with a specific phase.                                                                                                                                                                                                                                                                        
 Text Shaddow       | ` [Text](shadow=#ff00ff) `                                      | Specify the shadow of the text                                                                                                                                                                                                                                                                                
 Formatting         | ` [Text](format=underline,bold) `                               | Specify formatting                                                                                                                                                                                                                                                                                           
 Disable Formatting | ` [Text](format=!underline) `                                   | Disable the specific formatting with a ! prefix                                                                                                                                                                                                                                                              
 Font               | ` [Text](font=custom_font) `                                    | Set a custom font from a resource pack                                                                                                                                                                                                                                                                       
 Translatable       | ` [fallback](translate=translation.key with={value1, value2}) ` | Translatable component with optional replacements                                                                                                                                                                                                                                                                     
 Run Command        | ` [Text](run_command=/command string) `                         | Run command on click                                                                                                                                                                                                                                                                                         
 Suggest Command    | ` [Text](suggest_command=/command) `                            | Suggest a command on click                                                                                                                                                                                                                                                                                   
 Simple Hover       | ` [Text](hover=Hover Text) `                                    | Show hover text                                                                                                                                                                                                                                                                                              
 Hover Text         | ` [Text](show_text=Hover Text) `                                | Show hover text                                                                                                                                                                                                                                                                                              
 Hover Entity Info  | ` [Text](show_entity=uuid:pig Name) `                           | Show entity information.                                                                                                                                                                                                                                                                                     
 Hover Item Info    | ` [Text](show_item=stone*2 nbt...) `                            | Show item information, additional information needs to be provided as a string of the nbt in json                                                                                                                                                                                                            
 Insertion          | ` [Text](insert=insert into input) `                            | Insert into input on shift click, can be combined with other events                                                                                                                                                                                                                                          
 
All advanced settings can be chained/included in a event definition.
You can't however add multiple different colors or click and hover actions!

## How to use it
The library's main API access is through the [MineDown.class](https://docs.minebench.de/minedown-adventure/de/themoep/minedown/adventure/MineDown.html) and its parse methods.

E.g. you can use it like this in your Bukkit plugin:
```java
BukkitAudiences.create(plugin).player(player).sendMessage(new MineDown(rawMessage).replace(replacements).toComponent());
```
or with a static approach:
```java
BukkitAudiences.create(plugin).player(player).sendMessage(MineDown.parse(rawMessage, replacements));
```

Take a look at the [MineDown JavaDocs](https://docs.minebench.de/minedown-adventure/) for more
detailed info on the library and the included classes.

### Include it into your plugin
You can easily include this library into your plugin by using maven.
Make sure to relocate it into your plugin's package!

#### Repository
```xml
<repositories>
    <repository>
        <id>minebench-repo</id>
        <url>https://repo.minebench.de/</url>
    </repository>
</repositories>
```

#### Artifact
```xml
<dependencies>
    <dependency>
        <groupId>de.themoep</groupId>
        <artifactId>minedown-adventure</artifactId>
        <version>1.7.4-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

##### Legacy BungeeCord-chat library
The original BungeeCord-chat library is no longer maintained and has been replaced by this adventure implementation. If you need to still use the legacy version (found on the `legacy` branch), you can include it like this:
```xml
<dependencies>
    <dependency>
        <groupId>de.themoep</groupId>
        <artifactId>minedown</artifactId>
        <version>1.7.1-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

#### Relocation
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

### Alternative to shading

Alternatively you can also directly depend on my [MineDownPlugin](https://github.com/Phoenix616/MineDownPlugin/) 
instead of shading in this library! MineDownPlugin includes a non-relocated version of this library.

## License
MineDown is licensed under the MIT open source license:

```
Copyright (c) 2024 Max Lee (https://github.com/Phoenix616)

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
