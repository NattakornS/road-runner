package com.senior.roadrunner.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.thoughtworks.xstream.XStream;

public class TrackDataBase {


	// private TrackData trackData;
	/**
	 * Nattakorn S.
	 */

	public static List<LatLngTimeData> loadXmlFile(String filePath) {
		// Static XML data which we will parse
		List<LatLngTimeData> myData = null;
		File file = new File(filePath);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			InputSource is = new InputSource(br);

			/************ Parse XML **************/

			XMLParser parser = new XMLParser();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser sp = factory.newSAXParser();
			XMLReader reader = sp.getXMLReader();
			reader.setContentHandler(parser);
			reader.parse(is);

			/************* Get Parse data in a ArrayList **********/
			myData = parser.list;

		} catch (Exception e) {

		}

		return myData;
	}

	public static List<LatLngTimeData> loadXmlString(String xmlString) {
		// Static XML data which we will parse
		List<LatLngTimeData> myData = null;
		try {
			StringReader sr= new StringReader(xmlString);
			BufferedReader br = new BufferedReader(sr);
			InputSource is = new InputSource(br);
			/************ Parse XML **************/

			XMLParser parser = new XMLParser();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser sp = factory.newSAXParser();
			XMLReader reader = sp.getXMLReader();
			reader.setContentHandler(parser);
			reader.parse(is);

			/************* Get Parse data in a ArrayList **********/
			myData = parser.list;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return myData;
	}

	public static void saveXmlFile(List<LatLngTimeData> latLngTimeData,
			String savePath) {
		XStream xstream = new XStream();
		xstream.alias("point", LatLngTimeData.class);
		xstream.alias("TrackData", TrackData.class);
		xstream.addImplicitCollection(TrackData.class, "tracks");
		// System.out.println(xstream.toXML(latLngTimeData));
		TrackData trackdata = new TrackData();
		for (Iterator<LatLngTimeData> iterator = latLngTimeData.iterator(); iterator
				.hasNext();) {
			trackdata.add(iterator.next());
		}
		try {
//			File myFile = new File(savePath);
//			myFile.mkdirs();
//			myFile.createNewFile();
//			FileOutputStream fOut = new FileOutputStream(myFile);
//			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
//			myOutWriter.append(xstream.toXML(trackdata));// xstream.toXML(latLngTimeData)
//			myOutWriter.close();
//			fOut.close();
//			// Toast.makeText(null,
//			// "Done writing SD 'mysdfile.txt'",
//			// Toast.LENGTH_SHORT).show();
			File f = new File(savePath);
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
					os = new FileOutputStream(savePath, false);
					if (os != null) {
						OutputStreamWriter myOutWriter = new OutputStreamWriter(
								os);
						myOutWriter.write(xstream.toXML(trackdata));//
						myOutWriter.close();
						System.out.println("Write file to memory");
					}
					os.flush();
					os.close();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(null, e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}
	}

}
