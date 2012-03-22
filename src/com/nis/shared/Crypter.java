package com.nis.shared;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


public class Crypter {

	public static String decrypt(String base64EncodedString, SecretKey aesKey) {
		byte [] raw_bytes = Base64Coder.decode(base64EncodedString);
		byte [] decoded = null;
		try {
			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte [] iv_bytes =	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			IvParameterSpec ivspec = new IvParameterSpec(iv_bytes);
			c.init(Cipher.DECRYPT_MODE, aesKey, ivspec);
			decoded = c.doFinal(raw_bytes);
		} catch (NoSuchPaddingException e) {
			//should not happen
		} catch (NoSuchAlgorithmException e) {
			//should not happen
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			//should not happen
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			//should not happen
			e.printStackTrace();
		} catch (BadPaddingException e) {
			//should not happen
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("I fucked up with the ivspec :(");
			e.printStackTrace();
		}
		//char set?
		return new String(decoded);
	}
	
	public static String encrypt(String message, SecretKey aesKey){
		byte [] raw = message.getBytes();
		byte [] encoded = null;
		try {
			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte [] iv_bytes =	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			IvParameterSpec ivspec = new IvParameterSpec(iv_bytes);
			c.init(Cipher.ENCRYPT_MODE, aesKey, ivspec);
			encoded = c.doFinal(raw);
		} catch (NoSuchPaddingException e) {
			//should not happen
		} catch (NoSuchAlgorithmException e) {
			//should not happen
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			//should not happen
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			//should not happen
			e.printStackTrace();
		} catch (BadPaddingException e) {
			//should not happen
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("I fucked up with the ivspec :(");
			e.printStackTrace();
		}
		return new String(Base64Coder.encode(encoded));
	}
}
