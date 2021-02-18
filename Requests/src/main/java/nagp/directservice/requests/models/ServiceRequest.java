package nagp.directservice.requests.models;

import java.util.ArrayList;
import java.util.List;

public class ServiceRequest {
	
	private static int currId = 10000000;
	
	/**
	 * Unique id of the service request
	 */
	private String requestId;
	
	/**
	 * Id of the consumer who have generated this service request
	 */
	private String consumerId;
	
	/**
	 * Seller who is currently requested for this request
	 */
	private String requestedSellerId;
	
	/**
	 * Quoted amount by the admin for this job
	 */
	private double amount;
	
	/**
	 * List of sellers who have rejected this job
	 */
	private List<String> sellersRejected;
	
	/**
	 * Description about this job
	 */
	private String description;
	
	/**
	 * Address where this job is to be completed
	 */
	private String address;
	
	/**
	 * Service required by the consumer
	 */
	private ServiceType service;

	public ServiceRequest(String consumerId, String description, String address,
			ServiceType service) {
		super();
		this.requestId = "SR" + currId++;
		this.consumerId = consumerId;
		this.description = description;
		this.address = address;
		this.service = service;
		this.sellersRejected = new ArrayList<String>();
	}
	
	public ServiceRequest(String consumerId, String description, String address,
			ServiceType service, String sellerId, String requestId) {
		super();
		this.requestId = requestId;
		this.consumerId = consumerId;
		this.description = description;
		this.address = address;
		this.service = service;
		this.sellersRejected = new ArrayList<String>();
		this.sellersRejected.add(sellerId);
	}

	public String getRequestedSellerId() {
		return requestedSellerId;
	}

	public void setRequestedSellerId(String requestedSellerId) {
		this.requestedSellerId = requestedSellerId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRequestId() {
		return requestId;
	}

	public String getConsumerId() {
		return consumerId;
	}

	public List<String> getSellersRejected() {
		return sellersRejected;
	}

	public ServiceType getService() {
		return service;
	}
	
}
