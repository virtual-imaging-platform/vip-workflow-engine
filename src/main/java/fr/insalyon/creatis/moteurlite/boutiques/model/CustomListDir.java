package fr.insalyon.creatis.moteurlite.boutiques.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;
import java.util.Map;

@Generated("jsonschema2pojo")
public class CustomListDir {
    @JsonProperty("inputs")
    private Map<String, CustomListDirItem> inputs;

    @JsonProperty("inputs")
    public Map<String, CustomListDirItem> getInputs() {
        return inputs;
    }
}
