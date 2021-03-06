package org.applab.digitizingdata.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.digitizingdata.datatransformation.RepaymentDataTransferRecord;
import org.applab.digitizingdata.datatransformation.SavingsDataTransferRecord;
import org.applab.digitizingdata.domain.schema.LoanIssueSchema;
import org.applab.digitizingdata.domain.schema.LoanRepaymentSchema;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.domain.schema.SavingSchema;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.MemberLoanRepaymentRecord;
import org.applab.digitizingdata.helpers.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 7/9/13.
 */
public class MeetingLoanRepaymentRepo {
    private Context context;
    public MeetingLoanRepaymentRepo() {

    }

    public MeetingLoanRepaymentRepo(Context context){
        this.context = context;
    }

    public double getTotalLoansRepaidInCycle(int cycleId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loansRepaid = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalRepayments FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    LoanRepaymentSchema.COL_LR_AMOUNT, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID,cycleId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loansRepaid = cursor.getDouble(cursor.getColumnIndex("TotalRepayments"));
            }

            return loansRepaid;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getTotalLoansRepaidInCycle", ex.getMessage());
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

    public double getTotalLoansRepaidInMeeting(int meetingId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loansRepaid = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalRepayments FROM %s WHERE %s=%d",
                    LoanRepaymentSchema.COL_LR_AMOUNT, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loansRepaid = cursor.getDouble(cursor.getColumnIndex("TotalRepayments"));
            }

            return loansRepaid;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getTotalLoansRepaidInMeeting", ex.getMessage());
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

    public double getTotalRepaymentByMemberInMeeting(int meetingId, int memberId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loansRepaid = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalRepayments FROM %s WHERE %s=%d AND %s=%d",
                    LoanRepaymentSchema.COL_LR_AMOUNT, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId,
                    LoanRepaymentSchema.COL_LR_MEMBER_ID, memberId
            );
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loansRepaid = cursor.getDouble(cursor.getColumnIndex("TotalRepayments"));
            }

            return loansRepaid;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getTotalRepaymentByMemberInMeeting", ex.getMessage());
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

    public int getMemberRepaymentId(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int repaymentId = 0;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId,
                    LoanRepaymentSchema.COL_LR_MEMBER_ID, memberId, LoanRepaymentSchema.COL_LR_REPAYMENT_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                repaymentId = cursor.getInt(cursor.getColumnIndex(LoanRepaymentSchema.COL_LR_REPAYMENT_ID));
            }
            return repaymentId;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getMemberRepaymentId", ex.getMessage());
            return repaymentId;
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

    public boolean saveMemberLoanRepayment(int meetingId, int memberId, int loanId, double amount,
                                           double balanceBefore, String comments, double balanceAfter,
                                           double interestAmount, double rolloverAmount, Date lastDateDue, Date nextDateDue) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int repaymentId = 0;
        try {
            //Check if exists and do an Update:
            repaymentId = getMemberRepaymentId(meetingId, memberId);
            if(repaymentId > 0) {
                performUpdate = true;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId);
            values.put(LoanRepaymentSchema.COL_LR_MEMBER_ID, memberId);
            values.put(LoanRepaymentSchema.COL_LR_LOAN_ID, loanId);
            values.put(LoanRepaymentSchema.COL_LR_AMOUNT, amount);
            values.put(LoanRepaymentSchema.COL_LR_COMMENTS, comments);
            values.put(LoanRepaymentSchema.COL_LR_INTEREST_AMOUNT, interestAmount);
            values.put(LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT, rolloverAmount);

            //Transaction entry of Balance Before and Balance After: May be best done at database layer with locking and syncing
            values.put(LoanRepaymentSchema.COL_LR_BAL_BEFORE, balanceBefore);
            //Balance after will be the balance upon which the rollover was calculated
            values.put(LoanRepaymentSchema.COL_LR_BAL_AFTER, balanceAfter);

            //The Last Date Due
            Date dtLastDateDue = lastDateDue;
            if(dtLastDateDue == null) {
                Calendar cal = Calendar.getInstance();
                dtLastDateDue = cal.getTime();
            }
            values.put(LoanRepaymentSchema.COL_LR_LAST_DATE_DUE, Utils.formatDateToSqlite(dtLastDateDue));

            //The Next Date Due
            Date dtNextDateDue = nextDateDue;
            if(dtNextDateDue == null) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH,1);
                dtNextDateDue = cal.getTime();
            }
            values.put(LoanRepaymentSchema.COL_LR_NEXT_DATE_DUE, Utils.formatDateToSqlite(dtNextDateDue));

            // Inserting or Updating Row
            long retVal = -1;
            if(performUpdate) {
                // updating row
                retVal = db.update(LoanRepaymentSchema.getTableName(), values, LoanRepaymentSchema.COL_LR_REPAYMENT_ID + " = ?",
                        new String[] { String.valueOf(repaymentId) });
            }
            else {
                retVal = db.insert(LoanRepaymentSchema.getTableName(), null, values);
            }

            if (retVal != -1) {
                //Now update the lastDateDue and nextDateDue
                //boolean retValDates = updateMemberLoanRepaymentDates(meetingId, memberId, lastDateDue, nextDateDue);
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.saveMemberLoanRepayment", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateMemberLoanRepaymentDates(int meetingId, int memberId, Date lastDateDue, Date nextDateDue) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int repaymentId = 0;
        try {
            //Check if exists and do an Update:
            repaymentId = getMemberRepaymentId(meetingId, memberId);
            if(repaymentId > 0) {
                performUpdate = true;
            }
            else {
                return false;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            //The Last Date Due
            Date dtLastDateDue = lastDateDue;
            if(dtLastDateDue == null) {
                Calendar cal = Calendar.getInstance();
                dtLastDateDue = cal.getTime();
            }
            values.put(LoanRepaymentSchema.COL_LR_LAST_DATE_DUE, Utils.formatDateToSqlite(dtLastDateDue));

            //The Next Date Due
            Date dtNextDateDue = nextDateDue;
            if(dtNextDateDue == null) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH,1);
                dtNextDateDue = cal.getTime();
            }
            values.put(LoanRepaymentSchema.COL_LR_NEXT_DATE_DUE, Utils.formatDateToSqlite(dtNextDateDue));

            // Inserting or Updating Row
            long retVal = -1;
            if(performUpdate) {
                // updating row
                retVal = db.update(LoanRepaymentSchema.getTableName(), values, LoanRepaymentSchema.COL_LR_REPAYMENT_ID + " = ?",
                        new String[] { String.valueOf(repaymentId) });
            }

            if (retVal != -1) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.updateMemberLoanRepaymentDates", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }


    //TODO: Update this query to display the added fields
    public ArrayList<MemberLoanRepaymentRecord> getLoansRepaymentsByMemberInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<MemberLoanRepaymentRecord> repayments;

        try {
            repayments = new ArrayList<MemberLoanRepaymentRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  L.%s AS RepaymentId, M.%s AS MeetingDate, L.%s AS Amount, " +
                    "L.%s AS LoanId, L.%s AS RolloverAmount, L.%s AS Comments, LI.%s AS LoanNo" +
                    " FROM %s AS L INNER JOIN %s AS M ON L.%s=M.%s INNER JOIN %s AS LI ON L.%s=LI.%s " +
                    " WHERE L.%s=%d AND L.%s IN (SELECT %s FROM %s WHERE %s=%d) ORDER BY L.%s DESC",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID,MeetingSchema.COL_MT_MEETING_DATE, LoanRepaymentSchema.COL_LR_AMOUNT,
                    LoanRepaymentSchema.COL_LR_LOAN_ID, LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT, LoanRepaymentSchema.COL_LR_COMMENTS,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanRepaymentSchema.getTableName(), MeetingSchema.getTableName(), LoanRepaymentSchema.COL_LR_MEETING_ID,MeetingSchema.COL_MT_MEETING_ID,
                    LoanIssueSchema.getTableName(), LoanRepaymentSchema.COL_LR_LOAN_ID, LoanIssueSchema.COL_LI_LOAN_ID, LoanRepaymentSchema.COL_LR_MEMBER_ID,memberId,
                    LoanRepaymentSchema.COL_LR_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID, MeetingSchema.getTableName(),MeetingSchema.COL_MT_CYCLE_ID, cycleId,
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID
            );
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MemberLoanRepaymentRecord repaymentRecord = new MemberLoanRepaymentRecord();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                    repaymentRecord.setMeetingDate(meetingDate);
                    repaymentRecord.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                    repaymentRecord.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                    repaymentRecord.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                    repaymentRecord.setRolloverAmount(cursor.getDouble(cursor.getColumnIndex("RolloverAmount")));
                    repaymentRecord.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
                    repaymentRecord.setRepaymentId(cursor.getInt(cursor.getColumnIndex("RepaymentId")));

                    repayments.add(repaymentRecord);
                } while (cursor.moveToNext());
            }
            return repayments;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getLoansRepaymentsByMemberInCycle", ex.getMessage());
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

    public MemberLoanRepaymentRecord getLoansRepaymentByMemberInMeeting(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MemberLoanRepaymentRecord repaymentRecord = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  LR.%s AS RepaymentId, M.%s AS MeetingDate, LR.%s AS Amount, " +
                    "LR.%s AS LoanId, LR.%s AS RolloverAmount, LR.%s AS Comments, LI.%s AS LoanNo, LR.%s AS BalanceBefore, " +
                    "LR.%s AS BalanceAfter, LR.%s AS InterestAmount, LR.%s AS LastDateDue , LR.%s AS NextDateDue " +
                    " FROM %s AS LR INNER JOIN %s AS M ON LR.%s=M.%s INNER JOIN %s AS LI ON LR.%s=LI.%s " +
                    " WHERE LR.%s=%d AND LR.%s=%d ORDER BY LR.%s DESC LIMIT 1",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID,MeetingSchema.COL_MT_MEETING_DATE, LoanRepaymentSchema.COL_LR_AMOUNT,
                    LoanRepaymentSchema.COL_LR_LOAN_ID, LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT, LoanRepaymentSchema.COL_LR_COMMENTS,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanRepaymentSchema.COL_LR_BAL_BEFORE, LoanRepaymentSchema.COL_LR_BAL_AFTER,
                    LoanRepaymentSchema.COL_LR_INTEREST_AMOUNT, LoanRepaymentSchema.COL_LR_LAST_DATE_DUE, LoanRepaymentSchema.COL_LR_NEXT_DATE_DUE,
                    LoanRepaymentSchema.getTableName(), MeetingSchema.getTableName(), LoanRepaymentSchema.COL_LR_MEETING_ID,MeetingSchema.COL_MT_MEETING_ID,
                    LoanIssueSchema.getTableName(), LoanRepaymentSchema.COL_LR_LOAN_ID, LoanIssueSchema.COL_LI_LOAN_ID, LoanRepaymentSchema.COL_LR_MEMBER_ID,memberId,
                    LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId, LoanRepaymentSchema.COL_LR_REPAYMENT_ID
            );
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {

                repaymentRecord = new MemberLoanRepaymentRecord();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                repaymentRecord.setMeetingDate(meetingDate);
                repaymentRecord.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                repaymentRecord.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                repaymentRecord.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                repaymentRecord.setRolloverAmount(cursor.getDouble(cursor.getColumnIndex("RolloverAmount")));
                repaymentRecord.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
                repaymentRecord.setRepaymentId(cursor.getInt(cursor.getColumnIndex("RepaymentId")));
                if(!cursor.isNull(cursor.getColumnIndex("LastDateDue"))){
                    Date lastDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("LastDateDue")));
                    repaymentRecord.setLastDateDue(lastDateDue);
                }
                if(!cursor.isNull(cursor.getColumnIndex("NextDateDue"))){
                    Date nextDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("NextDateDue")));
                    repaymentRecord.setNextDateDue(nextDateDue);
                }
                repaymentRecord.setBalanceBefore(cursor.getDouble(cursor.getColumnIndex("BalanceBefore")));
                repaymentRecord.setBalanceAfter(cursor.getDouble(cursor.getColumnIndex("BalanceAfter")));
                repaymentRecord.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

            }
            return repaymentRecord;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getLoansRepaymentsByMemberInMeeting", ex.getMessage());
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

    public ArrayList<RepaymentDataTransferRecord> getMeetingRepaymentsForAllMembers(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<RepaymentDataTransferRecord> repayments;

        try {
            repayments = new ArrayList<RepaymentDataTransferRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s AS RepaymentId, %s AS MemberId, %s AS LoanId, %s AS Amount, " +
                    " %s AS BalanceBefore, %s AS BalanceAfter, %s AS InterestAmount, %s AS RollOverAmount, " +
                    " %s AS LastDateDue, %s AS NextDateDue, %s AS Comments " +
                    " FROM %s WHERE %s=%d ORDER BY %s",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID, LoanRepaymentSchema.COL_LR_MEMBER_ID, LoanRepaymentSchema.COL_LR_LOAN_ID,
                    LoanRepaymentSchema.COL_LR_AMOUNT, LoanRepaymentSchema.COL_LR_BAL_BEFORE, LoanRepaymentSchema.COL_LR_BAL_AFTER,
                    LoanRepaymentSchema.COL_LR_INTEREST_AMOUNT, LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT,
                    LoanRepaymentSchema.COL_LR_LAST_DATE_DUE, LoanRepaymentSchema.COL_LR_NEXT_DATE_DUE,LoanRepaymentSchema.COL_LR_COMMENTS,
                    LoanRepaymentSchema.getTableName(), LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId, LoanRepaymentSchema.COL_LR_REPAYMENT_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    RepaymentDataTransferRecord repaymentRecord = new RepaymentDataTransferRecord();
                    repaymentRecord.setMeetingId(meetingId);
                    repaymentRecord.setMemberId(cursor.getInt(cursor.getColumnIndex("MemberId")));
                    repaymentRecord.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                    repaymentRecord.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                    repaymentRecord.setRollOverAmount(cursor.getDouble(cursor.getColumnIndex("RollOverAmount")));
                    repaymentRecord.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
                    repaymentRecord.setRepaymentId(cursor.getInt(cursor.getColumnIndex("RepaymentId")));
                    if(!cursor.isNull(cursor.getColumnIndex("LastDateDue"))){
                        Date lastDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("LastDateDue")));
                        repaymentRecord.setLastDateDue(lastDateDue);
                    }
                    if(!cursor.isNull(cursor.getColumnIndex("NextDateDue"))){
                        Date nextDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("NextDateDue")));
                        repaymentRecord.setNextDateDue(nextDateDue);
                    }
                    repaymentRecord.setBalanceBefore(cursor.getDouble(cursor.getColumnIndex("BalanceBefore")));
                    repaymentRecord.setBalanceAfter(cursor.getDouble(cursor.getColumnIndex("BalanceAfter")));
                    repaymentRecord.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

                    repayments.add(repaymentRecord);

                } while (cursor.moveToNext());
            }
            return repayments;
        }
        catch (Exception ex) {
            Log.e("MeetingSavingRepo.getMeetingRepaymentsForAllMembers", ex.getMessage());
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
}
