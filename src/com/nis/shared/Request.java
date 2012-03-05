package com.nis.shared;


public class Request {
	public String method;
	public Integer id;
	public String params;
	
	public Request(String method, Integer id, String params) {
		this.method = method;
		this.id = id;
		this.params = params;
	}
}
