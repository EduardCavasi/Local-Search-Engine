package org.example.service.file_search;

import org.example.error.SearchRequestParseException;
import org.example.model.search.SearchParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Component
public class SearchRequestParser {
    private static final Logger logger = LoggerFactory.getLogger(SearchRequestParser.class);
    public SearchParams parse(String searchRequest) {
        SearchParams searchParams = new SearchParams();
        searchParams.setSearchRequest(searchRequest);
        ///spit on spaces that aren t inside " "
        String[] qualifierAndValues = searchRequest.trim().split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        try {
            for (String qualifierAndValue : qualifierAndValues) {
                String[] keyValue = qualifierAndValue.split("=");
                String qualifier = keyValue[0];
                String value = keyValue[1];
                String[] orValues = value.split("\\|");

                ///remove " "
                orValues = Arrays.stream(orValues).map(orValue -> {
                    if(orValue.startsWith("\"")){
                        return orValue.substring(1, orValue.length()-1);
                    }
                    return orValue;
                }).toArray(String[]::new);

                ///replace spaces in content with &
                if(qualifier.equals("content")){
                    orValues = Arrays.stream(orValues).map(raw -> {
                        String normalized = raw.replaceAll("\\s+", " ");
                        return normalized.replaceAll("(?<=\\w)\\s+(?=\\w)", " & ");
                    }).toArray(String[]::new);
                }
                List<String> orValuesList = List.of(orValues);
                String adderName = "add" + Character.toUpperCase(qualifier.charAt(0)) + qualifier.substring(1);
                try {
                    Method adderMethod = SearchParams.class.getMethod(adderName, List.class);
                    boolean success = (boolean) adderMethod.invoke(searchParams, orValuesList);
                    if (!success) {
                        logger.error("Could not parse {}", Arrays.toString(orValues));
                        throw new SearchRequestParseException("Could not parse " + Arrays.toString(orValues));
                    }

                } catch (NoSuchMethodException e) {
                    logger.error("Wrong qualifier in searchRequest: {}", qualifier);
                    throw new SearchRequestParseException("Wrong qualifier in searchRequest: " + qualifier);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    logger.error("Wrong value in searchRequest corresponding to qualifier: {}", qualifier);
                    throw new SearchRequestParseException("Wrong value in searchRequest corresponding to qualifier: " + qualifier);
                }
            }
        }
        catch (Exception e){
            logger.error("Could not parse {}", Arrays.toString(qualifierAndValues));
            throw new SearchRequestParseException("Could not parse " + Arrays.toString(qualifierAndValues));
        }
        return searchParams;
    }
}
