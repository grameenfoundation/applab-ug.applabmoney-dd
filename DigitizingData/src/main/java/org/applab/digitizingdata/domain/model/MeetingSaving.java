package org.applab.digitizingdata.domain.model;

/**
 * Created by Moses on 7/7/13.
 */
public class MeetingSaving {
    private int savingId;
    private Meeting meeting;
    private Member member;
    private double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getSavingId() {
        return savingId;
    }

    public void setSavingId(int savingId) {
        this.savingId = savingId;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public MeetingSaving() {

    }

    public MeetingSaving(int savingId, Meeting meeting, Member member, double amount) {
        this.savingId = savingId;
        this.meeting = meeting;
        this.member = member;
        this.amount = amount;
    }

    public MeetingSaving(Meeting meeting, Member member, double amount) {
        //Saving ID will be generated by DB
        this.meeting = meeting;
        this.member = member;
        this.amount = amount;
    }
}
