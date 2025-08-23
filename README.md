# Pre Setup 

Create free account on browser stack
download android SKD and set environment variables: for local execution.
download appium server for local execution

# Set up

update username and accesskey fields for browser stack

`mvn clean install` - to fetch dependencies and run test (needed only for first time)

`mvn clean test -Denv=local` - for local execution on connected phone
`mvn clean test` - for cloud execution on connected phone
