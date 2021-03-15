# lc2mdl: LON-CAPA to Moodle Converter

This program **assists** you to convert [LON-CAPA](http://loncapa.org/index.html) `problem`-files 
into [Moodle-STACK](https://moodle.org/plugins/qtype_stack) `xml`-files. 
It comes with **no guarantee**, that the Moodle-STACK problems will work. 
Usually, you will have to do additional work by hand.

It handles quite well:
* Formularesponse
* Mathresponse
* Numericalresponse
* Optionresponse
* Radiobuttonresponse
* Stringresponse
* Matchresponse
* Essayresponse
* HTML elements
* Multi part problems
* Images (in the same folder/tree, will be included inline as base64-SVG)
* Gnuplot
* Conditional hints
* Multilingual questions (using [Multi-language content filter](https://docs.moodle.org/310/en/Multi-language_content_filter))

It assists:
* The use of conditional blocks
* Rather simple Perl scripts

It cannot handle:
* Other responses than those mentioned above (will be ignored)
* Rather complex Perl scripts containing Perl-Regex, Subs or Pointers (will be included as comment)

## Overview
- [Background](#background)
- [Installation](#installation)
  - [Build](#build)
  - [Bash Alias for Easy Use](#bash-alias-for-easy-use)
- [Usage](#usage)
  - [Example File-to-File](#example-file-to-file)
  - [Example Folder-to-Folder](#example-folder-to-folder)
- [Workflow: Moving a problem from LON-CAPA to Moodle-STACK](#workflow-moving-a-problem-from-lon-capa-to-moodle-stack)
  - [Managing XML Files](#managing-xml-files)
- [Version Support](#version-support)
- [Links](#links)

# Background
Our goal was to decrease number of used LMS.

One big feature of LON-CAPA is to generate *randomized* exercises for students.
Each student can receive an *unique* problem to solve which answer will be compared with a dynamically computed solution.
This feature is missing in Moodle.

With STACK as plugin randomized questions are possible using Maxima as CAS on top of Moodle.

Both LMS work with kind of XML. 
Quite different, but XML (one document-centered, one data-centric).

Sure, you could convert files by hand.
But that's boring and very time consuming.

So we need a converter, doing ~90% of this handcraft!

`lc2mdl` has its roots in my [bachelor thesis](https://serwiss.bib.hs-hannover.de/frontdoor/index/index/searchtype/all/start/1/rows/1/doctypefq/bachelorthesis/docId/1458).
After version 0.1, we continued to develop it until our courses were completely converted to Moodle.

The implementation of `lc2mdl` was done iteratively.
One LON-CAPA element was considered after another in order to how often it occurs in *our* pool of problems.
This explains the selection of *what* this converter can handle.
Thereby the most attention got the mathematics.


# Installation

Requirements:
- Java 7 or higher

Download our latest [releases](https://github.com/kiliandangendorf/lc2mdl/releases/latest).  
(There is also [development build](https://github.com/kiliandangendorf/lc2mdl/releases/tag/latest).)

When you got the `jar`-file, you can go on with "[Bash Alias for Easy Use](#bash-alias-for-easy-use)".

If you like to develop, then clone and build on your own as stated below.

## Build
To build project and a executable `jar` easily use Maven:
```
mvn -B package --file pom.xml 
```
This will build all classes in `target/` directory.

Now you can run `lc2mdl` with:
```
cd target/
java -jar lc2mdl.jar
```

## Bash Alias for Easy Use
To get rid of the uncomfortable "`java -jar`" you can add the following bash alias to your `.bashrc` (resp. `.bash_profile`):
```
echo "alias lc2mdl=\"java -jar <path-to-jar>/lc2mdl.jar\"" \
	>> ~/.bashrc && source ~/.bashrc
```

Now you can use `lc2mdl` easily like any other command.

# Usage
Running `lc2mdl --help` gives you the usage-message:
```
Usage: lc2mdl [options] FROMFILE TOFILE
FROMFILE: LON-CAPA problem-file or folder.
TOFILE:   Moodle-STACK xml-file or folder.
FROMFILE and TOFILE must be both files or folders.

General
-h, --help           Shows usage.
-v, --verbose        Verbose output.

Files Management
-r, --recursive      Find files recursively in folder (lists and ask for confirmation before converting).
-R, --recnocon       Find files recursively in folder (starts converting without confirmation).
-t, --rmtmp          Remove tmp-files (automatically done if empty).
-T, --rmlog          Remove log-files (NOT RECOMMENDED).

Convert Options
-p, --prefercheckbox Prefer checkbox, if only two options (optionresponse).
-m, --multilang      Use Moodle's multilang-plugin in multilanguage text-output (translated and languageblock).
    --language       Give a default language as two-letter code, ex. "--language=de".
   	                 If "--language=xx" is set without --multilang, other translations different from "xx" will be truncated.
   	                 If "--language=xx" is not set default language is "de".
```

## Example File-to-File
```
lc2mdl -m --language=en from.problem to.xml
```

Note: If `to.xml` already exists, it will be overwritten.

If there are multilingual text-outputs in `from.problem` (in `translated`- and `languageblock`-tags), 
`lc2mdl` will generate multilingual HTML output using Moodle plugin [Multi-language content filter](https://docs.moodle.org/310/en/Multi-language_content_filter).  
Multilingual output will be sorted that if a default language exists (stated explicitly in `problem`-file) is in first place, 
followed by language set by `--language=xx`.
This is because `multilang` will render only the first one, if no language matches.

A generated HTML-text could look like this:
```
<span lang="default" class="multilang">
	...
</span>
<span lang="en" class="multilang">
	English text.
</span>
<span lang="de" class="multilang">
	Deutscher Text.
</span>
```


## Example Folder-to-Folder
```
lc2mdl -r from/ to/
```

If directory `to/` doesn't exists, it will be created automatically.

If option `-r` or `-R` is set `lc2mdl` will copy the directory structure of `from/` into `to/`
(e.g. for `from/foo/bar.problem` you'll find corresponding `xml`-file in `to/foo/bar.xml`).


# Workflow: Moving a problem from LON-CAPA to Moodle-STACK

1. **"Export" from LON-CAPA**  
	Unfortunately there is no intended way to export problem-files, so we got the primitive one:
	From the XML editor of *each* problem you can copy content and paste into a local file (source code needs to be available).
	One by one...
	
	If someone found a better way to export problems, please let me know ;)

2. **Convert with `lc2mdl`**  
	```
	lc2mdl from.problem to.xml
	```
3. **Check Logs**  
	This is the part where you have "to do additional work by hand".
	Check warnings from log-file and the XML even if summary says something like "Great" ;)
	```
	Summary:
	- Great: DOM was 100% consumed. No unknown problem-tags occured.
	- deleted tmp file (because it's empty anyway).
	- check logfile for all warnings. (cat <path-to-xml>.xml.lc2mdl.log | grep WARNING)
	ANYWAYS: check generated Moodle-Stack xml file!
	- check questionsvariables for correct working maxima.
	- check questiontext for any 'not-HTML tags'.
	```
	The more problems you convert, the easier this inspection becomes ;)
	
4. **Import into Moodle-STACK**  
	Navigate to the question bank (Course administration > Question Bank > Import).
	Choose file format "Moodle XML Format", drag 'n' drop generated XML file and press "Import".
	At this point there is only test on XML-schema-layer. 
	Not on the content yet.
	
	Now check if STACK is okay with the generated *CASText* (Maxima plus STACK features) and the PRT (Potential Response Tree).
	Click "Edit"-Symbol next to the just imported question.
	In editor-mode scroll down and press the button "Verify the question text and update the form".
	If there is no trouble, fine!
	Go on with "Save Changes".
	
	You'll come back to the question bank.
	Now the imported question row is colored green (verified).
	Next step, click on the "Preview" symbol.
	Here you can check if the text is set correctly and the question works as expected.
	
	Even that worked?  
	Fine, now it's up to your students to test ;)

## Managing XML Files

Now you have some XML-files...

Check the helper scripts here in [`utils/`](utils/README.md) to brighten and/or collect batches of them.

# Version Support

In [`Prefs.java`](https://github.com/kiliandangendorf/lc2mdl/blob/master/src/lc2mdl/Prefs.java) there are two booleans for formatting / syntax.
Feel free to easily switch them and [rebuild as stated above](#build).

1. `ALLOW_MULTILINE_BLOCKS=true;`  
In older versions of STACK multiline blocks were not allowed. 
Switch this to remove all CR/LF and whitespaces within blocks (and other multiline statements as e.g. array assignments).

2. `ALLOW_MULTILINE_MAXIMA_STRINGS=false;`  
Basically Maxima supports multiline strings. 
But currently we could not use it in STACK.
So everything between quotation marks will be squeezed also into one line.
Someday in future I will switch this one too ;)

# Links 

- LON-CAPA  
	http://loncapa.org/index.html
- Moodle-STACK Plugin  
	https://moodle.org/plugins/qtype_stack  
	https://github.com/maths/moodle-qtype_stack  
	https://www.ed.ac.uk/maths/stack
	
- Moodle Plugin: Multi-Language Content Filter  
	https://docs.moodle.org/310/en/Multi-language_content_filter  

- Bachelor Thesis: "Entwurf und Implementierung eines Konverters für Aufgaben von LON-CAPA nach Moodle-STACK"  
	https://serwiss.bib.hs-hannover.de/frontdoor/index/index/searchtype/all/start/1/rows/1/doctypefq/bachelorthesis/docId/1458
	
- DOMAIN (Database of Math Instructions): Collection of digital exercises for diverse platforms  
	https://db.ak-mathe-digital.de/
