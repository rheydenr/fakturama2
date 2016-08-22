# Information for creating a changelog file
At first, look for an appropriate driver for your database (MySQL, Oracle etc.). Then you can run the following command (Windows syntax, change the path names for Linux if required):

```
liquibase --driver=com.mysql.jdbc.Driver --classpath=\path\to\mysql-connector-java-version-bin.jar --changeLogFile=db.changelog.xml --url="jdbc:mysql://yourhost/fakturamadb" --username=fakturama --password=whatever generateChangeLog
```

This is the command for creating a changelog from an existing  MySQL database. It should be entered as one line. If you want to create a changelog from another database vendor you have to change the ``classpath`` and the ``url`` switch according to the selected database.  