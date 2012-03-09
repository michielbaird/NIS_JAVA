package com.nis.shared.requests;

public class SendFile {
	public String filename;
	public long fileSize;
	public SendFile(String fileName, Long fileSize) {
		this.filename = fileName;
		this.fileSize = fileSize;
	}
}
