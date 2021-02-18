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
	ServiceRequest requestSeller(@RequestParam String sellerid, @RequestParam String requestid) throws RequestNotFoundException {
		logger.info("Working from port " + port + " of Requests service");
		return requestService.requestSeller(sellerid, requestid);
	}
	
	@GetMapping(value = "acceptRequest")
	String acceptRequest(@RequestParam String sellerid, @RequestParam String requestid) throws Exception {
		logger.info("Working from port " + port + " of Requests service");
		return requestService.acceptRequest(sellerid, requestid);
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
}
