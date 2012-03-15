package com.nis.client.handlers;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.gson.Gson;
import com.nis.client.Handle;
import com.nis.shared.interactive.SendFileConfirm;
import com.nis.shared.requests.SendFile;
import com.nis.shared.response.SendFileResult;

public class SendFileHandler implements Handle {
	public static final int buffer_size = 4096;
	@Override
	public String handle(HandleParameters parameters) {
		// TODO(michielbaird) Add confirmation.
		Gson gson = new Gson();
		SendFile sendFile = gson.fromJson(parameters.request, SendFile.class);
		SendFileConfirm confirm = new SendFileConfirm(true);
		SendFileResult result = null;
		parameters.outToHost.write(gson.toJson(confirm) + "\n");
		parameters.outToHost.flush();
		try {
			FileOutputStream fos = new FileOutputStream("copy_" + sendFile.filename);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			long fileSizeRemaining = sendFile.fileSize;
			while (fileSizeRemaining > 0) {
				byte [] byteArray = new byte[buffer_size];
				int readSize = fileSizeRemaining > buffer_size ? 
						buffer_size : (int)fileSizeRemaining;
				int bytes_read = parameters.inputStream.read(byteArray, 0, readSize);
				fileSizeRemaining -= bytes_read;
				// TODO(michielbaird) Add progress callbacks.
				bos.write(byteArray,0,bytes_read);
				bos.flush();
			}
			bos.close();
			result =  new SendFileResult("success");
		
		} catch (FileNotFoundException e) {
			//Should never happen.
			e.printStackTrace();
		} catch (IOException e) {
			result =  new SendFileResult("failure");
			e.printStackTrace();
		}
		return gson.toJson(result);
	}

}
