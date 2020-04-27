#!/bin/sh
# This script removes the doubled entries for persistence bundles from config files

echo "Fix tar archive for Linux"
mkdir ../target/products/Fakturama.ID/tmp
cd ../target/products/Fakturama.ID/tmp
tar -xf ../../Fakturama.ID-linux.gtk.x86_64.tar.gz
cd -

echo "Fix config.ini (remove javax.persistence)..."
for i in "win32/win32/x86_64" "tmp" "linux/gtk/x86_64" "macosx/cocoa/x86_64/Fakturama2.app/Contents/Eclipse"
  do 
	sed 's/reference\\:file\\:javax\.persistence_2\.2\.1\.v201807122140.jar@4\,//' "../target/products/Fakturama.ID/$i/configuration/config.ini" > config.ini
	mv -f config.ini "../target/products/Fakturama.ID/$i/configuration"
	rm "../target/products/Fakturama.ID/$i/plugins/javax.persistence_2.2.1.v201807122140.jar"
done

cd ../target/products/Fakturama.ID/tmp
rm ../../Fakturama.ID-linux.gtk.x86_64.tar.gz
tar -czf ../../Fakturama.ID-linux.gtk.x86_64.tar.gz *
cd ..
rm -rf tmp
echo "done."
