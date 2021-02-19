package nagp.directservice.requests.controller;

import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import nagp.directservice.requests.exceptions.RequestNotFoundException;
import nagp.directservice.requests.models.ServiceRequest;
import nagp.directservice.requests.service.IRequestService;



@RestController
@EnableCircuitBreaker
@RequestMapping("requests")
public class RequestController {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RequestController.class);

	@Resource(name = "restTemplate")
	private RestTemplate restTemplate;

	@Value("${server.port}")
	private int port;

	@Resource
	private IRequestService requestService;


	@GetMapping(value = "/{requestId}")
	ServiceRequest getRequest(@PathVariable(name = "requestId") String requestId) throws RequestNotFoundException {
		logger.info("Working from port " + port + " of Requests service");
		Optional<ServiceRequest>  serviceRequest= requestService.getRequest(requestId);
		if(serviceRequest.isPresent()) {
			return serviceRequest.get();
		}
		throw new RequestNotFoundException(requestId);
	}

	@GetMapping()
	List<ServiceRequest> getAllRequests() {
		logger.info("Working from port " + port + " of Requests service");
		return requestService.getAllRequests();
	}

	@GetMapping(value = "consumer/{consumerId}")
	List<ServiceRequest> getConsumerRequest(@PathVariable(name = "consumerId") String consumerId) {
		logger.info("Working from port " + port + " of Requests service");
		return requestService.getConsumerRequests(consumerId);
	}

	@GetMapping(value = "seller/{sellerid}")
	List<ServiceRequest> requestedSellers(@PathVariable(name = "sellerid") String sellerid) {
		logger.info("Working from port " + port + " of Requests service");
		return requestService.getSellerRequests(sellerid);
	}

	@GetMapping(value = "requestSeller")
	ServiceRequest requestSeller(@RequestParam String sellerid, @RequestParam String requestid) throws Exception {
		logger.info("Working from port " + port + " of Requests service");
		Optional<ServiceRequest> request = requestService.getRequest(requestid);
		if(!request.isPresent()) {
			throw new RequestNotFoundException(requestid);
		}
		if(requestService.validateSeller(sellerid, requestid))
			return request.get();
		else {
			throw new Exception("ERROR: Passed seller id is not valid.");
		}

	}

	@GetMapping(value = "acceptRequest")
	String acceptRequest(@RequestParam String sellerid, @RequestParam String requestid) throws Exception {
		logger.info("Working from port " + port + " of Requests service");
		Optional<ServiceRequest> request = requestService.getRequest(requestid);
		if(request.isPresent()) {

			if(request.get().getRequestedSellerId() == null || !request.get().getRequestedSellerId().equalsIgnoreCase(sellerid)) {
				throw new Exception("Passed seller id is not equal to the requested Seller Id.");
			}

			if(request.get().getAmount() <= 0) {
				throw new Exception("Amount is not added by the admin yet.");

			}

			return requestService.acceptRequest(sellerid, requestid);
		}
		else {
			throw new RequestNotFoundException(requestid);
		}
		
	}

	@GetMapping(value = "setAmount")
	String setAmount(@RequestParam String requestId, @RequestParam double amount) throws Exception {
		logger.info("Working from port " + port + " of Requests service");
		return requestService.setAmount(requestId, amount);
	}

	@GetMapping(value = "declineRequest")
	ServiceRequest declineRequest(@RequestParam String sellerid, @RequestParam String requestid) throws RequestNotFoundException {
		logger.info("Working from port " + port + " of Requests service");
		return requestService.declineRequest(sellerid, requestid);
	}


	@DeleteMapping(value = "/{requestId}")
	boolean deleteRequest(@PathVariable(name = "requestId") String requestId) throws RequestNotFoundException {
		logger.info("Working from port " + port + " of Requests service");
		return requestService.deleteRequest(requestId);
	}

	@PostMapping
	String addRequest(@RequestParam String consumerId, @RequestParam String description, 
			@RequestParam String address,@RequestParam String serviceType) {
		logger.info("Working from port " + port + " of Consumer service");
		String id = null;
		try {
			id = requestService.addRequest(consumerId, description, address, serviceType);
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
			return "ERROR: " + e.getMessage();
		}
		return "Request successfully created with request id " + id;
	}

	@PostMapping(value = "/cancelOrder")
	void cancelOrder(@RequestParam String sellerId, @RequestParam String address, 
			@RequestParam double amount,@RequestParam String description, @RequestParam String service,
			@RequestParam String consumerId, @RequestParam String requestId) {
		logger.info("Working from port " + port + " of Consumer service");
		try {
			requestService.cancelOrder(consumerId, description, address, amount, description,
					service, sellerId, requestId);
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
