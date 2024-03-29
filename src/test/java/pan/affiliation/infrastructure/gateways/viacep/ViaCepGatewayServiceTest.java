package pan.affiliation.infrastructure.gateways.viacep;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pan.affiliation.domain.modules.customers.valueobjects.PostalCode;
import pan.affiliation.infrastructure.gateways.viacep.contracts.PostalCodeInformationResponse;
import pan.affiliation.infrastructure.shared.http.helpers.HttpClientStubBuilder;
import pan.affiliation.infrastructure.shared.http.helpers.HttpGatewayServiceFactory;
import pan.affiliation.infrastructure.shared.http.helpers.ServiceCreator;
import pan.affiliation.shared.caching.CacheProvider;
import pan.affiliation.shared.environment.PropertiesReader;
import pan.affiliation.shared.environment.helpers.PropertiesReaderStubBuilder;

import java.util.Optional;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ViaCepGatewayServiceTest {
    private final CacheProvider cacheProvider;
    private final ObjectMapper mapper = new ObjectMapper()
            .disable(FAIL_ON_UNKNOWN_PROPERTIES);

    public ViaCepGatewayServiceTest() {
        cacheProvider = Mockito.mock(CacheProvider.class);
    }

    @SneakyThrows
    @Test
    public void getPostalCodeInformation_shouldReturnParsedPostalCodeInformation() {
        var baseUrl = "https://viacep.com.br";
        var requestPath = "ws/78085630/json";
        var responseBody = getResponseBody();
        var statusCode = 200;
        var gatewayService = getGatewayService(
                baseUrl,
                requestPath,
                responseBody,
                statusCode,
                factory -> new ViaCepGatewayService(factory, getPropertiesReader(baseUrl), this.cacheProvider)
        );

        var information = gatewayService.getPostalCodeInformation(new PostalCode("78085630"));

        assertEquals("78085630", information.getPostalCode());
    }

    @SneakyThrows
    @Test
    public void getPostalCodeInformation_shouldReturnCachedPostalCodeInformation() {
        var baseUrl = "https://viacep.com.br";
        var requestPath = "ws/78085630/json";
        var responseBody = getResponseBody();
        var information = this.mapper.readValue(responseBody, PostalCodeInformationResponse.class);
        var statusCode = 200;
        var gatewayService = getGatewayService(
                baseUrl,
                requestPath,
                responseBody,
                statusCode,
                factory -> new ViaCepGatewayService(factory, getPropertiesReader(baseUrl), this.cacheProvider)
        );
        Mockito.when(this.cacheProvider.get(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(information));

        var result = gatewayService.getPostalCodeInformation(new PostalCode("78085630"));

        assertEquals("78085630", result.getPostalCode());
        Mockito.verify(this.cacheProvider, Mockito.never()).set(Mockito.any(), Mockito.any());
    }

    private static String getResponseBody() {
        return """
                {
                  "cep": "78085630",
                  "logradouro": "Avenida Santo Antônio",
                  "complemento": "",
                  "bairro": "Coxipó",
                  "localidade": "Cuiabá",
                  "uf": "MT",
                  "ibge": "5103403",
                  "gia": "",
                  "ddd": "65",
                  "siafi": "9067"
                }""";
    }

    private static PropertiesReader getPropertiesReader(String baseUrl) {
        return new PropertiesReaderStubBuilder()
                .addProp("viacep.baseurl", baseUrl)
                .build();
    }

    private static<T> T getGatewayService(
            String baseUrl,
            String requestPath,
            String requestBody,
            int statusCode,
            ServiceCreator<T> createService) {
        var httpClient = new HttpClientStubBuilder()
                .setBaseUrl(baseUrl)
                .setRequestPath(requestPath)
                .setResponseBody(requestBody)
                .setStatusCode(statusCode)
                .build();
        var gatewayFactory = new HttpGatewayServiceFactory<>(
                createService,
                httpClient);
        return gatewayFactory.create(baseUrl);
    }
}