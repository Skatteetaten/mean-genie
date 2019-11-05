# Mean-genie
<img align="right" src="https://vignette.wikia.nocookie.net/muppet/images/d/d2/MeanGenie.jpg/revision/latest/scale-to-width-down/280?cb=20101120230645">

Mean-genie is a service that listens to the Kubernetes cluster in order to act when the CRD "ApplicationDeployment" gets deleted.
When a CRD gets deleted mean-genie will put the database schema associated with the CRD in cooldown by calling the [DBH](https://github.com/Skatteetaten/dbh) API.

The component is named after Mean-genie from the TV-show Fraggle Rock (https://muppet.fandom.com/wiki/Genie).

 ## Setup
 
 In order to use this project you must set repositories in your `~/.gradle/init.gradle` file
 
     allprojects {
         ext.repos= {
             mavenCentral()
             jcenter()
         }
         repositories repos
         buildscript {
          repositories repos
         }
     }

We use a local repository for distributionUrl in our gradle-wrapper.properties, you need to change it to a public repo in order to use the gradlew command. `../gradle/wrapper/gradle-wrapper.properties`

    <...>
    distributionUrl=https\://services.gradle.org/distributions/gradle-<version>-bin.zip
    <...>

## How it works  
Deploy Mean-genie in the cluster you want it to listen to and it will by default put database schemas in cooldown when an ApplicationDeployment is deleted.

Mean-genie can be customized to only act on spesific events by setting the environment variable OPERATION_SCOPE to a certain value. 
If OPERATION_SCOPE is set in the Mean-genie to `foo` then it will only act on delete events for ApplicationDeployments with the same label `foo`.
