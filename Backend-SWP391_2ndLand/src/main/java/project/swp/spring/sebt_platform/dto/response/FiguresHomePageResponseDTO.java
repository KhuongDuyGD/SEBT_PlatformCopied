package project.swp.spring.sebt_platform.dto.response;

public class FiguresHomePageResponseDTO {
    private long numberOfSoldListings;
    private long numberOfReviews;
    private double percentageOfSuccessTransaction;

    public FiguresHomePageResponseDTO(long numberOfSoldListings, long numberOfReviews, double percentageOfSuccessTransaction) {
        this.numberOfSoldListings = numberOfSoldListings;
        this.numberOfReviews = numberOfReviews;
        this.percentageOfSuccessTransaction = percentageOfSuccessTransaction;
    }

    public FiguresHomePageResponseDTO() {
    }

    public long getNumberOfSoldListings() {
        return numberOfSoldListings;
    }

    public void setNumberOfSoldListings(long numberOfSoldListings) {
        this.numberOfSoldListings = numberOfSoldListings;
    }

    public long getNumberOfReviews() {
        return numberOfReviews;
    }

    public void setNumberOfReviews(long numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
    }

    public double getPercentageOfSuccessTransaction() {
        return percentageOfSuccessTransaction;
    }

    public void setPercentageOfSuccessTransaction(double percentageOfSuccessTransaction) {
        this.percentageOfSuccessTransaction = percentageOfSuccessTransaction;
    }
}
