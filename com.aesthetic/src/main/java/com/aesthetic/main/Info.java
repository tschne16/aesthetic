package com.aesthetic.main;

import java.io.Serializable;
import java.util.Date;

import com.flickr4java.flickr.tags.Tag;

public class Info implements Serializable{

private String url;
private int views;
private int favs;
private long photoid;
private String base64;
private String phototitel;
private String tags;
private String format;
private String userid;
private Date posted;
public int getFavs() {
	return favs;
}
public void setFavs(int favs) {
	this.favs = favs;
}
public int getViews() {
	return views;
}
public void setViews(int views) {
	this.views = views;
}
public String getUrl() {
	return url;
}
public void setUrl(String url) {
	this.url = url;
}
public long getPhotoid() {
	return photoid;
}
public void setPhotoid(long photoid) {
	this.photoid = photoid;
}
public String getBase64() {
	return base64;
}
public void setBase64(String base64) {
	this.base64 = base64;
}
public String getPhototitel() {
	return phototitel;
}
public void setPhototitel(String phototitel) {
	
		
		  StringBuilder sb = new StringBuilder();
		  for(int i = 0 ; i < phototitel.length() ; i++){ 
		    if (Character.isHighSurrogate(phototitel.charAt(i))) continue;
		    sb.append(phototitel.charAt(i));
		  }
		  
		  this.phototitel   = sb.toString().substring(0, Math.min(sb.toString().length(), 10));
		  
		
	
	
	
	//this.phototitel = phototitel;
}
public String getTags() {
	return tags;
}
public void setTags(String tags) {
	this.tags = tags;
}
public void setTags(Tag[] tags) {
	String s = "";
	for(int i= 0; i < tags.length;i++)
	{
		if(i>0)
		{
			s = s + ";";
		}
		
		s = s + tags[i].getValue();
		
	}
	this.tags = s;
}
public String getFormat() {
	return format;
}
public void setFormat(String format) {
	this.format = format;
}
public void update() {
	
	
}
public String getUserid() {
	return userid;
}
public void setUserid(String userid) {
	this.userid = userid;
}
public Date getPosted() {
	return posted;
}
public void setPosted(Date posted) {
	this.posted = posted;
}

}
