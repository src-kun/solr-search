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
	/**solr��������ַ*/
	private static String baseURL = null;
	/**��ȡ��core���ϲ�ѯ��ַ*/
	private static String shards = null;
	private static SolrClient solr;
	private String[] colist = { "username", "email", "password", "salt", "ip", "source", "name", "number", "mark", "updatetime" }; // ����Ҫ�������ֶ��б�
	/**�ؼ��ֹ��˱�*/
	private String filterk = null;;

	public void init() throws ServletException {
		filterk = this.getServletConfig().getInitParameter("filter_keywords");
		baseURL = this.getServletContext().getInitParameter("solraddr");
		shards = this.getServletContext().getInitParameter("shards");
		solr = new HttpSolrClient(baseURL);
	}

	/**
	 * ���˼������
	 * 
	 * @param Result
	 *            �������
	 * @return String
	 */
	private String formatResult(String result) {
		return result.replaceAll("(?:\r|\n| )", "").replace("&quot", "\"").replace("&#92", "\\");
	}

	/**
	 * ����ؼ���
	 * 
	 * @param keyword
	 *            �ؼ���
	 * @return String
	 */
	private String formatKeyword(String keyword) {
		String keytemp = keyword;
		// �жϹؼ����Ƿ�Ϊ�����ֻ��ߴ���ĸ
		if (!keyword.matches("[0-9]+") && !keyword.matches("[a-zA-Z]+")) {
			// �жϹؼ����Ƿ��������
			if (keyword.getBytes().length != keyword.length()) {
				// ���ּ�˫���ž�ȷ����.������Ὺʼ�ִ�
				keytemp = "\"" + keyword + "\"";
			}else{
				// �ؼ��ּȷǴ����ִ���ĸ.Ҳ�Ǻ���,������*�Ų��ܾ�ȷ����
				keytemp = keyword + "*";
			}
		}
		return keytemp;
	}

	/**
	 * ���ش�����
	 * 
	 * @param message
	 *            ��������
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
			return "������ؼ���";
		}else if (keyword.length() < 5) {
			return "�ؼ��ʳ��Ȳ���С��5";
		} else if (keyword.length() > 30) {
			return "�ؼ��ʳ��Ȳ��ܴ���30";
		} else if (FilterUtil.futility(keyword)) {
			return "������Χ̫��";
		} else if (FilterUtil.sensitive(keyword)) {
			return "�ؼ��ֺ��������ַ�";
		}
		return null;
	}

	/**
	 * solr ����
	 * 
	 * @param keyword
	 *            �ؼ���
	 * @param start
	 *            ��ʼ��
	 * @param rows
	 *            ����
	 * @return JSONObject
	 */
	private JSONObject search(String keyword, int start, int rows) {
		try {
			if (legal(keyword) == null) {
				JSONObject json = new JSONObject();
				SolrQuery query = new SolrQuery();
				SolrDocumentList docs =  null;
				QueryResponse rsp = null;
				
				query.set("q", "keyword:" + keyword);	 // �ؼ���
				query.set("start", start);				 // ��ʼ��
				query.set("rows", rows); 				 // ����
				query.set("shards", shards); 			 // ��core��ѯ
				rsp = solr.query(query);				 // ִ�м���
				docs = rsp.getResults();
				json.put("count", docs.getNumFound()); 	 // ��ȡ���ص���Ŀ��
				if (docs.getNumFound() == 0) 			 // ��������н��
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
					return errorResult("δ�������κν��");
				}
			} else {
				return errorResult("������ؼ���");
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
			//Ԥ������ʼ�С�����
			if (start == null || rows == null){
				start = "0";
				rows = "20"; 
			}
			// ����������
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
