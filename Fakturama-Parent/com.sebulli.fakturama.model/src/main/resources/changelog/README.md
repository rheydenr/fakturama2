# Information for creating a changelog file
At first, look for an appropriate driver for your database (MySQL, Oracle etc.). Then you can run the following command (Windows syntax, change the path names for Linux if required):

```
liquibase --driver=com.mysql.jdbc.Driver --classpath=\path\to\mysql-connector-java-version-bin.jar --changeLogFile=db.changelog.xml --url="jdbc:mysql://yourhost/fakturamadb" --username=fakturama --password=whatever generateChangeLog
```

This is the command for creating a changelog from an existing  MySQL database. It should be entered as one line. If you want to create a changelog from another database vendor you have to change the ``classpath`` and the ``url`` switch according to the selected database.  

Changes after creation:
* change table names to upper case (search for 'ableName="(.+?)"' and replace it by 'ableName="\U\1"')
* change "categoryName" to "CATEGORYNAME"
* change (conditionally) the author's name
* add a start value to each id column (search for 'name="ID"' and replace it by 'name="ID" startWith="1"')
* change the constraint names for primary keys, because HSQL isn't able to process them. Search for 'constraintName="PRIMARY" tableName="(.+)"' and replace it by 'constraintName="PRIMARY_\1" tableName="\1"'

Hint: The database update can be prevented if you supply "NODBUPDATE=true" at startup. Then you have to enable the "eclipselink.ddl-generation" line for generation with eclipselink.
