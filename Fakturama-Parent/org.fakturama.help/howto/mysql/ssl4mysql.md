# Create and use SSL encrypted connections for MySQL DB with Fakturama

Follow these steps to secure your connections to the MySQL server with SSL. The steps describe creating self-signed certificates. If you want to use your own trust authority signed-off certificates you have to only copy these certificates in the cert-folder in your MySQL directory. Note that all these steps are written for Windows&trade; environments. If you use a Linux&trade; or MacOS&reg; operating system, the paths will be slightly different.

For your convenience you can set the `PATH` variable and the `OPENSSL_CONF` to the correct values. The `OPENSSL_CONF` variable points to a configuration file for OpenSSL where some default settings are stored (otherwise you have to enter these information at least twice :-) ). To collect all the certificates on a single place we create a directory for it and change to it:

    mkdir mysqlCerts
    cd mysqlCerts

Now set some convenience variables (download and install OpenSSL before if you haven't):

    set OPENSSL_CONF=%USERPROFILE%\openssl.cnf
    set PATH=d:\Programs\OpenSSL-Win32\bin;%PATH%

If the `openssl.cnf` file doesn't exist in your User Home directory you can create one from the template files supplied from OpenSSL (look in the OpenSSL install directory for files like `openssl.cnf` or `openssl.cfg`).

At first, you have to create some certificates and a CA Certificate:

    openssl genrsa 2048 > ca-key.pem
    openssl req -new -x509 -nodes -days 3600 -key  ca-key.pem > ca-cert.pem
    
Now, some server certificates will be created:

    openssl req -newkey rsa:2048 -days 3600 -nodes -keyout server-key.pem > server-req.pem
    openssl x509 -req -in server-req.pem -days 3600 -CA ca-cert.pem -CAkey ca-key.pem -set_serial 01 > server-cert.pem
    
Finally, some client certificates are created:

    openssl req -newkey rsa:2048 -days 3600 -nodes -keyout client-key.pem > client-req.pem
    openssl x509 -req -in client-req.pem -days 3600 -CA  ca-cert.pem  -CAkey ca-key.pem -set_serial 01 >  client-cert.pem

After creating all the certificates you have to edit the MySQL config file `my.ini`. If you don't know where MySQL looks for this file you can issue the command `mysql --help` at the command line. Between all the helpful stuff there are two important lines:

    Default options are read from the following files in the given order:
    C:\WINDOWS\my.ini C:\WINDOWS\my.cnf C:\my.ini C:\my.cnf d:\Programs\MySQL\MySQL Server 5.6\my.ini d:\Programs\MySQL\MySQL Server 5.6\my.cnf

Look at these given locations and search for a file with the given name. Mostly, you'll find only one :-)

Edit this file and write the following three lines within the `[mysqld]` section:

    ssl-ca     = "d:\\Programs\\MySQL\\mysqlCerts\\ca-cert.pem"
    ssl-cert   = "d:\\Programs\\MySQL\\mysqlCerts\\server-cert.pem"
    ssl-key    = "d:\\Programs\\MySQL\\mysqlCerts\\server-key.pem"

or (if you changed the key file like described above) replace the last line with:

    ssl-key    = "d:\\Programs\\MySQL\\mysqlCerts\\server-key-ppless.pem"
    
Adapt the file paths according to your system.

Add the following lines to the `[mysql]` section (note the missing 'd'!)

    ssl-ca     = "d:\\Programs\\MySQL\\mysqlCerts\\ca-cert.pem"
    ssl-cert   = "d:\\Programs\\MySQL\\mysqlCerts\\client-cert.pem"
    ssl-key    = "d:\\Programs\\MySQL\\mysqlCerts\\client-key.pem"
    
or (if you changed the key file like described above) replace the last line with:
    
    ssl-key    = "d:\\Programs\\MySQL\\mysqlCerts\\client-key-ppless.pem"

Note the double backslashes! Restart your MySQL service (e.g., via Task Manager) and login to your MySQL server:

    mysql -u root -p
    
Type the following command to show that the SSL was configured successfully:

    show variables like '%ssl%';
    
Then you must see something like that:

    +---------------+-----------------------------------------------------+
    | Variable_name | Value                                               |
    +---------------+-----------------------------------------------------+
    | have_openssl  | YES                                                 |
    | have_ssl      | YES                                                 |
    | ssl_ca        | d:\Programs\MySQL\mysqlCerts\ca-cert.pem           |
    | ssl_capath    |                                                     |
    | ssl_cert      | d:\Programs\MySQL\mysqlCerts\server-cert.pem       |
    | ssl_cipher    |                                                     |
    | ssl_crl       |                                                     |
    | ssl_crlpath   |                                                     |
    | ssl_key       | d:\Programs\MySQL\mysqlCerts\server-key-ppless.pem |
    +---------------+-----------------------------------------------------+
    9 rows in set, 1 warning (0.00 sec)
    
If not, you can look into the error log file which is normally located in the `data` directory of MySQL.

Now, inform the Java Runtime Environment about this new CA. Run this command from the `mysqlCerts` directory. Check if the Java binary path is located in your `PATH` variable. Else, you have to provide the full path to the `keytool` helper application (from JDK).

    keytool -importcert -alias MySQLCACert -file ca-cert.pem -keystore truststore -storepass aStrongPassword

Ok, almost done :-) Now you can start **Fakturama** for the first time. Remove the *use default settings* switch and enter the connection URL like in the following example:

    jdbc:mysql://localhost/fakturama?verifyServerCertificate=true&clientCertificateKeyStoreUrl=file:///d:/Programs/MySQL/mysqlCerts/truststore&clientCertificateKeyStorePassword=aStrongPassword&useSSL=true&characterEncoding=utf8&useUnicode=yes

Adapt the paths to your own path locations. At the moment, the password is stored in plain text in a settings file below your local user folder. This file is only readable by you and your Administrator. It is planned on a later release to encrypt this password along with the connection user password for the database. 