package com.mr_17.evira.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.mr_17.evira.R;
import com.mr_17.evira.adapter.CategoryRecyclerViewAdapter;
import com.mr_17.evira.model.CategoryRecyclerViewModel;
import com.mr_17.evira.model.FirebaseModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserDashboardActivity extends AppCompatActivity {

    private AppCompatTextView wishingMsg;
    private AppCompatTextView fullName;
    private CircleImageView profileImage;

    private ChipNavigationBar chipNavigationBar;

    private RecyclerView recyclerView;
    private ArrayList<CategoryRecyclerViewModel> list;
    private CategoryRecyclerViewAdapter.RecyclerViewClickListener listener;

    private FirebaseAuth myAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        InitializeFields();
        InitializeUserData();
        InitializeCategoryRecyclerView();
    }

    private void InitializeFields()
    {
        myAuth = FirebaseAuth.getInstance();

        wishingMsg = findViewById(R.id.wishing_msg);
        wishingMsg.setText("Good " + GetWishing() + ",");

        fullName = findViewById(R.id.full_name);
        profileImage = findViewById(R.id.profile_image);

        chipNavigationBar = findViewById(R.id.bottom_nav_bar);
        chipNavigationBar.setItemSelected(R.id.dashboard, true);
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (chipNavigationBar.getSelectedItemId())
                {
                    case R.id.settings:
                        SendToActivity(SettingsActivity.class, true);
                        chipNavigationBar.setItemSelected(R.id.dashboard, true);
                        break;
                }

            }
        });
    }

    private void InitializeUserData()
    {
        SharedPreferences sharedPref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        FirebaseModel.databaseRef_uids.child(myAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    editor.putString("username", snapshot.getValue().toString());
                    editor.apply();
                    FirebaseModel.databaseRef_users.child(sharedPref.getString("username", "!@#")).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            fullName.setText(snapshot.child(FirebaseModel.node_firstName).getValue() + " " + snapshot.child(FirebaseModel.node_lastName).getValue());
                            Picasso.get().load(snapshot.child(FirebaseModel.node_profilePic).getValue().toString()).into(profileImage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeCategoryRecyclerView()
    {
        SetOnClickListener();

        recyclerView = findViewById(R.id.recycler_view);
        list = new ArrayList<>();

        CategoryRecyclerViewAdapter adapter = new CategoryRecyclerViewAdapter(list, this, listener);
        recyclerView.setAdapter(adapter);

        list.add(new CategoryRecyclerViewModel("Clothes", 5, R.drawable.icon_clothes));
        list.add(new CategoryRecyclerViewModel("Bags", 5, R.drawable.icon_bags));
        list.add(new CategoryRecyclerViewModel("Shoes", 5, R.drawable.shoes));
        list.add(new CategoryRecyclerViewModel("Electronics", 6, R.drawable.icon_clothes));
        list.add(new CategoryRecyclerViewModel("Jewellery", 5, R.drawable.icon_clothes));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void SetOnClickListener()
    {
        listener = new CategoryRecyclerViewAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position)
            {

            }
        };
    }

    private String GetWishing()
    {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay < 12)
            return "Morning";
        else if(timeOfDay < 16)
            return "Afternoon";
        else if(timeOfDay < 24)
            return "Evening";
        else
            return "Morning";
    }

    private void SendToActivity(Class<? extends Activity> activityClass, boolean backEnabled)
    {
        Intent intent = new Intent(this, activityClass);

        if (!backEnabled)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        if (!backEnabled)
            finish();
    }
}