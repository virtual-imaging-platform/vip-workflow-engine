package fr.insalyon.creatis.moteurlite.JacksonParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class BoutiquesEntities {
        private String author;
        private String commandLine;
        private ToolContainerImage containerImage;
        //private ToolCustom custom;
        private Map<String, Object> custom;
        private ToolDeprecatedByDoi deprecatedByDoi;
        private String description;
        private String descriptorUrl;
        private String doi;
        private List<Map<String, Object>> environmentVariables;
        private List<Map<String, Object>> errorCodes;
        private List<Map<String, Object>> groups;
        private List<Map<String, Object>> inputs;
        private ToolInvocationSchema invocationSchema;
        private String name;
        private List<String> onlinePlatformUrls;
        private List<Map<String, Object>> outputFiles;
        private String schemaVersion;
        private String shell;
        private ToolSuggestedResources suggestedResources;
        private ToolTags tags;
        private List<Map<String, Object>> tests;
        private String toolDoi;
        private String toolVersion;
        private String url;
        Object inputId;
        Object outputId;
        Object inputType = new HashMap<>();
        Object inputValueKey = new HashMap<>();
        Object outputPathTemplate = new HashMap<>();

        HashMap<Integer, String> inputIdList = new HashMap<Integer, String>();
        HashMap<Integer, String> outputIdList = new HashMap<Integer, String>();
        HashMap<String, String> inputTypelist = new HashMap<String, String>();
        HashMap<String, String> inputValueKeylist = new HashMap<String, String>();
        HashMap<String, String> outputPathTemplateList = new HashMap<String, String>();
        BoutiquesObjectParser boutiquesObjectParser;
        Map<String, String> crossMap = new HashMap<>();
        Map<String, String> dotMap = new HashMap<>();
        private Set<String> crossSet = new HashSet<>();
        private Set<String> dotSet = new HashSet<>();
    
        @JsonProperty("author")
        public String getAuthor() {
            return author;
        }
    
        @JsonProperty("author")
        public void setAuthor(String author) {
            //System.out.println("author:" + author);
            this.author = author;
        }
    
        @JsonProperty("command-line")
        public String getCommandLine() {
            return commandLine;
        }
    
        @JsonProperty("command-line")
        public void setCommandLine(String commandLine) {
            //System.out.println("command-line:"+commandLine);
            this.commandLine = commandLine;
        }
    
        @JsonProperty("container-image")
        public ToolContainerImage getContainerImage() {
            return containerImage;
        }
    
        @JsonProperty("container-image")
        public void setContainerImage(ToolContainerImage containerImage) {
            //System.out.println("container-image:"+containerImage);
            this.containerImage = containerImage;
        }
    
        @JsonProperty("custom")
        public void setCustom(Map<String, Object> custom) {
            this.custom = custom;
    
            if (custom == null) {
                return; // Return early if custom is null
            }
    
            for (Map.Entry<String, Object> entry : custom.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
    
                if (value instanceof String) {
                    handleKeyValue(key, (String) value);
                } else if (value instanceof Iterable) {
                    for (Object item : (Iterable<?>) value) {
                        handleKeyValue(key, item.toString());
                    }
                }
            }
        }
    
        private void handleKeyValue(String key, String value) {
            if (key.contains("VIPcross")) {
                crossSet.add(value);
            } else if (key.contains("VIPdot")) {
                dotSet.add(value);
            }
        }
    
        public Set<String> getCrossMap() {
            return crossSet;
        }
    
        public Set<String> getDotMap() {
            return dotSet;
        }

    
        @JsonProperty("deprecated-by-doi")
        public ToolDeprecatedByDoi getDeprecatedByDoi() {
            return deprecatedByDoi;
        }
    
        @JsonProperty("deprecated-by-doi")
        public void setDeprecatedByDoi(ToolDeprecatedByDoi deprecatedByDoi) {
            //System.out.println("deprecated-by-doi:"+deprecatedByDoi);
            this.deprecatedByDoi = deprecatedByDoi;
        }
    
        @JsonProperty("description")
        public String getDescription() {
            return description;
        }
    
        @JsonProperty("description")
        public void setDescription(String description) {
            //System.out.println("description:"+description);
            this.description = description;
        }
    
        @JsonProperty("descriptor-url")
        public String getDescriptorUrl() {
            return descriptorUrl;
        }
    
        @JsonProperty("descriptor-url")
        public void setDescriptorUrl(String descriptorUrl) {
            //System.out.println("descriptor-url:"+descriptorUrl);
            this.descriptorUrl = descriptorUrl;
        }
    
        @JsonProperty("doi")
        public String getDoi() {
            return doi;
        }
    
        @JsonProperty("doi")
        public void setDoi(String doi) {
            //System.out.println("doi:"+doi);
            this.doi = doi;
        }
    
        @JsonProperty("environment-variables")
        public List<Map<String, Object>> getEnvironmentVariables() {
            return environmentVariables;
        }
    
        @JsonSetter("environment-variables")
        public void setEnvironmentVariables(List<Map<String, Object>> environmentVariables) {
            //System.out.println("environment-variables:"+environmentVariables);
            this.environmentVariables = environmentVariables;
        }
    
        @JsonProperty("error-codes")
        public List<Map<String, Object>> getErrorCodes() {
            return errorCodes;
        }
    
        @JsonSetter("error-codes")
        public void setErrorCodes(List<Map<String, Object>> errorCodes) {
            //System.out.println("error-codes:"+errorCodes);
            this.errorCodes = errorCodes;
        }
    
        @JsonProperty("groups")
        public List<Map<String, Object>> getGroups() {
            return groups;
        }
    
        @JsonSetter("groups")
        public void setGroups(List<Map<String, Object>> groups) {
            //System.out.println("groups:"+groups);
            this.groups = groups;
        }
    
        @JsonProperty("inputs")
        public List<Map<String, Object>> getInputs() {
            return inputs;
        }
    
        @JsonSetter("inputs")
        public void setInputs(List<Map<String, Object>> inputs) {
            //System.out.println("inputs:"+inputs);

            for(int inputNo =0; inputNo < inputs.size();inputNo++){
                //System.out.println("inputs:"+ inputNo+ inputs.get(inputNo));
                boutiquesObjectParser = new BoutiquesObjectParser(inputs, BoutiquesInput.class);
                inputId = inputs.get(inputNo).get("id");
                inputType = inputs.get(inputNo).get("type");
                inputValueKey = inputs.get(inputNo).get("value-key");
                inputIdList.put(inputNo,inputId.toString());
                inputTypelist.put(inputId.toString(),inputType.toString());
                inputValueKeylist.put(inputId.toString(),inputValueKey.toString());
            }
            this.inputs = inputs;
        }

        public HashMap<Integer, String> getInputId(){
            return inputIdList;
        }

        public HashMap<String, String> getInputTypes(){
            return inputTypelist;
        }

        public HashMap<String, String> getInputValueKey(){
            return inputValueKeylist;
        }
    
        @JsonProperty("invocation-schema")
        public ToolInvocationSchema getInvocationSchema() {
            return invocationSchema;
        }
    
        @JsonProperty("invocation-schema")
        public void setInvocationSchema(ToolInvocationSchema invocationSchema) {
            //System.out.println("invocation-schema:"+invocationSchema);
            this.invocationSchema = invocationSchema;
        }
    
        @JsonProperty("name")
        public String getName() {
            return name;
        }
    
        @JsonProperty("name")
        public void setName(String name) {
            //System.out.println("name:"+name);
            this.name = name;
        }
    
        @JsonProperty("online-platform-urls")
        public List<String> getOnlinePlatformUrls() {
            return onlinePlatformUrls;
        }
    
        @JsonSetter("online-platform-urls")
        public void setOnlinePlatformUrls(List<String> onlinePlatformUrls) {
            //System.out.println("online-platform-urls:"+onlinePlatformUrls);
            this.onlinePlatformUrls = onlinePlatformUrls;
        }
    
        @JsonProperty("output-files")
        public List<Map<String, Object>> getOutputFiles() {
            return outputFiles;
        }
    
        @JsonSetter("output-files")
        public void setOutputFiles(List<Map<String, Object>> outputFiles) {
            //System.out.println("output-files:"+outputFiles);

            for(int outputNo =0; outputNo < outputFiles.size();outputNo++){
                //System.out.println("outputs:"+ outputNo+ outputFiles.get(outputNo));
                boutiquesObjectParser = new BoutiquesObjectParser(outputFiles, BoutiquesOutput.class);
                outputId = outputFiles.get(outputNo).get("id");
                outputIdList.put(outputNo,outputId.toString());
                outputPathTemplate = outputFiles.get(outputNo).get("path-template");
                outputPathTemplateList.put(outputId.toString(),outputPathTemplate.toString());
            }

            
            this.outputFiles = outputFiles;
        }

        public HashMap<Integer, String> getOutputId(){
            return outputIdList;
        }

        public HashMap<String, String> getOutputPathTemplateList(){
            return outputPathTemplateList;
        }
    
        @JsonProperty("schema-version")
        public String getSchemaVersion() {
            return schemaVersion;
        }
    
        @JsonProperty("schema-version")
        public void setSchemaVersion(String schemaVersion) {
            //System.out.println("schema-version:"+schemaVersion);
            this.schemaVersion = schemaVersion;
        }
    
        @JsonProperty("shell")
        public String getShell() {
            return shell;
        }
    
        @JsonProperty("shell")
        public void setShell(String shell) {
            //System.out.println("shell:"+shell);
            this.shell = shell;
        }
    
        @JsonProperty("suggested-resources")
        public ToolSuggestedResources getSuggestedResources() {
            return suggestedResources;
        }
    
        @JsonProperty("suggested-resources")
        public void setSuggestedResources(ToolSuggestedResources suggestedResources) {
            //System.out.println("suggested-resources:"+suggestedResources);
            this.suggestedResources = suggestedResources;
        }
    
        @JsonProperty("tags")
        public ToolTags getTags() {
            return tags;
        }
    
        @JsonProperty("tags")
        public void setTags(ToolTags tags) {
            //System.out.println("tags:"+tags);
            this.tags = tags;
        }
    
        @JsonProperty("tests")
        public List<Map<String, Object>> getTests() {
            return tests;
        }
    
        @JsonSetter("tests")
        public void setTests(List<Map<String, Object>> tests) {
            this.tests = tests;
            //System.out.println("tests:"+tests);
        }
    
        @JsonProperty("tool-doi")
        public String getToolDoi() {
            return toolDoi;
        }
    
        @JsonProperty("tool-doi")
        public void setToolDoi(String toolDoi) {
            //System.out.println("tool-doi:"+toolDoi);
            this.toolDoi = toolDoi;
        }
    
        @JsonProperty("tool-version")
        public String getToolVersion() {
            return toolVersion;
        }
    
        @JsonProperty("tool-version")
        public void setToolVersion(String toolVersion) {
            //System.out.println("tool-version:"+toolVersion);
            this.toolVersion = toolVersion;
        }
    
        @JsonProperty("url")
        public String getUrl() {
            return url;
        }
    
        @JsonProperty("url")
        public void setUrl(String url) {
            //System.out.println("url:"+url);
            this.url = url;
        }
    
        // Define getter and setter methods for other fields as needed
    }
    
    class ToolContainerImage {
        // Define fields for container-image, if needed
    }
    
    class ToolCustom {
        // Define fields for custom, if needed
    }
    
    class ToolDeprecatedByDoi {
        // Define fields for deprecated-by-doi, if needed
    }
    
    class ToolInvocationSchema {
        // Define fields for invocation-schema, if needed
    }
    
    class ToolSuggestedResources {
        // Define fields for suggested-resources, if needed
    }
    
    class ToolTags {
        // Define fields for tags, if needed
    }
    
    // Define other nested classes for complex types as needed
