# Mean-genie
<img align="right" src="https://vignette.wikia.nocookie.net/muppet/images/d/d2/MeanGenie.jpg/revision/latest/scale-to-width-down/280?cb=20101120230645">

Mean-genie is a service that is under development.

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
It doesn't