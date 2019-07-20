# textbin-backend
Anonymous temporary text hosting site (backend)

# Deploying
Include Twine (version 1.0-alpha+) and [Cage](https://akiraly.github.io/cage/index.html) as dependencies, then compile to a jar. Place Cage in Twine's `./dependencies/` directory, then place the textbin jar into `./modules/`.

# Configuration
The TextBin configuration file will be generated in `./configs/textbin.json`. Go ahead and edit that file to setup the database connection, domain, and post limit. Be sure to upload `schema.sql` on your PostgreSQL server to setup the schema.
