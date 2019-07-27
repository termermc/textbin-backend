# textbin-backend
Anonymous temporary text hosting site (backend)

# Dependencies
 - [Cage](https://akiraly.github.io/cage/index.html) 1.0
 - [flexmark-java](https://github.com/vsch/flexmark-java) 0.13.0+
Place all dependencies in Twine's `./dependencies` directory before deploying.

# Deploying
Include Twine (version 1.0-alpha+) and all dependencies in the classpath, then compile to a jar. Copy all dependencies into Twine's `./dependencies/` directory, then place the TextBin jar into `./modules/`.

# Configuration
The TextBin configuration file will be generated in `./configs/textbin.json`. Go ahead and edit that file to setup the database connection, domain, and post limit. Be sure to upload `schema.sql` on your PostgreSQL server to setup the schema.