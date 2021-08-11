package com.unipi.marioschr.navscan;

import android.app.DatePickerDialog;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;


public class RegisterFragment extends Fragment implements View.OnClickListener {
    TextInputEditText tietBirthday;
    TextInputLayout tilBirthday;
    Button btnSignUp;
    DatePickerDialog datePickerDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tietBirthday = view.findViewById(R.id.tietBirthday);
        tilBirthday = view.findViewById(R.id.tilBirthday);
        btnSignUp = view.findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(this);
        tietBirthday.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                openDatePicker();
            } else {
                hideDatePicker();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignUp:
                signUp();
                break;
        }
    }

    private void signUp() {
        Toast.makeText(getContext(), String.valueOf(tietBirthday.getText()), Toast.LENGTH_SHORT).show();
    }

    private void openDatePicker() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        if (String.valueOf(tietBirthday.getText()).equals("")) {
            datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
                tietBirthday.setText(String.format("%02d",dayOfMonth)
                        + "-" + String.format("%02d",monthOfYear + 1)
                        + "-" + String.format("%02d",year));
                tietBirthday.clearFocus();
            }, mYear, mMonth, mDay);
        } else {
            LocalDate localDate = LocalDate.parse(String.valueOf(tietBirthday.getText()), DateTimeFormatter.ofPattern("d-M-yyyy"));
            datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
                tietBirthday.setText(String.format("%02d",dayOfMonth)
                        + "-" + String.format("%02d",monthOfYear + 1)
                        + "-" + String.format("%02d",year));
                tietBirthday.clearFocus();
            }, localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
        }
        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        Calendar minYear = Calendar.getInstance();
        minYear.add(Calendar.YEAR, -110);
        datePickerDialog.getDatePicker().setMinDate(minYear.getTimeInMillis());
        datePickerDialog.setOnCancelListener(l -> tietBirthday.clearFocus());
        datePickerDialog.show();
    }

    private void hideDatePicker() {
        datePickerDialog.hide();
    }
}