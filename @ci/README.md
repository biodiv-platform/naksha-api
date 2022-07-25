# Building

- This file is for reference purpose only that gives you basic idea on how CI system is going to build this or how to setup dev environment locally
- Dummy Credintials replace it with actual one

> **DO NOT COMMIT CREDINTIALS**

```sh
# Will be set globally on CD
export ARTIFACTORY_USERNAME=admin
export ARTIFACTORY_PASSWORD=password
export ARTIFACTORY_URL=http://venus.strandls.com/artifactory
export MTPROP_SCHEMES=http
export MTPROP_HOST=localhost:8080
export ARTIFACTORY_COMPAT=1

# Naksha Specific
export MTPROP_GEOSERVER_URL="http://localhost:8080/geoserver/"
export MTPROP_GEOSERVER_WEB_USERNAME="admin"
export MTPROP_GEOSERVER_WEB_PASSWORD="geoserver"
export MTPROP_GEOSERVER_DBUSER="postgres"
export MTPROP_DB_USERNAME="postgres"
export MTPROP_DB_PASSWORD="postgres123"
export MTPROP_TMPDIR_PATH="/app/data/geoserver/tmp/"
export MTPROP_TMPDIRGEOSERVERPATH="/app/data/geoserver/data"
export MTPROP_NAMESPACE_ID="NamespaceInfoImpl--21489445:78545345680:-7ffe"
export MTPROP_DATASTORE_ID="DataStoreInfoImpl--21483454:136743037af:-7ffg"
```

### Build commands

```sh
sh @ci/build-and-deploy.sh
```

### Maven Toolbox Documentation

- [maven-toolbox](https://github.com/harshzalavadiya/maven-toolbox/blob/master/README.md)
