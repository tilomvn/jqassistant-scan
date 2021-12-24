package com.sgh.scan.export.mapper;

import com.sgh.scan.export.dto.NodeDTO;
import com.sgh.scan.export.dto.RelationshipDTO;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MongoMapper {

	public static List<Document> nodeDtosToDocuments(Collection<NodeDTO> dtos) {
		return dtos.stream().map(nodeDTO -> nodeDtoToDocument(nodeDTO)).collect(Collectors.toList());
	}

	public static List<Document> relationDtosToDocuments(List<RelationshipDTO> dtos) {
		return dtos.stream().map(relationshipDTO -> relationDtoToDocument(relationshipDTO)).collect(Collectors.toList());
	}

	static Document nodeDtoToDocument(NodeDTO dto) {
		Document doc = new Document();
		doc.put("ID", dto.getId());
		doc.put("Service", dto.getService());
		doc.put("Class", dto.getClassName());
		doc.put("Method", dto.getMethod());
		doc.put("RequestMethod", dto.getRequestMethod());
		doc.put("URL", dto.getUrl());
		if (dto.getParameter() != null) {
			doc.put("Parameter", dto.getParameter());
		}
		return doc;
	}

	static Document relationDtoToDocument(RelationshipDTO dto) {
		Document doc = new Document();
		doc.put("Start", dto.getStart());
		doc.put("End", dto.getEnd());
		doc.put("Relation", dto.getRelation());
		return doc;
	}
}
