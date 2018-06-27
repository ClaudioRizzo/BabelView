#!/bin/bash


# The script will try to run BabelView on every apk listed in <apk_ids>
# Apks have to be in the <apks_folder>.
# jars are the Android-Platforms
# Timeout is the timeout for each apk analysed 
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ] || [ -z "$4" ]; then
   echo "$0 /path/to/<apk_ids> /path/to/<apks_folder> /path/to/android/jars timeout_in_seconds"
   exit 0
fi

FLOWS=flows
LOGS=logs

SCRIPT_LOG=completed.txt

if [ ! -d "$FLOWS" ]; then
   mkdir "$FLOWS"
fi

if [ ! -d "$LOGS" ]; then
   mkdir "$LOGS"
fi

if [ ! -f "$SCRIPT_LOG" ]; then
   touch "$SCRIPT_LOG"
fi

while IFS='' read -r line || [[ -n "$line" ]]; do
   if grep -q "$line" "$SCRIPT_LOG"; then
      continue
   fi
   java -jar BabelView.jar -apk "$2"/"${line}".apk -jars "$3" -luw -chain -ftimeout "$4" -saveflows ./"$FLOWS"/"${line}".xml -intents -lib -js ./.  > ./"$LOGS"/"${line}".log 2>&1

   if [ -f sootOutput/"$line".apk ]; then
      rm sootOutput/"$line".apk
   fi
   echo "$line" >> "$SCRIPT_LOG"
done < "$1"