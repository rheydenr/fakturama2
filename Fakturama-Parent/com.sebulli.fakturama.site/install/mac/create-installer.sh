#!/usr/bin/env bash

export INSTALLER_NAME=Installer_Fakturama_macos_x64_2.1.1
export PLUGIN_ROOT=/Users/rheydenr/git/fakturama-2/Fakturama-Parent/com.sebulli.fakturama.site

# only if needed (for deployinng application)
# ../prepare_installer.sh

xcrun codesign --force --options runtime --timestamp --entitlements entitlement.xml --sign "Developer ID" ${PLUGIN_ROOT}/target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app

# Since create-dmg does not clobber, be sure to delete previous DMG
[[ -f install/${INSTALLER_NAME}.dmg ]] && rm install/${INSTALLER_NAME}.dmg

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
  "install/${INSTALLER_NAME}.dmg" \
  "${PLUGIN_ROOT}/target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app/"
  
  
xcrun codesign --force --options runtime --entitlements entitlement.xml --timestamp --sign "Developer ID" install/${INSTALLER_NAME}.dmg
xcrun altool --notarize-app --primary-bundle-id org.fakturama.Fakturama -u "apple-dev@fakturama.net" -p "clfs-pwqz-rgcb-bneg" --file install/${INSTALLER_NAME}.dmg
# xcrun altool --notarization-info "f3de2943-ec92-4805-a0ea-c67c49cc220b" -u "apple-dev@fakturama.net" -p "clfs-pwqz-rgcb-bneg"
# xcrun stapler staple install/${INSTALLER_NAME}.dmg
# spctl --assess --type open --context context:primary-signature --verbose "install/${INSTALLER_NAME}.dmg"