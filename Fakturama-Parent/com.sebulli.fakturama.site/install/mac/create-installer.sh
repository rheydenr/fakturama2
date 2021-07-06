#!/usr/bin/env bash

export VERSION=2.1.2-BETA
export INSTALLER_NAME=Installer_Fakturama_macos_x64_${VERSION}
export PLUGIN_ROOT=/Users/rheydenr/git/fakturama-2/Fakturama-Parent/com.sebulli.fakturama.site

# only if needed (for deploying application)
# ../prepare_installer.sh

# enable some L10N (specific to MacOS)
cd ${PLUGIN_ROOT}/target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app/Contents/Resources
mkdir -v de.lproj it.lproj sv.lproj sk.lproj el.lproj es.lproj ar_LY.lproj pl.lproj fr.lproj de_CH.lproj de_LI.lproj de_AT.lproj eu.lproj hu.lproj ro.lproj ru.lproj tr.lproj uk.lproj
cd -

# create a jre directory below Fakturama2.app directory
# cp -R /Library/Java/JavaVirtualMachines/jdk-15.0.1.jdk ${PLUGIN_ROOT}/target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app/jre

# xcrun codesign --force --options runtime --timestamp --entitlements entitlement.xml --sign "Developer ID" ${PLUGIN_ROOT}/target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app

# Since create-dmg does not clobber, be sure to delete previous DMG
[[ -f ../install/${INSTALLER_NAME}.dmg ]] && rm ../install/${INSTALLER_NAME}.dmg

# create-dmg \
#   --volname "Fakturama Installer" \
#   --volicon "../../app/installer_icon.png" \
#   --background "Background_Fakturama2.png" \
#   --window-pos 200 120 \
#   --window-size 520 470 \
#   --icon-size 70 \
#   --icon "Fakturama2.app" 100 210 \
#   --text-size 12 \
#   --hide-extension "Fakturama2.app" \
#   --app-drop-link 380 210 \
#   "../install/${INSTALLER_NAME}.dmg" \
#   "${PLUGIN_ROOT}/target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app/"
  
./Packaging_v2.command
  
xcrun codesign --force --verbose --options runtime --entitlements entitlement.xml --timestamp --sign "Developer ID" ../install/${INSTALLER_NAME}.dmg
xcrun altool --notarize-app --verbose --primary-bundle-id org.fakturama.Fakturama -u "apple-dev@fakturama.net" -p "trkr-kgxh-mxvh-onae" --file ../install/${INSTALLER_NAME}.dmg
# xcrun altool --notarization-info "ebd71014-bb7a-4137-b5b9-8702074f58a7" -u "apple-dev@fakturama.net" -p "trkr-kgxh-mxvh-onae"
# xcrun stapler staple ../install/${INSTALLER_NAME}.dmg
# spctl --assess --type open --context context:primary-signature --verbose "../install/${INSTALLER_NAME}.dmg"

echo 'moving installer (tar.gz) to installer directory'
mv ${PLUGIN_ROOT}/target/products/Fakturama.ID-linux.gtk.x86_64.tar.gz ../install/Installer_Fakturama_linux_x64_${VERSION}.tar.gz
zip -m -o -v -j ../install/Installer_Fakturama_windows_x64_${VERSION}.zip ../install/Installer_Fakturama_windows-x64_${VERSION}.exe





