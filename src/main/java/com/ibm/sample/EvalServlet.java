package com.ibm.sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Diff;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/eval")
public class EvalServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		int score = 0;
		ArrayList<String> missingTokens = new ArrayList<>();
		JsonObject output = new JsonObject();
		String htmlOutput = "";
		
		String playgroundCode = request.getParameter("playgroundCode");
		String validCode = request.getParameter("validCode");
		String playgroundOutput = request.getParameter("playgroundOutput");
		String validOutput = request.getParameter("validOutput");

		try {
			//Verify if the provided playground code containing valid output, otherwise run again to generate output
			JsonObject studentAnswer = new JsonObject();
			if(playgroundOutput == null || playgroundOutput.isEmpty())
				studentAnswer = RunServlet.execCode(playgroundCode);
			else {
				JsonParser parser = new JsonParser();
				studentAnswer = parser.parse(playgroundOutput).getAsJsonObject();
			}
			
			//verification on the expected code
			JsonObject validAnswer = new JsonObject();
			if(validOutput == null || validOutput.isEmpty())
				validAnswer = RunServlet.execCode(validCode);
			else {
				JsonParser parser = new JsonParser();
				validAnswer = parser.parse(validOutput).getAsJsonObject();
			}
			
			boolean compilationError = false;
			//if compilation error
			if(studentAnswer == null || studentAnswer.get("err") == null || studentAnswer.get("err").getAsString().length() > 0)
				compilationError = true;
	
			
			List<List<String>> regexOutput = null;
			if(!compilationError) {
				//Matching the default answer with studentAnswer
				if(studentAnswer.get("out").getAsString().trim().equals(validAnswer.get("out").getAsString().trim())) {
					
					score += 50;
				}
				
				//Matching the process - to find out if expected method is used
				DiffMatchPatch dmp =  new DiffMatchPatch();
				
				//Get the missing token
				String formattedStudentCode = playgroundCode.replace("\n", "").replaceAll("\\s+", "");
				String formattedExpectedCode = validCode.replace("\n", "").replaceAll("\\s+", "");
				LinkedList<Diff> tokenDiffList =  dmp.diffMain(formattedStudentCode, formattedExpectedCode);
				dmp.diffCleanupSemantic(tokenDiffList);
				for(Diff diff : tokenDiffList) {
					if("insert".equalsIgnoreCase(diff.operation.name())) {
						//split the missingToken by ;
						String[] rawTokens = diff.text.split(";");
						for(int i=0; i < rawTokens.length; i++) 
							missingTokens.add(rawTokens[i]);
					}
				}
				
				
				//if there is no missing token, give another 50 points
				if(missingTokens.size() <= 0)
					score += 50;
				
				//TODO::
				//Match Regular Expressions
		        
		        List<String> regExes = new ArrayList<String>();
		        regExes.add("An Apple and a pineapple. Prefer a small apple rather than a big Pineapple.");
		        regExes.add("\\s+");
		        regExes.add("text.replace('a', 'o')");
		        regExes.add("modifiedText");
		        regExes.add("small Pineapple");
		        regExes.add("text.replace('a'\\s*,\\s*'o')");

		
		        regexOutput =  evaluateRegExpressions(playgroundCode,regExes);		      
		        System.out.println(regexOutput.toString());
			}
			
			//Printing output
			if(compilationError)
				htmlOutput = "<p><strong>Compilation Error. Please check again the code</strong></p>";
			else if(score >= 100)
				htmlOutput = "<p><strong>Score: " + score + " - Excellent! </strong></p>";
			//Else, display expected code and missing tokens of student code (comparing to expected code)
			else {		
				htmlOutput += "<p><strong>Score: " + score + " </strong></p>";
				
				htmlOutput += "<p><strong>Missing token(s): " + missingTokens.size() + "</strong>";
				htmlOutput += "<ul>";
				for(String token: missingTokens) {
					htmlOutput += "<li>" + token + "</li>";
				}
				htmlOutput += "</ul>";
	
				DiffMatchPatch dmp =  new DiffMatchPatch();
				LinkedList<Diff> textDiffList =  dmp.diffMain(playgroundCode, validCode);
				dmp.diffCleanupSemantic(textDiffList);
				htmlOutput += "<p><strong>Best Answer</strong></p>";
				htmlOutput += dmp.diffPrettyHtml(textDiffList);
			}
			
			//TODO:
			//Adding regex result 
			
			htmlOutput += "</br>";
			htmlOutput += "</br>";
			htmlOutput += "<h4> Regex Result </h4>";
			
			htmlOutput += "<p> Matched Regular Expressions are :  </p>";		
			List<String> matchedRegularExpressions = regexOutput.get(0);
			int matchedExpressoinsNo = matchedRegularExpressions.size();
			htmlOutput += "<ul>";
			for(String currentRegEx : matchedRegularExpressions) {
				htmlOutput+="<li>";
				htmlOutput+=currentRegEx;
				htmlOutput+="</li>";					
			}
			htmlOutput += "</ul>";
			
			htmlOutput += "<p> Un-Matched Regular Expressions are :  </p>";		
			List<String> unMatchedRegularExpressions = regexOutput.get(1);
			int unMatchedExpressoinsNo = unMatchedRegularExpressions.size();
			htmlOutput += "<ul>";
			for(String currentRegEx : unMatchedRegularExpressions) {
				htmlOutput+="<li>";
				htmlOutput+=currentRegEx;
				htmlOutput+="</li>";					
			}
			htmlOutput += "</ul>";
			
			System.out.println("Number of matchedExpressoinsNo are "+matchedExpressoinsNo);
			System.out.println("Number of unmatched are "+unMatchedExpressoinsNo);
			float percentageOfMatchedRegExpressions =  ((float) (matchedExpressoinsNo)/ (matchedExpressoinsNo + unMatchedExpressoinsNo) ) * 100;
			htmlOutput+="</br>";
			htmlOutput+= "<h4> Percentage of matched regular expressions are "+(percentageOfMatchedRegExpressions)+"</h4>";

			
		} catch (Exception e) {
			htmlOutput += "Exception caught: " + e.getMessage();
		}
		output.addProperty("challengeOutput", htmlOutput);
		out.println(output);
		out.close();
    }
    
    public  List<List<String>> evaluateRegExpressions(String codeSnippet , List<String> regExpressions) {  
    	
    	List<List<String>> evaluatedRegEx = new ArrayList<List<String>>();    	
    	List<String> matchedRegExpressions = new ArrayList<String>();
    	List<String> unMatchedRegExpressions = new ArrayList<String>();
    	
    	for(String currentRegEx : regExpressions) {
    		Pattern currentRegPattern = Pattern.compile(currentRegEx);
    		Matcher matcher = currentRegPattern.matcher(codeSnippet);
    		//If the regEx is machined with student code    		
    		if(matcher.find()){
    			System.out.println(currentRegEx + "Matched");
        		//add to matched array
    			matchedRegExpressions.add(currentRegEx);
    		}else {
    			System.out.println(currentRegEx + "Not Matched");
    			//add to unmatched array
    			unMatchedRegExpressions.add(currentRegEx);
    		}
    	}    	   	
    	
    	evaluatedRegEx.add(matchedRegExpressions);
    	evaluatedRegEx.add(unMatchedRegExpressions);
    	
		return evaluatedRegEx;    	
    }
    
    private static final long serialVersionUID = 1L;
    
}
