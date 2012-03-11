package com.nis.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
    
    public static boolean writeKeyToFile (String fileName) throws FileNotFoundException {
        FileOutputStream f = new FileOutputStream(fileName);
        try {
			f.write(genKey().getEncoded());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
        return false;
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
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
        return key;
    }

    public static void main (String argv[]) {
        try {
			writeKeyToFile("test.key");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        System.out.println(getKeyFromFile("test.key").getEncoded());
    }
}
