package com.ibm.sample;

import com.ibm.sample.model.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
		out.write(allRegularExpressions.toString());
	
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
    		codeSnippetLineRegularExpressoins.put(i,new CodeSnippetRegExWrapper(codeSnippetLine, generateRegexForALine(codeSnippetLine)));    		
    		
    	}    	
    	return codeSnippetLineRegularExpressoins;
    	
    }
    
    public static List<String> generateRegexForALine(String codeSnippetLine){
    	List<String> regularExpressionsInCodeSnippetLine = new ArrayList<>();
    	
    	int j = 0;
    	//each word is a token. It is terminated by a space, if there are not open brackets. Else it is terminated by closed bracket.
    	for(int i = 0 ; i < codeSnippetLine.length() ; i++) {    		
    		char currentChar = codeSnippetLine.charAt(i);
			if(currentChar == ' ') {
    			regularExpressionsInCodeSnippetLine.add(codeSnippetLine.substring(j,i));
    			j = i;
    		}  
			//If there is an open bracket.. then do not consider space to get a token. The token now is till the closed bracket.
			else if(currentChar == '(')  {
    			i++;
    			while(codeSnippetLine.charAt(i) != ')' || i == codeSnippetLine.length()-1) {
    				i++;
    			}
    		}
    	}
    	//Last word in the line..
    	regularExpressionsInCodeSnippetLine.add(codeSnippetLine.substring( j, (codeSnippetLine.length() - 1)));
    	
    	return regularExpressionsInCodeSnippetLine;

    }

}
