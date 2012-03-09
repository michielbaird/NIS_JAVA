package com.nis.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;

public interface DataTransferCallback {
	public class TransferParameters{
		public final BufferedReader inFromHost;
		public final DataOutputStream outToHost;
		public TransferParameters(BufferedReader inFromHost, DataOutputStream outToHost){
			this.inFromHost = inFromHost;
			this.outToHost = outToHost;
		}
	}
	public void transferData(TransferParameters parameters);

}
