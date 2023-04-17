 mvn quarkus:add-extension -Dextensions="docker"
 mvn package -Dquarkus.package.type=native -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dmaven.test.skip=true
