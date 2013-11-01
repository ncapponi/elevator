# Generate zip file for cloudbees deployment
mvn clean install
cd target/classes
zip -r ../../elevator.zip org/nca/*
cd ../..
zip -r -j elevator.zip target/deps/*.jar  
