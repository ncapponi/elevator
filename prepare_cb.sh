# generate zip file for cloudbees deployment
mvn clean install
cd target/classes
zip -r ../../../elevator.zip org/nca/* target/deps/*.jar  
cd ../..
zip -r -j ../elevator.zip target/deps/*.jar  
