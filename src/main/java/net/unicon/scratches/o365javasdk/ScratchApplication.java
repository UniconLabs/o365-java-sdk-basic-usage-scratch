package net.unicon.scratches.o365javasdk;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.naming.ServiceUnavailableException;
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


	public static void main(String[] args) {
		SpringApplication.run(ScratchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
        String authorityEndpoint = String.format("https://login.windows.net/%s/oauth2/token", this.azureProperties.getTenant());
        AuthenticationResult authResult = getAccessTokenFromClientCredentials(authorityEndpoint);
        logger.info("<------------------------------ Authentication Result ------------------------------>");
        logger.info("ACCESS TOKEN: {}", authResult.getAccessToken());
        logger.info("ACCESS TOKEN TYPE: {}", authResult.getAccessTokenType());
        logger.info("ACCESS TOKEN EXPIRES ON: {}", authResult.getExpiresOnDate());
        logger.info("<------------------------------ Authentication Result ------------------------------>");
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
}
