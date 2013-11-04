package com.senior.roadrunner.data;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;



//SAX parser to parse job 
public class XMLParser extends DefaultHandler{
	public List<LatLngTimeData> list=null;
    
    // string builder acts as a buffer
    StringBuilder builder;

	private Coordinate coordinate;

	private LatLngTimeData latlngTimeData;
     
     
     // Initialize the arraylist
     // @throws SAXException
      
    @Override
    public void startDocument() throws SAXException {
         
        /******* Create ArrayList To Store XmlValuesModel object ******/
        list = new ArrayList<LatLngTimeData>();
    }
 
     
     // Initialize the temp XmlValuesModel object which will hold the parsed info
     // and the string builder that will store the read characters
     // @param uri
     // @param localName ( Parsed Node name will come in localName  )
     // @param qName
     // @param attributes
     // @throws SAXException
      
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
         
        /****  When New XML Node initiating to parse this function called *****/
         
        // Create StringBuilder object to store xml node value
        builder=new StringBuilder();
         
        if(localName.equals("TrackData")){
             

        }
        else if(localName.equals("point")){
             latlngTimeData=new LatLngTimeData();

        }
        else if(localName.equals("coordinate")){
             coordinate=new Coordinate();
        }
    }
     
     
     
     // Finished reading the login tag, add it to arraylist
     // @param uri
     // @param localName
     // @param qName
     // @throws SAXException
      
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        
         
        if(localName.equals("point")){

            /** finished reading a job xml node, add it to the arraylist **/
            list.add( latlngTimeData );
             
        }
        else  if(localName.equalsIgnoreCase("when")){  

        	latlngTimeData.setWhen(builder.toString());
        }
        else  if(localName.equalsIgnoreCase("lat")){
            coordinate.setLat(Double.parseDouble(builder.toString()));
            
        }
        else  if(localName.equalsIgnoreCase("lng")){
        	 coordinate.setLng(Double.parseDouble(builder.toString()));
            
        }
        else  if(localName.equalsIgnoreCase("coordinate")){
            latlngTimeData.setCoordinate(coordinate);
//            coordinate = null;
            
        }
     // Log.i("parse",localName.toString()+"========="+builder.toString());
    }
 
    
     // Read the value of each xml NODE
     // @param ch
     // @param start
     // @param length
     // @throws SAXException
      
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
             
        /******  Read the characters and append them to the buffer  ******/
        String tempString=new String(ch, start, length);
         builder.append(tempString);
    }
}
