package com.ibm.sample;

import com.google.gson.JsonObject;
import com.ibm.sample.model.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.print.attribute.standard.RequestingUserName;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GenerateRegularExpression
 */
@WebServlet("/generate-regular-expressions")
public class GenerateRegularExpression extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		

		PrintWriter out = response.getWriter();
		String validCode = request.getParameter("validCode");
		
		String test = request.getParameter("test");
		System.out.println(test);
		
		Map<Integer, CodeSnippetRegExWrapper> allRegularExpressions = generateRegex(validCode);
		//out.write(allRegularExpressions.toString());
		
		JsonObject output = new JsonObject();
		output.addProperty("generatedRegularExpressions", allRegularExpressions.toString());
		
		out.println(output);
		
		
		/*
		 * response.setContentType("text/HTML");
		 * String htmlOutput="<TABLE>   \r\n" + 
				"<tr style=\"text-align:center\"> \r\n" + 
				"    <th>Line No</th>\r\n" + 
				"    <th>Code</th> \r\n" + 
				"    <th>Regex</th>\r\n" + 
				"    <th>Required?</th>\r\n" + 
				"</tr>";
		
		for (Entry<Integer, CodeSnippetRegExWrapper> entry : allRegularExpressions.entrySet()) {
		    int lineNumber = entry.getKey();
		    CodeSnippetRegExWrapper  codeSnippetRegExWrapper = entry.getValue();
		    String code = codeSnippetRegExWrapper.getCodeSnippetLine();
		    
		    htmlOutput += "<TR>\r\n" + 
		    		"  <TD  WIDTH=\"5%\">"+lineNumber+"</td>\r\n" + 
		    		"  <TD  WIDTH=\"45%\">"+code+"</td>\r\n" + 
		    		"  <TD WIDTH=\"45%\"><input style=\"width: 85%;\"  type=\"text\" name=\"fname\" value=\""+codeSnippetRegExWrapper.getCodeSnippetRegularExpressions().toString()+"\"></td>\r\n" + 
		    		"  <TD WIDTH=\"5%\"><label><input type=\"checkbox\" id=\"cbox1\" value=\"first_checkbox\"></label><br></td>\r\n" + 
		    		"</TR>\r\n" + 
		    		"";
		    
		    htmlOutput+="</TABLE>";
		    out.write(htmlOutput);*/
		}
		
	
	
	
	
	//Generate Regular expression's for the provided sample code.
    public static Map<Integer, CodeSnippetRegExWrapper> generateRegex(String codeSnippet ) {
    	
    	Map<Integer,CodeSnippetRegExWrapper> codeSnippetLineRegularExpressoins = new TreeMap<>();
    	
    	
    	String[] codeSnippetLines = codeSnippet.split("\\n");    	
    	System.out.println("The string's after breaking by new line are : ");
    	
    	int i = 0;
    	for(String codeSnippetLine : codeSnippetLines) {
    		System.out.println("Line "+i+" "+codeSnippetLine);
    		i++;
    		
    		//Get words from the line
    		codeSnippetLineRegularExpressoins.put(i,new CodeSnippetRegExWrapper(codeSnippetLine, generateRegularExpressions(codeSnippetLine)));    		
    		
    	}    	
    	return codeSnippetLineRegularExpressoins;
    	
    }
    
    public static List<String> generateRegularExpressions(String codeSnippetLine){
		List<String> tokensInCodeSnippetLine =  deTokenize(codeSnippetLine);	
		List<String> regularExpressions = tokenToRegularExpression(tokensInCodeSnippetLine);
		return regularExpressions;
    
    }
    
    public static List<String> deTokenize(String codeSnippetLine){
    	List<String> tokensInCodeSnippetLine = new ArrayList<>();    	
    	//De-Tokennize
    	int j = 0;
    	char currentChar;
    	String currentToken;
    	//each word is a token. It is terminated by a space, if there are not open brackets. Else it is terminated by closed bracket.
    	int length = codeSnippetLine.length();
		for(int i = 0 ; i < length ; i++) {    		
    		currentChar = codeSnippetLine.charAt(i);
			if(currentChar == ' ') {
    			currentToken = (codeSnippetLine.substring(j,i));    			
				tokensInCodeSnippetLine.add(currentToken);
    			j = i;
    		}  
			//If there is an open bracket.. then do not consider space to get a token. The token now is till the closed bracket or End of the line
			else if(currentChar == '(')  {
    			i++;
    			while(codeSnippetLine.charAt(i) != ')' || i == length-1) {
    				i++;
    			}
    		} else if(currentChar == '"') {
    			i++;
    			while(codeSnippetLine.charAt(i) != '"' || i == length-1) {
    				i++;
    			}    			
    		}
    	}
    	//Last word in the line..
		currentToken = codeSnippetLine.substring( j, (length - 1));
    	tokensInCodeSnippetLine.add(currentToken);    	
    	
    	//Create regular expression for every token
    	//1. When ever there is a space , replace with //s* , But anything in "" need to be ignored    	
    	return tokensInCodeSnippetLine;
    }
    
    public static List<String> tokenToRegularExpression(List<String> tokens) {
    	List<String> regularExpressions = new ArrayList<String>();
    	
    	
    	for(String token : tokens) {
    		String trimmedToken = token.trim();
    		String tokenWithoutSpaces = trimmedToken.replaceAll("\\s+","\\\\\\s*");
    		regularExpressions.add(tokenWithoutSpaces);
    		/*char currentChar;
    		int length = token.length();
    		for(int i = 0 ; i < token.length() ; i++) {
    			currentChar = token.charAt(i);
    			if(currentChar == '"') {
        			i++;
        			while(token.charAt(i) != '"' || i == length-1) {
        				i++;
        			}   			
    			}  			
    		} */	
    	}
    	
    	return regularExpressions;
    }
    
    
}
