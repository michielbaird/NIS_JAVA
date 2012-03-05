package com.nis.shared;


public class Response {
	public String result;
	public Integer id;
	public Integer error;
	public Response(String result, Integer id, Integer error) {
		this.result = result;
		this.id = id;
		this.error = error;
	}
}
