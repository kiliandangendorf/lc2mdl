#!/bin/bash
# This script formats all xml-files in source directory
# into xml-files in target directory.
# Sources will not be modified.
#
# Uses tidy from http://tidy.sourceforge.net
# 2020-02-06 Kilian

skriptname=`basename "${0}"`
usage="Usage: ${skriptname} <source directory> <target directory>"

if [ $# -ne 2 ]
then
	echo "wrong number of parameters"
	echo $usage
	exit 1
fi

srcFolder="$1"
targetFolder="$2"

if [ ! -d "${srcFolder}" ]
then
	echo "${srcFolder} is not a directory"
	echo $usage
	exit 2
fi

if [ ! -d "$targetFolder" ]
then
	mkdir "$targetFolder"
fi

logfile="${targetFolder}/log_${skriptname}.txt"

echo "formatting all xml-files in '${srcFolder}' into same-named files in '${targetFolder}'..."

#clear log file
date > "$logfile"

#run through all xml files
for f in "${srcFolder}"/*.xml
do
	filename=`basename "${f}" ".xml"`
	inFile=$f
	outFile="${targetFolder}/${filename}_format.xml"
	
	echo | tee -a "$logfile"
	echo ------------------------------------ | tee -a "$logfile"
	echo FROM: $inFile | tee -a "$logfile"
	echo TO:   $outFile | tee -a "$logfile"
	
	#magic line:
	tidy -xml -i -utf8 -o "${outFile}" "${inFile}" 2>&1| tee -a "$logfile"
done
