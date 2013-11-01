# copy all classes to bin for openshift deployment
rm -rf ./bin
mvn clean install
cp -R target/classes/ bin
git add -A
