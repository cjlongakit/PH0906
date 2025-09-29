package com.example.ph906_spalshscreen.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.ph906_spalshscreen.PrefsHelper;
import com.example.ph906_spalshscreen.R;
import com.example.ph906_spalshscreen.api.ApiCallback;
import com.example.ph906_spalshscreen.api.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private TextView tvUserId;
    private ImageView imgProfileEdit;
    private Button btnChoosePhoto, btnTakePhoto;
    private EditText etFirstName, etLastName, etBirthdate, etSex, etAge, etNickname,
            etMobileNumber, etAddress, etCaseworkerAssigned, etGuardianName, etGuardianMobile,
            etWaterBaptized, etTeacher;
    private Button btnSave, btnCancel;

    private String ph906;
    private RequestQueue queue;
    private PrefsHelper prefsHelper;

    private Uri pendingPhotoUri = null; // staged photo until user taps Save
    private Uri cameraTempUri = null;
    private boolean isPhotoChanged = false;

    private ApiClient apiClient;

    // Launcher for picking image from gallery
    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        // Persist read permission for the picked image
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                        pendingPhotoUri = uri;
                        imgProfileEdit.setImageURI(uri);
                        isPhotoChanged = true;
                        // Save immediately so other screens see it
                        if (prefsHelper != null) {
                            prefsHelper.saveLocalPhotoUri(uri.toString());
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to access selected photo", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // Launcher for taking picture with camera
    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (Boolean.TRUE.equals(success) && cameraTempUri != null) {
                    pendingPhotoUri = cameraTempUri;
                    imgProfileEdit.setImageURI(cameraTempUri);
                    isPhotoChanged = true;
                    // Save immediately so other screens see it
                    if (prefsHelper != null) {
                        prefsHelper.saveLocalPhotoUri(cameraTempUri.toString());
                    }
                }
            });

    // Launcher for requesting permissions
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                for (Boolean isGranted : permissions.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    // Permissions granted, proceed with photo operations
                    handlePhotoOperation();
                } else {
                    Toast.makeText(this, "Permissions required to handle photos", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        apiClient = new ApiClient(this);
        prefsHelper = new PrefsHelper(this);
        queue = Volley.newRequestQueue(this);

        initializeViews();
        setupListeners();
        loadExistingData();
    }

    private void initializeViews() {
        tvUserId = findViewById(R.id.tv_user_id);
        imgProfileEdit = findViewById(R.id.img_profile_edit);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);

        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etBirthdate = findViewById(R.id.et_birthdate);
        etSex = findViewById(R.id.et_sex);
        etAge = findViewById(R.id.et_age);
        etNickname = findViewById(R.id.et_nickname);
        etMobileNumber = findViewById(R.id.et_mobile_number);
        etAddress = findViewById(R.id.et_address);
        etCaseworkerAssigned = findViewById(R.id.et_caseworker_assigned);
        etGuardianName = findViewById(R.id.et_guardian_name);
        etGuardianMobile = findViewById(R.id.et_guardian_mobile);
        etWaterBaptized = findViewById(R.id.et_water_baptized);
        etTeacher = findViewById(R.id.et_teacher);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupListeners() {
        btnChoosePhoto.setOnClickListener(v -> checkPermissionsAndProceed("gallery"));
        btnTakePhoto.setOnClickListener(v -> checkPermissionsAndProceed("camera"));
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveAllChanges());
    }

    private void loadExistingData() {
        // Load existing profile photo if any
        ph906 = getIntent().getStringExtra("ph906Id");
        if (ph906 == null) ph906 = "";
        tvUserId.setText(formatPh906(ph906));

        String photoUrl = prefsHelper.getProfilePhotoUri();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            try {
                // Show last saved server url; use Glide to handle http/https
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.account_circle)
                        .error(R.drawable.account_circle)
                        .into(imgProfileEdit);
            } catch (Exception ignored) {}
        }

        // Prefill form fields (if Extras were provided by caller)
        etFirstName.setText(getIntent().getStringExtra("firstName"));
        etLastName.setText(getIntent().getStringExtra("lastName"));
        etBirthdate.setText(getIntent().getStringExtra("birthdate"));
        etNickname.setText(getIntent().getStringExtra("nickname"));
        etMobileNumber.setText(getIntent().getStringExtra("mobile"));
        etAddress.setText(getIntent().getStringExtra("address"));
        etGuardianName.setText(getIntent().getStringExtra("guardian"));
        etGuardianMobile.setText(getIntent().getStringExtra("guardianMobile"));
        etWaterBaptized.setText(getIntent().getStringExtra("baptized"));
        etTeacher.setText(getIntent().getStringExtra("teacher"));
        etSex.setText(getIntent().getStringExtra("sex"));
        etAge.setText(getIntent().getStringExtra("age"));
        etCaseworkerAssigned.setText(getIntent().getStringExtra("caseworker"));
    }

    private void checkPermissionsAndProceed(String action) {
        List<String> permissions = new ArrayList<>();

        if ("camera".equals(action)) {
            permissions.add(Manifest.permission.CAMERA);
        }

        // For Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            // For Android 12 and below
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // Check if we have all permissions
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            if ("camera".equals(action)) {
                launchCamera();
            } else {
                openDocumentLauncher.launch(new String[]{"image/*"});
            }
        } else {
            permissionLauncher.launch(permissions.toArray(new String[0]));
        }
    }

    private void handlePhotoOperation() {
        if (btnTakePhoto.isPressed()) {
            launchCamera();
        } else {
            openDocumentLauncher.launch(new String[]{"image/*"});
        }
    }

    private void launchCamera() {
        try {
            File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (picturesDir == null) {
                Toast.makeText(this, "Storage not available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a unique filename using timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File photoFile = new File(picturesDir, "IMG_" + timeStamp + ".jpg");

            // Get URI using FileProvider
            cameraTempUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);

            takePictureLauncher.launch(cameraTempUri);
        } catch (Exception e) {
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String formatPh906(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("[^0-9]", "");
        return "PH906-" + digits;
    }

    private void saveAllChanges() {
        if (isPhotoChanged && pendingPhotoUri != null) {
            // Upload photo first, then save profile
            btnSave.setEnabled(false); // Prevent double-submission
            Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show();

            apiClient.uploadProfilePhoto(pendingPhotoUri, new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    // Save the photo URL from response
                    String photoUrl = response.optString("url");
                    if (!photoUrl.isEmpty()) {
                        prefsHelper.saveServerPhotoUrl(photoUrl);

                        // Show the server image now (bypass cache once)
                        Glide.with(EditProfileActivity.this)
                                .load(photoUrl + (photoUrl.contains("?") ? "&" : "?") + "t=" + System.currentTimeMillis())
                                .placeholder(R.drawable.account_circle)
                                .error(R.drawable.account_circle)
                                .into(imgProfileEdit);
                    }

                    // Now save the rest of the profile
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Photo uploaded", Toast.LENGTH_SHORT).show();
                        saveProfile();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this,
                                "Photo upload failed: " + message, Toast.LENGTH_LONG).show();
                        btnSave.setEnabled(true);
                        // Proceed with saving other changes anyway
                        saveProfile();
                    });
                }
            });
        } else {
            // No photo change, just save profile
            saveProfile();
        }
    }

    private void saveProfile() {
        try {
            JSONObject payload = new JSONObject();
            payload.put("first_name", etFirstName.getText().toString().trim());
            payload.put("last_name", etLastName.getText().toString().trim());
            payload.put("sex", etSex.getText().toString().trim());
            payload.put("birthday", etBirthdate.getText().toString().trim());
            payload.put("age", etAge.getText().toString().trim());
            payload.put("caseworker_assigned", etCaseworkerAssigned.getText().toString().trim());
            payload.put("teacher", etTeacher.getText().toString().trim());
            payload.put("nickname", etNickname.getText().toString().trim());
            payload.put("mobile_number", etMobileNumber.getText().toString().trim());
            payload.put("address", etAddress.getText().toString().trim());
            payload.put("guardian_name", etGuardianName.getText().toString().trim());
            payload.put("guardian_mobile", etGuardianMobile.getText().toString().trim());
            payload.put("water_baptized", etWaterBaptized.getText().toString().trim());

            btnSave.setEnabled(false);

            // Route through ApiClient (which has endpoint fallback logic)
            apiClient.updateMyProfile(payload, new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Save failed: " + message, Toast.LENGTH_LONG).show();
                        btnSave.setEnabled(true);
                    });
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to prepare data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (queue != null) {
            queue.cancelAll(request -> true);
        }
    }
}

