package com.example.sttg.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.sttg.Fragment.FavoriteFragment;
import com.example.sttg.Fragment.HomeFragment;
import com.example.sttg.Fragment.LibaryFragment;
import com.example.sttg.Fragment.PersonFragment;
import com.example.sttg.Fragment.SearchFragment;
import com.example.sttg.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar_home);

        BottomNavigationView navigationBT = findViewById(R.id.navigationBT);
        navigationBT.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        toolbar.setTitle("Home");
        loadFragment(new HomeFragment());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.nv_home:
                    toolbar.setTitle("Home");
                    fragment = new HomeFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.nv_libary:
                    toolbar.setTitle("Libary");
                    fragment = new LibaryFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.nv_search:
                    toolbar.setTitle("Search");
                    fragment = new SearchFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.nv_favorite:
                    toolbar.setTitle("Favorite");
                    fragment = new FavoriteFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.nv_person:
                    toolbar.setTitle("Profile");
                    fragment = new PersonFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}