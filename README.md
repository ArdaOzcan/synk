![logo](img/synk_logo.png)
# synk
Synk is a basic tool to download files from another device in a network.

You can sync your projects, songs, images with two diferent devices as long 
as they are in the same network. 

*Notes:* 

* Synk doesn't delete any files but it can overwrite files with the same 
name. 

* Synk is not a version control system so it __doesn't__ keep track of older versions.

* Synk connections are __not__ yet encrypted. Only using in your private LAN is recommended.

# Downloading Release
Go to [releases](https://github.com/ArdaOzcan/synk/releases) and download the jar.

Go to the folder of your .jar and run

`java -jar synk-0.0.1-jar-with-dependencies.jar`

or create a script running the jar and add it to your PATH
to use synk from anywhere.


# Downloading the Source
Run this if you have git installed

`git clone https://github.com/ArdaOzcan/synk.git`

or download the zip from github

# Building
## Dependencies
* [Java SE >= 13](https://www.oracle.com/tr/java/technologies/javase-downloads.html)
* [Maven CLI](https://maven.apache.org/download.cgi)

Run build.sh or build.bat

# Executing the .jar
cd to target/ and run

`java -jar synk-0.0.1-jar-with-dependencies.jar`

# Usage
*Commands:* 
### exit
Stop the synk process and exit.
### serve
Start a synk server.
### connect
Connect to a synk server.

*Positional argument:* IP or name of the server.
### synk
Download hosted files from the connected server.
### list
List running synk server informations in the network.
