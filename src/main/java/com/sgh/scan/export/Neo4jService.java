package com.sgh.scan.export;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
class Neo4jService implements AutoCloseable {

	final Driver driver;
	final Neo4jDbConfiguration neo4jDbConfiguration;

	Neo4jService(Neo4jDbConfiguration neo4jDbConfiguration) {
		log.info("Start Preparing Neo4j Database Configuration");
		this.neo4jDbConfiguration = neo4jDbConfiguration;
		if (!"".equals(neo4jDbConfiguration.user) && !"".equals(neo4jDbConfiguration.pass)) {
			driver = GraphDatabase.driver(neo4jDbConfiguration.host,
					AuthTokens.basic(neo4jDbConfiguration.user, neo4jDbConfiguration.pass));
		} else {
			driver = GraphDatabase.driver(neo4jDbConfiguration.host);
		}
		log.info("End Preparing Neo4j Database Configuration");
	}

	public List<Record> query(final String query) {
		Session session = null;
		var sessionID = UUID.randomUUID().toString();
		try {
			log.info("Create New Neo4j Session[" + sessionID + "]");
			session = driver.session();
			log.info("Query Neo4j Data");
			return session.writeTransaction(tx -> tx.run(query).list());
		} finally {
			if (session != null) { session.close(); log.info("Neo4j Session[" + sessionID + "] Closed"); }
		}
	}

	@Override
	public void close() throws Exception {
		if (driver != null) { driver.close(); log.info("Neo4j Driver Instance Automatically Closed"); }
	}
}
