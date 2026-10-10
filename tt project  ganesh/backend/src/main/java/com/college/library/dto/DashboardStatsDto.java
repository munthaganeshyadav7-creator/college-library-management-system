package com.college.library.dto;

/**
 * DTO containing summary metrics for Student, Librarian, and Admin dashboards.
 */
public class DashboardStatsDto {

    private long totalTitles;
    private long totalCopies;
    private long availableCopies;
    private long issuedCopies;
    private long activeLoans;
    private long overdueLoans;
    private long returnedBooks;
    private long registeredStudents;
    private double totalRecordedPenalties;
    private double unpaidPenalties;
    private double paidPenalties;

    // Student specific stats
    private long myActiveLoans;
    private long myOverdueLoans;
    private long myTotalBorrowed;
    private double myUnpaidPenalties;

    public DashboardStatsDto() {
    }

    public long getTotalTitles() {
        return totalTitles;
    }

    public void setTotalTitles(long totalTitles) {
        this.totalTitles = totalTitles;
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

    public long getIssuedCopies() {
        return issuedCopies;
    }

    public void setIssuedCopies(long issuedCopies) {
        this.issuedCopies = issuedCopies;
    }

    public long getActiveLoans() {
        return activeLoans;
    }

    public void setActiveLoans(long activeLoans) {
        this.activeLoans = activeLoans;
    }

    public long getOverdueLoans() {
        return overdueLoans;
    }

    public void setOverdueLoans(long overdueLoans) {
        this.overdueLoans = overdueLoans;
    }

    public long getReturnedBooks() {
        return returnedBooks;
    }

    public void setReturnedBooks(long returnedBooks) {
        this.returnedBooks = returnedBooks;
    }

    public long getRegisteredStudents() {
        return registeredStudents;
    }

    public void setRegisteredStudents(long registeredStudents) {
        this.registeredStudents = registeredStudents;
    }

    public double getTotalRecordedPenalties() {
        return totalRecordedPenalties;
    }

    public void setTotalRecordedPenalties(double totalRecordedPenalties) {
        this.totalRecordedPenalties = totalRecordedPenalties;
    }

    public double getUnpaidPenalties() {
        return unpaidPenalties;
    }

    public void setUnpaidPenalties(double unpaidPenalties) {
        this.unpaidPenalties = unpaidPenalties;
    }

    public double getPaidPenalties() {
        return paidPenalties;
    }

    public void setPaidPenalties(double paidPenalties) {
        this.paidPenalties = paidPenalties;
    }

    public long getMyActiveLoans() {
        return myActiveLoans;
    }

    public void setMyActiveLoans(long myActiveLoans) {
        this.myActiveLoans = myActiveLoans;
    }

    public long getMyOverdueLoans() {
        return myOverdueLoans;
    }

    public void setMyOverdueLoans(long myOverdueLoans) {
        this.myOverdueLoans = myOverdueLoans;
    }

    public long getMyTotalBorrowed() {
        return myTotalBorrowed;
    }

    public void setMyTotalBorrowed(long myTotalBorrowed) {
        this.myTotalBorrowed = myTotalBorrowed;
    }

    public double getMyUnpaidPenalties() {
        return myUnpaidPenalties;
    }

    public void setMyUnpaidPenalties(double myUnpaidPenalties) {
        this.myUnpaidPenalties = myUnpaidPenalties;
    }
}
