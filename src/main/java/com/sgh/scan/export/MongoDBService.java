package com.sgh.scan.export;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.List;

@Service
@Slf4j
class MongoDBService implements AutoCloseable {

	final MongoClient mongoDbClient;
	final MongoDbConfiguration mongoDbConfiguration;

	MongoDBService(MongoDbConfiguration mongoDbConfiguration) {
		this.mongoDbConfiguration = mongoDbConfiguration;

		log.info("Start Preparing Mongo Database Configuration");
		var credential = String.format("%s:%s@", mongoDbConfiguration.user, mongoDbConfiguration.pass);
		if (credential.equals(":@")) {
			credential = "";
		}

		//TODO: mongo of production
		//String connection = String.format("mongodb+srv://%s%s/?retryWrites=true&w=majority", credential, mongoDbConfiguration.host);

		//TODO: sgh mongo url
		var connection = String.format("mongodb://%s%s/?retryWrites=true&w=majority", credential, mongoDbConfiguration.host);

		this.mongoDbClient = MongoClients.create(connection);
		log.info("End Preparing Mongo Database Configuration");
	}

	public boolean exportToMongoDB(List<Document> nodes, String collectionName, boolean reset) {
		try {
			var pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
					fromProviders(PojoCodecProvider.builder().automatic(true).build()));

			var database = mongoDbClient.getDatabase(mongoDbConfiguration.db);
			database = database.withCodecRegistry(pojoCodecRegistry);

			var components = database.getCollection(collectionName);
			if (components.countDocuments() > 0 && reset) {
				components.drop();
			}
			components.insertMany(nodes);
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
		return true;
	}

	public boolean exportToMongoDB(String content, String collectionName) {
		try {
			var pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
					fromProviders(PojoCodecProvider.builder().automatic(true).build()));

			var database = mongoDbClient.getDatabase(mongoDbConfiguration.db);
			database = database.withCodecRegistry(pojoCodecRegistry);

			var components = database.getCollection(collectionName);
			if (components.countDocuments() > 0) {
				components.drop();
			}
			components.insertOne(new Document().append("items", content));
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void close() throws Exception {
		if (mongoDbClient != null) { mongoDbClient.close(); log.info("MongoDB Client Instance Automatically Closed"); }
	}
}
