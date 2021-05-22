#!/bin/bash

# DEVELOPER_PASSWORD is set in .bash_profile

# by Andy Maloney
# http://asmaloney.com/2013/07/howto/packaging-a-mac-os-x-application-using-a-dmg/

wait_while_in_progress() 
{
	uuid=`cat tmp | grep -Eo '\w{8}-(\w{4}-){3}\w{12}$'`
	while true; do
	    echo "checking for notarization..."
	 
	    xcrun altool --notarization-info "$uuid" -u "apple-dev@fakturama.net" -p ${DEVELOPER_PASSWORD} &> tmp
	    r=`cat tmp`
	    t=`echo "$r" | grep "success"`
	    f=`echo "$r" | grep "invalid"`
	    if [[ "$t" != "" ]]; then
	        echo "notarization done!"
#	        xcrun stapler staple "APP_NAME.app"
	        xcrun stapler staple ../install/${DMG_FINAL}
	        echo "stapler done!"
	        break
	    fi
	    if [[ "$f" != "" ]]; then
	        echo "$r"
	        return 1
	    fi
	    echo "not finish yet, sleep 2m then check again..."
	    sleep 120
	done
}


export PLUGIN_ROOT=/Users/rheydenr/git/fakturama-2/Fakturama-Parent/com.sebulli.fakturama.site
export INSTALL_MAIN_DIR=${PLUGIN_ROOT}/install/mac

# make sure we are in the correct dir when we double-click a .command file
dir=${0%/*}
if [ -d "$dir" ]; then
  cd "$dir"
fi

# set up your app name, version number, and background image file name
APP_NAME="Fakturama2"

export VERSION=2.1.2-BETA

DMG_BACKGROUND_IMG="Background_${APP_NAME}.png"

# you should not need to change these
APP_EXE="${APP_NAME}.app/Contents/MacOS/Fakturama"

VOL_NAME="Installer_Fakturama_macos_x64_${VERSION}"   # volume name will be "Installer_Fakturama_macos_x64_2.0.0”
DMG_TMP="${VOL_NAME}-temp.dmg"
DMG_FINAL="${VOL_NAME}.dmg" # final DMG name will be "Installer_Fakturama_macos_x64_2.1.1.dmg"
STAGING_DIR="./Install"             # we copy all our stuff into this dir

# Check the background image DPI and convert it if it isn't 72x72
_BACKGROUND_IMAGE_DPI_H=`sips -g dpiHeight ${DMG_BACKGROUND_IMG} | grep -Eo '[0-9]+\.[0-9]+'`
_BACKGROUND_IMAGE_DPI_W=`sips -g dpiWidth ${DMG_BACKGROUND_IMG} | grep -Eo '[0-9]+\.[0-9]+'`


if [ $(echo " $_BACKGROUND_IMAGE_DPI_H != 72.0 " | bc) -eq 1 -o $(echo " $_BACKGROUND_IMAGE_DPI_W != 72.0 " | bc) -eq 1 ]; then
   echo "WARNING: The background image's DPI is not 72.  This will result in distorted backgrounds on Mac OS X 10.7+."
   echo "         I will convert it to 72 DPI for you."
   
   _DMG_BACKGROUND_TMP="${DMG_BACKGROUND_IMG%.*}"_dpifix."${DMG_BACKGROUND_IMG##*.}"

   sips -s dpiWidth 72 -s dpiHeight 72 ${DMG_BACKGROUND_IMG} --out ${_DMG_BACKGROUND_TMP}
   
   DMG_BACKGROUND_IMG="${_DMG_BACKGROUND_TMP}"
fi

# clear out any old data
rm -rf "${STAGING_DIR}" "${DMG_TMP}" "${DMG_FINAL}"

# copy over the stuff we want in the final disk image to our staging dir
mkdir -p "${STAGING_DIR}"

echo "staging dir created: ${STAGING_DIR}"

# prepare the correct directory structure
cp -rpf ${PLUGIN_ROOT}/target/products/Fakturama.ID/macosx/cocoa/x86_64/"${APP_NAME}".app "${STAGING_DIR}"
# ... cp anything else you want in the DMG - documentation, etc.

# copy current JRE into the product
echo "copy JRE 15 into product..."
mkdir "${STAGING_DIR}"/${APP_NAME}.app/jre
cp -R /Library/Java/JavaVirtualMachines/adoptopenjdk-15.jre/* "${STAGING_DIR}"/${APP_NAME}.app/jre/Contents

# enable some L10N (specific to MacOS)
cd "${STAGING_DIR}"/${APP_NAME}.app/Contents/Resources
echo "creating L10N directories..."
mkdir -v de.lproj it.lproj sv.lproj sk.lproj el.lproj es.lproj ar_LY.lproj pl.lproj fr.lproj de_CH.lproj de_LI.lproj de_AT.lproj eu.lproj hu.lproj ro.lproj ru.lproj tr.lproj uk.lproj
cd -

# cp DS_Store ${STAGING_DIR}/.DS_Store

pushd "${STAGING_DIR}"

# strip the executable
echo "Stripping ${APP_EXE}..."
strip -u -r "${APP_EXE}"

# compress the executable if we have upx in PATH
#  UPX: http://upx.sourceforge.net/
if hash upx 2>/dev/null; then
   echo "Compressing (UPX) ${APP_EXE}..."
   upx -9 "${APP_EXE}"
fi

# ... perform any other stripping/compressing of libs and executables

# echo "sign the app..."
# codesign -f -v -s "Developer ID" ${APP_NAME}.app 
xcrun codesign --force --options runtime --timestamp --entitlements ${INSTALL_MAIN_DIR}/entitlement.xml --sign "Developer ID" ${APP_NAME}.app

popd

#  assumes our contents are at least 1M!
SIZE=`du -sh "${STAGING_DIR}" | sed 's/\([0-9\.]*\)M\(.*\)/\1/'  | sed 's/,/\./'` 
SIZE=`echo "${SIZE} + 2.0" | bc | awk '{print int($1+0.5)}'`

echo "INFO:  SIZE=$SIZE"

if [ $? -ne 0 ]; then
   echo "Error: Cannot compute size of staging dir"
   exit
fi

# create the temp DMG file
hdiutil create -srcfolder "${STAGING_DIR}" -volname "${VOL_NAME}" -fs HFS+ \
      -fsargs "-c c=64,a=16,e=16" -format UDRW -size ${SIZE}M "${DMG_TMP}"

echo "Created DMG: ${DMG_TMP}"

# mount it and save the device
DEVICE=$(hdiutil attach -readwrite -noverify "${DMG_TMP}" | \
         egrep '^/dev/' | sed 1q | awk '{print $1}')

sleep 2

# add a link to the Applications dir
echo "Add link to /Applications"
pushd /Volumes/"${VOL_NAME}"
ln -s /Applications
popd

# add a background image
mkdir /Volumes/"${VOL_NAME}"/.background
cp "${DMG_BACKGROUND_IMG}" /Volumes/"${VOL_NAME}"/.background/

# tell the Finder to resize the window, set the background,
#  change the icon size, place the icons in the right position, etc.
echo '
   tell application "Finder"
     tell disk "'${VOL_NAME}'"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {400, 100, 940, 560}
           set viewOptions to the icon view options of container window
           set arrangement of viewOptions to not arranged
           set icon size of viewOptions to 72
           set background picture of viewOptions to file ".background:'${DMG_BACKGROUND_IMG}'"
           set position of item "'${APP_NAME}'.app" of container window to {160, 205}
           set position of item "Applications" of container window to {360, 205}
           close
           open
           update without registering applications
           delay 2
     end tell
   end tell
' | osascript

sync

# unmount it
hdiutil detach "${DEVICE}"

# now make the final image a compressed disk image
echo "Creating compressed image"
hdiutil convert "${DMG_TMP}" -format UDZO -imagekey zlib-level=9 -o "${DMG_FINAL}"

# echo "Moving installer to install directory"
mv "${DMG_FINAL}" ../install

echo 'clean up...'
rm -rf "${DMG_TMP}"
rm -rf "${STAGING_DIR}"
rm -rf "${APP_NAME}.app"

echo 'signing application...'
xcrun codesign --force --verbose --options runtime --entitlements entitlement.xml --timestamp --sign "Developer ID" ../install/${DMG_FINAL}

echo 'notarize application...'
xcrun altool --notarize-app --verbose --primary-bundle-id org.fakturama.Fakturama -u "apple-dev@fakturama.net" -p ${DEVELOPER_PASSWORD} --file ../install/${DMG_FINAL}

########## alternative method to check the success of notarization: ########################
# no --verbose output
# xcrun altool --notarize-app  --primary-bundle-id org.fakturama.Fakturama -u "apple-dev@fakturama.net" -p ${DEVELOPER_PASSWORD} --file ../install/${DMG_FINAL} --output-format xml &> tmp
# xcrun altool --notarization-info `/usr/libexec/PlistBuddy -c "Print :notarization-upload:RequestUUID" ${UPLOAD_INFO_PLIST}` -u "apple-dev@fakturama.net" -p ${DEVELOPER_PASSWORD} --output-format xml > $(REQUEST_INFO_PLIST)
# wait_while_in_progress
#############################################################################################


# xcrun altool --notarization-info "97ded686-b617-47bd-a815-27e56f721725" -u "apple-dev@fakturama.net" -p ${DEVELOPER_PASSWORD} 
# xcrun stapler staple ../install/${DMG_FINAL}
# spctl --assess --type open --context context:primary-signature --verbose "../install/${DMG_FINAL}"

echo 'moving installer (tar.gz) to installer directory'
mv ${PLUGIN_ROOT}/target/products/Fakturama.ID-linux.gtk.x86_64.tar.gz ../install/Installer_Fakturama_linux_x64_${VERSION}.tar.gz

echo 'create ZIP file for Windows installer...'
zip -m -o -v -j ../install/Installer_Fakturama_windows_x64_${VERSION}.zip ../install/Installer_Fakturama_windows-x64_${VERSION}.exe

echo 'Done.'

echo 'NOTE: You have to check the notarization success manually!'

# exit

