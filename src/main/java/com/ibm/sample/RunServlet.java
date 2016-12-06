package com.ibm.sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/run")
public class RunServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	String code = request.getParameter("code");
    	
		JsonObject output = execCode(code);
		
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.println(output);
		
		out.close();
    }
    
    protected static JsonObject execCode(String code) throws ServletException, IOException {
    	String url = "http://cloudsandbox.mybluemix.net/exec";
		
		//Send a post request containing the code to be processed
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("code", code));
		
		post.setEntity(new UrlEncodedFormEntity(urlParameters));

		HttpResponse resp = client.execute(post);
		
		BufferedReader rd = new BufferedReader(
		        new InputStreamReader(resp.getEntity().getContent()));

		String result = new String();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result += line;
		}
		JsonParser parser = new JsonParser();
		return parser.parse(result).getAsJsonObject();
    }

    private static final long serialVersionUID = 1L;
}
