package com.whcl.report.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.whcl.report.model.Order;
import com.whcl.report.model.User;
import com.whcl.report.util.PDFGenerator;

@RestController
@RequestMapping("report")
public class ReportController {
	
	protected final Log logger = LogFactory.getLog(ReportController.class);


	@Autowired
	private PDFGenerator reportService;

	@Autowired
	private DiscoveryClient discoveryClient;

	@GetMapping(value = "/api/v1.0/order/{orderid}", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> orderInvoiceReport(@PathVariable long orderid) throws IOException {
		ByteArrayInputStream bis = null;
		Order order = getOrderByOrderId(orderid);

		if(order != null) {
			
			//TODO, get user information by calling user-service
			//getUserByUserId(Integer.parseInt(order.getCustomerId()));
			bis = reportService.generateInviceReport(order);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=invoice.pdf");

		return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
				.body(new InputStreamResource(bis));
	}
	
	private User getUserByUserId(int userid) {
		
		List<ServiceInstance> instances = discoveryClient.getInstances("auth-service");
		ServiceInstance serviceInstance = instances.get(0);

		String baseUrl = serviceInstance.getUri().toString();

		baseUrl = baseUrl + "/user/" + userid;

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<User> response = null;
		try {
			response = restTemplate.exchange(baseUrl, HttpMethod.GET, getHeaders(), User.class);
		} catch (Exception ex) {
			logger.error(ex);
		}
		//System.out.println(response.getBody());
		return response.getBody();
	}

	@GetMapping("/api/v1.0/hello")
	public String hello() {
		return "welcome to report service";
	}

	private Order getOrderByOrderId(long orderid) throws RestClientException, IOException {

		List<ServiceInstance> instances = discoveryClient.getInstances("order-service");
		ServiceInstance serviceInstance = instances.get(0);

		String baseUrl = serviceInstance.getUri().toString();

		baseUrl = baseUrl + "/api/v1.0/orders/" + orderid;

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Order> response = null;
		try {
			response = restTemplate.exchange(baseUrl, HttpMethod.GET, getHeaders(), Order.class);
		} catch (Exception ex) {
			logger.error(ex);
		}

		return response.getBody();
	}

	private static HttpEntity<?> getHeaders() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		return new HttpEntity<>(headers);
	}

}
