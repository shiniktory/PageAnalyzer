package com.agileengine.analyzer;

import com.agileengine.analyzer.comparator.ElementComparator;
import com.agileengine.analyzer.parser.DocumentParser;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Retrieves and processes passed parameters.
 * Outputs an XML path to the most similar element.
 */
public class PageAnalyzer {

   private static final String EXAMPLE_ELEMENT_ID = "make-everything-ok-button";

   public static void main(String[] args) throws Exception {

      if (args.length < 2) {
         System.err.println("Not all arguments passed");
         System.out.println("USAGE: <input_origin_file_path> <input_other_sample_file_path> <input_original_element_id> \nThird parameter is optional");

         return;
      }

      String elementId = getElementId(args);
      Element originalElement = new DocumentParser(getOriginalFileName(args)).getElementById(elementId);
      Elements withSameParent = new DocumentParser(getSampleFileName(args)).findSimilarElements(originalElement);

      ElementComparator elementComparator = new ElementComparator(originalElement, withSameParent);
      Element mostSimilarElement = elementComparator.findMostSimilarElement();

      String pathToResultedElement = DocumentParser.getPathToElement(mostSimilarElement);

      System.out.println("RESULT: \n" + pathToResultedElement);
   }

   private static String getSampleFileName(String[] args) {
      return args[1];
   }

   private static String getOriginalFileName(String[] args) {
      return args[0];
   }

   private static String getElementId(String[] args) {
      String elementId;

      if (args.length == 3) {
         elementId = args[2];
      } else {
         elementId = EXAMPLE_ELEMENT_ID;
      }

      return elementId;
   }
}
