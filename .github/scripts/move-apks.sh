#!/bin/bash
set -e
shopt -s globstar nullglob extglob

# Get APKs from previous jobs' artifacts
cp -R ~/apk-artifacts/ $PWD
APKS=( **/*".apk" )

echo "Moving ${#APKS[@]} APKs"

DEST=$PWD/apk
rm -rf $DEST && mkdir -p $DEST

for APK in ${APKS[@]}; do
    BASENAME=$(basename $APK)
    APKNAME="${BASENAME%%+(-release*)}.apk"
    APKDEST="$DEST/$APKNAME"

    cp $APK $APKDEST
done
