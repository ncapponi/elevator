elevator
========

Elevator for code story

* prepare
 * install maven
 * install bees SDK (to deploy to cloudbees) 
* build
 * mvn clean install
* run 
 * with main class org.nca.elevator.Server and three args : hostname port strategy
  * hostname : "localhost" or "find" to automatically retrieve ip address (needed by cloudbees)
  * port : listening port
  * strategy : name of strategy class to use, the class must be located in org.nca.elevator.package
   * possible values: ClassicStrategy, MostUsersStrategy 

* deploy to cloudbees
 * create cloudbees app: bees create myApp
 * prepare: ./prepare_cloudbees.sh
 * deploy: ./deploy_to_cloudbees.sh myApp



