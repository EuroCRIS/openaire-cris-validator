#!/bin/sh
#
# Lists the CERIF XML payload from the files 
# harvested in the data/ directory as one large XML stream
#
DIR="${1:-data}"
echo '<?xml version="1.0" encoding="UTF-8" ?>'
echo '<CERIF>'
echo
( if [ -d $DIR ]
  then
    cat "$DIR"/*verb=Identify.xml
  else
    unzip -p $DIR \*verb=Identify.xml
  fi ) | \
sed -e 's/.*\(<Service.*\)/\1/' | sed -e 's/\(<\/Service>\).*/\n\1/' | sed -ne '/<Service/,/<\/Service>/p'
echo
( if [ -d $DIR ]
  then 
    find "$DIR"/ -name \*.xml | xargs cat
  else
    unzip -p $DIR
  fi ) | \
sed -ne '/<metadata>/,/<\/metadata>/p' | \
sed -e 's/.*<metadata>//' -e 's/<\/metadata>.*//'
echo '</CERIF>'
