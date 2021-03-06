package org.applab.digitizingdata.repo;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.domain.model.VslaMiddleStartCycle;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.domain.schema.VslaCycleSchema;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.Utils;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;
import android.content.Context;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 7/3/13.
 */
public class VslaCycleRepo {
    private Context context;

    public VslaCycleRepo() {
    }

    public VslaCycleRepo(Context context) {
        this.context = context;
    }

    // Adding new Entity
    public boolean addCycle(VslaCycle cycle) {
        SQLiteDatabase db = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            if(cycle.getStartDate() == null) {
                cycle.setStartDate(new Date());
            }
            values.put(VslaCycleSchema.COL_VC_START_DATE, Utils.formatDateToSqlite(cycle.getStartDate()));
            if(cycle.getEndDate() == null) {
                cycle.setEndDate(new Date());
            }
            values.put(VslaCycleSchema.COL_VC_END_DATE, Utils.formatDateToSqlite(cycle.getEndDate()));
            values.put(VslaCycleSchema.COL_VC_INTEREST_RATE, cycle.getInterestRate());
            values.put(VslaCycleSchema.COL_VC_MAX_SHARE_QTY, cycle.getMaxSharesQty());
            values.put(VslaCycleSchema.COL_VC_MAX_START_SHARE, cycle.getMaxStartShare());
            values.put(VslaCycleSchema.COL_VC_SHARE_PRICE, cycle.getSharePrice());
            values.put(VslaCycleSchema.COL_VC_IS_ACTIVE, (cycle.isActive()) ? 1 : 0);

            // Inserting Row
            long retVal = db.insert(VslaCycleSchema.getTableName(), null, values);
            if (retVal != -1) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.addCycle", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<VslaCycle> getAllCycles() {

        ArrayList<VslaCycle> cycles = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            cycles = new ArrayList<VslaCycle>();
            String columnList = VslaCycleSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s ORDER BY %s DESC", columnList, VslaCycleSchema.getTableName(),
                    VslaCycleSchema.COL_VC_CYCLE_ID);

            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    VslaCycle cycle = new VslaCycle();
                    cycle.setCycleId(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_CYCLE_ID)));
                    cycle.setStartDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_START_DATE))));
                    cycle.setEndDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_END_DATE))));
                    cycle.setInterestRate(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_RATE)));
                    cycle.setSharePrice(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARE_PRICE)));
                    cycle.setMaxSharesQty(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_SHARE_QTY)));
                    cycle.setMaxStartShare(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_START_SHARE)));
                    if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ACTIVE)) == 1) {
                        cycle.activate();
                    }
                    else {
                        cycle.deactivate();
                    }

                    if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ENDED)) == 1) {
                        Date dateEnded = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_DATE_ENDED)));
                        double sharedAmount = cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARED_AMOUNT));
                        cycle.end(dateEnded,sharedAmount);
                    }

                    cycles.add(cycle);

                } while (cursor.moveToNext());
            }

            // return the list
            return cycles;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getAllCycles", ex.getMessage());
            return null;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    //Return cycles that are still active
    public ArrayList<VslaCycle> getActiveCycles() {
        ArrayList<VslaCycle> activeCycles = null;

        try {
            activeCycles = new ArrayList<VslaCycle>();
            for(VslaCycle cycle: getAllCycles()) {
                if(cycle.isActive() && !cycle.isEnded()) {
                    activeCycles.add(cycle);
                }
            }
        }
        catch(Exception ex) {
            Log.e("VslaCycleRepo.getActiveCycles", ex.getMessage());
        }

        return activeCycles;
    }

    // Getting single Cycle
    public VslaCycle getCycle(int cycleId) {

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            cursor = db.query(VslaCycleSchema.getTableName(), VslaCycleSchema.getColumnListArray(),
                    VslaCycleSchema.COL_VC_CYCLE_ID + "=?",
                    new String[] { String.valueOf(cycleId) }, null, null, null, null);

            // Determine whether there was data
            if (cursor == null)
            {
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }

            VslaCycle cycle = new VslaCycle();
            cycle.setCycleId(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_CYCLE_ID)));
            cycle.setStartDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_START_DATE))));
            cycle.setEndDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_END_DATE))));
            cycle.setInterestRate(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_RATE)));
            cycle.setSharePrice(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARE_PRICE)));
            cycle.setMaxSharesQty(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_SHARE_QTY)));
            cycle.setMaxStartShare(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_START_SHARE)));
            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ACTIVE)) == 1) {
                cycle.activate();
            }
            else {
                cycle.deactivate();
            }

            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ENDED)) == 1) {
                Date dateEnded = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_DATE_ENDED)));
                double sharedAmount = cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARED_AMOUNT));
                cycle.end(dateEnded,sharedAmount);
            }

            // return data
            return cycle;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getCycle", ex.getMessage());
            return null;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    // Getting the Current Cycle
    public VslaCycle getCurrentCycle() {

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            cursor = db.query(VslaCycleSchema.getTableName(), VslaCycleSchema.getColumnListArray(),
                    VslaCycleSchema.COL_VC_IS_ACTIVE + "=?",
                    new String[] { String.valueOf(1) }, null, null, null, null);

            // Determine whether there was data
            if (cursor == null)
            {
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }

            VslaCycle cycle = new VslaCycle();
            cycle.setCycleId(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_CYCLE_ID)));
            cycle.setStartDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_START_DATE))));
            cycle.setEndDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_END_DATE))));
            cycle.setInterestRate(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_RATE)));
            cycle.setSharePrice(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARE_PRICE)));
            cycle.setMaxSharesQty(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_SHARE_QTY)));
            cycle.setMaxStartShare(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_START_SHARE)));
            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ACTIVE)) == 1) {
                cycle.activate();
            }
            else {
                cycle.deactivate();
            }

            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ENDED)) == 1) {
                Date dateEnded = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_DATE_ENDED)));
                double sharedAmount = cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARED_AMOUNT));
                cycle.end(dateEnded,sharedAmount);
            }

            // return data
            return cycle;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getCycle", ex.getMessage());
            return null;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    // Getting the Current Cycle
    public VslaCycle getMostRecentCycle() {

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1",
                    VslaCycleSchema.getColumnList(), VslaCycleSchema.getTableName(), VslaCycleSchema.COL_VC_CYCLE_ID);
            cursor = db.rawQuery(selectQuery, null);

            // Determine whether there was data
            if (cursor == null)
            {
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }

            VslaCycle cycle = new VslaCycle();
            cycle.setCycleId(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_CYCLE_ID)));
            cycle.setStartDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_START_DATE))));
            cycle.setEndDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_END_DATE))));
            cycle.setInterestRate(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_RATE)));
            cycle.setSharePrice(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARE_PRICE)));
            cycle.setMaxSharesQty(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_SHARE_QTY)));
            cycle.setMaxStartShare(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_START_SHARE)));
            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ACTIVE)) == 1) {
                cycle.activate();
            }
            else {
                cycle.deactivate();
            }

            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ENDED)) == 1) {
                Date dateEnded = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_DATE_ENDED)));
                double sharedAmount = cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARED_AMOUNT));
                cycle.end(dateEnded,sharedAmount);
            }

            // return data
            return cycle;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getMostRecentCycle", ex.getMessage());
            return null;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }



    public boolean updateCycle(VslaCycle cycle) {
        SQLiteDatabase db = null;

        try {
            if(cycle == null) {
                return false;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            if(cycle.getStartDate() == null) {
                cycle.setStartDate(new Date());
            }
            values.put(VslaCycleSchema.COL_VC_START_DATE, Utils.formatDateToSqlite(cycle.getStartDate()));
            if(cycle.getEndDate() == null) {
                cycle.setEndDate(new Date());
            }
            values.put(VslaCycleSchema.COL_VC_END_DATE, Utils.formatDateToSqlite(cycle.getEndDate()));
            values.put(VslaCycleSchema.COL_VC_INTEREST_RATE, cycle.getInterestRate());
            values.put(VslaCycleSchema.COL_VC_MAX_SHARE_QTY, cycle.getMaxSharesQty());
            values.put(VslaCycleSchema.COL_VC_MAX_START_SHARE, cycle.getMaxStartShare());
            values.put(VslaCycleSchema.COL_VC_SHARE_PRICE, cycle.getSharePrice());
            values.put(VslaCycleSchema.COL_VC_IS_ACTIVE, (cycle.isActive()) ? 1 : 0);
            values.put(VslaCycleSchema.COL_VC_IS_ENDED, (cycle.isEnded()) ? 1 : 0);

            //if dateEnded is Null use the current date
            if(cycle.getDateEnded() == null) {
                Calendar c = Calendar.getInstance();
                values.put(VslaCycleSchema.COL_VC_DATE_ENDED, Utils.formatDateToSqlite(c.getTime()));
            }
            else {
                values.put(VslaCycleSchema.COL_VC_DATE_ENDED, Utils.formatDateToSqlite(cycle.getDateEnded()));
            }

            // updating row
            int retVal = db.update(VslaCycleSchema.getTableName(), values, VslaCycleSchema.COL_VC_CYCLE_ID + " = ?",
                    new String[] { String.valueOf(cycle.getCycleId()) });

            if (retVal > 0) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("VslaCycleSchema.updateCycle", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Deleting single VslaCycle
    public void deleteCycle(VslaCycle cycle) {
        SQLiteDatabase db = null;

        try {
            if(cycle == null) {
                return;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // To remove all rows and get a count pass "1" as the whereClause.
            db.delete(VslaCycleSchema.getTableName(), VslaCycleSchema.COL_VC_CYCLE_ID + " = ?",
                    new String[] { String.valueOf(cycle.getCycleId()) });
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.deleteCycle", ex.getMessage());
            return;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public int getCyclesCount() {

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            String countQuery = "SELECT  * FROM " + VslaCycleSchema.getTableName();
            cursor = db.rawQuery(countQuery, null);

            // return count
            return cursor.getCount();
        }
        catch (Exception ex) {
            Log.e("VslaCycleSchema.CyclesCount", ex.getMessage());
            return 0;
        }
        finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public boolean activateCycle(VslaCycle cycle) {
        if(cycle != null && cycle.getCycleId()>0) {
            //Deactivate all
            //for(VslaCycle vslaCycle: vslaCycles) {
            //    vslaCycle.deactivate();
            //}

            //Activate the target
            cycle.activate();
            return true;
        }
        else {
            return false;
        }
    }




    public boolean updateMiddleStartCycle(VslaMiddleStartCycle cycle) {
        /*
        Method to update middle start cycle
        For the getting started wizard
        TO DO:
        */

        //Add the cycle...

        return false;
    }


    public boolean addMiddleStartCycle(VslaMiddleStartCycle cycle) {
        /*
        Method to add middle start cycle
        For the getting started wizard
        TO DO:
        */
        boolean returnVal = addCycle(cycle);
        if(! returnVal)
        {
            //return;
            return returnVal;
        }

        //Create a dummy meeting to asisst with storing the interest collected and fines information
        MeetingRepo repo = new MeetingRepo(context);
        Meeting dummyMeeting = new Meeting();

        //Load recently added cycle from db
        VslaCycle mostRecentCycle = getMostRecentCycle();
        dummyMeeting.setVslaCycle(mostRecentCycle);

        //Set the meeting date as the start date of the cycle
        dummyMeeting.setMeetingDate(mostRecentCycle.getStartDate());

        //Add this meeting
        returnVal = repo.addMeeting(dummyMeeting);
        if(returnVal)
        {
            return returnVal;
        }

        //Load the most recent meeting now and set the fines
        dummyMeeting = repo.getMostRecentMeeting();
        dummyMeeting.setFines(cycle.getFinesCollected());
        returnVal = repo.updateOpeningCash(dummyMeeting.getMeetingId(), 0, 0, dummyMeeting.getFines());


        return returnVal;
    }


}