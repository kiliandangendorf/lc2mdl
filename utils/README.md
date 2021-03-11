# Utils
Some scripts that help dealing with all these question files...

## `format_xmls.sh`
Formats all ("messed up") xml-files in source directory into ("beautifully formatted") xml-files in target directory.
Sources will not be modified.
```
Usage: format_xmls.sh <source directory> <target directory>
```
(Uses `tidy` from http://tidy.sourceforge.net)

## `collect_questions2quiz.sh`
Combines all "question"-elements in all xml-files in source folder into one xml-file within "quiz"-elements (
`<?xml version=\"1.0\" encoding=\"UTF-8\"?><quiz>`
and 
`</quiz>`).
Sources will not be modified.
```
Usage: collect_questions2quiz.sh <directory>
```
(Uses `xmllint` from http://xmlsoft.org/xmllint.html)