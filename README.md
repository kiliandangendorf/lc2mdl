# lc2mdl
This program tries to convert LON-CAPA problem-files into Moodle-STACK xml-files.

# requirements
Java 8 or higher

# usage
```
Usage: lc2mdl [options] FROMFILE TOFILE
FROMFILE: LON-CAPA problem-file or folder.
TOFILE:   Moodle-STACK xml-file or folder.
FROMFILE and TOFILE must be both files or folders.
-h, --help 				shows usage
-v, --verbose			verbose output
-r, --recursive		find files recursively in folder
-t, --rmtmp				remove tmp-files (automatically done if empty)
-T, --rmlog				remove log-files (NOT RECOMMENDED)
```
