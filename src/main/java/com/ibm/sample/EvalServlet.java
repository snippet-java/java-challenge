package com.ibm.sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

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
		} catch (Exception e) {
			htmlOutput += "Exception caught: " + e.getMessage();
		}
		output.addProperty("challengeOutput", htmlOutput);
		out.println(output);
		out.close();
    }
    
    private static final long serialVersionUID = 1L;
    
}
