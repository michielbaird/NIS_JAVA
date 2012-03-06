package com.nis.shared.requests;

public class GetSessionKey {
	public String handleA;
	public String handleB;
	public int nonceA;
	public int nonceB;
	
	public GetSessionKey(String handleA, String handleB, int nonceA, int nonceB) {
		this.handleA = handleA;
		this.handleB = handleB;
		this.nonceA = nonceA;
		this.nonceB = nonceB;
	}
}
