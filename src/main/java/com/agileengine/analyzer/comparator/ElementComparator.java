package com.agileengine.analyzer.comparator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Compares given elements and returns the most similar one.
 */
public class ElementComparator {

   private static final Logger LOGGER = Logger.getLogger(ElementComparator.class.getName());

   private static final String TITLE_ATTRIBUTE_NAME = "title";
   private static final String ONCLICK_ATTRIBUTE_NAME = "onclick";
   private static final String HREF_ATTRIBUTE_NAME = "href";

   private Element originalElement;
   private Elements similarElements;

   public ElementComparator(Element originalElement, Elements similarElements) {
      Objects.requireNonNull(originalElement, "Example element cannot be null");
      Objects.requireNonNull(similarElements, "Similar elements cannot be null");

      this.originalElement = originalElement;
      this.similarElements = similarElements;
   }

   /**
    * Compares by href, title, onclick, text and class attributes values.
    */
   public Element findMostSimilarElement() {
      if (similarElements.isEmpty()) {
         LOGGER.log(Level.WARNING, "No similar elements present");
         return null;
      }

      Map<Element, Double> elementsWithRates = similarElements.stream()
            .collect(Collectors.toMap(Function.identity(), this::getSimilarityRate));

      Optional<Map.Entry<Element, Double>> maxRateElement = elementsWithRates.entrySet().stream()
            .max(Comparator.comparing(Map.Entry::getValue));

      return getElement(maxRateElement.get());
   }

   private Element getElement(Map.Entry<Element, Double> maxRateElement) {
      Element element;

      if (Objects.nonNull(maxRateElement)) {

         element = maxRateElement.getKey();

         LOGGER.log(Level.INFO, "The most similar element " + element +
               " with rate " + maxRateElement.getValue() + " of maximum 5.0");
      } else {

         element = null;
      }

      return element;
   }

   private double getSimilarityRate(Element similarElement) {
      double hrefRate = getSimilarityRateImpl(originalElement.attr(HREF_ATTRIBUTE_NAME), similarElement.attr(HREF_ATTRIBUTE_NAME));
      double titleRate = getSimilarityRateImpl(originalElement.attr(TITLE_ATTRIBUTE_NAME), similarElement.attr(TITLE_ATTRIBUTE_NAME));
      double onclickRate = getSimilarityRateImpl(originalElement.attr(ONCLICK_ATTRIBUTE_NAME), similarElement.attr(ONCLICK_ATTRIBUTE_NAME));
      double textRate = getSimilarityRateImpl(originalElement.text(), similarElement.text());

      String originalSortedClassNames = getSortedClassNames(originalElement);
      String similarSortedClassNames = getSortedClassNames(similarElement);

      double classRate = getSimilarityRateImpl(originalSortedClassNames, similarSortedClassNames);

      return hrefRate + textRate + titleRate + onclickRate + classRate;
   }

   /**
    * Uses Levenshtein algorithm.
    * Return value: 1 - most similar, 0 - not similar at all.
    *
    * @param original original element property value
    * @param another  the same property value of another element to compare with
    * @return rate of similarity.
    */
   private double getSimilarityRateImpl(String original, String another) {
      if (StringUtils.isAllBlank(original, another)) {
         return 1.;
      }

      if (StringUtils.isAnyBlank(original, another)) {
         return 0.;
      }

      String longerStr = original;
      String shorterStr = another;

      if (longerStr.length() < shorterStr.length()) {
         longerStr = another;
         shorterStr = original;
      }

      Integer longerStrLength = longerStr.length();
      Integer levenshteinDistance = LevenshteinDistance.getDefaultInstance().apply(longerStr, shorterStr);

      return (longerStrLength - levenshteinDistance) / longerStrLength.doubleValue();
   }

   private String getSortedClassNames(Element element) {
      List<String> originalSortedClassNames = element.classNames().stream()
            .map(String::toLowerCase)
            .sorted(String::compareTo)
            .collect(Collectors.toList());

      return String.join(" ", originalSortedClassNames);
   }
}
