package nagp.directservice.requests.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Repository;

import nagp.directservice.requests.dao.IRequestDao;
import nagp.directservice.requests.exceptions.RequestNotFoundException;
import nagp.directservice.requests.models.ServiceRequest;
import nagp.directservice.requests.models.ServiceType;


@Repository
public class RequestDao implements IRequestDao{

	private static Random random;

	private static List<ServiceRequest> requests = new ArrayList<>();
	@Override
	public Optional<ServiceRequest> getRequest(String requestId) {
		return requests.stream()
				.filter(r -> requestId.trim().equalsIgnoreCase(r.getRequestId())).findFirst();
	}

	@Override
	public List<ServiceRequest> getAllRequests() {
		return requests;
	}

	@Override
	public String setAmount(String requestId, double amount) throws RequestNotFoundException {
		Optional<ServiceRequest> request = getRequest(requestId);
		if(request.isPresent())
		{
			request.get().setAmount(amount);
			return "Request updated successfully with amount "+ amount;
		}
		else {
			throw new RequestNotFoundException(requestId);
		}
	}
	@Override
	public boolean deleteRequest(String requestId) {
		return requests.removeIf(r -> requestId.trim().equalsIgnoreCase(r.getRequestId()));
	}

	@Override
	public void addRequest(ServiceRequest request) {
		requests.add(request);

	}

	static {
		random = new Random();
		for (int i = 0; i<10; i++) {
			requests.add(randomRequest(i));
		}
	}

	private static ServiceRequest randomRequest(int i) {
		ServiceType service = ServiceType.values()[random.nextInt(ServiceType.values().length)];
		return new ServiceRequest("C1000000"+i,"New Description "+i , "Random address "+i, service);
	}

	



}
