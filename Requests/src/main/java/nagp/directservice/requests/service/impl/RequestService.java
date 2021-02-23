package nagp.directservice.requests.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import nagp.directservice.requests.dao.IRequestDao;
import nagp.directservice.requests.exceptions.InvalidCategoryException;
import nagp.directservice.requests.exceptions.RequestNotFoundException;
import nagp.directservice.requests.models.ServiceRequest;
import nagp.directservice.requests.models.ServiceType;
import nagp.directservice.requests.service.IRequestService;

@Service
public class RequestService implements IRequestService{

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RequestService.class);

	@Resource(name = "restTemplate")
	private RestTemplate restTemplate;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Resource
	IRequestDao requestDao;

	@Autowired
	LoadBalancerClient loadBalancerClient;

	public Optional<ServiceRequest> getRequest(String requestId) {
		return requestDao.getRequest(requestId);
	}

	public List<ServiceRequest> getAllRequests() {
		return requestDao.getAllRequests();

	}


	public List<ServiceRequest> getSellerRequests(String sellerid) {
		return requestDao.getAllRequests().stream().filter(r -> r.getRequestedSellerId() != null && r.getRequestedSellerId().equalsIgnoreCase(sellerid))
				.collect(Collectors.<ServiceRequest>toList());
	}

	public List<ServiceRequest> getConsumerRequests(String consumerId) {
		return requestDao.getAllRequests().stream().filter(r -> r.getConsumerId().equalsIgnoreCase(consumerId))
				.collect(Collectors.<ServiceRequest>toList());

	}

	
	@HystrixCommand(fallbackMethod = "validateSellerFallback")
	public boolean validateSeller(String sellerid, String requestid) throws Exception {

		try {
			String baseUrl = loadBalancerClient.choose("sellers").getUri().toString() + "/sellers/"+sellerid;
			ResponseEntity<String> response = null;
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl);

			response = restTemplate.exchange(builder.buildAndExpand().toUri(), HttpMethod.GET, null,
					String.class);
			boolean isValidSeller = response.getStatusCode() == HttpStatus.OK;
			if(isValidSeller) {
				ServiceRequest request = getRequest(requestid).get();
				request.setRequestedSellerId(sellerid);
				try {
					logger.info("Sending notification to Seller...!!!");
					jmsTemplate.convertAndSend("SELLER_REQUESTED", new String[] {requestid, sellerid});
				}
				catch(Exception e) {
					logger.warn(e.getMessage(), e);
				}
			}
			return isValidSeller;

		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			throw ex;
		}


	}



	@HystrixCommand(fallbackMethod = "acceptRequestFallback")
	public String acceptRequest(String sellerId, String requestid) throws Exception {
		String baseUrl = loadBalancerClient.choose("orders").getUri().toString() + "/orders/";
		ResponseEntity<String> response = null;
		Optional<ServiceRequest> request = getRequest(requestid);
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
					.queryParam("sellerId", sellerId)
					.queryParam("address", request.get().getAddress())
					.queryParam("amount", request.get().getAmount())
					.queryParam("description", request.get().getDescription())
					.queryParam("service", request.get().getService())
					.queryParam("consumerId", request.get().getConsumerId())
					.queryParam("requestId", request.get().getRequestId());

			response = restTemplate.exchange(builder.buildAndExpand().toUri(), HttpMethod.POST, null,
					String.class);
			deleteRequest(requestid);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
		}
		return response.getBody();
	}

	
	public String setAmount(String requestId, double amount) throws RequestNotFoundException {
		return requestDao.setAmount(requestId, amount);
	}
	public ServiceRequest declineRequest(String sellerid, String requestid) throws RequestNotFoundException {
		Optional<ServiceRequest> request = getRequest(requestid);
		if(request.isPresent()) {
			request.get().setRequestedSellerId(null);
			request.get().getSellersRejected().add(sellerid);
			return request.get();
		}
		throw new RequestNotFoundException(requestid);
	}

	public boolean deleteRequest(String requestId) {
		return requestDao.deleteRequest(requestId);
	}

	public String addRequest(String consumerId, String description, String address, String serviceType) throws InvalidCategoryException {
		ServiceType service = ServiceType.valueOf(serviceType);
		if(service == null) {
			throw new InvalidCategoryException("Invalid Service name was passed.");
		}
		ServiceRequest request = new ServiceRequest(consumerId, description, address, service);
		requestDao.addRequest(request);
		return request.getRequestId();
	}

	public String acceptRequestFallback(String sellerId, String requestid) {
		logger.warn("Orders Service is down!!! fallback route enabled...");

		return "CIRCUIT BREAKER ENABLED!!! No Response From Orders Service at this moment. " +
		" Service will be back shortly - ";

	}

	public boolean validateSellerFallback(String sellerid, String requestid) throws Exception  {
		logger.warn("Sellers Service is down!!! fallback route enabled...");

		throw new Exception( "CIRCUIT BREAKER ENABLED!!! No Response From Sellers Service at this moment. " +
				" Service will be back shortly - ");

	}

	@Override
	public void cancelOrder(String consumerId, String description, String address, double amount, String description2,
			String serviceType, String selllerId, String requestId) throws InvalidCategoryException {
		ServiceType service = ServiceType.valueOf(serviceType);
		if(service == null) {
			throw new InvalidCategoryException("Invalid Service name was passed.");
		}
		ServiceRequest request = new ServiceRequest(consumerId, description, address, service, selllerId, requestId);
		requestDao.addRequest(request);
	}






}
