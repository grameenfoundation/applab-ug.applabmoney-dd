package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.applab.digitizingdata.R;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/9/13.
 */
public class MembersLoansIssuedArrayAdapter extends ArrayAdapter<Member>  {
    Context context;
    ArrayList<Member> values;
    int meetingId;
    Meeting targetMeeting = null;
    MeetingLoanIssuedRepo loansIssuedRepo = null;
    MeetingSavingRepo savingRepo = null;
    MeetingRepo meetingRepo = null;

    public MembersLoansIssuedArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.row_member_loans_issued, values);
        this.context = context;
        this.values = values;

        loansIssuedRepo = new MeetingLoanIssuedRepo(getContext());
        meetingRepo = new MeetingRepo(getContext());
        savingRepo = new MeetingSavingRepo(getContext());
    }

    public void setMeetingId(int meetingId){
        this.meetingId = meetingId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_member_loans_issued, parent, false);

            if(null == meetingRepo) {
                meetingRepo = new MeetingRepo(getContext());
            }
            if(null == loansIssuedRepo) {
                loansIssuedRepo = new MeetingLoanIssuedRepo(getContext());
            }
            //Get the Widgets
            final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtRMLIssuedFullNames);
            final TextView txtLoanIssuedToday = (TextView)rowView.findViewById(R.id.txtRMLIssuedTodaysLoan);
            final TextView txtTotalIssued = (TextView)rowView.findViewById(R.id.txtRMLIssuedTotals);
            final TextView txtOutstanding = (TextView)rowView.findViewById(R.id.txtRMLIssuedOutstanding);
            final TextView txtTotalSavings = (TextView)rowView.findViewById(R.id.txtRMLIssuedSavings);

            //Assign Values to the Widgets
            Member member = values.get(position);
            txtFullNames.setText(member.toString());

            //Get the Total
            targetMeeting = meetingRepo.getMeetingById(meetingId);

            double totalIssuedToMemberInMeeting = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                totalIssuedToMemberInMeeting = loansIssuedRepo.getTotalLoansIssuedToMemberInMeeting(targetMeeting.getMeetingId(), member.getMemberId());
            }
            txtLoanIssuedToday.setText(String.format("Today: %,.0fUGX", totalIssuedToMemberInMeeting));

            double totalLoansToMember = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                totalLoansToMember = loansIssuedRepo.getTotalLoansIssuedToMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }
            txtTotalIssued.setText(String.format("Total Loans: %,.0fUGX", totalLoansToMember));

            double outstandingLoansByMember = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                outstandingLoansByMember = loansIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }
            txtOutstanding.setText(String.format("Outstanding Bal: %,.0fUGX", outstandingLoansByMember));

            double totalSavingsByMember = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                totalSavingsByMember = savingRepo.getMemberTotalSavingsInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }
            txtTotalSavings.setText(String.format("Total Savings: %,.0fUGX", totalSavingsByMember));


            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
