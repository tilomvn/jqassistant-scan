package com.sgh.scan.export.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RelationshipDTO {
	String start;
	String end;
	String relation;
}
