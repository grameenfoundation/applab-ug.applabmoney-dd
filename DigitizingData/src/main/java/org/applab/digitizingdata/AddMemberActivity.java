package org.applab.digitizingdata;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.applab.digitizingdata.R;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.helpers.CustomGenderSpinnerListener;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 7/15/13.
 */
public class AddMemberActivity extends SherlockActivity {
    private ActionBar actionBar;
    private Member selectedMember;
    private int selectedMemberId;
    private boolean successAlertDialogShown = false;
    private String dlgTitle = "Add Member";
    MemberRepo repo;
    private boolean isEditAction;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        if(getIntent().hasExtra("_isEditAction")){
            this.isEditAction = getIntent().getBooleanExtra("_isEditAction",false);
        }
        if(getIntent().hasExtra("_id")){
            this.selectedMemberId = getIntent().getIntExtra("_id",0);
        }

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        if(isEditAction){
            actionBar.setTitle("Edit Member");
        }
        else{
            actionBar.setTitle("New Member");
        }

        //Setup the Spinner Items
        Spinner cboGender = (Spinner)findViewById(R.id.cboAMGender);
        String[] genderList = new String[]{"Male", "Female"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item,genderList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboGender.setAdapter(adapter);

        cboGender.setOnItemSelectedListener(new CustomGenderSpinnerListener());

        clearDataFields();
        if(isEditAction){
            repo = new MemberRepo(getApplicationContext());
            selectedMember = repo.getMemberById(selectedMemberId);
            populateDataFields(selectedMember);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.add_member, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MainActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.mnuAMNext:
                //Toast.makeText(getBaseContext(), "You have successfully added a new member", Toast.LENGTH_LONG).show();
                return saveMemberData();
            case R.id.mnuAMFinished:
                return saveMemberData();
        }
        return true;
    }

    private boolean saveMemberData() {
        boolean successFlg = false;
        AlertDialog dlg = null;

        Member member = new Member();
        repo = new MemberRepo(getApplicationContext());
        if (selectedMember != null) {
            member = selectedMember;
        }

        if (validateData(member)) {
            boolean retVal = false;
            if (member.getMemberId() != 0) {
                retVal = repo.updateMember(member);
            }
            else {
                retVal = repo.addMember(member);
            }
            if (retVal) {
                if (member.getMemberId() == 0) {
                    //Set this new entity as the selected one
                    selectedMember = member;
                    dlg = Utils.createAlertDialog(AddMemberActivity.this,"Add Member","The new member was added successfully.", Utils.MSGBOX_ICON_TICK);
                    // Setting OK Button
                    dlg.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(successAlertDialogShown) {
                                Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                                startActivity(i);
                                successAlertDialogShown = false;
                            }
                        }
                    });
                    dlg.show();

                }
                else {
                    dlg = Utils.createAlertDialog(AddMemberActivity.this,"Edit Member","The member was updated successfully.", Utils.MSGBOX_ICON_TICK);
                    // Setting OK Button
                    dlg.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Clear the fields for new data
                            //Or go back
                        }
                    });
                    dlg.show();
                }

                if(dlg.isShowing()) {
                    //Flag that ready to goto Next
                    successAlertDialogShown = true;
                }

                successFlg = true;
                //clearDataFields(); //Not needed now
            }
            else {
                dlg = Utils.createAlertDialogOk(AddMemberActivity.this, "Add Member", "A problem occurred while adding the new member.", Utils.MSGBOX_ICON_TICK);
                dlg.show();
            }
        }
        else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }

    private boolean validateData(Member member) {
        try {
            if(null == member) {
                return false;
            }

            // Validate: MemberNo
            TextView txtMemberNo = (TextView)findViewById(R.id.txtAMMemberNo);
            String memberNo = txtMemberNo.getText().toString().trim();
            if (memberNo.length() < 1) {
                Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Member Number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtMemberNo.requestFocus();
                return false;
            }
            else {
                int theMemberNo = Integer.parseInt(memberNo);
                if (theMemberNo <= 0) {
                    Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Member Number must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtMemberNo.requestFocus();
                    return false;
                }
                else {
                    member.setMemberNo(theMemberNo);
                }
            }

            //Validate: Surname
            TextView txtSurname = (TextView)findViewById(R.id.txtAMSurname);
            String surname = txtSurname.getText().toString().trim();
            if(surname.length() < 1) {
                Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Surname is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtSurname.requestFocus();
                return false;
            }
            else {
                member.setSurname(surname);
            }

            //Validate: OtherNames
            TextView txtOtherNames = (TextView)findViewById(R.id.txtAMOtherNames);
            String otherNames = txtOtherNames.getText().toString().trim();
            if(otherNames.length() < 1) {
                Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "At least one other name is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtOtherNames.requestFocus();
                return false;
            }
            else {
                member.setOtherNames(otherNames);
            }

            //Validate: Gender
            TextView txtGender = (TextView)findViewById(R.id.txtAMGender);
            String gender = txtGender.getText().toString().trim();
            if(gender.length() < 1) {
                Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Sex is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtGender.requestFocus();
                return false;
            }
            else {
                member.setGender(gender);
            }

            // Validate: Age
            TextView txtAge = (TextView)findViewById(R.id.txtAMAge);
            String age = txtAge.getText().toString().trim();
            if (age.length() < 1) {
                Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Age is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtAge.requestFocus();
                return false;
            }
            else {
                int theAge = Integer.parseInt(age);
                if (theAge <= 0) {
                    Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Age must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtAge.requestFocus();
                    return false;
                }
                else {
                    //Get the DateOfBirth from the Age
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.YEAR, -theAge);
                    member.setDateOfBirth(c.getTime());
                }
            }

            //Validate: Occupation
            TextView txtOccupation = (TextView)findViewById(R.id.txtAMOccupation);
            String occupation = txtOccupation.getText().toString().trim();
            if(occupation.length() < 1) {
                Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Occupation is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtOccupation.requestFocus();
                return false;
            }
            else {
                member.setOccupation(occupation);
            }

            //Validate: PhoneNumber
            TextView txtPhoneNo = (TextView)findViewById(R.id.txtAMPhoneNo);
            String phoneNo = txtPhoneNo.getText().toString().trim();
            if(phoneNo.length() < 1) {
                //Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Phone Number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                //txtPhoneNo.requestFocus();
                //return false;
            }
            else {
                member.setPhoneNumber(phoneNo);
            }

            // Validate: Cycles Completed
            TextView txtCycles = (TextView)findViewById(R.id.txtAMCycles);
            String cycles = txtCycles.getText().toString().trim();
            int theCycles = 0;
            if (cycles.length() < 1) {
//                Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Number of Completed Cycles is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
//                txtCycles.requestFocus();
//                return false;
            }
            else {
                theCycles = Integer.parseInt(cycles);
                if (theCycles <= 0) {
                    Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The number of cycles must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtCycles.requestFocus();
                    return false;
                }
                else {
                    member.setCyclesCompleted(theCycles);
                }
            }


            //Final Verifications
            if(!repo.isMemberNoAvailable(member.getMemberNo(),member.getMemberId())) {
                Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "Another member is using this Member Number.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtMemberNo.requestFocus();
                return false;
            }

            return true;
        }
        catch (Exception ex){
            return false;
        }
    }

    private void populateDataFields(Member member) {
        try {

            clearDataFields();
            if (member == null) {
                return;
            }

            // Populate the Fields
            TextView txtMemberNo = (TextView)findViewById(R.id.txtAMMemberNo);
            txtMemberNo.setText(Utils.formatLongNumber(member.getMemberNo()));

            TextView txtSurname = (TextView)findViewById(R.id.txtAMSurname);
            if (member.getSurname() != null) {
                txtSurname.setText(member.getSurname());
            }
            TextView txtOtherNames = (TextView)findViewById(R.id.txtAMOtherNames);
            if (member.getOtherNames() != null) {
                txtOtherNames.setText(member.getOtherNames());
            }
            TextView txtGender = (TextView)findViewById(R.id.txtAMGender);
            if (member.getGender() != null) {
                txtGender.setText(member.getGender());
            }

            TextView txtOccupation = (TextView)findViewById(R.id.txtAMOccupation);
            if (member.getOccupation() != null) {
                txtOccupation.setText(member.getOccupation());
            }
            TextView txtPhone = (TextView)findViewById(R.id.txtAMPhoneNo);
            if (member.getPhoneNumber() != null) {
                txtPhone.setText(member.getPhoneNumber());
            }
            TextView txtAge = (TextView)findViewById(R.id.txtAMAge);
            //txtAge.setText(String.format("%d", 0));

            //TODO: I need to retrieve the Age from the DateOfBirth

        }
        finally {

        }

    }

    private void clearDataFields() {
        // Populate the Fields
        TextView txtMemberNo = (TextView)findViewById(R.id.txtAMMemberNo);
        txtMemberNo.setText(null);
        TextView txtSurname = (TextView)findViewById(R.id.txtAMSurname);
        txtSurname.setText(null);
        TextView txtOtherNames = (TextView)findViewById(R.id.txtAMOtherNames);
        txtOtherNames.setText(null);
        TextView txtGender = (TextView)findViewById(R.id.txtAMGender);
        txtGender.setText(null);
        TextView txtOccupation = (TextView)findViewById(R.id.txtAMOccupation);
        txtOccupation.setText(null);
        TextView txtPhone = (TextView)findViewById(R.id.txtAMPhoneNo);
        txtPhone.setText(null);
        TextView txtAge = (TextView)findViewById(R.id.txtAMAge);
        txtAge.setText(String.format("%d", 0));

        txtMemberNo.requestFocus();
    }

}