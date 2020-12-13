package com.shym.petfind;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    private EditText mNameField, mPhoneField, mDescriptionField;

    private Button mBack, mConfirm;

    private ImageView mProfileImage;
    private ImageView[] extraImages;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId, name, phone, description, profileImageUrl, userSex;

    String[] cities = {"Kyiv", "Lviv", "Kharkiv", "Dnipro", "Cherkasy"};
    String[] colors = {"White", "Black", "Brown", "Orange", "Red", "Yellow", "Blue", "Grey", "Green", "Several"};
    private TextView age;
    private SeekBar ageSlider;
    private Spinner spinnerCities, spinnerColors;


    private Uri resultUri;
    private Uri[] extraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mDescriptionField = (EditText) findViewById(R.id.description);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        extraUri = new Uri[4];
        extraImages = new ImageView[] {
                (ImageView) findViewById(R.id.profileImage3),
                (ImageView) findViewById(R.id.profileImage4),
                (ImageView) findViewById(R.id.profileImage5),
                (ImageView) findViewById(R.id.profileImage6)};

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        ArrayAdapter<String> adapterCities = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities);
        adapterCities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCities = (Spinner) findViewById(R.id.spinner3);
        spinnerCities.setAdapter(adapterCities);
        spinnerCities.setPrompt("City");

        ArrayAdapter<String> adapterColors = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, colors);
        adapterColors.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerColors = (Spinner) findViewById(R.id.spinner4);
        spinnerColors.setAdapter(adapterColors);
        spinnerColors.setPrompt("Colors");

        ageSlider = (SeekBar) findViewById(R.id.seekBar2);
        age = (TextView) findViewById(R.id.textView8);
        ageSlider.setOnSeekBarChangeListener(this);
        age.setText("0");

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        for (int i = 0; i < extraImages.length; i++){
            final int j = i;
            extraImages[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, j+2);
                }
            });
        }

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
    }


    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("description")!=null){
                        description = map.get("description").toString();
                        mDescriptionField.setText(description);
                    }
                    if(map.get("sex")!=null){
                        userSex = map.get("sex").toString();
                        if (userSex.equals("Human")) {
                            findViewById(R.id.photo1).setVisibility(View.GONE);
                            findViewById(R.id.photo2).setVisibility(View.GONE);
                        }
                        if (!userSex.equals("Human")) {
                            findViewById(R.id.textView6).setVisibility(View.VISIBLE);
                            findViewById(R.id.spinner3).setVisibility(View.VISIBLE);
                            findViewById(R.id.textView7).setVisibility(View.VISIBLE);
                            findViewById(R.id.spinner4).setVisibility(View.VISIBLE);
                            findViewById(R.id.textView9).setVisibility(View.VISIBLE);
                            findViewById(R.id.seekBar2).setVisibility(View.VISIBLE);
                            findViewById(R.id.textView8).setVisibility(View.VISIBLE);
                            findViewById(R.id.photo1).setVisibility(View.VISIBLE);
                            findViewById(R.id.photo2).setVisibility(View.VISIBLE);
                            if (map.get("city")!=null) {
                                String user_city = map.get("city").toString();
                                spinnerCities.setSelection(Arrays.asList(cities).indexOf(user_city));
                            }
                            if (map.get("color")!=null) {
                                String user_color = map.get("color").toString();
                                spinnerColors.setSelection(Arrays.asList(colors).indexOf(user_color));
                            }
                            if (map.get("age")!=null){
                                int user_age = Integer.parseInt(map.get("age").toString());
                                ageSlider.setProgress(user_age);
                                age.setText(map.get("age").toString());
                            }
                            for (int i = 0; i < extraImages.length; i++){
                                Glide.clear(extraImages[i]);
                                if(map.get("profileImageUrl" + Integer.toString(i))!=null){
                                    String imageUrl;
                                    imageUrl = map.get("profileImageUrl" + Integer.toString(i)).toString();
                                    switch(imageUrl){
                                        case "default":
                                            Glide.with(getApplication()).load(R.mipmap.ic_launcher).into(extraImages[i]);
                                            break;
                                        default:
                                            Glide.with(getApplication()).load(imageUrl).into(extraImages[i]);
                                            break;
                                    }
                                }
                            }
                        }
                    }
                    Glide.clear(mProfileImage);
                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        switch(profileImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.mipmap.ic_launcher).into(mProfileImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void saveUserInformation() {
        name = mNameField.getText().toString();
        phone = mPhoneField.getText().toString();
        description = mDescriptionField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        userInfo.put("description", description);


        if (!userSex.equals("Human")) {
            userInfo.put("color", spinnerColors.getSelectedItem());
            userInfo.put("city", spinnerCities.getSelectedItem());
            userInfo.put("age", ageSlider.getProgress());
        }

        mUserDatabase.updateChildren(userInfo);
        if(resultUri != null){
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Map userInfo = new HashMap();
                    userInfo.put("profileImageUrl", downloadUrl.toString());
                    mUserDatabase.updateChildren(userInfo);

                    finish();
                    return;
                }
            });
        }else{
            finish();
        }
        for (int i = 0; i < extraUri.length; i++) {
            if (extraUri[i] != null) {
                final int j = i;
                StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImageUrl" + Integer.toString(i)).child(userId);
                Bitmap bitmap = null;

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), extraUri[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = filepath.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        finish();
                    }
                });
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        Map userInfo = new HashMap();
                        userInfo.put("profileImageUrl" + Integer.toString(j), downloadUrl.toString());
                        mUserDatabase.updateChildren(userInfo);

                        finish();
                        return;
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        } else {
            if (requestCode >= 2 && requestCode <= 5 && resultCode == Activity.RESULT_OK) {
                final Uri imageUri = data.getData();
                extraUri[requestCode - 2] = imageUri;
                extraImages[requestCode - 2].setImageURI(extraUri[requestCode - 2]);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        age.setText(String.valueOf(seekBar.getProgress()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        age.setText(String.valueOf(seekBar.getProgress()));
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        age.setText(String.valueOf(seekBar.getProgress()));
    }
}
