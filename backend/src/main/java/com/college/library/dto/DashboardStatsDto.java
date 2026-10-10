package com.college.library.dto;

public class DashboardStatsDto {

    private long totalBookTitles;
    private long totalCopies;
    private long availableCopies;
    private long registeredStudents;
    private long activeLoans;
    private long returnedLoans;
    private long overdueLoans;
    private double totalPenalties;
    private double unpaidPenalties;

    public DashboardStatsDto() {
    }

    public long getTotalBookTitles() {
        return totalBookTitles;
    }

    public void setTotalBookTitles(long totalBookTitles) {
        this.totalBookTitles = totalBookTitles;
    }

    public long getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(long totalCopies) {
        this.totalCopies = totalCopies;
    }

    public long getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(long availableCopies) {
        this.availableCopies = availableCopies;
    }

    public long getRegisteredStudents() {
        return registeredStudents;
    }

    public void setRegisteredStudents(long registeredStudents) {
        this.registeredStudents = registeredStudents;
    }

    public long getActiveLoans() {
        return activeLoans;
    }

    public void setActiveLoans(long activeLoans) {
        this.activeLoans = activeLoans;
    }

    public long getReturnedLoans() {
        return returnedLoans;
    }

    public void setReturnedLoans(long returnedLoans) {
        this.returnedLoans = returnedLoans;
    }

    public long getOverdueLoans() {
        return overdueLoans;
    }

    public void setOverdueLoans(long overdueLoans) {
        this.overdueLoans = overdueLoans;
    }

    public double getTotalPenalties() {
        return totalPenalties;
    }

    public void setTotalPenalties(double totalPenalties) {
        this.totalPenalties = totalPenalties;
    }

    public double getUnpaidPenalties() {
        return unpaidPenalties;
    }

    public void setUnpaidPenalties(double unpaidPenalties) {
        this.unpaidPenalties = unpaidPenalties;
    }
}
