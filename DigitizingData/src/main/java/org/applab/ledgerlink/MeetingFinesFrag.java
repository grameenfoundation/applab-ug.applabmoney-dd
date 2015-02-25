package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.helpers.MembersFinesArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingFinesFrag extends SherlockFragment {
    private ArrayList<Member> members;
    private String meetingDate;
    private int meetingId;
    private MeetingActivity parentActivity;
    private RelativeLayout fragmentView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MeetingActivity) getSherlockActivity();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
        fragmentView = (RelativeLayout) inflater.inflate(R.layout.frag_meeting_fines, container, false);
        initializeFragment();
        return fragmentView;
    }


    private void initializeFragment()
    {

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        ActionBar actionBar = parentActivity.getSupportActionBar();
        meetingDate = parentActivity.getIntent().getStringExtra("_meetingDate");
        String title = String.format("Meeting");
        switch (Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = "Send Data";
                break;
            case VIEW_MODE_READ_ONLY:
                title = "Sent Data";
                break;
            default:
                // title="Meeting";
                break;
        }
        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        /** TextView lblMeetingDate = (TextView)parentActivity.findViewById(R.id.lblMSavFMeetingDate);
         meetingDate = parentActivity.getIntent().getStringExtra("_meetingDate");
         lblMeetingDate.setText(meetingDate); */
        meetingId = parentActivity.getIntent().getIntExtra("_meetingId", 0);
        //Wrap and run long task
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                //Populate the Members
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(runnable, "Please wait", "Loading list of fines...", parentActivity);
    }


    //Populate Members List
    private void populateMembersList() {
        //Load the Main Menu
        members = parentActivity.ledgerLinkApplication.getMemberRepo().getAllMembers();

        //Now get the data via the adapter
        final MembersFinesArrayAdapter adapter = new MembersFinesArrayAdapter(parentActivity.getBaseContext(), members);
        adapter.setMeetingId(meetingId);

        //Assign Adapter to ListView
        //setListAdapter(adapter);
        final ListView lvwMembers = (ListView) fragmentView.findViewById(R.id.lvwMFineMembers);
        final TextView txtEmpty = (TextView) fragmentView.findViewById(R.id.txtMFineEmpty);

        Runnable runOnUiThread = new Runnable()
        {
            @Override
            public void run()
            {
                lvwMembers.setEmptyView(txtEmpty);
                lvwMembers.setAdapter(adapter);
            }
        };
        parentActivity.runOnUiThread(runOnUiThread);



        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // Do not invoke the event when in Read only Mode
                if(parentActivity.isViewOnly()) {
                    Toast.makeText(parentActivity.getBaseContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    return;
                }
                if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = members.get(position);
                    Intent i = new Intent(view.getContext(), MemberFinesHistoryActivity.class);

                    // Pass on data
                    i.putExtra("_meetingDate", meetingDate);
                    i.putExtra("_memberId", selectedMember.getMemberId());
                    i.putExtra("_name", selectedMember.toString());
                    i.putExtra("_meetingId", meetingId);

                    startActivity(i);

                    //parentActivity.finish();

                }
            }
        });
    }
}