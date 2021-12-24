package com.sgh.scan.export.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NodeDTO {
	private String id;
	private String service;
	private String className;
	private String method;
	private String requestMethod;
	private String url;
	private List<Parameter> parameter;

	@Getter
	@AllArgsConstructor
	public static class Parameter {
		String type;
		String name;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Parameter nodeDTO = (Parameter) o;

			if (!type.equals(nodeDTO.type)) return false;
			if (!name.equals(nodeDTO.name)) return false;
			return true;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NodeDTO nodeDTO = (NodeDTO) o;

		if (!method.equals(nodeDTO.method)) return false;
		if (!url.equals(nodeDTO.url)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + method.hashCode();
		result = 31 * result + url.hashCode();
		return result;
	}
}
