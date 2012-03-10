package com.nis.shared;


public class Request {
	public String from;
	public String method;
	public Integer id;
	public String params;
	public String signature;

	public Request(String from, String method, Integer id, String params) {
		this.from = from;
		this.method = method;
		this.id = id;
		this.params = params;
	}
}
