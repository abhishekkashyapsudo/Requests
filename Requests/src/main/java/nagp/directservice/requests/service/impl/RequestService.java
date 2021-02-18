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

	@HystrixCommand(fallbackMethod = "requestSellerFallback")
	public ServiceRequest requestSeller(String sellerid, String requestid) throws RequestNotFoundException {
		Optional<ServiceRequest> request = getRequest(requestid);
		if(request.isPresent()) {
			String baseUrl = loadBalancerClient.choose("sellers").getUri().toString() + "/sellers/"+sellerid;
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = null;
			try {
				UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl);

				response = restTemplate.exchange(builder.buildAndExpand().toUri(), HttpMethod.GET, null,
						String.class);
				if(response.getStatusCode() ==HttpStatus.OK)
					request.get().setRequestedSellerId(sellerid);
				else
					throw new Exception("Invalid Seller id.");
				return request.get();


			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
			}
			
		}
		throw new RequestNotFoundException(requestid);
	}

	
	public String acceptRequest(String sellerId, String requestid) throws Exception {
		Optional<ServiceRequest> request = getRequest(requestid);
		if(request.isPresent()) {

			if(!request.get().getRequestedSellerId().equalsIgnoreCase(sellerId)) {
				throw new Exception("Passed seller id is not equal to the requested Seller Id.");
			}
			
			if(request.get().getAmount() <= 0) {
				throw new Exception("Amount is not added by the admin yet.");

			}

			return createOrder(sellerId, request);
		}
		else {
			throw new RequestNotFoundException(requestid);
		}
	}

	@HystrixCommand(fallbackMethod = "acceptRequestFallback")
	private String createOrder(String sellerId, Optional<ServiceRequest> request) {
		String baseUrl = loadBalancerClient.choose("orders").getUri().toString() + "/orders/";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;
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

	public String acceptRequestFallback(String sellerId, Optional<ServiceRequest> request) {
		logger.warn("Orders Service is down!!! fallback route enabled...");

		return "CIRCUIT BREAKER ENABLED!!! No Response From Orders Service at this moment. " +
		" Service will be back shortly - ";

	}
	
	public ServiceRequest requestSellerFallback(String sellerid, String requestid) throws Exception  {
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
