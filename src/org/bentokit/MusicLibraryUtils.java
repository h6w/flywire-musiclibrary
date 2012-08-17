package org.bentokit;

import java.util.HashMap;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
import javax.xml.xpath.*;


import java.io.StringWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.XMLSerializer;


public class MusicLibraryUtils {
    private static String downloadURL = "/track/?type=xml&title=";

    private static final HashMap<String,String> staticParameters = new HashMap<String,String>() {
        public static final long serialVersionUID = 1L;
        {
            put("sent","login");
            put("type","xml");
            put("component","musiclibrary");
            put("version","1");
        }
    };

    private static final HashMap<String,String> apiParameters = new HashMap<String,String>() {
        public static final long serialVersionUID = 1L;
        {
            put("sent","login");
            put("type","xml");
            put("component","musiclibrary");
            put("version","1");
        }
    };

    public static Node search(String searchString) {
        ClientHttpRequest httpreq;

        ErrorHandler.info("MusicLibrary: MusicLibrary API URL is:"+Pref.getPref(Constants.MUSICLIBRARY_SERVER_KEY, Constants.MUSICLIBRARY_SERVER_DEFAULT)+downloadURL+searchString);

            
        try {
            httpreq = new ClientHttpRequest(Pref.getPref(Constants.MUSICLIBRARY_SERVER_KEY, Constants.MUSICLIBRARY_SERVER_DEFAULT)+downloadURL+searchString, ClientHttpRequest.ConnectionType.GET);
        } catch (IOException ioe) {
            ErrorHandler.userError("MusicLibraryUtils: Request could not be sent.  Are you connected to the internet?");
            return(null);
        } catch (Exception e) {
            ErrorHandler.userError("MusicLibraryUtils: Request could not be sent.  Generic Error.  Are you connected to the internet?");
            return(null);
        }
        String result = "";
        try {
            Calendar calendar = GregorianCalendar.getInstance();
            java.text.SimpleDateFormat stampformat = new java.text.SimpleDateFormat("yyyy-MM-dd HHmmss");           
            FileWriter fileStream = new FileWriter("MusicLibrary-Transaction-"+stampformat.format(calendar.getTime())+".log",true);
            PrintWriter pw = new PrintWriter(fileStream);
            ErrorHandler.userInfo("MusicLibrary: Sending POST request to MusicLibrary API for MeetCollection Tree.");
            ErrorHandler.info("MusicLibrary: MusicLibrary API URL is:"+Pref.getPref(Constants.MUSICLIBRARY_SERVER_KEY, Constants.MUSICLIBRARY_SERVER_DEFAULT)+downloadURL+searchString);
            pw.println("SENDING...");
            pw.flush();
            InputStream resultStream = httpreq.post(apiParameters);
            pw.println("RESPONSE");
            pw.println("================================================================");
            pw.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(resultStream));
            int count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                result += line + "\n";
                pw.println(line); count++;
            }
            reader.close();
            pw.flush();            
            pw.close();
            ErrorHandler.info("MusicLibrary Transaction returned "+count+" lines.");
        } catch (IOException ioe) {
            ErrorHandler.userError("MusicLibraryUtils: Request failed to be posted to website.");
            ErrorHandler.error(ioe);
            return(null);
        } catch (Exception e) {
            ErrorHandler.userError("MusicLibraryUtils: Generic Error.  Request failed to be posted to website.");
            ErrorHandler.error(e);
            e.printStackTrace();
            return(null);
        }
        ErrorHandler.userInfo("MusicLibrary: Meets Requested successfully.");
        
        NodeList meetCollections = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        //    Document doc = docBuilder.parse (new StringBufferInputStream(result));
            Document doc = docBuilder.parse (new ByteArrayInputStream(result.getBytes()));
            // normalize text representation
            doc.getDocumentElement ().normalize ();
            System.out.println ("Root element of the doc is " + 
                    doc.getDocumentElement().getNodeName());
            return(doc.getDocumentElement());
        }catch (SAXParseException err) {
            ErrorHandler.userInfo("Problem incurred while downloading available Disc Information.");
            ErrorHandler.error("MusicLibraryUtils:** Parsing error" + ", line " 
                    + err.getLineNumber () + ", uri " + err.getSystemId ());
            ErrorHandler.error("MusicLibraryUtils: " + err.getMessage ());
        }catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();
        }catch (Exception e) {
           e.printStackTrace ();
        }catch (Throwable t) {
           t.printStackTrace ();
        }
        return(null);
    }
}
