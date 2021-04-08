# Displaying text

The Graphic Entity Module includes two different classes to display text, here's how they work.

## Text <a name="Text"></a>

The basic class to display text, it will be displayed as a label in the viewer.

```java
graphicEntityModule.createText("Hello World")
    .setFontFamily("Lato")
    .setStrokeThickness(5) // Adding an outline
    .setStrokeColor(0xffffff) // a white outline
    .setFontSize(50)
    .setFillColor(0x000000); // Setting the text color to black
```
![Example](resources/text1.png)

The only font family values that will work are: 
"Lato" and those that are available by default on all browsers.
If the browser does not recognise the font family, it will be displayed in a fallback font chosen by the browser.

## BitmapText <a name="BitmapText"></a>

This class is used to display text using a bitmap font in your assets folder.

```java
graphicEntityModule.createBitmapText()
    .setText("Hello World")
    .setFont("myCustomFont")
    // Assuming that you have a working 'myCustomFont.fnt' and 'myCustomFont.png' in your assets folder
    .setFontSize(50)
    .setTint(0xff0000); // Tinting it in red
```
![Example](resources/text-2.png)

Extra tips :
- A tool to turn fonts into bitmap fonts : https://github.com/andryblack/fontbuilder (export in Sparrow format)
- You can put your font in a sub folder of the assets repository whithout modifying any code
