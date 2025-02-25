package fr.insalyon.creatis.moteurlite.boutiques.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;

@Generated("jsonschema2pojo")
public class CustomListDirItem {
    @JsonProperty("patterns")
    private List<String> patterns;

    @JsonProperty("patterns")
    public List<String> getPatterns() { return patterns; }
}
