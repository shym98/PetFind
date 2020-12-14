package com.shym.petfind;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SearchSettingsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private CheckBox[] petTypes;
    private CheckBox[] colors;

    private Button mSave, mReset;
    private String userId;

    private TextView age;
    private SeekBar ageSlider;

    private Spinner spinner;
    String[] cities = {"Any","Kyiv", "Lviv", "Kharkiv", "Dnipro", "Cherkasy"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seacrh_settings);

        mSave = (Button) findViewById(R.id.save_settings);
        mReset = (Button) findViewById(R.id.reset);
        ageSlider = (SeekBar) findViewById(R.id.seekBar);
        age = (TextView) findViewById(R.id.textView5);
        ageSlider.setOnSeekBarChangeListener(this);
        age.setText("0");
        petTypes = new CheckBox[] {(CheckBox) findViewById(R.id.checkBox), (CheckBox) findViewById(R.id.checkBox2),
                (CheckBox) findViewById(R.id.checkBox3), (CheckBox) findViewById(R.id.checkBox4),
                (CheckBox) findViewById(R.id.checkBox5), (CheckBox) findViewById(R.id.checkBox6)};

        colors = new CheckBox[] {(CheckBox) findViewById(R.id.checkBox7), (CheckBox) findViewById(R.id.checkBox8),
                (CheckBox) findViewById(R.id.checkBox13), (CheckBox) findViewById(R.id.checkBox12),
                (CheckBox) findViewById(R.id.checkBox15), (CheckBox) findViewById(R.id.checkBox14),
                (CheckBox) findViewById(R.id.checkBox17), (CheckBox) findViewById(R.id.checkBox16),
                (CheckBox) findViewById(R.id.checkBox19), (CheckBox) findViewById(R.id.several)};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner = (Spinner) findViewById(R.id.spinner2);
        spinner.setAdapter(adapter);
        spinner.setPrompt("City");

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        getUserInfo();

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSearchSettings();
            }
        });
        mReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResetSettings();
            }
        });
    }

    private void ResetSettings() {
        for (int i = 0; i < petTypes.length; i++) {
            petTypes[i].setChecked(false);
        }

        for (int i = 0; i < colors.length; i++){
            colors[i].setChecked(false);
        }

        ageSlider.setProgress(0);

        String type = "";
        String color = "";
        String age = "20";
        String city = "Any";

        Map userInfo = new HashMap();
        userInfo.put("type", type);
        userInfo.put("color", color);
        userInfo.put("city", city);
        userInfo.put("age", age);
        mUserDatabase.updateChildren(userInfo);

        Intent intent = new Intent(SearchSettingsActivity.this, MainActivity.class);
        startActivity(intent);
        return;

    }

    private void saveSearchSettings() {
        String type = "";
        String color = "";
        String city = "";

        String age = String.valueOf(ageSlider.getProgress());

        for (int i = 0; i < petTypes.length; i++) {
            if (petTypes[i].isChecked()) {
                if (type.length() > 0)
                    type += ";";
                type += petTypes[i].getText();
            }
        }

        for (int i = 0; i < colors.length; i++) {
            if (colors[i].isChecked()) {
                if (color.length() > 0)
                    color += ";";
                color += colors[i].getText();
            }
        }

        city = spinner.getSelectedItem().toString();

        Map userInfo = new HashMap();
        userInfo.put("type", type);
        userInfo.put("color", color);
        userInfo.put("city", city);
        userInfo.put("age", age);
        mUserDatabase.updateChildren(userInfo);

        Intent intent = new Intent(SearchSettingsActivity.this, MainActivity.class);
        startActivity(intent);
        return;

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

    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("age")!=null){
                        int user_age = Integer.parseInt(map.get("age").toString());
                        ageSlider.setProgress(user_age);
                        age.setText(map.get("age").toString());
                    }

                    if (map.get("city")!=null) {
                        String user_city = map.get("city").toString();
                        spinner.setSelection(Arrays.asList(cities).indexOf(user_city));
                    }

                    if (map.get("color")!=null) {
                        String[] user_colors = map.get("color").toString().split(";",-1);
                        for (int i = 0; i < user_colors.length; i++) {
                            for (int j = 0; j < colors.length; j++) {
                                if (colors[j].getText().equals(user_colors[i])) {
                                    colors[j].setChecked(true);
                                }
                            }
                        }
                    }

                    if (map.get("type")!=null) {
                        String[] user_types = map.get("type").toString().split(";",-1);
                        for (int i = 0; i < user_types.length; i++) {
                            for (int j = 0; j < petTypes.length; j++) {
                                if (petTypes[j].getText().equals(user_types[i])) {
                                    petTypes[j].setChecked(true);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
