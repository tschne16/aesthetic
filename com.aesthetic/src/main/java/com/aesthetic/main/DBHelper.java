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
static final String READ_ALLPHOTO_IDS_SQL = "SELECT photoid FROM flickr.flickr;";
static final String Update_OBJECT_SQL = "UPDATE flickr.flickr set phototitel =  ?, tags = ? where photoid = ?";
static final String LOAD_DATA_INFO = "SELECT photoid,url,phototitel,favs,views,CONVERT(base64 USING utf8) as base64 FROM flickr.flickr where favs >= ?";
static final String LOAD_DATA_INFO_LAZY = "SELECT photoid,favs,views,pictureformat FROM flickr.flickr where views >= ?";
static final String Delete_BY_PhoTOID = "DELETE FROM flickr.flickr where photoid = ?";
static final String GET_AMOUNT = "SELECT COUNT(PHOTOID) from flickr.flickr";
static Connection conn;
static final String WRITE_AVA_SQL = "INSERT INTO flickr.ava(photoid,rating,tags) values(?,?,?)";
static final String READ_AVA_SQL = "SELECT * FROM flickr.ava";
private static long called;
static String driver;
static String pw;
static String user;
static String url;

private static final Logger LOGGER = Logger.getLogger( DBHelper.class.getName() );


public DBHelper(String d, String pw, String user, String u)
{
	 driver = "com.mysql.jdbc.Driver";
     url = "jdbc:mysql://localhost:3306/flickr?autoReconnect=true&useSSL=false";
     user = "root";
     pw = "root";
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
    String url = "jdbc:mysql://localhost:3306/flickr";
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
	 if(info.getBase64() == "")
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
public static List<Info> LoadAllPictures(int min_favcount) throws Exception
{
	List<Info> result = new ArrayList();
	getConnection();
	PreparedStatement statement = conn.prepareStatement(LOAD_DATA_INFO_LAZY,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	
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
			dictionary.put(photoid,base64);
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
