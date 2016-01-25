package net.unicon.scratches.o365javasdk;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.services.graph.Group;
import com.microsoft.services.graph.fetchers.GraphServiceClient;
import com.microsoft.services.graph.fetchers.GroupCollectionOperations;
import com.microsoft.services.graph.fetchers.GroupFetcher;
import com.microsoft.services.orc.core.OrcCollectionFetcher;
import com.microsoft.services.orc.resolvers.JavaDependencyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.naming.ServiceUnavailableException;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
public class ScratchApplication implements CommandLineRunner {

    @Autowired
    private AzureProperties azureProperties;

    private static final Logger logger = LoggerFactory.getLogger(ScratchApplication.class);

    private static final String PROTECTED_RESOURCE = "https://graph.microsoft.com";

    private static final String GRAPH_ENDPOINT = "https://graph.microsoft.com/beta";


    public static void main(String[] args) {
        SpringApplication.run(ScratchApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String authorityEndpoint = String.format("https://login.windows.net/%s/oauth2/token", this.azureProperties.getTenant());
        AuthenticationResult authResult = getAccessTokenFromClientCredentials(authorityEndpoint);
        logAuthnResult(authResult);
        tryAndLogGroupApiAccess(authResult);
    }

    private AuthenticationResult getAccessTokenFromClientCredentials(String authorityEndpoint) throws Exception {
        AuthenticationResult result = null;
        ExecutorService svc = Executors.newFixedThreadPool(1);
        try {
            AuthenticationContext context = new AuthenticationContext(authorityEndpoint, true, svc);
            Future<AuthenticationResult> future =
                    context.acquireToken(PROTECTED_RESOURCE,
                            new ClientCredential(this.azureProperties.getClientId(), this.azureProperties.getClientKey()), null);
            result = future.get();
        }
        catch (ExecutionException e) {
            logger.error("ERROR!", e);
        }
        finally {
            svc.shutdownNow();
        }
        if (result == null) {
            throw new ServiceUnavailableException("authentication result was null");
        }
        return result;
    }

    private static void logAuthnResult(AuthenticationResult authenticationResult) {
        logger.info("<------------------------------ Authentication Result ------------------------------>");
        logger.info("ACCESS TOKEN: {}", authenticationResult.getAccessToken());
        logger.info("ACCESS TOKEN TYPE: {}", authenticationResult.getAccessTokenType());
        logger.info("ACCESS TOKEN EXPIRES ON: {}", authenticationResult.getExpiresOnDate());
        logger.info("<------------------------------ Authentication Result ------------------------------>");
    }

    private static void tryAndLogGroupApiAccess(AuthenticationResult authenticationResult) throws Exception {
        JavaDependencyResolver resolver = new JavaDependencyResolver(authenticationResult.getAccessToken());
        resolver.getLogger().setEnabled(true);
        GraphServiceClient client = new GraphServiceClient(GRAPH_ENDPOINT, resolver);
        readAllGroups(client.getGroups());
        //addOneGroup(client.getGroups());

    }

    private static void readAllGroups(OrcCollectionFetcher<Group, GroupFetcher, GroupCollectionOperations> groupsFetcher)
            throws InterruptedException, ExecutionException {

        //Read all groups
        logger.info("{} Groups exist", groupsFetcher.read().get().size());
        groupsFetcher.read().get().forEach(g -> {
            logger.info("\n");
            logger.info("<------------------------------ Group ------------------------------>");
            logger.info("GROUP ID: {}", g.getObjectId());
            logger.info("GROUP TYPE: {}", g.getGroupType());
            logger.info("GROUP DESC: {}", g.getDescription());
            logger.info("GROUP DISPLAY NAME: {}", g.getDisplayName());
            logger.info("GROUP EMAIL: {}", g.getEmailAddress());
            logger.info("GROUP MAIL NICKNAME: {}", g.getMailNickname());
            logger.info("<------------------------------ Group ------------------------------>");
            logger.info("\n");
        });
    }

    private static void addOneGroup(OrcCollectionFetcher<Group, GroupFetcher, GroupCollectionOperations> groupsFetcher)
            throws Exception {
        Group g = new Group();
        g.setDescription("Group created via REST API");
        //Required
        g.setDisplayName("Test Group");
        //Required
        g.setMailNickname("test_nick");
        //Required
        g.setMailEnabled(false);
        //Required
        g.setSecurityEnabled(true);
        logger.info("Creating test group...");
        groupsFetcher.add(g);
    }


}
