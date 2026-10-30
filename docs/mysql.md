# MySQL verification and local use

The optional workspace MySQL instance runs on `127.0.0.1:13306` and stores its
files under ignored `.cache/mysql`. It does not install a Windows service or
change a system MySQL installation. No Docker is used.

```powershell
npm.cmd run mysql:start
npm.cmd run mysql:status
npm.cmd run test:mysql
npm.cmd run mysql:stop
```

The first start downloads the official MySQL Community 8.4.13 Windows ZIP archive,
checks the checksum published on the [MySQL download page](https://dev.mysql.com/downloads/mysql/8.4.html?os=3),
and initializes a database directory. The generated local password is stored in
`.cache/mysql/credentials.json` and its client configuration in `client.ini`.
Neither file is committed. The setup follows MySQL's [manual ZIP installation
instructions](https://dev.mysql.com/doc/refman/8.4/en/windows-install-archive.html).

`test:mysql` creates a uniquely named schema and runs the complete backend test
suite against the actual server with the MySQL JDBC driver. Test tables use
`create-drop` inside that isolated schema. Application databases and local H2
data are preserved. The local MySQL server remains available until `mysql:stop`.

For your normal MySQL server, copy the root `.env.example` to `.env`, set
`SPRING_PROFILES_ACTIVE=mysql`, and provide `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
and a private `JWT_SECRET`. Set `BOOTSTRAP_ADMIN_EMAIL` and
`BOOTSTRAP_ADMIN_PASSWORD` for the first administrator, then remove the bootstrap
password from the environment after successful setup.

On macOS/Linux, use an existing MySQL server and supply the equivalent datasource
environment variables when running `npm run test:api`; the portable convenience
scripts are for Windows.
