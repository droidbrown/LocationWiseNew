package com.hashbrown.erebor.locationwisenew.datetimepicker;

/**
 * Created by hashbrown systems on 5/26/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TimePicker;


import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.utils.ViewUtils;

import java.util.Date;

public class DateTimePicker extends DialogFragment {
    public static final String TAG_FRAG_DATE_TIME = "fragDateTime";
    private static final String KEY_DIALOG_TITLE = "dialogTitle";
    private static final String KEY_INIT_DATE = "initDate";
    private static final String TAG_DATE = "date";
    private static final String TAG_TIME = "time";
    private Context mContext;
    private ButtonClickListener mButtonClickListener;
    private OnDateTimeSetListener mOnDateTimeSetListener;
    private Bundle mArgument;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    Date dateFrom = new Date(System.currentTimeMillis());

    // DialogFragment constructor must be empty
    public DateTimePicker() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mButtonClickListener = new ButtonClickListener();
    }

    /**
     * @param dialogTitle Title of the DateTimePicker DialogFragment
     * @param initDate    Initial Date and Time set to the Date and Time Picker
     * @return Instance of the DateTimePicker DialogFragment
     */
    public static DateTimePicker newInstance(CharSequence dialogTitle, Date initDate) {
        // Create a new instance of DateTimePicker
        DateTimePicker mDateTimePicker = new DateTimePicker();
        // Setup the constructor parameters as arguments
        Bundle mBundle = new Bundle();
        mBundle.putCharSequence(KEY_DIALOG_TITLE, dialogTitle);
        mBundle.putSerializable(KEY_INIT_DATE, initDate);
        mDateTimePicker.setArguments(mBundle);
        // Return instance with arguments
        return mDateTimePicker;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Retrieve Argument passed to the constructor
        mArgument = getArguments();
        //  mDatePicker.setMinDate(System.currentTimeMillis()-1000);
        // Use an AlertDialog Builder to initially create the Dialog
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(mContext);
        }

        // Setup the Dialog
        builder.setTitle(mArgument.getCharSequence(KEY_DIALOG_TITLE));
        builder.setNegativeButton(android.R.string.no, mButtonClickListener);
        builder.setPositiveButton(android.R.string.yes, mButtonClickListener);
        // Create the Alert Dialog
        AlertDialog mDialog = builder.create();
        // Set the View to the Dialog
        mDialog.setView(
                createDateTimeView(mDialog.getLayoutInflater())
        );
        // Return the Dialog created
        return mDialog;
    }

    /**
     * Inflates the XML Layout and setups the tabs
     *
     * @param layoutInflater Layout inflater from the Dialog
     * @return Returns a view that will be set to the Dialog
     */
    private View createDateTimeView(LayoutInflater layoutInflater) {
        // Inflate the XML Layout using the inflater from the created Dialog
        View mView = layoutInflater.inflate(R.layout.date_time_picker, null);
        // Extract the TabHost
        TabHost mTabHost = (TabHost) mView.findViewById(R.id.tab_host);
        mTabHost.setup();
        // Create Date Tab and add to TabHost
        TabHost.TabSpec mDateTab = mTabHost.newTabSpec(TAG_DATE);
        mDateTab.setIndicator("Date");
        mDateTab.setContent(R.id.date_content);
        mTabHost.addTab(mDateTab);
        // Create Time Tab and add to TabHost
        TabHost.TabSpec mTimeTab = mTabHost.newTabSpec(TAG_TIME);
        mTimeTab.setIndicator("Time");
        mTimeTab.setContent(R.id.time_content);
        mTabHost.addTab(mTimeTab);
        // Retrieve Date from Arguments sent to the Dialog
        DateTime mDateTime = new DateTime((Date) mArgument.getSerializable(KEY_INIT_DATE));
        // Initialize Date and Time Pickers
        mDatePicker = (DatePicker) mView.findViewById(R.id.date_picker);
        mTimePicker = (TimePicker) mView.findViewById(R.id.time_picker);
        mDatePicker.init(mDateTime.getYear(), mDateTime.getMonthOfYear(),
                mDateTime.getDayOfMonth(), null);
      //  mDatePicker.setMinDate(dateFrom.getTime());


        if (Build.VERSION.SDK_INT >= 23) {
            mTimePicker.setHour(mDateTime.getHourOfDay());
            mTimePicker.setMinute(mDateTime.getMinuteOfHour());
        } else {
            mTimePicker.setCurrentHour(mDateTime.getHourOfDay());
            mTimePicker.setCurrentMinute(mDateTime.getMinuteOfHour());


        }


        // Return created view
        return mView;
    }

    /**
     * Sets the OnDateTimeSetListener interface
     *
     * @param onDateTimeSetListener Interface that is used to send the Date and Time
     *                              to the calling object
     */
    public void setOnDateTimeSetListener(OnDateTimeSetListener onDateTimeSetListener) {
        mOnDateTimeSetListener = onDateTimeSetListener;
    }

    private class ButtonClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int result) {
            // Determine if the user selected Ok
            if (DialogInterface.BUTTON_POSITIVE == result) {



                if (Build.VERSION.SDK_INT >= 23) {

                    DateTime mDateTime = new DateTime(
                            mDatePicker.getYear(),
                            mDatePicker.getMonth(),
                            mDatePicker.getDayOfMonth(),
                            mTimePicker.getHour(),
                            mTimePicker.getMinute()
                    );
                    mOnDateTimeSetListener.DateTimeSet(mDateTime.getDate());
                   /* if (mTimePicker.getHour() >= dateFrom.getHours() && mTimePicker.getMinute() >= dateFrom.getMinutes()) {
                        mOnDateTimeSetListener.DateTimeSet(mDateTime.getDate());
                    } else {
                        ViewUtils.ShowToast(mContext, "You Cannot Choose Past Time.");
                    }*/
                } else {

                    DateTime mDateTime = new DateTime(
                            mDatePicker.getYear(),
                            mDatePicker.getMonth(),
                            mDatePicker.getDayOfMonth(),
                            mTimePicker.getCurrentHour(),
                            mTimePicker.getCurrentMinute()
                    );

                    mOnDateTimeSetListener.DateTimeSet(mDateTime.getDate());

                   /* if (mTimePicker.getCurrentHour() >= dateFrom.getHours() && mTimePicker.getCurrentMinute() >= dateFrom.getMinutes()) {
                        mOnDateTimeSetListener.DateTimeSet(mDateTime.getDate());
                    } else {
                        ViewUtils.ShowToast(mContext, "You Cannot Choose Past Time.");
                    }*/

                }


            }
        }
    }

    /**
     * Interface for sending the Date and Time to the calling object
     */
    public interface OnDateTimeSetListener {
        void DateTimeSet(Date date);
    }
}
