# Application Terminology
* Git Cloner
* API to register repository information
* JQAssistant Scan Plugin
* JQAssistant Plugin Runner
* Database Neo4j to store scanned data
* Export to MongoDB for visualization by Graphviz

# Other
* First modify maven repository folder in settings.xml according to your machine type. 
* Open terminal, locate to root directory of project, run cmd [mvnw -s settings.xml jqassistant:server]
* Open other terminal, locate to root directory of project, run cmd [mvnw -s settings.xml spring-boot:run]. Open browser with url: http://localhost:7474/browser/ and enjoy !
* Sample query for CS -> RE : MATCH (n:Service)-[r]->(n1)-[r1]->(n2)<-[r2:DECLARES_ENDPOINT]-(n3) RETURN *
* Sample query for CS -> RM : MATCH (n:Service)-[r]->(n1)-[r1]->(n2) RETURN *
* Database directory: /target/jqassistant
* Manually Run Ekb Scan: make request to api: http://localhost:8088/api/scan/ekb
* Git sample: https://golb.hplar.ch/2019/08/git-with-jgit.html
