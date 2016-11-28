package com.sgk.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import com.sgk.util.FilterUtil;

import net.sf.json.JSONObject;

public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**solr服务器地址*/
	private static String baseURL = null;
	/**读取多core联合查询地址*/
	private static String shards = null;
	private static SolrClient solr;
	private String[] colist = { "username", "email", "password", "salt", "ip", "source", "name", "number", "mark", "updatetime" }; // 定义要检索的字段列表
	/**关键字过滤表*/
	private String filterk = null;;

	public void init() throws ServletException {
		filterk = this.getServletConfig().getInitParameter("filter_keywords");
		baseURL = this.getServletContext().getInitParameter("solraddr");
		shards = this.getServletContext().getInitParameter("shards");
		solr = new HttpSolrClient(baseURL);
	}

	/**
	 * 过滤检索结果
	 * 
	 * @param Result
	 *            检索结果
	 * @return String
	 */
	private String formatResult(String result) {
		return result.replaceAll("(?:\r|\n| )", "").replace("&quot", "\"").replace("&#92", "\\");
	}

	/**
	 * 处理关键字
	 * 
	 * @param keyword
	 *            关键字
	 * @return String
	 */
	private String formatKeyword(String keyword) {
		String keytemp = keyword;
		// 判断关键字是否为纯数字或者纯字母
		if (!keyword.matches("[0-9]+") && !keyword.matches("[a-zA-Z]+")) {
			// 判断关键字是否包含汉字
			if (keyword.getBytes().length != keyword.length()) {
				// 汉字加双引号精确搜索.不加则会开始分词
				keytemp = "\"" + keyword + "\"";
			}else{
				// 关键字既非纯数字纯字母.也非汉字,需后面加*号才能精确搜索
				keytemp = keyword + "*";
			}
		}
		return keytemp;
	}

	/**
	 * 返回错误结果
	 * 
	 * @param message
	 *            错误内容
	 * @return JSONObject
	 */
	private JSONObject errorResult(String message) {
		JSONObject json = new JSONObject();
		json.put("count", 0);
		json.put("result", "null");
		json.put("Message", message);
		return json;
	}

	private String legal(String keyword) {
		if (keyword.equals("")) {
			return "请输入关键词";
		}else if (keyword.length() < 5) {
			return "关键词长度不能小于5";
		} else if (keyword.length() > 30) {
			return "关键词长度不能大于30";
		} else if (FilterUtil.futility(keyword)) {
			return "搜索范围太大";
		} else if (FilterUtil.sensitive(keyword)) {
			return "关键字含有敏感字符";
		}
		return null;
	}

	/**
	 * solr 搜索
	 * 
	 * @param keyword
	 *            关键字
	 * @param start
	 *            起始行
	 * @param rows
	 *            行数
	 * @return JSONObject
	 */
	private JSONObject search(String keyword, int start, int rows) {
		try {
			if (legal(keyword) == null) {
				JSONObject json = new JSONObject();
				SolrQuery query = new SolrQuery();
				SolrDocumentList docs =  null;
				QueryResponse rsp = null;
				
				query.set("q", "keyword:" + keyword);	 // 关键词
				query.set("start", start);				 // 起始行
				query.set("rows", rows); 				 // 行数
				query.set("shards", shards); 			 // 多core查询
				rsp = solr.query(query);				 // 执行检索
				docs = rsp.getResults();
				json.put("count", docs.getNumFound()); 	 // 获取返回的条目数
				if (docs.getNumFound() == 0) 			 // 如果检索有结果
				{
					JSONObject json2 = new JSONObject();
					for (int i = start; i < rows; i++) {
						JSONObject json1 = new JSONObject();
						for (int j = 0; j < colist.length; j++) {
							json1.put(colist[j], formatResult(String.valueOf(docs.get(i).getFieldValue(colist[j]))));
						}
						json2.put(String.valueOf(i), json1.toString());
					}
					json.put("result", json2.toString());
					return json;
				} else {
					return errorResult("未搜索到任何结果");
				}
			} else {
				return errorResult("请输入关键词");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/plain;charset=GBK");
		PrintWriter out = response.getWriter();
		String k, start, rows;
		k = request.getParameter("keyword");
		start = request.getParameter("start");
		rows = request.getParameter("rows");
		
		if (k != null){
			//预定义起始行、行数
			if (start == null || rows == null){
				start = "0";
				rows = "20"; 
			}
			// 输出检索结果
			out.println(search(k, Integer.parseInt(start), Integer.parseInt(rows)).toString()); 
		}else{
			out.println("Keyword error!"); 
		}
			
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}
