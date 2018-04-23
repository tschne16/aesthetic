package com.aesthetic.main;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;



public class DBHelper {
static final String WRITE_OBJECT_SQL = "INSERT INTO flickr.flickr(photoid, views,favs,url,base64,phototitel,tags,pictureformat,userid,posted,InDB) VALUES (?,?,?, ?,?,?,?,?,?,?,?)";
static final String READ_ALLPHOTO_IDS_SQL = "SELECT photoid FROM flickr.flickr limit 1000000;";
static final String Update_OBJECT_SQL = "UPDATE flickr.flickr set phototitel =  ?, tags = ? where photoid = ?";
static final String LOAD_DATA_INFO = "SELECT photoid,url,phototitel,favs,views,CONVERT(base64 USING utf8) as base64 FROM flickr.flickr where favs >= ?";
static final String LOAD_DATA_INFO_LAZY = "SELECT photoid,favs,views,pictureformat FROM flickr.flickr where views >= ? and favs > 0 order by photoid LIMIT 1000000";
static final String LOAD_DATA_INFO_LAZY_TAGS = "SELECT photoid,favs,views,pictureformat FROM flickr.flickr where views >= ? and favs > 0 and tags like ? order by photoid LIMIT 1000000";
static final String Delete_BY_PhoTOID = "DELETE FROM flickr.flickr where photoid = ?";
static final String GET_AMOUNT = "SELECT COUNT(PHOTOID) from flickr.flickr";
static Connection conn;
static final String WRITE_AVA_SQL = "INSERT INTO flickr.ava(photoid,rating,tags) values(?,?,?)";
static final String READ_AVA_SQL = "SELECT * FROM flickr.ava Limit 300000";
static final String READ_AVA_SQL_RAPID = "select * from flickr.ava where rating < 4.5 or rating >= 5.5 Limit 13200";
private static String invalid_base = "/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCADgAOADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+iiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKzrzVRBdC0toHurojcY0IAUe57UAaNFZ1nq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";
private static String invalid_base2 = "/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAD0APQDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+iiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKA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c+S+BW5gZzgZ6ZoIBIJAyOntQByOn6jK8th5GpzXc80bm+hcj9zhCc4A+QhsDHfP40WFzqEUGjTrd3FzLd2cjPHKwKswQMuBjg549667ABJwMnr70mxcAbQMDAxxj6UAcdY6izXWjfZ9Vmu55hIbmBnGA4iY4Ix8vzDp7Uujaje3M8DG+jeZ43N1A1yCynHaPaChDYHXp69a6C20W3t7uO5aa5nkiBEfnS7gmRgke+OMnJrR2qCTgZPU460AZHhvzm0G1uri6nuJZ4VkYyEHHHQYFYmn6hc3uoWsaX0oS+jmDobkO8eBkEqFHlsD2GfzrsgAAAAAB2FGxc52jPXOKAOQh1e8ntLm4ledP7OtTDOIsAtcZwTyCOAoPII+aq76jdJZ6tEt55qxxQSoy3HnbWZyDh8DI4HGK7cADOABnk0nlpjGxcdOlAHIPdtbnUI1kaIz6syb/OEKj90pwz4O3p2GSeKrreGddJe9v2RIr65i+07xnYquAS2MdOM8etdxsUgjaMHk8daTYv8AdHr0oA4+3up57zS3ErXSR3tyttK55lQQtg5785Ge+KfoGoXt1e2ha9jeV1Y3du9yGZTjtHtGwg4HXp69a67avHA46cdKAoBJAAJ6n1oAWiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAP//Z";
public static String getInvalid_base() {
	return invalid_base;
}


public static void setInvalid_base(String invalid_base) {
	DBHelper.invalid_base = invalid_base;
}


private static long called;
static final String GET_SINGLE_BASE64 = "Select CONVERT(base64 USING utf8) as base64 from flickr where photoid =?";

static String driver = "com.mysql.jdbc.Driver";
static String password = "root";
static String user = "root";
static String url ="jdbc:mysql://localhost:3306/flickr?autoReconnect=true&useSSL=false";

private static final Logger LOGGER = Logger.getLogger( DBHelper.class.getName() );


public DBHelper(String d, String pw, String us, String u)
{
	 driver = d;
     url = "jdbc:mysql://" + u + "?autoReconnect=true&useSSL=false";
     user = us;
     password = pw;
}


public static String GetSingleBase64(Long photoid) throws Exception
{
	String s = "";
	getConnection();
	  
    PreparedStatement pstmt = conn.prepareStatement(GET_SINGLE_BASE64);
    
    pstmt.setLong(1, photoid);
    pstmt.execute();
    ResultSet rstest = pstmt.getResultSet();
    
    
    while(rstest.next())
    {
    	s = rstest.getString("base64");
    	break;
    }
	
	rstest.close();
	pstmt.close();
	return s;
}
public static List<AVAHelper> Load_AVA() throws Exception
{
	
	getConnection();
	PreparedStatement pstmt = conn.prepareStatement(READ_AVA_SQL);
	
	pstmt.execute();
    ResultSet rstest = pstmt.getResultSet();
    List<AVAHelper> ava = new ArrayList<>();
    
    while(rstest.next())
    {
    	AVAHelper av = new AVAHelper();
    	av.setId(rstest.getLong(1));
    	av.setRating(rstest.getDouble(2));
    	
    	ava.add(av);
    	
    }
    
	return ava;
}
public static void update(Info inf) throws Exception
{
	getConnection();
	  
    PreparedStatement pstmt = conn.prepareStatement(Update_OBJECT_SQL);

    // set input parameters
    pstmt.setString(1,  inf.getPhototitel());
    pstmt.setString(2,  inf.getTags());
    pstmt.setLong(3, inf.getPhotoid());
    
    try
    {
    pstmt.executeUpdate();
    
   // System.out.println("UPDATE! - " + inf.getPhotoid());
    pstmt.close();
    }
    catch(Exception e)
    {
    	System.out.println(e.getMessage());
    	
    }
    
}
public static void insertIntoAva(String photoid, double ranking,String tags) throws Exception
{
	
	getConnection();
	PreparedStatement pstmt = conn.prepareStatement(WRITE_AVA_SQL);
	pstmt.setLong(1, Long.parseLong(photoid));
	pstmt.setDouble(2, ranking);
	pstmt.setString(3, tags);
	pstmt.execute();


}
public static void getConnection() throws Exception {
   // String driver = "org.gjt.mm.mysql.Driver";
	if(conn == null)
	{
	String driver= "com.mysql.jdbc.Driver";
    String url = "jdbc:mysql://localhost:3306/flickr?autoReconnect=true&useSSL=false";
    String username = "root";
    String password = "root";
    Class.forName(driver).newInstance();
     conn = DriverManager.getConnection(url, username, password);
    
	}
	else
	{
		if(conn.isClosed())
		{
			conn = null;
			getConnection();	
		}
		
		
	}
	
	//return conn;
  }

public static List<Long> LoadAllIds() throws Exception
{
	List<Long> list = new ArrayList<Long>();
	
	getConnection();
	PreparedStatement pstmt = conn.prepareStatement(READ_ALLPHOTO_IDS_SQL);
	
	pstmt.execute();
    ResultSet rstest = pstmt.getResultSet();

    while (rstest.next()) 
    	{               // Position the cursor                  4 
    	list.add(rstest.getLong("PhotoId"));
    	}
    
    rstest.close();                       // Close the ResultSet                  5 
    pstmt.close();    
    
    
return list;
}
 public static Boolean writeInfoObject(Info info) throws Exception {
  //  String className = object.getClass().getName();
	  
	 ///NUR IN DB SCHREIBEN WENN BASE64 != null
	 if(info.getBase64() == "" || info.getBase64().equals(invalid_base)|| info.getBase64().equals(invalid_base2))
		 return false;
	 
	 
 getConnection();
		  
    PreparedStatement pstmt = conn.prepareStatement(WRITE_OBJECT_SQL);

    // set input parameters
    pstmt.setString(1,  Long.toString(info.getPhotoid()));
    pstmt.setInt(2, info.getViews());
    pstmt.setInt(3, info.getFavs());
    pstmt.setString(4, info.getUrl());
    pstmt.setString(5, info.getBase64());
    pstmt.setString(6, info.getPhototitel());
    pstmt.setString(7, info.getTags());
    pstmt.setString(8, info.getFormat());
    pstmt.setString(9, info.getUserid());
    pstmt.setDate(10, new java.sql.Date(info.getPosted().getTime()));
    pstmt.setDate(11, new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
    try
    {
    LOGGER.log(Level.INFO, "WROTE IN DATABASE: {0}", info.getPhotoid());
    pstmt.executeUpdate();
    called++;
    return true;
    //System.out.print("IN DB!");
    }
    catch(Exception e)
    {
    	System.out.print(e.getMessage());
    	return false;
    }
    // get the generated key for the id
  /*  ResultSet rs = pstmt.getGeneratedKeys();
    int id = -1;
    if (rs.next()) {
      id = rs.getInt(1);
    }

    rs.close();
    pstmt.close();
    System.out.println("writeJavaObject: done serializing: " + className);
    return id;*/
  }

public static long Get_Amount() throws Exception
{
	getConnection();
	PreparedStatement ps = conn.prepareStatement(GET_AMOUNT);
	ResultSet rs = null;
	
	long result = 0;
	try{
		rs = ps.executeQuery();
		
		while(rs.next())
		{
			result = rs.getLong(1);
		}
		
		rs.close();
		rs = null;
		return result;
		}
	catch(Exception e)
	{
		LOGGER.log(Level.WARNING, e.getMessage());
		if(rs != null)
		rs.close();
		
		return result;
	}
	
}
public static List<Info> LoadAllPictures(int min_favcount, String[] tag) throws Exception
{
	List<Info> result = new ArrayList();
	getConnection();
	PreparedStatement statement = null;
	
	if(tag == null)
	{
	 statement = conn.prepareStatement(LOAD_DATA_INFO_LAZY,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	}
	else
	{
	 statement = conn.prepareStatement(LOAD_DATA_INFO_LAZY_TAGS,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	String tagstring = "";
	
	for(int i = 0;i< tag.length;i++)
	{
		tagstring = "%" + tag[i];
	}
	tagstring = tagstring + "%";
	 
	 statement.setString(2, tagstring);
	}
	statement.setInt(1, min_favcount);
	statement.setFetchSize(1000);
	ResultSet rs = null;
	try{
	 rs = statement.executeQuery();
	}
	catch(Exception e)
	{
		System.out.println(e.getMessage());
		return null;
	}
	LOGGER.log(Level.INFO, "Writting Results to List");
	int counter = 0;
	rs.last();
	Info[] arr = new Info[rs.getRow()];
	LOGGER.log(Level.INFO, "FETCHED: {0}", rs.getRow());
	rs.first();
	
    ResultSetMetaData md = rs.getMetaData();
    int columns = md.getColumnCount();
    HashMap<String, String> mapper = new HashMap<String, String>();
	List<Info> zwischenresult = new ArrayList<Info>();
	
	while(rs.next())
	{
		
		Info inf = new Info();
		try{
		inf.setPhotoid( rs.getLong("photoid"));
		
		//mapper.put(Long.toString(inf.getPhotoid()), rs.getString("base64"));
		
		//inf.setBase64(rs.getString("base64"));
		//inf.setUrl(rs.getString("url"));
	/*	if(rs.getString("phototitel") != null)
		inf.setPhototitel(rs.getString("phototitel"));
		else
		{
			inf.setPhototitel("");
		}*/
		inf.setFavs(rs.getInt("favs"));
		inf.setViews(rs.getInt("views"));
		if(rs.getString("pictureformat") != null)
		{
			inf.setFormat(rs.getString("pictureformat"));
		}
		else
		{
			inf.setFormat("");
		}
		//arr[counter] = inf;
		
		result.add(inf);
		//mapper.put(Long.toString(inf.getPhotoid()), inf);
		//zwischenresult.add(inf);
		counter++;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.WARNING, e.getMessage());
		}
		LOGGER.log(Level.INFO, "AMOUNT PROCESSED {0}", counter);
		//result.addAll(zwischenresult);
		//zwischenresult.clear();
		
	}
	rs.close();
	rs = null;
	LOGGER.log(Level.INFO, "Wrote all Results to List : {0}",result.size());
	
	//List<Info> res = new ArrayList<Info>(mapper.values());
	return result;
}
public static Map<Long, String> Eager_load_base64(List<Info> ids) throws Exception
{
	StringBuilder builder = new StringBuilder();

	for( int i = 0 ; i < ids.size(); i++ ) {
	    builder.append("?,");
	}

	String stmt = "select photoid,CONVERT(base64 USING utf8) as base64 from flickr.flickr where photoid in (" 
	               + builder.deleteCharAt( builder.length() -1 ).toString();
	stmt = stmt + ")";
	
	getConnection();
	PreparedStatement pstmt =conn.prepareStatement(stmt);
	
	
	for(int i = 0; i< ids.size();i++)
	{
		pstmt.setLong(  i+1, ids.get(i).getPhotoid() );
	}
	
	ResultSet rs = null;
	
	try{
		rs = pstmt.executeQuery();
		Map<Long, String> dictionary = new HashMap<Long, String>();
		String base64 ="";
		long photoid = 0;
		while(rs.next())
		{
			photoid = rs.getLong("photoid");
			base64 = rs.getString("base64");
			
			///FALLS FLICKR EIN GESPERRTES BILD ÜBERMITTELT HAT 
			if(base64.equals(invalid_base)== false && base64.equals(invalid_base2)== false)
			{			
				dictionary.put(photoid,base64);
			}
			else
			{
				LOGGER.info("EXCLUDED: " + photoid);
			}
			
		}
		
		
		return dictionary;
	}
	catch(Exception e)
	{
		return null;
	}
	
	
	
}

public static void DeleteByID(long photoid) throws Exception
{
	getConnection();
	
	PreparedStatement prep = conn.prepareStatement(Delete_BY_PhoTOID);
	prep.setLong(1, photoid);

	prep.execute();
	
	return;
}


public static long getCalled() {
	return called;
}


public static void setCalled(long called) {
	DBHelper.called = called;
}

}
