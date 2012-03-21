package com.nis.server.handlers;

import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.nis.server.Handle;
import com.nis.server.ServerInfo;
import com.nis.shared.requests.GetSessionKey;
import com.nis.shared.response.GetSessionKeyResult;
import com.nis.shared.KeyGen;
import com.nis.shared.SessionKeyEnvelope;

public class GetSessionKeyHandler implements Handle {

	@Override
	public String handle(String request, InetSocketAddress source, 
			ServerInfo serverInfo) {
		Gson gson = new Gson();
		SecureRandom sr = new SecureRandom();
		GetSessionKey getSessionKey = gson.fromJson(request,
				GetSessionKey.class);
		String handleA = getSessionKey.handleA;
		String handleB = getSessionKey.handleB;
		int nonceA = getSessionKey.nonceA;
		int nonceB = getSessionKey.nonceB;
		int saltA = sr.nextInt();
		int saltB = sr.nextInt();
		
		// TODO(henkjoubert) check that encryption works
		SecretKey sessionKey = KeyGen.genKey();
		String keyString = DatatypeConverter.printBase64Binary(sessionKey.getEncoded());
		SecretKey keyA = serverInfo.getUserKey(handleA);
		SecretKey keyB = serverInfo.getUserKey(handleB);
		SessionKeyEnvelope envA = new SessionKeyEnvelope(
				saltA, nonceA, handleA, keyString);
		SessionKeyEnvelope envB = new SessionKeyEnvelope(
				saltB, nonceB, handleB, keyString);
		String rawA = gson.toJson(envA);
		String rawB = gson.toJson(envB);
		byte [] encodedA = null;
		byte [] encodedB = null;
		try {
			Cipher cA = Cipher.getInstance("AES/CBC/PKCS5Padding");
			Cipher cB = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte [] iv_bytes =	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			IvParameterSpec ivspec = new IvParameterSpec(iv_bytes);
			cA.init(Cipher.ENCRYPT_MODE, keyA, ivspec);
			cB.init(Cipher.ENCRYPT_MODE, keyB, ivspec);
			encodedA = cA.doFinal(rawA.getBytes());
			encodedB = cB.doFinal(rawB.getBytes());
		} catch (NoSuchPaddingException e) {
			//should not happen
		} catch (NoSuchAlgorithmException e) {
			//should not happen
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			//should not happen
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			System.err.println("I fucked up with the ivspec :(");
			e.printStackTrace();
		}
		
		String encryptedKeyA = DatatypeConverter.printBase64Binary(encodedA) ;
		String encryptedKeyB = DatatypeConverter.printBase64Binary(encodedB);
		
		GetSessionKeyResult result = new GetSessionKeyResult(encryptedKeyA,
				encryptedKeyB);
		
		return gson.toJson(result);
	}
}
