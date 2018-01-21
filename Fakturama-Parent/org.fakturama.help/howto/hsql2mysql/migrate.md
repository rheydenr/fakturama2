### Hinweise
Die folgende Beschreibung ist für die Konvertierung einer HSQL-DB (Standard-Datenbank in Fakturama) in eine MySQL-Datenbank. Bevor die Schritte zur Konvertierung ausgeführt werden, müssen folgende Voraussetzungen erfüllt sein:
- Daten sichern (Arbeitsverzeichnis incl. Datenbank)
- Verzeichnis `~/.fakturama2` (bei Linux / Mac OS) bzw. `%USERPROFILE%\.fakturama2` (unter Windows) umbenennen nach `.fakturama2.old`
- leere MySQL-Datenbank anlegen und einen Datenbank-Benutzer darauf berechtigen (`GRANT ALL`, der User muss Rechte zum Anlegen und Löschen von Tabellen haben sowie zum Erzeugen von Indizes usw.)
- optional: testweise über die Konsole anmelden, ob der Zugang funktioniert (mysql -u nutzername datenbank -p)
- Fakturama einmal starten und den Initialisierungsdialog ausfüllen (Häkchen bei _verwende Standardeinstellungen_ entfernen, _Arbeitsverzeichnis (alt)_ leer lassen!), wobei bei _Datenbank_ die Zugangsdaten zur neuen MySQL-Datenbank einzutragen sind. WICHTIG: An die URL muss noch folgender Text angehängt werden: `?useSSL=false&characterEncoding=utf8&useUnicode=yes`
- Fakturama legt nun die Datenbanktabellen in der neuen MySQL-Datenbank an
- Hinweis: Es werden keine Daten angezeigt, da die Datenbank fast leer ist und noch nichts übernommen wurde
- Fakturama beenden

### Schritte für die Konvertierung

- download [HSQL2MYSQL4FAK.ZIP](https://files.fakturama.info/release/HSQL2MYSQL4FAK.ZIP)
- unzip `HSQL2MYSQL4FAK.ZIP`
- cd `HSQL2MYSQL4FAK`
- Zugangsdaten in allen `migration*.grf`-Dateien ändern (suche nach MYSQL) &rArr; URL, User und Passwort eintragen (nur diese drei Parameter sind anzupassen!)

**Beispiel:**

	<Connection database="MYSQL" dbURL="jdbc:mysql://localhost/fktest" id="JDBC1" jdbcSpecific="MYSQL" name="FktMysql" password="fktestuser" type="JDBC" user="fktestuser"/>
	<Connection database="HSQLDB" dbURL="jdbc:hsqldb:d:\MeineDaten\Fakt2tmp\Database\Database" id="JDBC0" jdbcSpecific="HSQLDB" name="hsqldb" type="JDBC" user="sa"/>

- HSQL-Server starten:

	java -cp plugins/org.jetel.jdbc/lib/hsqldb/hsqldb.jar org.hsqldb.server.Server --database.0 file:PFAD_ZUM_ARBEITSVERZEICHNIS_V2/Database/Database --dbname.0 Fakturama2 --silent=false

- `bin\clover migration.grf`
- `bin\clover migration_002.grf`
- `bin\clover migration_003.grf`

Am Ende jeder Ausführung muss `INFO  [main] - Execution of graph successful !` ausgegeben werden, ansonsten war die Konvertierung nicht erfolgreich. Die Ausführung des dritten Skriptes dauert etwas länger, weil hier alle Beziehungen zwischen den Tabellen wieder neu aufgebaut werden.

Anschließend kann man Fakturama wieder starten und mit der MySQL-Datenbank arbeiten. Bitte zuerst prüfen, ob alle Standardwerte korrekt gesetzt sind (Zahlungsart, Versandart, Steuersatz). Ebenfalls sollte geprüft werden, ob alle Beschreibungstexte und Notizen vorhanden sind. 