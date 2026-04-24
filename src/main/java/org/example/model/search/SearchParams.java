package org.example.model.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * Class containing all the possible search parameters usable by the search engine
 * When the search uses file metadata the field needsMetadata = true
 * When the search uses file content the field needsContent = true
 */
@NoArgsConstructor
@Getter
@ToString
public class SearchParams {
    @Setter
    private String searchRequest;

    private List<String> queryFileName;
    private List<String> queryFileExtension;
    private List<String> queryFilePath;


    /// OUTER List elements joined with AND, INNER List elements joined with OR
    private boolean needsMetadata = false;
    private List<List<Character>> querySizeSigns;
    private List<List<Long>> querySize;

    private List<List<Character>> queryLastModifiedSigns;
    private List<List<FileTime>> queryLastModified;

    private List<List<Character>> queryCreatedSigns;
    private List<List<FileTime>> queryCreated;


    private boolean needsContent = false;
    private List<List<String>> queryContent;

    public boolean addName(List<String> queryFileName) {
        if(this.queryFileName != null){
            return false;
        }
        this.queryFileName = queryFileName;
        return true;
    }

    public boolean addExtension(List<String> queryFileExtension) {
        if(this.queryFileExtension != null){
            return false;
        }
        this.queryFileExtension = queryFileExtension;
        return true;
    }

    public boolean addPath(List<String> queryFilePath) {
        if(this.queryFilePath != null){
            return false;
        }
        this.queryFilePath = queryFilePath;
        return true;
    }

    public boolean addSize(List<String> querySize) {
        List<Character> signList = new ArrayList<>();
        List<Long> sizeList = new ArrayList<>();
        for(String sizeString: querySize){
            char sign = sizeString.charAt(0);
            String size = sizeString.substring(1);
            if(sign == '<'){
                signList.add('<');
            }
            else if(sign == '>'){
                signList.add('>');
            }
            else{
                return false;
            }
            try {
                sizeList.add(Long.parseLong(size));
            }
            catch (NumberFormatException e){
                return false;
            }
        }
        if(this.querySizeSigns == null){
            this.querySizeSigns = new ArrayList<>();
            this.querySize = new ArrayList<>();
            this.needsMetadata = true;
        }
        this.querySizeSigns.add(signList);
        this.querySize.add(sizeList);
        return true;
    }

    public boolean addModified(List<String> queryLastModified) {
        List <Character> signList = new ArrayList<>();
        List<FileTime> lastModifiedList = new ArrayList<>();
        for(String lastModifiedString: queryLastModified){
            char sign = lastModifiedString.charAt(0);
            String lastModified = lastModifiedString.substring(1);
            if(sign == '<'){
                signList.add('<');
            }
            else if(sign == '>'){
                signList.add('>');
            }
            else{
                return false;
            }
            try{
                lastModifiedList.add(FileTime.from(Instant.parse(lastModified)));
            }
            catch (DateTimeParseException e){
                return false;
            }
        }
        if(this.queryLastModifiedSigns == null){
            this.queryLastModifiedSigns = new ArrayList<>();
            this.queryLastModified = new ArrayList<>();
            this.needsMetadata = true;
        }
        this.queryLastModifiedSigns.add(signList);
        this.queryLastModified.add(lastModifiedList);
        return true;
    }

    public boolean addCreated(List<String> queryCreated) {
        List <Character> signList = new ArrayList<>();
        List<FileTime> createdList = new ArrayList<>();
        for(String createdString: queryCreated){
            char sign = createdString.charAt(0);
            String created = createdString.substring(1);
            if(sign == '<'){
                signList.add('<');
            }
            else if(sign == '>'){
                signList.add('>');
            }
            else{
                return false;
            }
            try{
                createdList.add(FileTime.from(Instant.parse(created)));
            }
            catch (DateTimeParseException e){
                return false;
            }
        }
        if(this.queryCreatedSigns == null){
            this.queryCreatedSigns = new ArrayList<>();
            this.queryCreated = new ArrayList<>();
            this.needsMetadata = true;
        }
        this.queryCreatedSigns.add(signList);
        this.queryCreated.add(createdList);
        return true;
    }

    public boolean addContent(List<String> queryContent) {
        if(this.queryContent == null){
            this.queryContent = new ArrayList<>();
            this.needsContent = true;
        }
        this.queryContent.add(queryContent);
        return true;
    }
}
