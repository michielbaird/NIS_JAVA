package com.nis.client;

import java.util.Set;

import com.nis.shared.requests.SendFile;

public interface ClientCallbacks {
	public static class ConfirmResult{
		public boolean accept;
		public String fileName;
	}
	public void onClientListReceived(Set<String> clientList);
	public void onClientMessageRecieved(String handle, String message);
	public void onFileReceived(String filename);
	public ConfirmResult onIncomingFile(SendFile sendFile);
}
