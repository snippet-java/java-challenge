$(function() {
	
	$editor = CodeMirror.fromTextArea(document.getElementById("text1"), {
		title: "Student Code",
		lineNumbers: true,
		matchBrackets: true,
		mode: "text/x-java",
		lineWrapping: true
	});
	
	$editor2 = CodeMirror.fromTextArea(document.getElementById("expected-text"), {
		title: "Expected Code",
		lineNumbers: true,
		matchBrackets: true,
		mode: "text/x-java",
		lineWrapping: true,
		readOnly: "nocursor"
	});
	
	// Buttons click action
	$("#submit").click(computeDiff);
	$("#run").click(testRun);
	$("#expected-run").click(expectedRun);
})


function computeDiff() {
	var studentCode = $editor.getValue();
	var validCode = $editor2.getValue();
	var options = {
			url : "/eval",
			method : "post",
			dataType: "json",
			data : {
				playgroundCode : studentCode,
				validCode : validCode
			}
	};
	$('#outputdiv').html("<p>Submission in progress. Please wait...</p>");
	
	$.ajax(options)
	.done(function(data) {
		 $('#outputdiv').html(data.challengeOutput);
	})
}

function testRun() {
	var code = $editor.getValue();
	var options = {
			url : "/run",
			method : "post",
			dataType: "json",
			data : {code : code}
	};
	$('#outputdiv').html('<textarea id="result" STYLE="width: 100%"></textarea>');
	$("#result").val("Your code is being processed...");
	
	$.ajax(options)
	.done(function(data) {
		if(data.out) {
			$("#result").val(data.out);
		}
		else {
			$("#result").val(data.err);
		}
	})
}

function expectedRun() {
	var code = $editor2.getValue();
	var options = {
			url : "/run",
			method : "post",
			dataType: "json",
			data : {code : code}
	};
	$('#expected-outputdiv').html('<textarea id="expected-result" STYLE="width: 100%"></textarea>');
	$("#expected-result").val("Your code is being processed...");
	
	$.ajax(options)
	.done(function(data) {
		$("#expected-result").val(data.out);
	})
}