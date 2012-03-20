package com.nis.shared;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
//to simplify creating the client keys
import com.google.gson.Gson;
import com.nis.client.ClientKeys;
import com.nis.server.ServerInfo;
import javax.xml.bind.DatatypeConverter;

public class KeyGen {
	
	public static SecretKey genKey () { 
		KeyGenerator kgen = null;
		try {
			kgen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			// this should not happen
			e.printStackTrace();
		}
		kgen.init(256);
		SecretKey key = kgen.generateKey();
		return key;
	}
	
	public static SecretKey getKeyFromFile(String fileName)  {
		SecretKey key = null;
		File f = new File(fileName);
		int len = (int) f.length();
		byte [] encoded = new byte[len];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		
			fis.read(encoded, 0, len);
			key = new SecretKeySpec(encoded, "AES");
		} catch (FileNotFoundException e) {
			System.err.println("AES not supported on this platform");
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return key;
	}
	
	public static KeyPair genKeyPair() {
		KeyPairGenerator kpGen = null;
		try {
			kpGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			// This should not be thrown
			System.err.println("RSA not supported on this platform");
			e.printStackTrace();
		}
		kpGen.initialize(1024, new SecureRandom());
		return kpGen.generateKeyPair();
	}

	public static void main (String argv[]) {
		Gson gson = new Gson();
		Scanner scanner;
		scanner = new Scanner(System.in);
		String handle = "";
		System.out.println("Enter handle: ");
		handle = scanner.next();
		String fname = handle + ".key";
		ClientKeys result = new ClientKeys();
		result.keypair = genKeyPair();
		result.masterkey = genKey();
		//generate client key file
		try {
			FileOutputStream fos = new FileOutputStream(fname);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(result);
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// can't write to the specified file
			System.err.println(fname + " could not be written");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//open server keyfile or create it if does not exist
		//this is a haxxy way of doing key distribution
		try {
			File f = new File(ServerInfo.KEYFILE);
			FileWriter fstream = new FileWriter(f, true);
			BufferedWriter out = new BufferedWriter(fstream);
			String tmp = DatatypeConverter.printBase64Binary(result.masterkey.getEncoded());
			out.write(handle + ' ' + tmp + '\n');
			out.close();
		} catch (FileNotFoundException e) {
			System.err.println(ServerInfo.KEYFILE + " could not be read");
			e.printStackTrace();
			//so we have now created a new server key file
		} catch (IOException e) {
			System.err.println(ServerInfo.KEYFILE + " could not be read");
			e.printStackTrace();
		}
		System.out.println( DatatypeConverter.printBase64Binary(result.masterkey.getEncoded()) );
	}
}
