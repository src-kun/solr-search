package com.sgk.util;

import com.sgk.bean.Sgk161125;

import net.sf.json.JSONObject;

public class JsonAnalysisUtil {

	/**
	 * Json ×ª javaBean
	 * 
	 * @param json
	 * @return
	 */
	public static Sgk161125 JsonToBean(String json ) {
		return (Sgk161125) JSONObject.toBean(JSONObject.fromObject(json), Sgk161125.class);
	}
	
	/**
	 * Json ×ª ×Ö·û´®
	 * 
	 * @param json
	 * @return
	 */
	public static String JsonToString(String json){
		System.out.println(json);
		JSONObject j = JSONObject.fromObject(json);
		String  str = String.valueOf(((JSONObject)j.get("response")).get("docs"));
		str = str.substring(1,str.length() - 1);
		return str;
	}
	
	
	public static void main(String[] args) {
		Sgk161125 sgk = null;
		String json = "{  \"responseHeader\":{    \"status\":0,    \"QTime\":5,    \"params\":{      \"q\":\"keyword:1025236110\",      \"indent\":\"true\"}},  \"response\":{\"numFound\":1,\"start\":0,\"docs\":[      {        \"password\":\"1314521@jia\",        \"ip\":\"60.172.1.252\",        \"id\":173562,        \"username\":\"1025236110\",        \"_version_\":1551956144645210112}]  }}";
		sgk = JsonToBean(JsonToString(json));
		System.out.println(sgk.getPassword());
		
	}
}
