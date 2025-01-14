Instruction:
1. clone this repo from github
2. load the project into your ide ( IntelliJ )
3. create new module and check from Existing source
4. navigate to the services/validation/pom.xml and import the module
5. run the tradeEligibity.feature file from test area


To test retry and dead letter topic
1. make sure docker is installed in your machine
2. cd into risk-service folder
3. run the following docker command : docker compose up -d
4. make sure the container is up  and kafaka broker and zookeeper is also up
5. Start the ValidationApplication, make sure no errors in the log
6. open a terminal window/git-bash shell and send the following curl command

test post endpoint:
curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"tradeRef":"abcdef","productType":"FX Swap", "account":"abcd12345","maturity":"2025-01-14", "notionalAmount":"1000" }' http://localhost:8090/api/v1/trades
