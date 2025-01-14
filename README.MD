Instruction:
1. clone this repo from github
2. load the project into your ide ( IntelliJ )
3. create new module and check from Existing source
4. navigate to the services/validation/pom.xml and import the module
5. run the tradeEligibity.feature file from test area



test post endpoint:
curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"tradeRef":"abcdef","productType":"FX Spot", "account":"abcd12345","maturity":"2025-01-14", "notionalAmount":"1000" }' http://localhost:8090/api/v1/trades
