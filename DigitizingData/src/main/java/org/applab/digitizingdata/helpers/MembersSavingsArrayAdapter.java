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
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/7/13.
 */
public class MembersSavingsArrayAdapter extends ArrayAdapter<Member> {
    Context context;
    ArrayList<Member> values;
    int meetingId;
    Meeting targetMeeting = null;
    MeetingSavingRepo savingRepo = null;
    MeetingRepo meetingRepo = null;

    public MembersSavingsArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.row_member_savings, values);
        this.context = context;
        this.values = values;

        savingRepo = new MeetingSavingRepo(getContext());
        meetingRepo = new MeetingRepo(getContext());
    }

    public void setMeetingId(int meetingId){
        this.meetingId = meetingId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            if(null == savingRepo) {
                savingRepo = new MeetingSavingRepo(getContext());
            }
            if(null == meetingRepo) {
                meetingRepo = new MeetingRepo(getContext());
            }
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_member_savings, parent, false);

            //Get the Widgets
            final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtRMSavFullNames);
            final TextView txtSavingsToday = (TextView)rowView.findViewById(R.id.txtRMSavTodaysSavings);
            final TextView txtTotals = (TextView)rowView.findViewById(R.id.txtRMSavTotals);

            //Assign Values to the Widgets
            Member member = values.get(position);
            txtFullNames.setText(member.toString());
            //Get members savings today i.e. in current meeting
            double todaysSavings = savingRepo.getMemberSaving(meetingId, values.get(position).getMemberId());
            txtSavingsToday.setText(String.format("Saved Today: %,.0f UGX", todaysSavings));

            //Get the Total SavingSchema
            targetMeeting = meetingRepo.getMeetingById(meetingId);
            double totalSavings = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                totalSavings  = savingRepo.getMemberTotalSavingsInCycle(targetMeeting.getVslaCycle().getCycleId(),member.getMemberId());
            }

            txtTotals.setText(String.format("Total: %,.0f UGX", totalSavings));
            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
