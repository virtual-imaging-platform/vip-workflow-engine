package fr.insalyon.creatis.moteurlite.iterationStrategy;

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
        
        // Use custom properties from the descriptor to compute iteration strategy
        Map<String, Object> customProperties = descriptor.getCustom().getAdditionalProperties();

        // Get optional inputs (if applicable)
        Set<String> inputOptional = boutiquesService.getInputOptionalOfBoutiquesFile(descriptor);

        // Pass inputValues, customProperties, and inputOptional to IterationStratergy
        return iterationStrategy.IterationStratergy(inputValues, customProperties, inputOptional);
    }
}
