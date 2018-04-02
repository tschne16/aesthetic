package com.aesthetic.main;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Extras;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.stats.Stats;
import com.flickr4java.flickr.tags.Tag;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.imgscalr.Scalr;
import org.joda.time.YearMonth;

/*
 *  - Java 7 is needed
 *  - insert your api- and secretkey
 *
 *  start main with wanted tags as parameter, for example: FlickrCrawler.main(Sunset) and all pics will be saved in original size or large to pics\sunset\...
 */
public class FlickrCrawler extends SwingWorker<Void, Integer>{

   private static String path = "";
   private static Preferences userPrefs = Preferences.userNodeForPackage(FlickrCrawler.class);
   private static List<Long> ids;
   private static Flickr flickr;
   private static PhotosInterface pi;
   private static List<Info> writeToDB;
   private static List<Long> blacklist;
   private static String[] searchtags;
   private static int maxpictures;
   private static JProgressBar jpb;
   private static JLabel	lbl_allpics;
   private static Long amountviews;
   private static long current_amount = 0;
   
   private final static Logger LOGGER = Logger.getLogger(FlickrCrawler.class.getName());
    // convert filename to clean filename
    

   public FlickrCrawler(String[] searcht, int maxpic,long views ,JProgressBar original,JLabel lbl)
   {
	   searchtags = searcht;
	   maxpictures = maxpic; 
	   jpb = original;
	   amountviews = views;
	   lbl_allpics = lbl;
   }
   
   public static String convertToFileSystemChar(String name) {
        String erg = "";
        Matcher m = Pattern.compile("[a-z0-9 _#&@\\[\\(\\)\\]\\-\\.]", Pattern.CASE_INSENSITIVE).matcher(name);
        while (m.find()) {
            erg += name.substring(m.start(), m.end());
        }
        if (erg.length() > 200) {
            erg = erg.substring(0, 200);
            System.out.println("cut filename: " + erg);
        }
        return erg;
    }

    @SuppressWarnings("deprecation")
	public static boolean saveImage(Flickr f, Photo p) throws FlickrException {

        String cleanTitle = convertToFileSystemChar(p.getTitle());
        
        System.out.println(p.getComments());
        
        
   
        Stats s = p.getStats();
        
        if(s != null)
        {
        int fav1 =	s.getFavorites();
        int view1 = s.getViews();
        
        System.out.println(fav1 + " " + view1 + p.getUrl());
        
        	return true;
        }
        else
        {
        	
        	return false;
        }
       
        
    }
    public static String encodeToString(BufferedImage image, String type,Photo p) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

           // byte[] encodedBytes = Base64.encodeBase64(image.getBytes());
            	
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, p.getOriginalFormat(), baos);
            byte[] bytes = baos.toByteArray();
            
           // byte[] bytesEncoded = Base64.encodeBase64(bytes);
            imageString = new String(Base64.encodeBase64(bytes), "UTF-8");

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }
    

    
    
    @SuppressWarnings("deprecation")
    public static String convertToBase(Flickr f,Photo p,PhotosInterface pi)
    {
    	
    	//String cleanTitle = convertToFileSystemChar(p.getTitle());
    	//File orgFile = new File(path + File.separator + cleanTitle + "_" + p.getId() + "_o." + p.getOriginalFormat());
        //File largeFile = new File(path + File.separator + cleanTitle + "_" + p.getId() + "_b." + p.getOriginalFormat());
        String erg = "";
        try {
            Photo nfo = f.getPhotosInterface().getInfo(p.getId(), null);

            if (nfo.getOriginalSecret().isEmpty()) {
                //ImageIO.write(p.getLargeImage(), p.getOriginalFormat(), largeFile);
            	BufferedImage thumbnail = Scalr.resize(p.getLargeImage(),Scalr.Mode.FIT_EXACT, 30,30);
            	erg = encodeToString(thumbnail, p.getOriginalFormat(), p);
            	System.out.println(p.getTitle() + "\t" + erg);
            } else {
                p.setOriginalSecret(nfo.getOriginalSecret());
                //ImageIO.write(p.getOriginalImage(), p.getOriginalFormat(), orgFile);
                BufferedImage pic = pi.getImage(p, Size.SMALL);
                
              //  BufferedImage thumbnail = Scalr.resize(pi.getImage(p.getUrl()), 60);
                BufferedImage thumbnail = Scalr.resize(pic, Scalr.Mode.FIT_EXACT, 30,30);
                
               // BufferedImage thumbnail = Scalr.resize(p.getOriginalImage(), 60);
                erg =   encodeToString(thumbnail, p.getOriginalFormat(), p);
                
                pic = null;
                thumbnail = null;
                //System.out.println(p.getTitle() + "\t" + erg);
            }
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	
    	return erg;
    }
    public static void main(String[] args)  throws Exception  {
    
     
     /*    /* if (args.length == 0) {
     
            System.out.println("Parameter are needed as for searching. Example: FlickrCrawler.java sunset");
            return;
        }
        ids = DBHelper.LoadAllIds();
        
        String apikey = "184e0ff4b8d59757caef5b3ff062fece";
        String secret = "816773af795cd0dd";
        blacklist = new ArrayList<Long>();
        
         flickr = new Flickr(apikey, secret, new REST());
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setAccuracy(1);
        searchParameters.setPrivacyFilter(1);
        //searchParameters.setExtras(Extras.VIEWS);
        
        //UpdatePhoto(flickr);
   
        searchParameters.setExtras(Extras.ALL_EXTRAS);
        
        ///Set<String> set = new HashSet<String>();
        //set.add(Extras.VIEWS);
        searchParameters.setExtras(Extras.ALL_EXTRAS);
        //searchParameters.setExtras(set);
        
        
       // Token requestToken = flickr.getAuthInterface().getRequestToken();
        
        //Token accessToken = flickr.getAuthInterface().getAccessToken(requestToken, new Verifier(flickr.getAuth().getToken()));
        
        int[] params = new int[5];
        params[0] = SearchParameters.DATE_POSTED_DESC;
        params[1] = SearchParameters.RELEVANCE;
        params[2] = SearchParameters.INTERESTINGNESS_DESC;
        params[3] = SearchParameters.DATE_POSTED_ASC;
        params[4] = SearchParameters.DATE_TAKEN_ASC;
      
	int perPage = 500;
        writeToDB = new ArrayList<Info>();
        StringBuilder tagsBuilder = new StringBuilder();
        for (String tmp : args) {
            tagsBuilder.append(" " + tmp);
        }
        path = "pics" + File.separator + tagsBuilder.toString().substring(1);

        new File(path).mkdirs();
        searchParameters.setTags(searchtags);
         pi = new PhotosInterface(apikey, secret, new REST());
         
       // userPrefs.getInt(path, 0)
      for(int y = 0; y < params.length;y++)
      {  
    	  LOGGER.log(Level.INFO, "START KATEGORIE : {0}", params[y]);
    	  searchParameters.setSort(params[y]);
    	  
    	  
    	  ///JAHRWEISE SUCHEN UM API BESCHRÄNKUNG ZU UMGEHEN -> dieses jahr und die 4 letzten
    for(int x = 0; x < 5;x++)
    {
    	  
    	Date mindate = new Date();
    	Date maxdate = new Date();
    	
    	Calendar calendar = Calendar.getInstance();
    	int year = calendar.get(Calendar.YEAR) -x;
    	calendar.set(Calendar.YEAR, year);
    	calendar.set(Calendar.DAY_OF_YEAR, 1);
    	mindate = calendar.getTime();
    	
    	
    	
    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.YEAR,year );
    	calendar.set(Calendar.MONTH, 11);
    	calendar.set(Calendar.DAY_OF_MONTH, 31);
    	
    	maxdate = calendar.getTime();
    	 
    	searchParameters.setMinUploadDate(mindate);
    	searchParameters.setMinUploadDate(maxdate);
    	
    	LOGGER.log(Level.INFO, "Start MIN DATE:{0}", mindate.toString());
    	LOGGER.log(Level.INFO, "END MAX DATE:{0}", maxdate.toString());
        for(int z = 0;z < args.length;z++)
        {
         ///TAGS einzeln suchen --> evtl Bug in der API
        searchParameters.setTags(new String[]{args[z]});
        int wdh = 0; 
        
        
        
        
        //gib mir nur die ersten 30 seiten zurück
        for (int i = 1; i<51; i++) {
            //userPrefs.putInt( path, i );
            //System.out.println("\tcurrent page: " + userPrefs.getInt(path, 0));
            System.out.println("current page :" + i);
        	
            try {
            	 PhotoList<Photo> list = new PhotoList<Photo>();
            	
            		list = getList(searchParameters, i,flickr,perPage);
            		
                 //list = flickr.getPhotosInterface().search(searchParameters, 500, i);
            	
                 	if(list == null)
                 		continue;
                 	
                 	
                if (list.isEmpty())
                    break;
                
                int amountthreads = Runtime.getRuntime().availableProcessors();
               // int amountthreads = 20;
                
                System.out.print("ANZAHL THREADS: " + amountthreads);
                
                Iterator itr = list.iterator();
                List<Photo> allphotos = new ArrayList<Photo>();
                int counter = 0;
                while (itr.hasNext()) {                	
                	Photo p = (Photo) itr.next();
                	if(ids.contains(Long.parseLong(p.getId()))== false && blacklist.contains(Long.parseLong(p.getId()))==false)
                	{
                		//System.out.println(p.getViews());
                		ids.add(Long.parseLong(p.getId()));
                		allphotos.add(p);            		
                	}
                	else
                	{
                		counter++;	 		
                	}
                	
                	
                }
                
                if(counter >= perPage-1 )
            	{
            		wdh++;
            		System.out.println("Fehler :" + wdh);
            	}
            	else
            	{
            		wdh = 0;
            	}
                
                if(wdh >= 9)
                {
                	break;
                }
                
                System.out.println("BEREITS BEKANNT:" + counter);               
                System.out.println("ALLPHOTO SIZE:" + allphotos.size());
                
                if(allphotos.size() == 0)
                	continue;
                
               
                int count = 0;
                List<Future<?>> get = new ArrayList<Future<?>>();
                ExecutorService es = Executors.newFixedThreadPool(amountthreads);
                for(final Photo p : allphotos) {
                	count = count+1;
                	
                	Future <?>f = es.submit(new Runnable() {
                       
                		public void run() { try {
							process(p,flickr,pi);
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out.println(e.getMessage());
						} }
                    });
                	
                	get.add(f);
                }
                	
                LOGGER.log(Level.INFO,"GETTING FUTURES :{0}",get.size());
                counter = 0;	
                for(Future<?> f: get)
                	{
                	try{
                		///Wenn nach 8 Sekunden keine Antwort vom Thread -> Überspringen
                		f.get(8, TimeUnit.SECONDS);
                	}
                	catch(Exception e)
                	{
                		//Fehler -> gehe zum nächsten Thread
                		continue;
                	}
                		counter++;
                		//LOGGER.log(Level.INFO,"Threads collected :{0}",counter);
                	}
                LOGGER.log(Level.INFO,"Total amount of Threads collected :{0} , amount of Threads started :"+get.size() ,counter);
                	
         	
                ///Nochmal 3 Sekunden warten und dann ExecutorService abschalten
                if (es.awaitTermination(3, TimeUnit.SECONDS)) {
                	
                	} else {
                	  
                	  es.shutdownNow();
                	}	
                	
            }
             catch (Exception e) {
                System.out.print(e.getMessage());
                continue;
            }
        }
        }
        LOGGER.log(Level.INFO, "DONE");
      }
      }
    }
    /*/}
    
    public static void UpdatePhoto(Flickr flickr) throws Exception
    {
    	List<Info> all = DBHelper.LoadAllPictures(0,null);
    	
    	for(Info i:all)
    	{
    		
    		if(i.getTags() == "")
    		{
    			String username = FlickrAPIHelper.getUserID(i.getUrl());
    			
    			SearchParameters search = new SearchParameters();
    			
    			search.setUserId(username);
    			Boolean found = false;
    			int counter = 1;
    			while(found == false && counter < 11)
    			{
    				List<Photo> photos = getList(search, 1, flickr, 500);
    				counter = counter +1;
    				for(Photo p: photos)
    				{
    					if(Long.parseLong(p.getId())== i.getPhotoid())
    					{
    						found = true;
    						String tags = "";						
    						
    						for(Tag t: p.getTags())
    						{
    							if(tags != "")
    							{
    								tags = tags + ";";
    							}
    							tags = tags + t.getValue();
    						}
    						i.setTags(tags);
    						LOGGER.log(Level.INFO, "UPDATED {0}", username);
    						i.update();
    						break;		
    					}   					
    					
    				}
    				
    			}
    		}
		
    	}
    }
    private static PhotoList<Photo> getList(SearchParameters searchParameters,int i,Flickr flickr,int perPage)
    {
    	 PhotoList<Photo> list = new PhotoList<Photo>();
     	try{
          list = flickr.getPhotosInterface().search(searchParameters, perPage, i);
          return list;
     	}
     	catch(FlickrException e)
     	{
     		return null;
     	}
    	
    	
    }
    @SuppressWarnings("deprecation")
	private static Boolean process(Photo p, Flickr f,PhotosInterface pi) throws Exception
    {
    	
    	
    	LOGGER.log(Level.INFO,Integer.toString(p.getViews())+ " " + p.getId());
    	
    	Collection<Tag> tags = p.getTags();
    	 Info x = null;
    	 x = FlickrAPIHelper.getStats(p.getUrl());
    	
    	if(x != null)
    	{
    		x.setPhotoid(Long.parseLong(p.getId()));
    		LOGGER.log(Level.INFO, "GOT STATS FOR {0}",x.getPhotoid());
    		x.setTags(tags.toArray(new Tag[tags.size()]));
    		if(x.getViews() > 0)
    		{
    	
    		try{
    		x.setBase64(convertToBase(flickr,p,pi));
    		x.setPhototitel(p.getTitle());
    		x.setFormat(p.getOriginalFormat());
    		x.setUserid(p.getOwner().getId());
    		x.setPosted(p.getDatePosted());
    		LOGGER.log(Level.INFO, "INFOS SET FOR {0}",x.getPhotoid());
    		}
    	catch(Exception e)
    	{
    		LOGGER.log(Level.INFO, "FEHLER BEI KONVERTIERUNG/SET TITLE/setFormat für {0}", x.getPhotoid());
    		///Fehler aufgetreten -> sicherstellen dass das Bild nicht mehr berücksichtigt wird damit abbruch kriterium erfüllt wird
    		blacklist.add(x.getPhotoid());
    		x = null;    		
    		///DAFÜR SORGEN, Dass das Bild nicht mehr berücksichtigt wird
    		System.out.print(e.getMessage());
    		return false;
    	}
    	
        if(x != null)
        	{
        		
            	//x.setPhotoid(Long.parseLong(p.getId()));
            	//DoppelEinträge verhindern
            	ids.add(x.getPhotoid());
            	
            	if(x.getViews() > amountviews)
            	{
            		LOGGER.log(Level.INFO, "WRITE TO DB: {0}", x.getPhotoid());         		
            		return DBHelper.writeInfoObject(x);	
            		//System.out.println("SUCCESS");          		
            	}
            	else
            	{
            		LOGGER.log(Level.INFO, "NOT ENOUGH VIEWS!" + x.getUrl() +" {0}", x.getViews());
            	}
        	}
    	}
    	
    		else
    		{		
    			blacklist.add(x.getPhotoid());
    			LOGGER.log(Level.INFO, "0 Views ? {0}", x.getUrl());
    		}
    		
    	}
	    	else
			{	
	    		LOGGER.log(Level.INFO, "ERROR BEI HTML AUSLESEN!");
				blacklist.add(Long.parseLong(p.getId()));
			}
    	
    	return false;
    }

	@Override
	protected Void doInBackground() throws Exception {
		
		
	 	if(isCancelled())
    	{
    		return null;
    	}
		current_amount = DBHelper.Get_Amount();
		
		ids = DBHelper.LoadAllIds();
        String apikey = "184e0ff4b8d59757caef5b3ff062fece";
        String secret = "816773af795cd0dd";
        blacklist = new ArrayList<Long>();
        
        flickr = new Flickr(apikey, secret, new REST());
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setAccuracy(1);
        searchParameters.setPrivacyFilter(1);
        searchParameters.setExtras(Extras.ALL_EXTRAS);
        searchParameters.setMedia("photos");
        int[] params = new int[4];
        params[0] = SearchParameters.RELEVANCE;
        params[1] = SearchParameters.INTERESTINGNESS_DESC;
        params[2] = SearchParameters.DATE_POSTED_ASC;
        params[3] = SearchParameters.DATE_TAKEN_ASC;
        int perPage = 500;
        writeToDB = new ArrayList<Info>();

       // new File(path).mkdirs();
        
         pi = new PhotosInterface(apikey, secret, new REST());
         
       // userPrefs.getInt(path, 0)
      for(int y = 0; y < params.length;y++)
      {  
    	  LOGGER.log(Level.INFO, "START KATEGORIE : {0}", params[y]);
    	  searchParameters.setSort(params[y]);
      	if(isCancelled())
      	{
      		return null;
      	}
    	  
    	  ///JAHRWEISE SUCHEN UM API BESCHRÄNKUNG ZU UMGEHEN -> dieses jahr und die 4 letzten
    for(int x = 0; x < 5;x++)
    {
     	if(isCancelled())
    	{
    		return null;
    	}
     	
    	Date mindate = new Date();
    	Date maxdate = new Date();
    	
    	Calendar calendar = Calendar.getInstance();
    	int year = calendar.get(Calendar.YEAR) -x;
    	calendar.set(Calendar.YEAR, year);
    	calendar.set(Calendar.DAY_OF_YEAR, 1);
    	mindate = calendar.getTime();

    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.YEAR,year );
    	calendar.set(Calendar.MONTH, 11);
    	calendar.set(Calendar.DAY_OF_MONTH, 31);
    	
    	maxdate = calendar.getTime();
    	 
    	searchParameters.setMinUploadDate(mindate);
    	searchParameters.setMinUploadDate(maxdate);
    	
    	 for(int z = 0;z < searchtags.length;z++)
        {
         ///TAGS einzeln suchen --> evtl Bug in der API
        	String ta = searchtags[z];
        	System.out.println(ta);
        searchParameters.setTags(new String[]{ta});
        int wdh = 0; 
     	
        if(isCancelled())
    	{
    		return null;
    	}
        
        //gib mir nur die ersten 30 seiten zurück
        for (int i = 1; i<51; i++) {
            //userPrefs.putInt( path, i );
            //System.out.println("\tcurrent page: " + userPrefs.getInt(path, 0));
            System.out.println("current page :" + i);
        	
            if(isCancelled())
        	{
        		return null;
        	}
            
            
            if(ids.size() >= maxpictures)
            	return null;
            
            try {
            	 PhotoList<Photo> list = new PhotoList<Photo>();
            	
            		list = getList(searchParameters, i,flickr,perPage);
            		
                 //list = flickr.getPhotosInterface().search(searchParameters, 500, i);
            	
                 	if(list == null)
                 		continue;
                 	
                 	if(isCancelled())
                	{
                		break;
                	}
                 	
                if (list.isEmpty())
                    break;
                
                int amountthreads = Runtime.getRuntime().availableProcessors();
               // int amountthreads = 20;
                  
                Iterator itr = list.iterator();
                List<Photo> allphotos = new ArrayList<Photo>();
                int counter = 0;
                while (itr.hasNext()) {      
                	
                 	if(isCancelled())
                	{
                		return null;
                	}
                	
                	
                	
                	Photo p = (Photo) itr.next();
                	if(ids.contains(Long.parseLong(p.getId()))== false && blacklist.contains(Long.parseLong(p.getId()))==false)
                	{
                		//System.out.println(p.getViews());
                		//ids.add(Long.parseLong(p.getId()));
                		allphotos.add(p);            		
                	}
                	else
                	{
                		counter++;	 		
                	}
                	
                	
                }
                
                if(counter >= perPage-1 )
            	{
            		wdh++;
            		System.out.println("Fehler :" + wdh);
            	}
            	else
            	{
            		wdh = 0;
            	}
                
                if(wdh >= 5)
                {
                	break;
                }
                
                System.out.println("BEREITS BEKANNT:" + counter);               
                System.out.println("ALLPHOTO SIZE:" + allphotos.size());
                
                if(allphotos.size() == 0)
                	continue;
                
               
                int count = 0;
                List<Future<?>> get = new ArrayList<Future<?>>();
               // ExecutorService es = Executors.newFixedThreadPool(amountthreads);
                ExecutorService es = Executors.newFixedThreadPool(2);
                for(final Photo p : allphotos) {
                	count = count+1;
                	
                	if(isCancelled())
                	{
                		break;
                	}
                	
                	
                	Future <?>f = es.submit(new Runnable() {           
                		public void run() { try {
                			process(p,flickr,pi);
							//LOGGER.log(Level.INFO,Thread.currentThread().getName());
						
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out.println(e.getMessage());
							
						} }
                    });
                	
                	get.add(f);
                }
                	
                LOGGER.log(Level.INFO,"GETTING FUTURES :{0}",get.size());
                counter = 0;	
                
            	if(isCancelled())
            	{
            		LOGGER.log(Level.INFO,"Cancelled");
            		return null;
            	}
            	
                for(Future<?> f: get)
                	{
                	try{
                		///Wenn nach 8 Sekunden keine Antwort vom Thread -> Überspringen
                		f.get(8, TimeUnit.SECONDS);
                		publish(ids.size());
                	}
                	catch(Exception e)
                	{
                		//Fehler -> gehe zum nächsten Thread
                		continue;
                	}
                		counter++;
                		//LOGGER.log(Level.INFO,"Threads collected :{0}",counter);
                	}
               // LOGGER.log(Level.INFO,"Total amount of Threads collected :{0} , amount of Threads started :"+get.size() ,counter);

                ///Nochmal 3 Sekunden warten und dann ExecutorService abschalten
                
                if (es.awaitTermination(3, TimeUnit.SECONDS)) {
                	
                	} else {
                	  
                	  es.shutdownNow();
                	}	
            }
             catch (Exception e) {
                System.out.print(e.getMessage());
                continue;
            }
            //setProgress(10);
            publish(ids.size());
        }
        }
        
        LOGGER.log(Level.INFO, "DONE");
      }
      }

		return null;
	}
	@Override
	protected void done()
	{
		return;
	}
    @Override
    protected void process(List<Integer> chunks) {
    	
    	jpb.setMaximum(100);
    	
    	double total = (double) current_amount  + (double) DBHelper.getCalled();
    	lbl_allpics.setText( String.valueOf(total));
    	double val = (total)/ ((double) maxpictures);
    	//double val = (double) ids.size()/ (double) maxpictures;
    	
    	LOGGER.log(Level.INFO,Double.toString(val));
    	
    	
    	if (val > 1)
    		val = 1;
    	
    	val = val*100;
    	
    jpb.setValue((int)val);
   // jpb.setString
    	//int amount = jpb.getMaximum() - jpb.getMinimum();
    	
    	//jpb.setValue( ( int ) (jpb.getMinimum() + ( amount * chunks.get( chunks.size() - 1 ))) );
    }
    
}