package com.sgh.scan.export;

import com.sgh.scan.export.dto.NodeDTO;
import com.sgh.scan.export.dto.RelationshipDTO;
import com.sgh.scan.export.mapper.MongoMapper;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Rank.RankDir;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.InternalEntity;
import org.neo4j.driver.internal.InternalRelationship;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sgh.scan.common.CommonConstants.*;
import static com.sgh.scan.export.DataExportService.NodeType.*;
import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

@Slf4j
@Service
public class DataExportService {

	Set<NodeDTO> nodes;
	List<RelationshipDTO> relationships;
	MutableGraph graphviz;
	Map<String, String> nodesId;

	long nodeCSId;
	long nodeREId;
	long nodeRMId;

	//TODO: paging will be implemented : SKIP 0 LIMIT 255 -> 0 - 254 = page 1, 255 - 509 = page 2, record per page = 255
	private final String queryCSToRE = "MATCH (n:Service)-[r]->(n1)-[r1]->(n2)<-[r2:DECLARES_ENDPOINT]-(n3) RETURN * LIMIT 255";
	private final String queryCSToRM = "MATCH (n:Service)-[r]->(n1)-[r1:CALL_TO_SERVICE]->(n2) RETURN * LIMIT 255";

	final Neo4jService neo4jDriver;
	final MongoDBService mongoService;

	DataExportService(Neo4jService neo4jDriver, MongoDBService mongoService) {
		this.neo4jDriver = neo4jDriver; this.mongoService = mongoService;
	}

	// every twelve hours
	//@Scheduled(cron="0 * * ? * *")
	public void exportData() {
		var rsCSToRE = neo4jDriver.query(queryCSToRE);//102 records
		if (rsCSToRE != null && !rsCSToRE.isEmpty()) {
			nodes = new HashSet<>();
			nodesId = new HashMap<>();
			nodeCSId = 0L;
			nodeREId = 0L;
			nodeRMId = 0L;
			graphviz = mutGraph().setDirected(true).graphAttrs().add(Rank.dir(RankDir.LEFT_TO_RIGHT)).nodeAttrs().add(Style.FILLED);
			relationships = new ArrayList<>(rsCSToRE.size());

			rsCSToRE.forEach(re -> processingCSToRE(re));
			var dotFile = Graphviz.fromGraph(graphviz).render(Format.DOT).toString();
			log.info(dotFile);
			var docRelations =  MongoMapper.relationDtosToDocuments(relationships);
			mongoService.exportToMongoDB(docRelations, "interactions", true);
			mongoService.exportToMongoDB(dotFile, "graphCSToRE");
		}

		var rsCSToRM = neo4jDriver.query(queryCSToRM);//44 records
		if (rsCSToRM != null && !rsCSToRM.isEmpty()) {
			if (nodes == null) {
				nodes = new HashSet<>();
				nodesId = new HashMap<>();
				nodeCSId = 0L;
				nodeREId = 0L;
				nodeRMId = 0L;
			}
			graphviz = mutGraph().setDirected(true).graphAttrs().add(Rank.dir(RankDir.LEFT_TO_RIGHT)).nodeAttrs().add(Style.FILLED);
			relationships = new ArrayList<>(rsCSToRM.size());

			rsCSToRM.forEach(re -> processingCSToRM(re));
			var dotFile = Graphviz.fromGraph(graphviz).render(Format.DOT).toString();
			log.info(dotFile);
			var docRelations =  MongoMapper.relationDtosToDocuments(relationships);
			mongoService.exportToMongoDB(docRelations, "interactions", rsCSToRE == null || rsCSToRE.isEmpty());
			mongoService.exportToMongoDB(dotFile, "graphCSToRM");
		}

		var docNodes =  MongoMapper.nodeDtosToDocuments(nodes);
		mongoService.exportToMongoDB(docNodes, "components", true);

		log.info("Total Nodes: " + (nodeCSId + nodeREId + nodeRMId));

//			try {
//				Graphviz.fromGraph(graphviz).render(Format.PNG).toFile(new File("example/demo.png"));
//			} catch (Exception e) {
//				log.error(e.getMessage());
//			}
	}

	String generateId(NodeType type, long oldId) {
		var id = nodesId.get(type.name() + oldId);
		if (id == null) {
			if (CS.equals(type)) {
				nodeCSId++;
				id = type + String.format("%03d", nodeCSId);
			} else if (RE.equals(type)) {
				nodeREId++;
				id = type + String.format("%03d", nodeREId);
			} else if (RM.equals(type)) {
				nodeRMId++;
				id = type + String.format("%03d", nodeRMId);
			}
			nodesId.put(type.name() + oldId, id);
		}
		return id;
	}

	void processingCSToRE(Record re) {
		var nodesData = re.asMap();
		var fileService = ((InternalEntity) nodesData.get("n"));
		//var declareEndpointCallerRelation = ((InternalRelationship) nodesData.get("r"));
		var endpointCaller = ((InternalEntity) nodesData.get("n1"));
		var callToEndPointRelation = ((InternalRelationship) nodesData.get("r1"));
		var restEndpoint = ((InternalEntity) nodesData.get("n2"));
		//var declareEndpointRelation = ((InternalRelationship) nodesData.get("r2"));
		var restController = ((InternalEntity) nodesData.get("n3"));

		var serviceDto = NodeDTO.builder().id(generateId(CS, endpointCaller.id())/*"CS" + endpointCaller.id()*/)
				.service(String.valueOf(endpointCaller.asMap().get("microservice")))
				.className(fileService.asMap().get("value").toString())
				.method(endpointCaller.asMap().get("value").toString())
				.requestMethod(endpointCaller.asMap().get("requestMethod").toString())
				.url(endpointCaller.asMap().get("endpointFullPath").toString())
				.parameter(null).build();
		nodes.add(serviceDto);
		var endpointDto = NodeDTO.builder().id(generateId(RE, restEndpoint.id()))
				.service(endpointCaller.asMap().get("microservice").toString())
				.className(restController.asMap().get("value").toString())
				.method(restEndpoint.asMap().get("value").toString())
				.requestMethod(restEndpoint.asMap().get("requestMethod").toString())
				.url(restEndpoint.asMap().get("endpointPath").toString())
				.parameter(getParameters(restEndpoint.asMap().get("paramTypes").toString())).build();
		nodes.add(endpointDto);

		var relationshipDto = RelationshipDTO.builder()
				.start(serviceDto.getId()).end(endpointDto.getId())
				.relation(callToEndPointRelation.type())
				.build();
		relationships.add(relationshipDto);

		//Build graphviz
		var fs = mutNode(fileService.asMap().get("value").toString());
		var sc = mutNode(endpointCaller.asMap().get("value").toString());
		var ep = mutNode(restEndpoint.asMap().get("endpointPath").toString());
		var fc = mutNode(restController.asMap().get("value").toString());

		graphviz.add(
				fs.addLink(sc),
				sc.addLink(ep),
				fc.addLink(ep)
				);
	}

	void processingCSToRM(Record re) {
		var nodesData = re.asMap();
		var fileService = ((InternalEntity) nodesData.get("n"));
		//var declareEndpointCallerRelation = ((InternalRelationship) nodesData.get("r"));
		var endpointCaller = ((InternalEntity) nodesData.get("n1"));
		var callToServiceRelation = ((InternalRelationship) nodesData.get("r1"));
		var remoteService = ((InternalEntity) nodesData.get("n2"));

		var serviceDto = NodeDTO.builder().id(generateId(CS, endpointCaller.id()))
				.service(String.valueOf(endpointCaller.asMap().get("microservice")))
				.className(fileService.asMap().get("value").toString())
				.method(endpointCaller.asMap().get("value").toString())
				.requestMethod(String.valueOf(endpointCaller.asMap().get("requestMethod")))
				.url(String.valueOf(endpointCaller.asMap().get("endpointFullPath")))
				.parameter(null).build();
		nodes.add(serviceDto);
		var remoteServiceDto = NodeDTO.builder().id(generateId(RM, remoteService.id()))
				.service(remoteService.asMap().get("microservice").toString())
				.className("undefined")
				.method(remoteService.asMap().get("value").toString())
				.requestMethod(remoteService.asMap().get("requestMethod").toString())
				.url(remoteService.asMap().get("endpointFullPath").toString())
				.parameter(null).build();
		nodes.add(remoteServiceDto);

		var relationshipDto = RelationshipDTO.builder()
				.start(serviceDto.getId()).end(remoteServiceDto.getId())
				.relation(callToServiceRelation.type())
				.build();
		relationships.add(relationshipDto);

		//Build graphviz
		var fs = mutNode(fileService.asMap().get("value").toString());
		var sc = mutNode(endpointCaller.asMap().get("value").toString());
		var ep = mutNode(remoteService.asMap().get("endpointFullPath").toString());
		var fc = mutNode("undefined");

		graphviz.add(
				fs.addLink(sc),
				sc.addLink(ep),
				fc.addLink(ep)
		);
	}

	List<NodeDTO.Parameter> getParameters(String paramTypes) {
		if (paramTypes == null || BLANK.equals(paramTypes)) { return Collections.emptyList(); }
		return Stream.of(paramTypes.split(COMMA)).map(param -> {
			var spt = param.split(SPACE);
			return new NodeDTO.Parameter(spt[0], spt[1]);
		}).collect(Collectors.toList());
	}

	enum NodeType { CS, RE, RM }
}
