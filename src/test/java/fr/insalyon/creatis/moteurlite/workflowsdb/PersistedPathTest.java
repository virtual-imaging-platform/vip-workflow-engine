package fr.insalyon.creatis.moteurlite.workflowsdb;

import fr.insalyon.creatis.moteur.plugins.workflowsdb.WorkflowsDBException;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.DataType;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.InputDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.OutputDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOException;
import fr.insalyon.creatis.moteurlite.MoteurLiteException;
import fr.insalyon.creatis.boutiques.model.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PersistedPathTest {

    WorkflowsDBRepository workflowsDBRepository;

    @Mock
    InputDAO inputDAO;
    @Mock
    OutputDAO outputDAO;

    @Captor
    ArgumentCaptor<fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Input> inputCaptor;
    @Captor
    ArgumentCaptor<fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Output> outputCaptor;

    static String workflowId = "testWorkflowId";
    static Input stringInput;
    static Input fileInput;
    static Map<String, Input> boutiquesInputs;

    @BeforeAll
    static void beforeAll() {
        stringInput = new Input();
        stringInput.setType(Input.Type.STRING);
        stringInput.setId("stringInput");
        fileInput = new Input();
        fileInput.setType(Input.Type.FILE);
        fileInput.setId("fileInput");
        boutiquesInputs = Map.of(
                stringInput.getId(), stringInput,
                fileInput.getId(), fileInput
        );
    }

    @BeforeEach
    void beforeEach() throws WorkflowsDBDAOException, WorkflowsDBException {
        MockitoAnnotations.openMocks(this);
        workflowsDBRepository = new WorkflowsDBRepository(inputDAO, outputDAO, null, null);
    }

    @Test
    public void testLFN() throws MoteurLiteException, WorkflowsDBDAOException, URISyntaxException {
        String inputValue = "lfn:/path/to/file.txt";
        String outputValue = "lnf:/path/to/res-dir";
        verifyPersistStringInput(inputValue);
        verifyPersistFileInput(inputValue, inputValue);
        verifyPersistOutput(outputValue, outputValue);
    }


    @Test
    public void testFile() throws MoteurLiteException, WorkflowsDBDAOException, URISyntaxException {
        String inputValue = "file:/path/to/file.txt";
        String outputValue = "file:/path/to/res-dir";
        verifyPersistStringInput(inputValue);
        verifyPersistFileInput(inputValue, inputValue);
        verifyPersistOutput(outputValue, outputValue);
    }

    @Test
    public void testGirderPaths() throws MoteurLiteException, WorkflowsDBDAOException, URISyntaxException {
        String inputValue = "girder:/fit3T_3_1Mac_DKNTMN_10.control?apiurl=https://girder.example.com/warehouse/api/v1&fileId=67a9dbeeuieiueiu114e0b189e3412e10&token=srcrdgOJCoUwYr4G0MDLdqliPwTedTvyDjMZxXhFmnFZFQPAFPQCGRJJNMt2jw8i";
        String expectedInputValue = "girder:/fit3T_3_1Mac_DKNTMN_10.control?apiurl=https://girder.example.com/warehouse/api/v1&fileId=67a9dbeeuieiueiu114e0b189e3412e10";
        String outputValue = "girder:///?apiurl=https://girder.example.com/warehouse/api/v1&fileId=67acce0814eiueuieueiei1983c&token=9gWt7HOVcxCVxzsCiwM6eiueuieiuttsOCJPtsvdApbyZsC1o0";
        String expectedOutputValue = "girder:///?apiurl=https://girder.example.com/warehouse/api/v1&fileId=67acce0814eiueuieueiei1983c";
        verifyPersistStringInput(inputValue);
        verifyPersistFileInput(inputValue, expectedInputValue);
        verifyPersistOutput(outputValue, expectedOutputValue);
    }

    @Test
    public void testShanoirPaths() throws MoteurLiteException, WorkflowsDBDAOException, URISyntaxException {
        String inputValue = "shanoir:/resource_id+41fc2a43-a2ad-4ea8-bad7-944174096800+dataset.zip?apiUrl=https://shanoir.example.com/shanoir-ng/datasets/carmin-data/path&resourceId=41fc2a43-a2ad-4ea8-bad7-944174096800&format=dcm&converterId=8&keycloak_client_id=shanoir-ng-front&refresh_token_url=https://shanoir.example.com/auth/realms/shanoir-ng/protocol/openid-connect/token&token=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUI&refreshToken=eyJhbGciOiJIUzUxMiIsInR5cCIgOiAiSldUIiwia2l";
        String expectedInputValue = "shanoir:/resource_id+41fc2a43-a2ad-4ea8-bad7-944174096800+dataset.zip?apiUrl=https://shanoir.example.com/shanoir-ng/datasets/carmin-data/path&resourceId=41fc2a43-a2ad-4ea8-bad7-944174096800&format=dcm&converterId=8&keycloak_client_id=shanoir-ng-front";
        String outputValue = "shanoir:/311/20250303143618365?upload_url=https://shanoir.example.com/shanoir-ng/import/carmin-data/&type=File&md5=none&keycloak_client_id=shanoir-ng-front&refresh_token_url=https://shanoir.example.com/auth/realms/shanoir-ng/protocol/openid-connect/token&token=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ5Ykk5NHJIX2F4TjJVMjdWc&refreshToken=eyJhbGciOiJIUzUxMiIsIn";
        String expectedOutputValue = "shanoir:/311/20250303143618365?upload_url=https://shanoir.example.com/shanoir-ng/import/carmin-data/&type=File&keycloak_client_id=shanoir-ng-front";
        verifyPersistStringInput(inputValue);
        verifyPersistFileInput(inputValue, expectedInputValue);
        verifyPersistOutput(outputValue, expectedOutputValue);
    }

    private void verifyPersistStringInput(String value) throws MoteurLiteException, WorkflowsDBDAOException {
        Mockito.reset(inputDAO);
        Map<String, List<String>> inputValues = Map.of(stringInput.getId(), Collections.singletonList(value));
        workflowsDBRepository.persistInputs(workflowId, inputValues, boutiquesInputs);
        Mockito.verify(inputDAO).add(inputCaptor.capture());
        fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Input input = inputCaptor.getValue();
        Assertions.assertEquals(DataType.String, input.getType());
        Assertions.assertEquals(stringInput.getId(), input.getInputID().getProcessor());
        Assertions.assertEquals(workflowId, input.getInputID().getWorkflowID());
        Assertions.assertEquals(value, input.getInputID().getPath());
    }

    private void verifyPersistFileInput(String value, String expectedValue) throws MoteurLiteException, WorkflowsDBDAOException {
        Mockito.reset(inputDAO);
        Map<String, List<String>> inputValues = Map.of(fileInput.getId(), Collections.singletonList(value));
        workflowsDBRepository.persistInputs(workflowId, inputValues, boutiquesInputs);
        Mockito.verify(inputDAO).add(inputCaptor.capture());
        fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Input input = inputCaptor.getValue();
        Assertions.assertEquals(DataType.URI, input.getType());
        Assertions.assertEquals(fileInput.getId(), input.getInputID().getProcessor());
        Assertions.assertEquals(workflowId, input.getInputID().getWorkflowID());
        Assertions.assertEquals(expectedValue, input.getInputID().getPath());
    }

    private void verifyPersistOutput(String value, String expectedValue) throws MoteurLiteException, WorkflowsDBDAOException, URISyntaxException {
        Mockito.reset(outputDAO);
        String outputID = "testOutput";
        Map<String, URI> outputMap = Map.of(outputID, new URI(value));
        workflowsDBRepository.persistOutputs(workflowId, outputMap);
        Mockito.verify(outputDAO).add(outputCaptor.capture());
        fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Output output = outputCaptor.getValue();
        Assertions.assertEquals(DataType.URI, output.getType());
        Assertions.assertEquals(outputID, output.getOutputID().getProcessor());
        Assertions.assertEquals(workflowId, output.getOutputID().getWorkflowID());
        Assertions.assertEquals(expectedValue, output.getOutputID().getPath());
    }
}
