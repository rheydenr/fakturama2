#!/usr/bin/env bash

export INSTALLER_NAME=Installer_Fakturama_macos_x64_${VERSION}
export PLUGIN_ROOT=/Users/rheydenr/git/fakturama-2/Fakturama-Parent/com.sebulli.fakturama.site
export VERSION=2.1.1

# only if needed (for deploying application)
# ../prepare_installer.sh

mkdir de.lproj
mkdir it.lproj
mkdir sv.lproj
mkdir sk.lproj
mkdir el.lproj
mkdir es.lproj
mkdir ar_LY.lproj
mkdir pl.lproj   
mkdir fr.lproj
mkdir de_CH.lproj
mkdir de_LI.lproj
mkdir de_AT.lproj
mkdir eu.lproj   
mkdir hu.lproj
mkdir ro.lproj
mkdir tr.lproj
mkdir uk.lproj

xcrun codesign --force --options runtime --timestamp --entitlements entitlement.xml --sign "Developer ID" ${PLUGIN_ROOT}/target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app

# Since create-dmg does not clobber, be sure to delete previous DMG
[[ -f ../install/${INSTALLER_NAME}.dmg ]] && rm ../install/${INSTALLER_NAME}.dmg

create-dmg \
  --volname "Fakturama Installer" \
  --volicon "../../app/installer_icon.png" \
  --background "Background_Fakturama2.png" \
  --window-pos 200 120 \
  --window-size 520 470 \
  --icon-size 70 \
  --icon "Fakturama2.app" 100 210 \
  --text-size 12 \
  --hide-extension "Fakturama2.app" \
  --app-drop-link 380 210 \
  "../install/${INSTALLER_NAME}.dmg" \
  "${PLUGIN_ROOT}/target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app/"
  
  
xcrun codesign --force --options runtime --entitlements entitlement.xml --timestamp --sign "Developer ID" ../install/${INSTALLER_NAME}.dmg
xcrun altool --notarize-app --primary-bundle-id org.fakturama.Fakturama -u "apple-dev@fakturama.net" -p "clfs-pwqz-rgcb-bneg" --file ../install/${INSTALLER_NAME}.dmg
# xcrun altool --notarization-info "7e3a222f-3109-41fb-baa2-7428f2370b1e" -u "apple-dev@fakturama.net" -p "clfs-pwqz-rgcb-bneg"
# xcrun stapler staple ../install/${INSTALLER_NAME}.dmg
# spctl --assess --type open --context context:primary-signature --verbose "../install/${INSTALLER_NAME}.dmg"

echo 'moving installer (tar.gz) to installer directory'
mv ${PLUGIN_ROOT}/target/products/Fakturama.ID-linux.gtk.x86_64.tar.gz ../install/Installer_Fakturama_linux_x64_${VERSION}.tar.gz
zip -m -o -v -j ../install/Installer_Fakturama_windows_x64_${VERSION}.zip ../install/Installer_Fakturama_windows-x64_${VERSION}.exe
