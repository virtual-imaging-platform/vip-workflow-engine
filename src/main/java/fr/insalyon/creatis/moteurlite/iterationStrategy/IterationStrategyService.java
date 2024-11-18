package fr.insalyon.creatis.moteurlite.iterationStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.insalyon.creatis.moteurlite.boutiques.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.boutiques.BoutiquesService;

/**
 * Author: Sandesh Patil [https://github.com/sandepat]
 */

public class IterationStrategyService {
    
    private final BoutiquesService boutiquesService;

    public IterationStrategyService(BoutiquesService boutiquesService) {
        this.boutiquesService = boutiquesService;
    }

    public List<Map<String, String>> compute(BoutiquesDescriptor descriptor, Map<String, List<String>> inputValues) {
        IterationStrategy iterationStrategy = new IterationStrategy();
        
        // handle custom properties if they do not exist
        Map<String, Object> customProperties = descriptor.getCustom() != null 
                ? descriptor.getCustom().getAdditionalProperties() 
                : Collections.emptyMap();

        // Get optional inputs (if applicable)
        Set<String> inputOptional = boutiquesService.getInputOptionalOfBoutiquesFile(descriptor);

        // Pass inputValues, customProperties, and inputOptional to IterationStrategy
        return iterationStrategy.IterationStratergy(inputValues, customProperties, inputOptional);
    }
}
