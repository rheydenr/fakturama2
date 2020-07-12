#!/bin/sh
# This script removes the doubled entries for persistence bundles from config files

echo "Fix tar archive for Linux"
mkdir ../target/products/Fakturama.ID/tmp
cd ../target/products/Fakturama.ID/tmp
tar -xf ../../Fakturama.ID-linux.gtk.x86_64.tar.gz
cd -

echo "Fix config.ini (remove javax.persistence) for Linux..."
sed 's/reference\\:file\\:javax\.persistence_2\.2\.1\.v201807122140.jar@4\,//' "../target/products/Fakturama.ID/linux/gtk/x86_64/configuration/config.ini" > config.ini
mv -f config.ini "../target/products/Fakturama.ID/linux/gtk/x86_64/configuration"
rm "../target/products/Fakturama.ID/linux/gtk/x86_64/plugins/javax.persistence_2.2.1.v201807122140.jar"

cd ../target/products/Fakturama.ID/tmp
rm ../../Fakturama.ID-linux.gtk.x86_64.tar.gz
tar -czf ../../Fakturama.ID-linux.gtk.x86_64.tar.gz *
cd ..
rm -rf tmp/*

echo "Fix zip archive for Windows"
cd tmp
unzip -q ../../Fakturama.ID-win32.win32.x86_64.zip
cd ../../..

echo "Fix config.ini (remove javax.persistence) for Windows..."
sed 's/reference\\:file\\:javax\.persistence_2\.2\.1\.v201807122140.jar@4\,//' "../target/products/Fakturama.ID/win32/win32/x86_64/configuration/config.ini" > config.ini
mv -f config.ini "../target/products/Fakturama.ID/win32/win32/x86_64/configuration"
rm "../target/products/Fakturama.ID/win32/win32/x86_64/plugins/javax.persistence_2.2.1.v201807122140.jar"

cd ../target/products/Fakturama.ID/tmp
rm ../../Fakturama.ID-win32.win32.x86_64.zip
zip -qr ../../Fakturama.ID-win32.win32.x86_64.zip *
cd ..
rm -rf tmp/*


echo "Fix tar archive for MacOS"
cd tmp
tar -xf ../../Fakturama.ID-macosx.cocoa.x86_64.tar.gz

echo "Fix config.ini (remove javax.persistence) for MacOS..."
cd ../../..
sed 's/reference\\:file\\:javax\.persistence_2\.2\.1\.v201807122140.jar@4\,//' "../target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app/Contents/Eclipse/configuration/config.ini" > config.ini
mv -f config.ini "../target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app/Contents/Eclipse/configuration"
rm "../target/products/Fakturama.ID/macosx/cocoa/x86_64/Fakturama2.app/Contents/Eclipse/plugins/javax.persistence_2.2.1.v201807122140.jar"

cd ../target/products/Fakturama.ID/tmp
rm ../../Fakturama.ID-macosx.cocoa.x86_64.tar.gz
tar -czf ../../Fakturama.ID-macosx.cocoa.x86_64.tar.gz *
cd ..
rm -rf tmp
echo "done."
