package io.intelehealth.client.views.activites;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.intelehealth.client.R;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dao.PatientsDAO;
import io.intelehealth.client.databinding.ActivityPatientDetailBinding;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.utilities.DateAndTimeUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.viewModels.PatientDetailViewModel;
import io.intelehealth.client.viewModels.requestModels.Patient;

public class PatientDetailActivity extends AppCompatActivity {
    private static final String TAG = PatientDetailActivity.class.getSimpleName();
    ActivityPatientDetailBinding binding;
    String patientName;
    String visitUuid;
    String patientUuid;
    String intentTag = "";
    SessionManager sessionManager = null;
    Patient patient_new = new Patient();
    PatientsDAO patientsDAO = new PatientsDAO();
    PatientDetailViewModel patientDetailViewModel;
    private boolean hasLicense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_patient_detail);
        patientDetailViewModel = ViewModelProviders.of(this).get(PatientDetailViewModel.class);
        binding.setPatientdetailViemodel(patientDetailViewModel);
        binding.setLifecycleOwner(this);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        sessionManager = new SessionManager(this);
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            patientName = intent.getStringExtra("patientName");

            intentTag = intent.getStringExtra("tag");
            Logger.logD(TAG, "Patient ID: " + patientUuid);
            Logger.logD(TAG, "Patient Name: " + patientName);
            Logger.logD(TAG, "Intent Tag: " + intentTag);
        }

        setDisplay(patientUuid);


    }

    public void setDisplay(String dataString) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();

        String patientSelection = "uuid = ?";
        String[] patientArgs = {dataString};
        String[] patientColumns = {"uuid", "openmrs_id", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw",
                "patient_photo"};
        final Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patient_new.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                patient_new.setOpenmrs_id(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")));
                patient_new.setFirst_name(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patient_new.setMiddle_name(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patient_new.setLast_name(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patient_new.setDate_of_birth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patient_new.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patient_new.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patient_new.setCity_village(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patient_new.setState_province(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patient_new.setPostal_code(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patient_new.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patient_new.setPhone_number(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patient_new.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {dataString};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        final Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    e.printStackTrace();
                }

                if (name.equalsIgnoreCase("caste")) {
                    patient_new.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patient_new.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patient_new.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patient_new.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patient_new.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patient_new.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

//changing patient to patient_new object
        if (patient_new.getMiddle_name() == null) {
            patientName = patient_new.getLast_name() + ", " + patient_new.getFirst_name();
        } else {
            patientName = patient_new.getLast_name() + ", " + patient_new.getFirst_name() + " " + patient_new.getMiddle_name();
        }
        setTitle(patientName);

//        if (patient.getPatientPhoto() != null && patient.getPatientPhoto() != "") {
//            File image = new File(patient.getPatientPhoto());
//            Glide.with(this)
//                    .load(image)
//                    .thumbnail(0.3f)
//                    .centerCrop()
//                    .into(photoView);
//        }

        if (patient_new.getOpenmrs_id() != null && !patient_new.getOpenmrs_id().isEmpty()) {
            binding.textViewID.setText(patient_new.getOpenmrs_id());
        } else {
            binding.textViewID.setText(getString(R.string.patient_not_registered));
        }

        int age = DateAndTimeUtils.getAge(patient_new.getDate_of_birth());
        binding.textViewAge.setText(String.valueOf(age));

        String dob = patient_new.getDate_of_birth();
        binding.textViewDOB.setText(dob);
        if (patient_new.getAddress1() == null || patient_new.getAddress1().equals("")) {
            binding.textViewAddress1.setVisibility(View.GONE);
        } else {
            binding.textViewAddress1.setText(patient_new.getAddress1());
        }
        if (patient_new.getAddress2() == null || patient_new.getAddress2().equals("")) {
            binding.textViewAddress2.setVisibility(View.GONE);
        } else {
            binding.textViewAddress2.setText(patient_new.getAddress2());
        }
        String city_village;
        if (patient_new.getCity_village() != null) {
            city_village = patient_new.getCity_village().trim();
        } else {
            city_village = "";
        }

        String postal_code;
        if (patient_new.getPostal_code() != null) {
            postal_code = patient_new.getPostal_code().trim() + ",";
        } else {
            postal_code = "";
        }

        String addrFinalLine =
                String.format("%s, %s, %s %s",
                        city_village, patient_new.getState_province(),
                        postal_code, patient_new.getCountry());
        binding.textViewAddressFinal.setText(addrFinalLine);
        binding.textViewPhone.setText(patient_new.getPhone_number());
        binding.textViewEducationStatus.setText(patient_new.getEducation_level());
        binding.textViewEconomicStatus.setText(patient_new.getEconomic_status());
        binding.textViewCaste.setText(patient_new.getCaste());
//
        if (patient_new.getSdw() != null && !patient_new.getSdw().equals("")) {
            binding.textViewSDW.setText(patient_new.getSdw());
        } else {
            binding.textViewSDW.setVisibility(View.GONE);
        }
//
        if (patient_new.getOccupation() != null && !patient_new.getOccupation().equals("")) {
            binding.textViewOccupation.setText(patient_new.getOccupation());
        } else {
            binding.textViewOccupation.setVisibility(View.GONE);
        }

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

}
