Page Analyzer

Project provides a functionality to compare two HTML-files and find modified element by example.


PageAnalyzer.jar in the root is working example of the application. 
Command to run:

java -cp PageAnalyzer.jar com/agileengine/analyzer/PageAnalyzer <input_origin_file_path> <input_other_sample_file_path> <input_original_element_id>

where:
 <input_origin_file_path> - path to the original html-file (mandatory);
 <input_other_sample_file_path> - path to the html-file to compare with (mandatory);
 <input_original_element_id> - id of the element to compare. Optional parameter. If not specified, 'make-everything-ok-button' id will be used.

The result is XML path from the root to the most similar element. Example:

html > body > div > div > div[3] > div[1] > div > div[2] > a[2]