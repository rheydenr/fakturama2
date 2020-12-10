@REM Collects all the l10n files that are necessary for Fakturama
@REM and puts them in a directory (separated by plugin project).
@REM That directory can be zipped and given to translators,
@REM along with a description from ../../org.fakturama.help/howto/translate/howto_translate.md
@REM or ../../org.fakturama.help/howto/translate/howto_translate_DE.md.

@ECHO OFF
SET FAKTURAMA_INSTALLDIR=d:\User\GitHome\fakturama-2\Fakturama-Parent\

@REM create a folder with name as current date
SET COLLECTIONS_FOLDER=%date:~-4,4%%date:~-7,2%%date:~-10,2%_l10n-fakturama
mkdir %COLLECTIONS_FOLDER%
cd %COLLECTIONS_FOLDER%

copy %FAKTURAMA_INSTALLDIR%\com.sebulli.fakturama.rcp\project\i18nedit.properties .

mkdir rcp
xcopy /f %FAKTURAMA_INSTALLDIR%\com.sebulli.fakturama.rcp\OSGI-INF\l10n\*.properties rcp
move rcp\i18nedit.properties .

mkdir import
xcopy /f %FAKTURAMA_INSTALLDIR%\org.fakturama.import\OSGI-INF\l10n\*.properties import

mkdir export
xcopy /f %FAKTURAMA_INSTALLDIR%\org.fakturama.export\OSGI-INF\l10n\*.properties export

mkdir zugferd
xcopy /f %FAKTURAMA_INSTALLDIR%\org.fakturama.exporter.zugferd\OSGI-INF\l10n\*.properties zugferd

cd ..

