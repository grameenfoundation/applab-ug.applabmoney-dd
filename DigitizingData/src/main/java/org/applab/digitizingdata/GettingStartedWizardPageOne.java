package org.applab.digitizingdata;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.fontutils.TypefaceTextView;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.VslaInfoRepo;

public class GettingStartedWizardPageOne  extends SherlockActivity {
    VslaInfoRepo vslaInfoRepo = null;
    VslaInfo vslaInfo = null;
    ActionBar actionBar;
    TextView savingsGroupName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());


        setContentView(R.layout.activity_getting_started_wizard_page_1);

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;

        actionBar = getSupportActionBar();


        //For test purposes, create vsla info if not exists
        /* VslaInfoRepo repo = new VslaInfoRepo(getBaseContext());
        if(!repo.vslaInfoExists()) {
           repo.saveVslaInfo("TESTVSLA", "TESTVSLA", "1234");
        }
        */

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_next, null);
        customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent stage = new Intent(getBaseContext(), GettingsStartedWizardNewCycleActivity.class);
                        startActivity(stage);
                        finish();
                    }
                });

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle("Get started");
       /** actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE); */
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

        actionBar.setDisplayShowCustomEnabled(true);

        vslaInfoRepo = new VslaInfoRepo(this);
        vslaInfo = vslaInfoRepo.getVslaInfo();
        savingsGroupName = (TextView)findViewById(R.id.txtNCP_header);
        if(!vslaInfo.isActivated()) {
          //If not activated, show message to avoid displaying "Offline Mode" as vsla name
            savingsGroupName.setText("(not yet activated)");
        }
        else {

        savingsGroupName.setText(vslaInfo.getVslaName());
        }

        TypefaceTextView txtGSW_info = (TypefaceTextView) findViewById(R.id.txtGSW_info);
        String txtGSWInfoText = "If it is not the beginning of a cycle, you will also need to enter the number of stars (shares) bought so far during the current cycle and the amount of loans outstanding for each member.\n\nAre you prepared to enter all member and cycle information now? If so you may get started by pressing ";
        Spannable nextText = new SpannableString("\"next.\"");
        nextText.setSpan(new ForegroundColorSpan(Color.BLUE), 0,nextText.length()-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtGSWInfoText += nextText;
        txtGSW_info.setText(txtGSWInfoText);

        vslaInfoRepo.updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_ONE);
    }



  /**  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case R.id.mnuNCNext:
                //First Save the Cycle Dates
                //If successful move to next activity
                //Next page
                Intent mainMenu = new Intent(getBaseContext(), GettingStartedWizardPageTwo.class);
                startActivity(mainMenu);
                finish();

        }
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.getting_started_wizard_page_one_menu, menu);
        return true;

    }
    */
}
