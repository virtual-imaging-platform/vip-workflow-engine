package fr.insalyon.creatis.moteurlite.runner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insalyon.creatis.moteurlite.MoteurLiteException;

public class InputsFileService {

    private static final Logger logger = Logger.getLogger(InputsFileService.class);

    public Map<String, List<String>> parse(String filePath) throws MoteurLiteException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(new File(filePath), new TypeReference<Map<String, List<String>>>() {});

        } catch (IOException e) {
            logger.error("Cannot transform JSON String to JSON Map!");
            throw new MoteurLiteException(e);
        }
    }
}
