## Deploy the elevator to cloudbees 
## You must first have created your cloudbees application with following command :
##  > bees create APP_NAME

[ -z "$1" ] && echo "Please provide your cloudbees application name as argument" 

APP_NAME=$1

# Deploy APP_NAME application to cloudbees
bees app:deploy -a $APP_NAME -t java -Rclass=org.nca.elevator.Server -Rjava_version=1.7 -Rargs='find $app_port ClassicStrategy' target/elevator-0.1.0-SNAPSHOT.jar

