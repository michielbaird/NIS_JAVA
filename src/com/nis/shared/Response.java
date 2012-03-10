package com.nis.shared;


public class Response {
	public String result;
	public Integer id;
	public ErrorMessages error;
	public String signature;

	public Response(String result, Integer id, ErrorMessages error) {
		this.result = result;
		this.id = id;
		this.error = error;
	}
}
