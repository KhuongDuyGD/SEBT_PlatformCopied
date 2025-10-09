package project.swp.spring.sebt_platform.dto.response;

import java.util.List;

public class FiguresAdminDashboardResponseDTO {
    private long numberOfMember;
    private long numberOfActiveListings;
    private long numberOfComplaints;
    private long numberOfEvListings;
    private long numberOfBatteryListings;

    private long numberOfMemberInCurrentMonth;
    private long numberOfPendingListings;
    private double benefitOfPreviousMonth;

    private double percentageOfBenefitGrowth;

    private List<Double> benefitFromBeginningOfYearToCurrentMonth;

    public FiguresAdminDashboardResponseDTO(List<Double> benefitFromBeginningOfYearToCurrentMonth,
                                            double benefitInCurrentMonth, double benefitOfPreviousMonth,
                                            long numberOfPendingListings, long numberOfMemberInCurrentMonth,
                                            long numberOfBatteryListings, long numberOfEvListings,
                                            long numberOfComplaints, long numberOfActiveListings,
                                            long numberOfMember) {
        this.benefitFromBeginningOfYearToCurrentMonth = benefitFromBeginningOfYearToCurrentMonth;
        this.percentageOfBenefitGrowth = benefitInCurrentMonth;
        this.benefitOfPreviousMonth = benefitOfPreviousMonth;
        this.numberOfPendingListings = numberOfPendingListings;
        this.numberOfMemberInCurrentMonth = numberOfMemberInCurrentMonth;
        this.numberOfBatteryListings = numberOfBatteryListings;
        this.numberOfEvListings = numberOfEvListings;
        this.numberOfComplaints = numberOfComplaints;
        this.numberOfActiveListings = numberOfActiveListings;
        this.numberOfMember = numberOfMember;
    }

    public FiguresAdminDashboardResponseDTO() {
    }

    public long getNumberOfMemberInCurrentMonth() {
        return numberOfMemberInCurrentMonth;
    }

    public void setNumberOfMemberInCurrentMonth(long numberOfMemberInCurrentMonth) {
        this.numberOfMemberInCurrentMonth = numberOfMemberInCurrentMonth;
    }

    public long getNumberOfPendingListings() {
        return numberOfPendingListings;
    }

    public void setNumberOfPendingListings(long numberOfPendingListings) {
        this.numberOfPendingListings = numberOfPendingListings;
    }

    public double getBenefitOfPreviousMonth() {
        return benefitOfPreviousMonth;
    }

    public void setBenefitOfPreviousMonth(double benefitOfPreviousMonth) {
        this.benefitOfPreviousMonth = benefitOfPreviousMonth;
    }

    public long getNumberOfMember() {
        return numberOfMember;
    }

    public void setNumberOfMember(long numberOfMember) {
        this.numberOfMember = numberOfMember;
    }

    public long getNumberOfActiveListings() {
        return numberOfActiveListings;
    }

    public void setNumberOfActiveListings(long numberOfActiveListings) {
        this.numberOfActiveListings = numberOfActiveListings;
    }

    public long getNumberOfComplaints() {
        return numberOfComplaints;
    }

    public void setNumberOfComplaints(long numberOfComplaints) {
        this.numberOfComplaints = numberOfComplaints;
    }

    public long getNumberOfEvListings() {
        return numberOfEvListings;
    }

    public void setNumberOfEvListings(long numberOfEvListings) {
        this.numberOfEvListings = numberOfEvListings;
    }

    public long getNumberOfBatteryListings() {
        return numberOfBatteryListings;
    }

    public void setNumberOfBatteryListings(long numberOfBatteryListings) {
        this.numberOfBatteryListings = numberOfBatteryListings;
    }

    public double getPercentageOfBenefitGrowth() {
        return percentageOfBenefitGrowth;
    }

    public void setPercentageOfBenefitGrowth(double percentageOfBenefitGrowth) {
        this.percentageOfBenefitGrowth = percentageOfBenefitGrowth;
    }

    public List<Double> getBenefitFromBeginningOfYearToCurrentMonth() {
        return benefitFromBeginningOfYearToCurrentMonth;
    }

    public void setBenefitFromBeginningOfYearToCurrentMonth(List<Double> benefitFromBeginningOfYearToCurrentMonth) {
        this.benefitFromBeginningOfYearToCurrentMonth = benefitFromBeginningOfYearToCurrentMonth;
    }
}
