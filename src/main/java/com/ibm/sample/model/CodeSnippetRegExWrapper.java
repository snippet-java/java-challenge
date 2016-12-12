package com.ibm.sample.model;

import java.util.List;

public class CodeSnippetRegExWrapper {
	
	String codeSnippetLine;
	List<String> codeSnippetRegularExpressions;
	
	
	
	
	public CodeSnippetRegExWrapper(String codeSnippetLine, List<String> codeSnippetRegularExpressions) {
		super();
		this.codeSnippetLine = codeSnippetLine;
		this.codeSnippetRegularExpressions = codeSnippetRegularExpressions;
	}
	
	public String getCodeSnippetLine() {
		return codeSnippetLine;
	}
	public void setCodeSnippetLine(String codeSnippetLine) {
		this.codeSnippetLine = codeSnippetLine;
	}
	public List<String> getCodeSnippetRegularExpressions() {
		return codeSnippetRegularExpressions;
	}
	public void setCodeSnippetRegularExpressions(List<String> codeSnippetRegularExpressions) {
		this.codeSnippetRegularExpressions = codeSnippetRegularExpressions;
	}

	@Override
	public String toString() {
		return "CodeSnippetRegExWrapper [codeSnippetLine=" + codeSnippetLine + ", codeSnippetRegularExpressions="
				+ codeSnippetRegularExpressions.toString() + "]";
	}
}
