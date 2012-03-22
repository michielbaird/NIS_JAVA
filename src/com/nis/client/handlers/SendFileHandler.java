package com.nis.client.handlers;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import com.google.gson.Gson;
import com.nis.client.ClientCallbacks.ConfirmResult;
import com.nis.client.DataTransferException;
import com.nis.client.Handle;
import com.nis.shared.interactive.SendFileConfirm;
import com.nis.shared.requests.SendFile;
import com.nis.shared.response.SendFileResult;

public class SendFileHandler implements Handle {
	public static final int buffer_size = 4096;
	@Override
	public String handle(HandleParameters parameters) throws DataTransferException {
		// TODO(michielbaird) Add confirmation.
		Gson gson = new Gson();
		SendFile sendFile = gson.fromJson(parameters.request, SendFile.class);
		SendFileConfirm confirm;
		SendFileResult result = null;
		String fileName = null;
		
		if (parameters.sessionHandler.getCallbacks() != null) {
			ConfirmResult confirmReq = parameters.sessionHandler
				.getCallbacks()
				.onIncomingFile(sendFile);
			if (confirmReq.accept) {
				fileName = confirmReq.fileName;
				confirm = new SendFileConfirm(true);
				parameters.outToHost.write(gson.toJson(confirm) + "\n");
				parameters.outToHost.flush();
			} else {
				confirm = new SendFileConfirm(false);
				parameters.outToHost.write(gson.toJson(confirm) + "\n");
				parameters.outToHost.flush();
				result = new SendFileResult("rejected");
				return gson.toJson(result);
			}
		} else {
			confirm = new SendFileConfirm(false);
			parameters.outToHost.write(gson.toJson(confirm) + "\n");
			parameters.outToHost.flush();
			result = new SendFileResult("rejected");
			return gson.toJson(result);
		}

		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			long fileSizeRemaining = sendFile.fileSize;
			
			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte [] iv_bytes =	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			IvParameterSpec ivspec = new IvParameterSpec(iv_bytes);
			c.init(Cipher.DECRYPT_MODE, parameters.sessionHandler.getKey(parameters.handle), ivspec);
			CipherInputStream cis = new CipherInputStream(parameters.inputStream, c);
			
			while (fileSizeRemaining > 0) {
				byte [] byteArray = new byte[buffer_size];
				int readSize = fileSizeRemaining > buffer_size ? 
						buffer_size : (int)fileSizeRemaining;
				int bytes_read = cis.read(byteArray, 0, readSize);
				fileSizeRemaining -= bytes_read;
				// TODO(michielbaird) Add progress callbacks.
				bos.write(byteArray,0,bytes_read);
				bos.flush();
			}
			bos.close();
			if (parameters.sessionHandler.getCallbacks() != null) {
				parameters.sessionHandler.getCallbacks()
					.onFileReceived("copy_" + sendFile.filename);
			}
			throw new DataTransferException();
		
		} catch (FileNotFoundException e) {
			//Should never happen.
			e.printStackTrace();
		} catch (IOException e) {
			result =  new SendFileResult("failure");
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
