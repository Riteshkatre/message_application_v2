package com.example.massageapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class ScheduleDialog extends DialogFragment {
    RadioButton radioOption1, radioOption2, radioOption3, radioOption4;
    CardView btnCancel;


    @NonNull
    @Override

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_schedule_dialog);

        setCancelable(false);


        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // Initialize radio buttons
        radioOption1 = dialog.findViewById(R.id.radioOption1);
        radioOption2 = dialog.findViewById(R.id.radioOption2);
        radioOption3 = dialog.findViewById(R.id.radioOption3);
        radioOption4 = dialog.findViewById(R.id.radioOption4);
        btnCancel = dialog.findViewById(R.id.btnCancel);

        View.OnClickListener radioClickListener = v -> {
            radioOption1.setChecked(v == radioOption1);
            radioOption2.setChecked(v == radioOption2);
            radioOption3.setChecked(v == radioOption3);
            radioOption4.setChecked(v == radioOption4);

            // Open Date and Time Picker when radioOption4 is selected
            if (v == radioOption4) {
                openDateTimePicker();
            }
        };

        radioOption1.setOnClickListener(radioClickListener);
        radioOption2.setOnClickListener(radioClickListener);
        radioOption3.setOnClickListener(radioClickListener);
        radioOption4.setOnClickListener(radioClickListener);

        btnCancel.setOnClickListener(v -> dismiss());


        return dialog;
    }

    private void openDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Open DatePicker first
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;

                    // After selecting date, open TimePicker
                    openTimePicker(selectedDate);

                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void openTimePicker(String selectedDate) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Open TimePicker
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = selectedHour + ":" + String.format("%02d", selectedMinute);
                    String dateTime = selectedDate + " " + selectedTime;

                    // Update radioOption4 text with selected date and time
                    radioOption4.setText(dateTime);

                    // Optional: Show a toast message with selected date and time
                    Toast.makeText(getActivity(), "Selected: " + dateTime, Toast.LENGTH_SHORT).show();

                },
                hour, minute, false // false for 12-hour format, true for 24-hour format
        );

        timePickerDialog.show();
    }
}
