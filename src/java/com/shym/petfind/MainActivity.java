package com.shym.petfind;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.shym.petfind.Cards.arrayAdapter;
import com.shym.petfind.Cards.cards;
import com.shym.petfind.Matches.MatchesActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private cards cards_data[];
    private com.shym.petfind.Cards.arrayAdapter arrayAdapter;
    private int i;

    private FirebaseAuth mAuth;

    private String currentUId;

    private DatabaseReference usersDb;

    Button search;

    ListView listView;
    List<cards> rowItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        search = (Button) findViewById(R.id.search_button);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

        getUserSearchSettings();

        rowItems = new ArrayList<cards>();

        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems );

        final SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                //flingContainer.removeAllViewsInLayout();
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {

                cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true);
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("yeps").child(currentUId).setValue(true);
                isConnectionMatch(userId);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float v) {

            }

        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                int index = rowItems.indexOf((cards) dataObject);
                cards temp = rowItems.get(index);
                temp.nextPhoto();
                temp.setClicked();
                rowItems.remove(0);
                rowItems.add(0, temp);
                flingContainer.removeAllViewsInLayout();
                arrayAdapter.notifyDataSetChanged();

            }
        });

    }

    public  int age;
    public String city;
    public String[] colors;
    public String[] types;
    public String[] yeps = new String[] {};

    private void getUserSearchSettings() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if (!dataSnapshot.child("sex").getValue().toString().equals("Human")) {
                        search.setVisibility(View.INVISIBLE);
                        userSex = dataSnapshot.child("sex").getValue().toString();
                    }

                    if (dataSnapshot.child("age").getValue() != null) {
                        age = Integer.parseInt(dataSnapshot.child("age").getValue().toString());
                    } else age = 20;

                    if (dataSnapshot.child("city").getValue() != null && !dataSnapshot.child("city").getValue().equals("")) {
                        city = dataSnapshot.child("city").getValue().toString();
                    } else city = "Any";

                    if (dataSnapshot.child("color").getValue() != null) {
                        colors = dataSnapshot.child("color").getValue().toString().split(";",-1);
                    }

                    if (dataSnapshot.child("type").getValue() != null) {
                        types = dataSnapshot.child("type").getValue().toString().split(";",-1);
                    }

                    for (DataSnapshot sampleSnapshot: dataSnapshot.child("connections").child("yeps").getChildren()) {
                            yeps = Arrays.copyOf(yeps, yeps.length + 1);
                            yeps [yeps.length - 1] = sampleSnapshot.getKey().toString();
                    }

                    if (userSex.equals("Human")) getOppositeUsers(); else
                        getHumanUsers();

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Toast.makeText(MainActivity.this, "IT'S A MATCH!!!", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).child("ChatId").setValue(key);
                    usersDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).child("ChatId").setValue(key);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String userSex = "Human";

    public boolean checkCompatibility(DataSnapshot dataSnapshot) {

        if (dataSnapshot.child("age").getValue() != null) {
            if (Integer.parseInt(dataSnapshot.child("age").getValue().toString()) > age) return false;
        } else if (age > 0) return false;

        if (dataSnapshot.child("city").getValue() != null && !city.equals("Any")) {
            if (!dataSnapshot.child("city").getValue().toString().equals(city)) return false;
        }

        if (dataSnapshot.child("sex").getValue() != null && types != null && types[0].length() > 0) {
            if (!Arrays.asList(types).contains(dataSnapshot.child("sex").getValue().toString())) return false;
        }

        if (dataSnapshot.child("color").getValue() != null && colors != null && colors[0].length() > 0) {
            if (!Arrays.asList(colors).contains(dataSnapshot.child("color").getValue().toString())) return false;
        }

        return true;
    }

    public void getOppositeUsers(){
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("sex").getValue() != null && !dataSnapshot.child("sex").getValue().equals("Human")) {
                    if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUId) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId) && checkCompatibility(dataSnapshot)) {
                        String[] profileImageUrl = new String[]{};
                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImageUrl = Arrays.copyOf(profileImageUrl, profileImageUrl.length + 1);
                            profileImageUrl[profileImageUrl.length-1] = dataSnapshot.child("profileImageUrl").getValue().toString();
                        } else {
                            profileImageUrl = Arrays.copyOf(profileImageUrl, profileImageUrl.length + 1);
                            profileImageUrl[profileImageUrl.length-1] = "default";
                        }
                        for (int i = 0; i < 4; i++) {
                            if (dataSnapshot.child("profileImageUrl" + Integer.toString(i)).getValue() != null &&
                                    !dataSnapshot.child("profileImageUrl" + Integer.toString(i)).getValue().equals("default")) {
                                profileImageUrl = Arrays.copyOf(profileImageUrl, profileImageUrl.length + 1);
                                profileImageUrl[profileImageUrl.length-1] = dataSnapshot.child("profileImageUrl" + Integer.toString(i)).getValue().toString();
                            }
                        }
                        String nameAge;
                        nameAge = dataSnapshot.child("name").getValue().toString();
                        if (dataSnapshot.child("age").getValue() != null) {
                            nameAge += ", ";
                            if (dataSnapshot.child("age").getValue().toString().equals("0")) {
                                nameAge += " < 1";
                            } else {
                                nameAge += dataSnapshot.child("age").getValue().toString();
                            }
                        }
                        cards item = new cards(dataSnapshot.getKey(), nameAge, profileImageUrl, 0);
                        if (dataSnapshot.child("color").getValue() != null) {
                            item.setColor(dataSnapshot.child("color").getValue().toString());
                        }
                        if (dataSnapshot.child("sex").getValue() != null) {
                            item.setPet(dataSnapshot.child("sex").getValue().toString());
                        }
                        if (dataSnapshot.child("city").getValue() != null) {
                            item.setCity(dataSnapshot.child("city").getValue().toString());
                        }
                        if (dataSnapshot.child("description").getValue() != null) {
                            item.setDescription(dataSnapshot.child("description").getValue().toString());
                        }
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getHumanUsers(){
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("sex").getValue() != null && dataSnapshot.child("sex").getValue().equals("Human")) {
                    if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUId) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId)
                            && Arrays.asList(yeps).contains(dataSnapshot.getKey())) {
                        String[] profileImageUrl = new String[]{};
                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImageUrl = Arrays.copyOf(profileImageUrl, profileImageUrl.length + 1);
                            profileImageUrl[profileImageUrl.length-1] = dataSnapshot.child("profileImageUrl").getValue().toString();
                        } else {
                            profileImageUrl = Arrays.copyOf(profileImageUrl, profileImageUrl.length + 1);
                            profileImageUrl[profileImageUrl.length-1] = "default";
                        }
                        for (int i = 0; i < 4; i++) {
                            if (dataSnapshot.child("profileImageUrl" + Integer.toString(i)).getValue() != null &&
                                    !dataSnapshot.child("profileImageUrl" + Integer.toString(i)).getValue().equals("default")) {
                                profileImageUrl = Arrays.copyOf(profileImageUrl, profileImageUrl.length + 1);
                                profileImageUrl[profileImageUrl.length-1] = dataSnapshot.child("profileImageUrl" + Integer.toString(i)).getValue().toString();
                            }
                        }
                        String nameAge;
                        nameAge = dataSnapshot.child("name").getValue().toString();
                        cards item = new cards(dataSnapshot.getKey(), nameAge, profileImageUrl, 0);
                        if (dataSnapshot.child("description").getValue() != null) {
                            item.setDescription(dataSnapshot.child("description").getValue().toString());
                        }
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void logoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        return;
    }

    public void goToMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }

    public void goToSearchSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SearchSettingsActivity.class);
        startActivity(intent);
        return;
    }
}