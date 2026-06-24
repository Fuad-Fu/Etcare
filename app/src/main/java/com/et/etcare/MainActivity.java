package com.et.etcare;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.et.etcare.fragments.AppointmentFragment;
import com.et.etcare.fragments.ChatListFragment;
import com.et.etcare.fragments.HomeFragment;
import com.et.etcare.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_appointments) {
                loadFragment(new AppointmentFragment());
                return true;
            } else if (id == R.id.nav_chat) {
                loadFragment(new ChatListFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void selectProfileTab() {
        bottomNav.setSelectedItemId(R.id.nav_profile);
    }

}