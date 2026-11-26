package fr.insalyon.creatis.moteurlite;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.jupiter.api.Test;

public class ParseConfigurationTest {

    @Test
    public void parseConfigFile() throws Exception {
        URL url = getClass().getClassLoader().getResource("settings.conf");
        File f = new File(url.toURI());
        PropertiesConfiguration conf = MoteurLiteConfiguration.parseConfig(f);
        assertAll(
                () -> assertEquals(conf.getString("moteurlite.grida.serverconf"), "/vip/server/conf"),
                () -> assertEquals(conf.getString("moteurlite.grida.proxy"), "/vip/grida/proxy"),
                () -> assertEquals(conf.getInt("moteurlite.maxjobsperworkflow"), 1000),
                () -> assertEquals(conf.getInt("moteurlite.maxcontentdispositionlength"), 1024),
                () -> assertEquals(
                        new ArrayList<>(Arrays.asList(conf.getStringArray("moteurlite.httpurl.domain.whitelist"))),
                        new ArrayList<>(List.of("test.org", "data.67dripped", "f.ai.fr"))),
                () -> assertArrayEquals(
                        conf.getStringArray("test.list.wrong.delim"),
                        new String[] { "spaces apart", "yet", "i hear your comma", "in my  memory" }));

    }
}
