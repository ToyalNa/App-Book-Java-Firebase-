package com.example.bookapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookapp.MainActivity;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityDashboardUserBinding;
import com.example.bookapp.fragment.BookUserFragment;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {

    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private ActivityDashboardUserBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.viewPage);
        binding.tabLayout.setupWithViewPager(binding.viewPage);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                startActivity(new Intent(DashboardUserActivity.this, MainActivity.class));
                finish();
            }
        });

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardUserActivity.this,ProfileActivity.class));
            }
        });
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);
        categoryArrayList = new ArrayList<>();

        //load categories from firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");// be carefull of spellings
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();

                ModelCategory modelALl = new ModelCategory("01", "All", "", 1);
                ModelCategory modelMostViewed = new ModelCategory("02", "Most Viewed", "", 1);
                ModelCategory modelMostDownloaded = new ModelCategory("03", "Most Downloaded", "", 1);

                categoryArrayList.add(modelALl);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        "" + modelALl.getId(),
                        "" + modelALl.getCategory(),
                        "" + modelALl.getUid()
                ),modelALl.getCategory());

                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        "" + modelMostViewed.getId(),
                        "" + modelMostViewed.getCategory(),
                        "" + modelMostViewed.getUid()
                ),modelMostViewed.getCategory());

                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        "" + modelMostDownloaded.getId(),
                        "" + modelMostDownloaded.getCategory(),
                        "" + modelMostDownloaded.getUid()
                ),modelMostDownloaded.getCategory());
                viewPagerAdapter.notifyDataSetChanged();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    //get data
                    ModelCategory modelCategory = dataSnapshot.getValue(ModelCategory.class);
                    //add data to list
                    categoryArrayList.add(modelCategory);
                    // add data to viewpagerAdapter
                    viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                            "" + modelCategory.getId(),
                            "" + modelCategory.getCategory(),
                            "" + modelCategory.getUid()
                    ), modelCategory.getCategory());
                    //refresh list
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<BookUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(BookUserFragment fragmentBookUser, String title){
            fragmentList.add(fragmentBookUser);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            binding.subTitleTv.setText(R.string.not_logged_in);

        } else {
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);
        }
    }
}