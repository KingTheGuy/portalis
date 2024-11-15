package com.kingtheguy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.kingtheguy.portalis.GlobalWarps;

public class Data {
	public static void serialize(Object obj, String fileName)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(obj);

		fos.close();
	}

	public static Object deserialize(String fileName) throws IOException,
			ClassNotFoundException {
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Object obj = ois.readObject();
		ois.close();
		return obj;
	}

	public static List<GlobalWarps> deserializeList(String fileName) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		List<GlobalWarps> list = (List<GlobalWarps>) ois.readObject(); // Cast the read object to List<C
		ois.close();
		return list;
	}

}
