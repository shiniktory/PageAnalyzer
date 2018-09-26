package com.agileengine.analyzer.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Parses the specified document and provides a functionality
 * for manipulations with it.
 */
public class DocumentParser {

   private static final Logger LOGGER = Logger.getLogger(DocumentParser.class.getName());

   private static final String CHARSET_NAME = "UTF-8";
   private static final String PATH_ELEMENTS_DELIMITER = " > ";

   private final Path pathToFile;

   public DocumentParser(String fileName) throws IOException {

      pathToFile = Files.find(Paths.get(fileName), 8,
            (path, basicFileAttributes) -> path.endsWith(fileName))
            .findFirst()
            .orElseThrow(FileNotFoundException::new);
   }

   public Element getElementById(String elementId) throws IOException {
      Document document = getDocument();

      String expression = "#" + elementId;

      Elements foundElements = document.select(expression);

      if (foundElements == null || foundElements.isEmpty()) {
         LOGGER.log(Level.WARNING, "No elements with id " + elementId + " found");
         return null;
      }

      if (foundElements.size() > 1) {
         LOGGER.log(Level.WARNING, "There are more than one element with id " + elementId + " found");
      }

      Element resultedElement = foundElements.first();

      LOGGER.log(Level.INFO, "Found element: " + resultedElement);

      return resultedElement;
   }

   public Elements findSimilarElements(Element exampleElement) throws IOException {
      String expression = getSameParentAndChildExpression(exampleElement);
      Elements similarElements = getDocument().select(expression);

      LOGGER.log(Level.INFO, "Found elements with the same parent and tag: " + similarElements.size());

      return similarElements;
   }

   public static String getPathToElement(Element element) {
      Elements parents = element.parents();
      StringBuilder stringBuilder = new StringBuilder();

      for (int i = parents.size() - 1; i >= 0; i--) {
         Element parent = parents.get(i);
         String elementDescription = getElementDescription(parent);

         stringBuilder.append(elementDescription)
               .append(PATH_ELEMENTS_DELIMITER);
      }

      stringBuilder.append(getElementDescription(element));

      return stringBuilder.toString().trim();
   }

   private static String getElementDescription(Element element) {
      String nodeName = element.nodeName();
      Integer currentElementIndex = element.elementSiblingIndex();
      List<Element> siblingElements = getSiblingsWithSameNodeName(element, nodeName);

      String elementDescription;

      if (!siblingElements.isEmpty()) {
         elementDescription = appendElementIndex(currentElementIndex, siblingElements, nodeName);
      } else {
         elementDescription = nodeName;
      }

      return elementDescription;
   }

   private static String appendElementIndex(Integer currentElementIndex, List<Element> siblingElements,
                                            String elementDescription) {

      long similarSiblingsCount = siblingElements.stream()
            .filter(el -> el.elementSiblingIndex() < currentElementIndex)
            .count();

      long elementIndex = similarSiblingsCount + 1;
      elementDescription += "[" + elementIndex + "]";

      return elementDescription;
   }

   private static List<Element> getSiblingsWithSameNodeName(Element element, String nodeName) {
      return element.siblingElements().stream()
            .filter(el -> el.nodeName().equals(nodeName))
            .collect(Collectors.toList());
   }

   private Document getDocument() throws IOException {
      return Jsoup.parse(pathToFile.toFile(), CHARSET_NAME);
   }

   private String getSameParentAndChildExpression(Element exampleElement) {
      return exampleElement.parent().nodeName() + " > " + exampleElement.nodeName();
   }
}
