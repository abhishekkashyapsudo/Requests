package nagp.directservice.requests.models;



public enum ServiceType {
	MAKE_UP(Category.GROOMING), BARBER(Category.GROOMING), ELECTRICIAN(Category.REPAIRS),
	PLUMBER(Category.REPAIRS), YOGA_TRAINER(Category.MISC), INTERIOR_DESIGNER(Category.PAINTING),
	PAINTER(Category.PAINTING), CARPENTER(Category.PAINTING) ;
	
	private Category category;
	private ServiceType(Category category) {
		this.category = category;
	}
	
	public Category getCatgory() {
		return this.category;
	}
	
}
