package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.digitizingdata.R;

import java.util.ArrayList;

/**
 * Created by Moses on 7/25/13.
 */
public class AttendanceArrayAdapter extends ArrayAdapter<AttendanceRecord> {

    Context context;
    ArrayList<AttendanceRecord> values;
    int position;

    public AttendanceArrayAdapter(Context context, ArrayList<AttendanceRecord> values) {
        super(context, R.layout.row_attendance_history, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        try {
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.row_attendance_history, parent, false);

            //Get the Widgets
            TextView txtMeetingDate = (TextView)rowView.findViewById(R.id.txtRAHMeetingDate);
            TextView txtAttendance = (TextView)rowView.findViewById(R.id.txtRAHAttendance);
            TextView txtComments = (TextView)rowView.findViewById(R.id.txtRAHComments);

            //Assign Values to the Widgets
            AttendanceRecord attendanceRecord = values.get(position);
            if(attendanceRecord != null) {
                txtComments.setText(attendanceRecord.getComment());
                txtMeetingDate.setText(Utils.formatDate(attendanceRecord.getMeetingDate(),Utils.DATE_FIELD_FORMAT));
                txtAttendance.setText((attendanceRecord.getPresent() == 1)? "Present" : "Absent");
            }

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}
