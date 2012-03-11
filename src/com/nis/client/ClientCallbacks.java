package com.nis.client;

import java.util.Set;

public interface ClientCallbacks {
	public void onClientListReceived(Set<String> clientList);
}
