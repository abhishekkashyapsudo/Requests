package nagp.directservice.requests.service;

import java.util.List;
import java.util.Optional;

import nagp.directservice.requests.exceptions.InvalidCategoryException;
import nagp.directservice.requests.exceptions.RequestNotFoundException;
import nagp.directservice.requests.models.ServiceRequest;

public interface IRequestService {

	Optional<ServiceRequest> getRequest(String requestId);

	List<ServiceRequest> getAllRequests();

	List<ServiceRequest> getSellerRequests(String sellerid);

	List<ServiceRequest> getConsumerRequests(String consumerId);

	ServiceRequest requestSeller(String sellerid, String requestid) throws RequestNotFoundException;

	String acceptRequest(String sellerid, String requestid) throws RequestNotFoundException, Exception;
	ServiceRequest declineRequest(String sellerid, String requestid) throws RequestNotFoundException;

	boolean deleteRequest(String requestId) throws RequestNotFoundException;

	String addRequest(String consumerId, String description, String address, String serviceType) throws InvalidCategoryException;

	String setAmount(String requestId, double amount) throws RequestNotFoundException;

}
