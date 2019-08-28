# textbin-backend
Anonymous temporary text hosting site (backend).
Provides an API for the frontend and connects to a PostgreSQL server to store and retrieve data.

# Dependencies
 - [Cage](https://akiraly.github.io/cage/index.html) 1.0
 - [commonmark-java](https://github.com/atlassian/commonmark-java/) 0.13.0
 - [jBCrypt](https://github.com/jeremyh/jBCrypt) 0.4
Place all dependencies in Twine's `./dependencies` directory before deploying.

# Deploying
Include Twine (version 1.0-alpha+) and all dependencies in the classpath, then compile to a jar. Copy all dependencies into Twine's `./dependencies/` directory, then place the TextBin jar into `./modules/`.

# Configuration
The TextBin configuration file will be generated in `./configs/textbin.json`. Go ahead and edit that file to setup the database connection, domain, and post limit. Be sure to upload `schema.sql` on your PostgreSQL server to setup the schema.
## textbin.json Fields to Take Note Of
While most fields in TextBin's configuration file are fairly self-explanatory, you should especially take note of the following:
 - `frontend_host`
 - `super_admin_username` & `super_admin_password`
 - `trip_strategy`
 - `trip_salt`
 - `ip_hash_algorithm`
 - `ip_hash_salt`
 - `site_stat_rank_required`

`frontend_host` should be set the to host address of the frontend. This field will be used in setting API `Access-Control-Allow-Origin` headers. Set to `*` to allow all hosts, although you should be advised that this is a security risk.

`super_admin_username` & `super_admin_password` represent the default super admin credentials. They will only be applied once, so be sure to delete the database entry containing the default credentials and restart in order to use your custom credentials.

`trip_strategy` determines what strategy to use when hashing tripcodes. The `classic` option will hash tripcodes using the classic 4chan and 2channel method. The `secure` option will hash tripcodes using a SHA-256 and a custom salt, making them resistant to cracking, but making 4chan and 2channel tripcode passwords produce different codes than if they were being processed using the classic method. The `classic-secure` option will hash tripcodes using the classic method, but will append `trip_salt` to it before putting it through the hash function. Will produce a different result from `classic`, but the output will look similar, unlike `secure`.

`trip_salt` defines the salt to be appended to a tripcode when hashing it, making it resistant to cracking. This field is only used if `trip_strategy` is set to `secure`.

`ip_hash_algorithm` defines the algorithm with which IP addresses are hashed for storage. You may enter `MD5`, `SHA-1`, or `SHA-256`, or optionally another algorithm that's supported by your JVM implementation (the provided examples are required to be included in every JVM implementation). Changing this value will nullify any recorded bans as a hashed IP address will be different than one under another algorithm.

`ip_hash_salt` defines the salt to be appended to and IP address before it is hashed and stored in the database. This is to avoid attackers resolving raw IP addresses, assuming they have access to the database. It's even more secure if the database is on a different server than the backend server. Once the value is set, it's unwise to change it as doing so will nullify all bans and make posts by users after the change appear to be different than those who posted before the change.

`site_stat_rank_required` defines the rank required for users to see site statistics. Defaults to `1`, which is `moderator`. To allow all users to see it, set it to `0`.

# Credits
This software uses several classes for encryption that are listed below, along with their copyright notices.

```
Java implementation of the UNIX crypt command
Copyright (c) 1996 Eric Young, (eay@mincom.oz.au)
All rights reserved.
```

```
Java-based tripcode generator
Copyright (c) 2012, Jeffrey Dileo
All rights reserved.
```