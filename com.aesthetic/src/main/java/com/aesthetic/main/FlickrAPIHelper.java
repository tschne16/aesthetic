package com.aesthetic.main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.*;
import java.net.*;

public class FlickrAPIHelper {


	    public static void main(String args[]) throws Exception {
	    	
	    	
	    }
	    
	    
	    public static String getUserID(String url)
	    {
	    	StringBuilder sb = new StringBuilder();
	    	try
	    	{
	        URLConnection uc = new URL(url).openConnection();
	        DataInputStream dis = new DataInputStream(uc.getInputStream());
	        
	        
	        
	        BufferedReader in = new BufferedReader(new InputStreamReader(
	                                    uc.getInputStream()));
	        String inputLine;
	        
	        while ((inputLine = in.readLine()) != null) 
	            sb.append(inputLine);
	        in.close();
	    	}
	    	catch(Exception e)
	    	{
	    		
	    		return null;
	    	}
	    	
	    	try
	        {
	        Document doc = Jsoup.parse(sb.toString());
	        
	        
	        Element v_label = doc.getElementsByClass("attribution-info").first().getAllElements().first();
	        String name = v_label.getElementsByAttribute("href").val();
	        
	        
	       name = name.replace("/photos/", "");
	       name =  name.replace("/","");
	        
	       return name;
	        }
	    	catch(Exception e)
	    	{
	    		return "";
	    	}
	    	
	    	
	    	
	    	
	    }
	    
	    
	    
	    public static Info getStats(String url) throws MalformedURLException, IOException
	    {
	    	BufferedReader in = null;
	    	StringBuilder sb = new StringBuilder();
	    	try
	    	{
	    		URLConnection uc = new URL(url).openConnection();
	    		DataInputStream dis = new DataInputStream(uc.getInputStream());
	    		in = new BufferedReader(new InputStreamReader(
                         uc.getInputStream()));
	    	}
	        catch(Exception e)
	        {
	        	System.out.println(e.getMessage());
	        }
	        
	        try{
	       
	        String inputLine;
	        
	        
	        while ((inputLine = in.readLine()) != null) 
	            sb.append(inputLine);
	        in.close();
	    	
	    	}
	    	catch(Exception e)
	    	{
	    		System.out.println(e.getMessage());
	    		return null;
	    	}
	        
	        try
	        {
	        Document doc = Jsoup.parse(sb.toString());
	        
	        
	        Element v_label = doc.select("span.view-count-label").first();
	        Element fav_label = doc.select("span.fave-count-label").first();
	        
	        int views = 0;
	        /*System.out.println("VIEWS " + v_label.text());
	        System.out.println("URL " + url);
	        System.out.println("favs " + fav_label.text());*/
	        
	       String views_all = v_label.text();
	       views_all = views_all.replace(".","");
	       views_all = views_all.replace(",","");
	       
	       String  favs_all = fav_label.text();
	       favs_all = favs_all.replace(".","");
	       favs_all = favs_all.replace(",","");
	       
	        if(tryParseInt(views_all));
	        {
	         views = Integer.parseInt(views_all);
	        }

	        int favs = 0;
	        if(tryParseInt(favs_all))
	        {
	        	 favs = Integer.parseInt(favs_all);     	       	
	        }
	        
	        Info ergebnis = new Info();
	        ergebnis.setFavs(favs);
	        ergebnis.setViews(views);
	        ergebnis.setUrl(url);
	        
	        
	        
	        return ergebnis;
	        }
	        catch(Exception e)
	        { 
	        	System.out.print(e.getMessage());
	        	return null;
	        	
	        }
	        
	        
	        
	        
	   
	        
	     /*   FileWriter fw = new FileWriter(new File("D:\\\\Hello1.xml"));
	        String nextline;
	        String[] servers = new String[10];
	        String[] ids = new String[10];
	        String[] secrets = new String[10];
	        
	        
	        
	        while ((nextline = dis.readLine()) != null) {
	            fw.append(nextline);
	        }
	        
	   
	        
	        dis.close();
	        fw.close();
	        String filename = "D:\\\\Hello1.xml";
	        XMLInputFactory factory = XMLInputFactory.newInstance();
	        System.out.println("FACTORY: " + factory);
	        XMLEventReader r = factory.createXMLEventReader(filename, new FileInputStream(filename));
	        int i = -1;
	        while (r.hasNext()) {
	            XMLEvent event = r.nextEvent();
	            if (event.isStartElement()) {
	                StartElement element = (StartElement) event;
	                String elementName = element.getName().toString();
	                if (elementName.equals("photo")) {
	                    i++;
	                    Iterator iterator = element.getAttributes();
	                    while (iterator.hasNext()) {
	                        Attribute attribute = (Attribute) iterator.next();
	                        QName name = attribute.getName();
	                        String value = attribute.getValue();
	                        System.out.println("Attribute name/value: " + name + "/" + value);
	                        if ((name.toString()).equals("server")) {
	                            servers[i] = value;
	                            System.out.println("Server Value" + servers[0]);
	                        }
	                        if ((name.toString()).equals("id")) {
	                            ids[i] = value;
	                        }
	                        if ((name.toString()).equals("secret")) {
	                            secrets[i] = value;
	                        }
	                    }
	                }
	            }
	        }
	        System.out.println(i);
	        String flickrurl = "http://static.flickr.com/" + servers[i] + "/" + ids[i] + "_" + secrets[i] + ".jpg";
	        try {
	            URI uri = new URI(flickrurl);
	            Desktop desktop = null;
	            if (Desktop.isDesktopSupported()) {
	                desktop = Desktop.getDesktop();
	            }
	            if (desktop != null) {
	                desktop.browse(uri);
	            }
	        } catch (IOException ioe) {
	            ioe.printStackTrace();
	        } catch (URISyntaxException use) {
	            use.printStackTrace();
	        }
	    */
	}
	   private static boolean tryParseInt(String value) {  
	        try {  
	            Integer.parseInt(value);  
	            return true;  
	         } catch (NumberFormatException e) {  
	            return false;  
	         }  
	   }
}
