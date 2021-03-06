package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.VslaInfoRepo;

public class GettingStartedWizardPageTwo extends SherlockActivity {
    VslaInfoRepo vslaInfoRepo = null;
    VslaInfo vslaInfo = null;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_getting_started_wizard_passcode_validation);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle("Get Started");

        vslaInfoRepo = new VslaInfoRepo(this);
        vslaInfo = vslaInfoRepo.getVslaInfo();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {

            case R.id.mnuNCNext:
                //First Save the Cycle Dates
                //If successful move to next activity
                //validate passkey
                ValidPassKey();

        }
        return true;

    }



    public void ValidPassKey()
    {
        TextView txtPassKey = null;
        try {
            txtPassKey = (TextView)findViewById(R.id.txtGSW_passkey);
            String passKey = txtPassKey.getText().toString().trim();

            //Test purposes
            vslaInfo.setPassKey("");

            if(passKey.equalsIgnoreCase(vslaInfo.getPassKey())) {
                Intent mainMenu = new Intent(getBaseContext(), GettingsStartedWizardNewCycleActivity.class);

                startActivity(mainMenu);
            }
            else {
                Utils.createAlertDialogOk(this, "Security", "The Pass Key is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
            }
        }
        catch(Exception ex) {
            Utils.createAlertDialogOk(this, "Security", "The Pass Key could not be validated.", Utils.MSGBOX_ICON_EXCLAMATION).show();
            txtPassKey.requestFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.new_cycle, menu);
        return true;

    }
    
}
