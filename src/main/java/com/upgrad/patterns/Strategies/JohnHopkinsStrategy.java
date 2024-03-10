package com.upgrad.patterns.Strategies;

import com.upgrad.patterns.Config.RestServiceGenerator;
import com.upgrad.patterns.Entity.JohnHopkinResponse;
import com.upgrad.patterns.Interfaces.IndianDiseaseStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

@Service
public class JohnHopkinsStrategy implements IndianDiseaseStat {

	private Logger logger = LoggerFactory.getLogger(JohnHopkinsStrategy.class);

	private RestTemplate restTemplate;

	@Value("${config.john-hopkins-url}")
	private String baseUrl;

	public JohnHopkinsStrategy() {
		restTemplate = RestServiceGenerator.GetInstance();
	}

	@Override
	public String GetActiveCount() {
		try {
			JohnHopkinResponse[] response = getJohnHopkinResponses();
			Optional<Float> optionalConfirmedCases = Arrays.stream(response)
					.filter(data -> data.getCountry().equals("India"))
					.map(data -> data.getStats().getConfirmed())
					.reduce(Float::sum);

			if (optionalConfirmedCases.isPresent()) {
				Integer confirmedCases = Math.round(optionalConfirmedCases.get());
				return String.valueOf(confirmedCases);
			} else {
				logger.error("No confirmed cases found for India in the response from john-hopkins");
				return null;
			}
		} catch (Exception e) {
			logger.error("Error occurred while fetching disease count from john-hopkins", e);
			return null;
		}
	}

	private JohnHopkinResponse[] getJohnHopkinResponses() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

		return restTemplate.exchange(
				baseUrl, HttpMethod.GET, new HttpEntity<Object>(headers),
				JohnHopkinResponse[].class).getBody();
	}
}
