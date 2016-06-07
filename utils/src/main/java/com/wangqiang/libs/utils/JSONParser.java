package com.wangqiang.libs.utils;

import android.content.ContentValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author wangqiang
 *
 */
public class JSONParser {

	public static final ContentValues convertJsonToContentValue(JSONObject json){
		ContentValues value=new ContentValues();
		value.put("original_data",json.toString());
		try {
			Iterator<String> iterator = json.keys();
			while (iterator.hasNext()) {
				String key = iterator.next();
				value.put(key, json.getString(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
            return value;
        }
	}//end convertJsonToContentValue
	
	public static final ArrayList<ContentValues> convertJSONArrayToList(JSONArray jsonArray){
		return convertJSONArrayToList(jsonArray, 0);
	}
	
	/**
	 * @param jsonArray
	 * @return
	 */
	public static final ArrayList<ContentValues> convertJSONArrayToList(JSONArray jsonArray,int offset){
		ArrayList<ContentValues> res=new ArrayList<ContentValues>();
		ContentValues value;
		try {
			for(int i=offset;i<jsonArray.length();i++){
				value=convertJsonToContentValue(jsonArray.getJSONObject(i));
				if(value!=null){
					res.add(value);
				}
			}//end for
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
            return  res;
        }
	}//end convertJSONArrayToList
}
