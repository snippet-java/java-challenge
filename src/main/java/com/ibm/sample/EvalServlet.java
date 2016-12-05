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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet("/eval")
public class EvalServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		int score = 0;
		
		String studentCode = request.getParameter("code");
		String questionNo = "java1";
		
//		String formattedText1 = textArea1.replace("\n", "").replaceAll("\\s+", "");
//		String formattedText2 = textArea2.replace("\n", "").replaceAll("\\s+", "");
		JsonObject studentAnswer = RunServlet.execCode(studentCode);
		
		boolean compilationError = false;
		//if compilation error
		if(studentAnswer == null || studentAnswer.get("err") == null || studentAnswer.get("err").getAsString().length() > 0)
			compilationError = true;

		//Matching the default answer with studentAnswer
		if(!compilationError) {
			if(studentAnswer.get("out").getAsString().trim().equals(
					RunServlet.answers.get(questionNo).get("answer").getAsString().trim())) {
				
				score += 50;
			}
		}
		//Matching the process - to find out if expected method is used
		JsonArray keywords = RunServlet.answers.get(questionNo).get("keywords").getAsJsonArray();
		if(keywords.size() > 0) {
			//if all keywords are matched, give full score, otherwise partial score
			int matchedKeywordNo = 0;
			for(int i=0; i < keywords.size(); i++) {
				if(studentCode.contains(keywords.get(i).getAsString()))
					matchedKeywordNo++;
			}
			//divide equally the points for each correct keypoint
			double keywordScore = (double)50 / (double)keywords.size() * (double)matchedKeywordNo;
			score += new Double(keywordScore).intValue();
		}
		//if no keyword to check, just give the another 50 score
		else
			score += 50;
		
		//Printing output
		if(score >= 100)
			out.println("<p><strong>Score: " + score + " - Excellent! </strong></p>");
		//Else, display expected code and missing tokens of student code (comparing to expected code)
		else
		{		
			DiffMatchPatch dmp =  new DiffMatchPatch();

			out.println("<p><strong>Score: " + score + " </strong></p>");
			LinkedList<Diff> textDiffList =  dmp.diffMain(studentCode, RunServlet.answers.get(questionNo).get("question").getAsString());
			dmp.diffCleanupSemantic(textDiffList);
	//		LinkedList<Diff> textDiffList =  dmp.diffMain(formattedText1, formattedText2);
			
			//Get the missing token
			String formattedStudentCode = studentCode.replace("\n", "").replaceAll("\\s+", "");
			String formattedExpectedCode = RunServlet.answers.get(questionNo).get("question").getAsString().replace("\n", "").replaceAll("\\s+", "");
			LinkedList<Diff> tokenDiffList =  dmp.diffMain(formattedStudentCode, formattedExpectedCode);
			ArrayList<String> missingTokens = new ArrayList<>();
			for(Diff diff : tokenDiffList) {
				if("insert".equalsIgnoreCase(diff.operation.name()))
					missingTokens.add(diff.text);
			}
			
			out.println("<p><strong>Missing token(s): " + missingTokens.size() + "</strong>");
			out.println("<ul>");
			for(String token: missingTokens) {
				out.println("<li>" + token + "</li>");
			}
			out.println("</ul>");
			out.println("<p><strong>Best Answer</strong></p>");
			out.println(dmp.diffPrettyHtml(textDiffList));
		}

		out.close();
    }
    
    private static final long serialVersionUID = 1L;
    
}
