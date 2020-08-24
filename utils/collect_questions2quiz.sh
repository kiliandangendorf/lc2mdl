#!/bin/bash
# This script combines all "question"-elements in all xml-files in source folder
# into one xml-file within "quiz"-elements ($pre & $post).
# Sources will not be modified.
#
# Uses on xmllint from http://xmlsoft.org/xmllint.html
# 2020-02-06 Kilian


pre="<?xml version=\"1.0\" encoding=\"UTF-8\"?><quiz>"
post="</quiz>"

skriptname=`basename "${0}"`
usage="Usage: ${skriptname} <directory>"

if [ $# -ne 1 ]
then
	echo "wrong number of parameters"
	echo $usage
	exit 1
fi

srcFolder=$1

if [ ! -d "${srcFolder}" ]
then
	echo "${srcFolder} is not a directory"
	echo $usage
	exit 2
fi

filename=`basename "${0}" .sh`
quizFile="${filename}_collection.xml"

echo "combining all xml-files in '${srcFolder}' into file '${quizFile}'..."

echo "${pre}" > "${quizFile}"

#run through all xml files
for f in "${srcFolder}"/*.xml
do
	echo "- add file ${f}"

	#magic line:
	xmllint --xpath "//question" "${f}" >> "${quizFile}"
	#cat "${f}" >> $quizFile #would do similar, but xmllint is more precise copying only questions elements
done

echo >> "${quizFile}"
echo "${post}" >> "${quizFile}"

echo "formatting xml..."
tidy -xml -i -utf8 -o "${quizFile}" "${quizFile}"
echo "done."