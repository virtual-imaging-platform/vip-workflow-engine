
package fr.insalyon.creatis.moteurlite.boutiques.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
@Generated("jsonschema2pojo")
public class Custom {
    @JsonProperty("vip:directoryInputs")
    private Map<String, CustomDirectoryInputsItem> vipDirectoryInputs;

    public Map<String, CustomDirectoryInputsItem> getDirectoryInputs() {
        return vipDirectoryInputs;
    }

    @JsonProperty("vip:intIteratorInputs")
    private List<String> vipIntIteratorInputs;

    public List<String> getIntIteratorInputs() { return vipIntIteratorInputs; }

    @JsonProperty("vip:resultsDirectorySuffix")
    private String vipResultsDirectorySuffix;

    public String getResultsDirectorySuffix() { return vipResultsDirectorySuffix; };

    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
