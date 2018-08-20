#!/bin/sh
# 
# Show the number of OAI-PMH records in the files harvested in the data/ directory
# broken down by object type
#
(
if [ -f "$1" ]
then
  unzip -p $1
else
  find data/ -name \*.xml | xargs cat
fi
) | \
grep -h '<identifier>' | \
grep -o '<identifier>oai:[^:]*:[^/]*/' | \
sed -e 's/<identifier>oai://' -e 's/\///' | \
sort | \
uniq -c
