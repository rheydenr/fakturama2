#!/bin/bash

# remove before comitting!
DEVELOPER_PASSWORD="trkr-kgxh-mxvh-onae"

# by Andy Maloney
# http://asmaloney.com/2013/07/howto/packaging-a-mac-os-x-application-using-a-dmg/

wait_while_in_progress() 
{
	while true; do \
		/usr/bin/xcrun altool --notarization-info `/usr/libexec/PlistBuddy -c "Print :notarization-upload:RequestUUID" $(UPLOAD_INFO_PLIST)` -u "apple-dev@fakturama.net" -p $(DEVELOPER_PASSWORD) --output-format xml > $(REQUEST_INFO_PLIST) ;\
		if [ `/usr/libexec/PlistBuddy -c "Print :notarization-info:Status" $(REQUEST_INFO_PLIST)` != "in progress" ]; then \
			break ;\
		fi ;\
		/usr/bin/osascript -e 'display notification "Zzz…" with title "Notarization"' ;\
		sleep 60 ;\
	done
}




export PLUGIN_ROOT=/Users/rheydenr/git/fakturama-2/Fakturama-Parent/com.sebulli.fakturama.site
export INSTALL_MAIN_DIR=${PLUGIN_ROOT}/install/mac

# make sure we are in the correct dir when we double-click a .command file
dir=${0%/*}
if [ -d "$dir" ]; then
  cd "$dir"
fi

export VERSION=2.1.2-BETA
# prepare the correct directory structure
cp -R ${PLUGIN_ROOT}/target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app .

# enable some L10N (specific to MacOS)
cd Fakturama2.app/Contents/Resources
mkdir -v de.lproj it.lproj sv.lproj sk.lproj el.lproj es.lproj ar_LY.lproj pl.lproj fr.lproj de_CH.lproj de_LI.lproj de_AT.lproj eu.lproj hu.lproj ro.lproj ru.lproj tr.lproj uk.lproj
cd -

# set up your app name, version number, and background image file name
APP_NAME="Fakturama2"

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

cp -rpf "${APP_NAME}.app" "${STAGING_DIR}"
# ... cp anything else you want in the DMG - documentation, etc.

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
xcrun codesign --force --verbose --options runtime --entitlements entitlement.xml --timestamp --sign "Developer ID" ../install/${INSTALLER_NAME}.dmg

echo 'notarize application...'
xcrun altool --notarize-app  --primary-bundle-id org.fakturama.Fakturama -u "apple-dev@fakturama.net" -p ${DEVELOPER_PASSWORD} --file ../install/${INSTALLER_NAME}.dmg


# alternative method to check the success of notarization:
# no --verbose output
# xcrun altool --notarize-app  --primary-bundle-id org.fakturama.Fakturama -u "apple-dev@fakturama.net" -p ${DEVELOPER_PASSWORD} --file ../install/${INSTALLER_NAME}.dmg --output-format xml > ${UPLOAD_INFO_PLIST}
# xcrun altool --notarization-info `/usr/libexec/PlistBuddy -c "Print :notarization-upload:RequestUUID" ${UPLOAD_INFO_PLIST}` -u "apple-dev@fakturama.net" -p ${DEVELOPER_PASSWORD} --output-format xml > ${REQUEST_INFO_PLIST}
# wait_while_in_progress



# xcrun altool --notarization-info "57de96e1-2e78-45dd-87be-06cb61287f25" -u "apple-dev@fakturama.net" -p ${DEVELOPER_PASSWORD} 
# xcrun stapler staple ../install/${INSTALLER_NAME}.dmg
# spctl --assess --type open --context context:primary-signature --verbose "../install/${INSTALLER_NAME}.dmg"

echo 'moving installer (tar.gz) to installer directory'
mv ${PLUGIN_ROOT}/target/products/Fakturama.ID-linux.gtk.x86_64.tar.gz ../install/Installer_Fakturama_linux_x64_${VERSION}.tar.gz

echo 'create ZIP file for Windows installer...'
zip -m -o -v -j ../install/Installer_Fakturama_windows_x64_${VERSION}.zip ../install/Installer_Fakturama_windows-x64_${VERSION}.exe

echo 'Done.'

echo 'NOTE: You have to check the notarization success manually!'


# exit

