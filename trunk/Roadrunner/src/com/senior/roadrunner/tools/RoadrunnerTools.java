package com.senior.roadrunner.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.IOUtils;

import android.graphics.Bitmap;

public class RoadrunnerTools {
	public static void writeStringToFile(String path, String str) {
		try {
			File f = new File(path);
			File pf = f.getParentFile();
			if (pf != null) {
				pf.mkdirs();
			}
			if ((pf.exists()) && (pf.isDirectory())) {
				if ((!f.exists()) || (!f.isFile())) {
					f.createNewFile();
				}
				if ((f.exists()) || (f.isFile())) {
					FileOutputStream os = null;
					os = new FileOutputStream(path, false);
					if (os != null) {
						OutputStreamWriter myOutWriter = new OutputStreamWriter(
								os);
						myOutWriter.write(str);//
						myOutWriter.close();
					}
					os.flush();
					os.close();
				}
			}
		} catch (IOException e) {
			String s = e.toString();
			System.out.println(s);
		}
	}

	public static boolean fileIsExists(String path) {
		File f = new File(path);
		File pf = f.getParentFile();
		if (pf != null) {
			pf.mkdirs();
		}
		if ((pf.exists()) && (pf.isDirectory())) {
			if ((f.exists()) || (f.isFile())) {
				return true;
			}
		}
		return false;
	}

	public static String readStringFromFile(String path) {
		String str = null;
		try {
			File f = new File(path);
			File pf = f.getParentFile();
			if (pf != null) {
				pf.mkdirs();
			}
			if ((pf.exists()) && (pf.isDirectory())) {
				if ((!f.exists()) || (!f.isFile())) {
					return null;
				}
				if ((f.exists()) || (f.isFile())) {
					FileInputStream fstream = null;
					fstream = new FileInputStream(path);
					try {
						str = IOUtils.toString(fstream);
					} finally {
						fstream.close();
					}
					return str;
				}
			}
		} catch (IOException e) {
			String s = e.toString();
			System.out.println(s);
		}
		return str;
	}

	public static void deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				File child = new File(dir, children[i]);
				if (child.isDirectory()) {
					deleteDirectory(child);
					child.delete();
				} else {
					child.delete();

				}
			}
			dir.delete();
			System.out.println("DIR DELETED");
		}
	}
}
