
# SshCommander

This is a basic JAVA/SWING programm used to send mutliple SSH or TELNET commands to multiple hosts.

![Preview1](./HMI_SshCommander.png)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

You'll need at least JAVA 1.6 (or above) and MAVEN 2 (or above)

### Installing

*Download the project folder.
*From the local copy, go in the project folder and execute:
**mvn clean install**
*It will build the file SshCommander-X.X-jar-with-dependencies.jar
*It is an executable jar, that you can run either with the following command:
**java -jar SshCommander-X.X-jar-with-dependencies.jar**
*either by double click on it (on windows for example).


## Built With

* [Maven](https://maven.apache.org/) - Dependency Management


## Authors

* **Quentin MORRIER** - *Creator* - [qmorrier](https://github.com/qmorrier)


## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

Based on [JSCH](http://www.jcraft.com/jsch/)


## Usage

The hosts are declared as:  
**user**:**password**@**hostIp**

And they are separated by a comma (**,**) char.

There are two types of command sending:

* **Exec mode** : sends all the command in one string (carriage return apply the commands line by line)
* **Shell mode** : sends commands one by one (line by line). The last command must close the connection (like exit for a standart linux Sssh server)


The option **Export as files**, creates a directory in the same folder where the executable jar is, with as name the cureent date/time.  
Within this folder, it creates one file per host holding the result of the command.  
There is also a root file, wich hold all the logs (just like the *Response* area).  

You can increase or degrease the font size via:  
**[CTRL]** + **mouse wheel**

You can switch the dark theme to default SWING theme via:  
**[CTRL]** + **[b]**



