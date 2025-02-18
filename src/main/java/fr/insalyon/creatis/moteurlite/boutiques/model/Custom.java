
package fr.insalyon.creatis.moteurlite.boutiques.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
@Generated("jsonschema2pojo")
public class Custom {
    public class VipListDir {
        public class VipListDirInput {
            @JsonProperty("name")
            public String name;
            @JsonProperty("patterns")
            public List<String> patterns;
        }
        @JsonProperty("inputs")
        public List<VipListDirInput> inputs;
    };
    @JsonProperty("vip:listDir")
    public VipListDir vipListDir;

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
