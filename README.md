# Pre Setup 
Create free account on browser stack

# local setup 
download android SKD , appium server and set environment variables

# Set up
update username and accesskey fields for browser stack

`mvn clean install` - to fetch dependencies and run test (needed only for first time)

`mvn clean test -Denv=local -Dtest=AppTest` - for local execution on connected Android phone
`mvn test -Dtest=TheAppTest` - for Browser Stack execution of iOS Test
`mvn test -Dtest=AppTest` - for Browser Stack execution of android Test
