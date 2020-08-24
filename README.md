# lc2mdl
This program assists you to convert LON-CAPA problem-files into Moodle-STACK xml-files. It comes with no guarantee, that the Moodle-Stack problems will work. Usually, you will have to do additional work by hand.

It handles quite well:
* Formularesponse
* Mathresponse
* Numericalresponse
* Optionresponse
* Radiobuttonresponse
* Stringresponse
* HTML elements
* Multi part problems
* Images (in the same folder, will be included as SVG)
* gnuplot
* conditional hints

It assists:
* the use of conditional blocks
* very simple Perl scripts
* Essayresponse

It cannot handle:
* other responses than those mentioned above (will be ignored)
* more complicated Perl scripts (will be included as comment)

# requirements
Java 8 or higher

ImageMagick www.imagemagick.org

# usage
```
Usage: lc2mdl [options] FROMFILE TOFILE
FROMFILE: LON-CAPA problem-file or folder.
TOFILE:   Moodle-STACK xml-file or folder.
FROMFILE and TOFILE must be both files or folders.
-h, --help 				shows usage
-v, --verbose			verbose output
-r, --recursive			find files recursively in folder
-R, --recnocon			find files recursively in folder (starts converting without confirmation)
-t, --rmtmp				remove tmp-files (automatically done if empty)
-T, --rmlog				remove log-files (NOT RECOMMENDED)
```
# Prefs class
In `Prefs.java` there are two booleans for formatting / syntax:
```
ALLOW_MULTILINE_BLOCKS=true;
```
In older versions of Stack multiline blocks were not allowed. 
Switch this to remove all CR/LF and whitespaces within blocks.

```
ALLOW_MULTILINE_MAXIMA_STRINGS=false;
```
Basically Maxima supports multiline strings. 
But currently we could not use it in Stack.
So everything between quotation marks will be squeezed also.
Someday in future I will switch this one too ;)