# dummy-mock-generator Maven Plugin

## Overview

The `dummy-mock-generator` Maven Plugin is designed for creating mock objects from GDSL files and generating GDSL files from a groovy scripts. It is primarily intended for testing Jenkins shared libraries but can be used in any scenario where classes are not available for mocking.

## Features

1. **Generate GDSL Files**: The plugin scans the `vars` directory and generates GDSL files for all scripts.
2. **Generate Mock Classes**: In the second step, the plugin generates mock classes from the GDSL file. These mock classes have the same signature as the definitions in the GDSL file but only print the parameter values in their implementation.



## Configuration

To configure the `dummy-mock-generator` Maven Plugin, add the following to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>cz.kahle.maven.plugins</groupId>
            <artifactId>dummy-mock-generator-maven-plugin</artifactId>
            <version>1.0.13</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate-gdsl</goal>
                        <goal>generate-mocks</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Replace the version with the appropriate version of the plugin you are using.

## Usage
Jenkins Global variables are called using  fileName.metodName reference. The file is compiled as a class with the same name, so when called outside Jenkins, it is assumed that the method is static. Therefore, it is necessary to dynamically add a static method to the class and then add the call to the mocked method within it.
See [example of groovy Spock mock](src/it/simple-it/test/groovy/CommonSpec.groovy)    



## Development of the plugin
### Run test insluding integration tests placed in srt/it/simple-it
```shell
mvn clean verify -Pintegration-test
```
### Release to sontype central
```shell
mvn clean release:prepare release:perform -Prelease -Pgpg-maven-central
```