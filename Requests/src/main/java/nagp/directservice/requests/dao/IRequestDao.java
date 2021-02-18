package nagp.directservice.requests.dao;

import java.util.List;
import java.util.Optional;

import nagp.directservice.requests.exceptions.RequestNotFoundException;
import nagp.directservice.requests.models.ServiceRequest;

public interface IRequestDao {

	Optional<ServiceRequest> getRequest(String requestId);

	List<ServiceRequest> getAllRequests();

	boolean deleteRequest(String requestId);

	void addRequest(ServiceRequest request);

	String setAmount(String requestId, double amount) throws RequestNotFoundException;

}
