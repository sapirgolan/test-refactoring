package de.codecentric.example.clean.test.code.chucknorris;

import de.codecentric.example.clean.test.code.chucknorris.domain.ChuckNorrisFact;

import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

import static de.codecentric.example.clean.test.code.chucknorris.ChuckNorrisService.FACT_URL;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChuckNorrisServiceStepTwoTest {

    private static final Long EXISTING_JOKE = 1L;
    private static final Map<String, Long> GOOD_HTTP_PARAMS = Collections.singletonMap("id", EXISTING_JOKE);
    private static final Long NON_EXISTING_JOKE = 15123123L;
    private static final Map<String, Long> NON_EXISTING_HTTP_PARAMS = Collections.singletonMap("id", NON_EXISTING_JOKE);
    private static final Long BAD_JOKE = 99999999L;
    private static final Map<String, Long> BAD_HTTP_PARAMS = Collections.singletonMap("id", BAD_JOKE);

    private static final ResponseEntity<ChuckNorrisFactResponse> ERROR_RESPONSE =
            new ResponseEntity<>(new ChuckNorrisFactResponse("NoSuchQuoteException", "No quote with id=15123123."), HttpStatus.OK);
    private static final ResponseEntity<ChuckNorrisFactResponse> ITEM_RESPONSE =
            new ResponseEntity<>(new ChuckNorrisFactResponse("success", new ChuckNorrisFact(1L, "Chuck Norris is awesome")), HttpStatus.OK);

    private RestTemplate restTemplate;
    private ChuckNorrisService myServiceUnderTest;

    @Before
    public void setUp(){
        restTemplate = mock(RestTemplate.class);
        myServiceUnderTest = new ChuckNorrisService(restTemplate);
    }

    @Test
    public void serviceShouldReturnFact() {
        restEndpointShouldAnswer(GOOD_HTTP_PARAMS, (invocation) -> ITEM_RESPONSE);

        ChuckNorrisFact chuckNorrisFact = myServiceUnderTest.retrieveFact(EXISTING_JOKE);

        assertThat(chuckNorrisFact, is(new ChuckNorrisFact(EXISTING_JOKE, "Chuck Norris is awesome")));
    }

    @Test
    public void serviceShouldReturnNothing() {
        restEndpointShouldAnswer(NON_EXISTING_HTTP_PARAMS, (invocation -> ERROR_RESPONSE));

        ChuckNorrisFact chuckNorrisFact = myServiceUnderTest.retrieveFact(NON_EXISTING_JOKE);

        assertThat(chuckNorrisFact, is(nullValue()));
    }

    @Test(expected = ResourceAccessException.class)
    public void serviceShouldPropagateException() {
        restEndpointShouldAnswer(BAD_HTTP_PARAMS, (invocation -> {throw new ResourceAccessException("I/O error");}));

        myServiceUnderTest.retrieveFact(BAD_JOKE);
    }

    private void restEndpointShouldAnswer(Map<String, Long> httpParams, Answer<ResponseEntity<ChuckNorrisFactResponse>> response){
        when(restTemplate.getForEntity(FACT_URL, ChuckNorrisFactResponse.class, httpParams)).thenAnswer(response);
    }
}
