package com.nis.shared;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;

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
    
    public static void writeKeyToFile (String fileName) throws FileNotFoundException {
        FileOutputStream f = new FileOutputStream(fileName);
        try {
			f.write(genKey().getEncoded());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static SecretKey getKeyFromFile(String fileName)  {
        File f = new File(fileName);
        int len = (int) f.length();
        FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        byte [] encoded = new byte[len];
        try {
			fis.read(encoded, 0, len);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        SecretKey key = new SecretKeySpec(encoded, "AES");
        return key;
    }

    public static void main (String argv[]) {
        try {
			writeKeyToFile("test.key");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(getKeyFromFile("test.key").getEncoded());
    }
}
