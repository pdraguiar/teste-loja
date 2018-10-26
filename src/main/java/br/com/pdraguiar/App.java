package br.com.pdraguiar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.content.ShoppingContent;
import com.google.api.services.content.ShoppingContentScopes;
import com.google.api.services.content.model.Price;
import com.google.api.services.content.model.Product;
import com.google.api.services.content.model.ProductShipping;
import com.google.api.services.content.model.ProductsListResponse;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Set;

public class App {
    private static final String CHANNEL = "online";
    private static final String CONTENT_LANGUAGE = "pt";
    private static final String TARGET_COUNTRY = "BR";
    private static final String OFFER_ID = "book123";
    private static final BigInteger MERC_ID = new BigInteger("126923644");
    private static final String USER_HOME = System.getProperty("user.home");
    private HttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private Set<String> scopes;

    public App() throws IOException {
        this.httpTransport = createHttpTransport();
        this.jsonFactory = JacksonFactory.getDefaultInstance();
        this.scopes = ShoppingContentScopes.all();
    }

    public HttpTransport createHttpTransport() throws IOException {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public Credential authenticate() throws IOException {
        File serviceAccountFile = new File(USER_HOME, "content-api-key.json");
        GoogleCredential credential = null;
        try (InputStream inputStream = new FileInputStream(serviceAccountFile)) {
            if (serviceAccountFile.exists()) {
                System.out.println("Loading service account credentials.");

                credential =
                        GoogleCredential.fromStream(inputStream, httpTransport, jsonFactory)
                                .createScoped(scopes);
                System.out.printf(
                        "Loaded service account credentials for %s%n", credential.getServiceAccountId());
                // GoogleCredential.fromStream does NOT refresh, since scopes must be added after.
                if (!credential.refreshToken()) {
                    System.out.println("The service account access token could not be refreshed.");
                    System.out.println("The service account key may have been deleted in the API Console.");
                    throw new IOException("Failed to refresh service account token");
                }
            }
            return credential;
        } catch (IOException e) {
            throw new IOException(
                    "Could not retrieve service account credentials from the file "
                            + serviceAccountFile.getCanonicalPath());
        }
    }

    public Product generateProduct() {
        return new Product()
                .setOfferId(OFFER_ID)
                .setTitle("A Tale of Two Cities")
                .setDescription("A classic novel about the French Revolution")
                .setLink("http://www.dsadasdsa.com/tale-of-two-cities.html")
                .setImageLink("http://www.dsadasdsa.com/tale-of-two-cities.jpg")
                .setChannel(CHANNEL)
                .setContentLanguage(CONTENT_LANGUAGE)
                .setTargetCountry(TARGET_COUNTRY)
                .setAvailability("in stock")
                .setCondition("new")
                .setGoogleProductCategory("Media > Books")
                .setGtin("9780007350896")
                .setPrice(new Price().setValue("2.50").setCurrency("GBP"))
                .setShipping(
                        ImmutableList.of(
                                new ProductShipping()
                                        .setPrice(new Price().setValue("0.99").setCurrency("GBP"))
                                        .setCountry("GB")
                                        .setService("1st class post")));
    }

    public static void main(String[] args) throws IOException {
        App app = new App();
        Credential authenticate = app.authenticate();

        ShoppingContent content = new ShoppingContent.Builder(app.httpTransport, app.jsonFactory, authenticate)
                .setApplicationName("Content API for Shopping Samples")
                .build();

        /*Product execute = content.products().insert(MERC_ID, app.generateProduct()).execute();
        System.out.println(execute.toString());*/
        ProductsListResponse execute = content.products().list(MERC_ID).execute();
        execute.getResources().forEach(System.out::println);

    }
}
