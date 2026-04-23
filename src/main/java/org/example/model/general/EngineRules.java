package org.example.model.general;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * General Engine Rules which can be modified by the controller and are used inside the file search service
 * Include paths to be ignored, file extensions to be ignored, default root folders to be indexed and last scan id
 * On construction, rules taken from engine_rules.json and on destruction rules saved to same file. If file not accessible rules are initialized with default values.
 */
@Component
public class EngineRules {
    private static final Logger logger = LoggerFactory.getLogger(EngineRules.class);
    private static final Integer MAX_DIM = 1024 * 1024;
    private static final File SAVE_FILE = new File("engine_rules.json");
    private final ObjectMapper mapper;

    @Getter
    private final List<String> ignorePaths = new ArrayList<>();
    @Getter
    private final List<String> ignoreExtensions = new ArrayList<>();
    @Getter
    private final List<String> rootDirs = new ArrayList<>();

    private static final List<String> INITIAL_IGNORE_PATHS = List.of(
        "C:/Windows",
        "C:/eSupport",
        "C:/Program Files",
        "C:/Program Files (x86)",
        ".git",
        "node_modules",
        ".cache",
        ".svn",
        "__pycache__"
    );
    private static final List<String> INITIAL_IGNORE_EXTENSIONS = List.of(
            "tmp", "swp", "bak", "class", "o", "obj", "exe", "dll"
    );
    private static final List<String> INITIAL_ROOT_DIRS = List.of(
            "C:/polibooks/an3/sem2/software_engineering/project/search_engine_core/src/main/java"
    );

    public EngineRules(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    public EngineRules() {
        this.mapper = new ObjectMapper();
    }
    @PostConstruct
    public void loadState() {
        if (SAVE_FILE.exists()) {
            try {
                EngineRules loaded = mapper.readValue(SAVE_FILE, EngineRules.class);
                this.ignorePaths.clear();
                this.ignorePaths.addAll(loaded.ignorePaths);

                this.ignoreExtensions.clear();
                this.ignoreExtensions.addAll(loaded.ignoreExtensions);

                this.rootDirs.clear();
                this.rootDirs.addAll(loaded.rootDirs);

                logger.info("Loaded engine rules from {}", SAVE_FILE);
            } catch (IOException e) {
                resetAll();
                logger.info("Fallback to default engine rules");
            }
        } else {
            resetAll();
            logger.info("Fallback to default engine rules");
        }
    }

    @PreDestroy
    public void saveState() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(SAVE_FILE, this);
            logger.info("EngineRules saved to {}", SAVE_FILE.getAbsolutePath());
        } catch (IOException e) {
            logger.warn("Could not save engine rules", e);
        }
    }

    public boolean continueIndexDirectory(Path path){
        String normalizedPath = path.toString().replace("\\", "/");
        for(String ignorePath : ignorePaths) {
            if(normalizedPath.contains(ignorePath)) {
                return false;
            }
        }
        return true;
    }
    public boolean continueIndexFile(Path path, BasicFileAttributes attrs){
        String normalizedPath = path.toString().replace("\\", "/");

        if(normalizedPath.charAt(normalizedPath.lastIndexOf('/') + 1) == '.'){
            return false;
        }
        for(String ignoreExtension : ignoreExtensions) {
            if(normalizedPath.endsWith(ignoreExtension)){
                return false;
            }
        }
        if(attrs.size() > MAX_DIM){
            return false;
        }
        return true;
    }

    public void addIgnoreExtensions(String extension){
        ignoreExtensions.add(extension);
    }
    public void addIgnorePaths(String path){
        ignorePaths.add(path);
    }
    public void addRootDirs(String dir){
        rootDirs.add(dir);
    }
    public void deleteIgnorePaths(String path){
        ignorePaths.remove(path);
    }
    public void deleteIgnoreExtensions(String extension){
        ignoreExtensions.remove(extension);
    }
    public void deleteRootDir(String path){
        rootDirs.remove(path);
    }
    public void resetIgnorePaths(){
        ignorePaths.clear();
        ignorePaths.addAll(INITIAL_IGNORE_PATHS);
    }
    public void resetIgnoreExtensions(){
        ignoreExtensions.clear();
        ignoreExtensions.addAll(INITIAL_IGNORE_EXTENSIONS);
    }
    public void resetRootDirs(){
        rootDirs.clear();
        rootDirs.addAll(INITIAL_ROOT_DIRS);
    }
    private void resetAll(){
        resetRootDirs();
        resetIgnorePaths();
        resetIgnoreExtensions();
    }

    public void deleteAllRootDirs(){
        rootDirs.clear();
    }
}
