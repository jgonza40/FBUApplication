package com.example.fbuapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.fbuapplication.fragments.HomeFragment;
import com.example.fbuapplication.fragments.MapFragment;
import com.example.fbuapplication.fragments.ProfileFragment;
import com.example.fbuapplication.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the toolbar view inside the activity layout
        toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        //Toast.makeText(MainActivity.this, "home", Toast.LENGTH_SHORT).show();
                        fragment = new HomeFragment();
                        //fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        break;
                    case R.id.action_map:
                        //Toast.makeText(MainActivity.this, "map", Toast.LENGTH_SHORT).show();
                        fragment = new MapFragment();
                        //Intent i = new Intent(MainActivity.this, MapComposeActivity.class);
                        //startActivity(i);
                        break;
                    case R.id.action_search:
                        //Toast.makeText(MainActivity.this, "search", Toast.LENGTH_SHORT).show();
                        fragment = new SearchFragment();
                        //fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        break;
                    case R.id.action_profile:
                    default:
                        //Toast.makeText(MainActivity.this, "profile", Toast.LENGTH_SHORT).show();
                        fragment = new ProfileFragment();
                        //fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        break;
                }
                // cannot do this here because not each of them are fragments so if map was clicked, the
                // variable fragment would be null
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.settings_icon) {
//            Toast.makeText(this, "clicked!", Toast.LENGTH_SHORT).show();
//            // Composed icon has been tapped
//            // Navigate to the compose activity
//            Intent intent = new Intent(this, Settings.class);
//            startActivity(intent);
//
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    public void goToSettings(View view){
        Toast.makeText(this, "clicked!", Toast.LENGTH_SHORT).show();
        // Composed icon has been tapped
        // Navigate to the compose activity
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}