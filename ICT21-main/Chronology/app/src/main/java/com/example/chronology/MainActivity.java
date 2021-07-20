package com.example.chronology;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    NavigationView navigationView;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) //Use Your Own WEB CLIENT ID OF GOOGLE API...
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        profilePic();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        removeColor(navigationView);
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_detection:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DetectionFragment()).commit();
                break;
            case R.id.nav_quiz:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new QuizFragment()).commit();
                break;
            case R.id.nav_reminder:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ReminderFragment()).commit();
                break;
            case R.id.nav_sleep:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SleepFragment()).commit();
                break;
            case R.id.nav_report:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ReportFragment()).commit();
                break;
            case R.id.nav_yoga:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new YogaFragment()).commit();
                break;
            case R.id.nav_doctor:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DoctorFragment()).commit();
                break;
            case R.id.nav_fall:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FallFragment()).commit();
                break;
            case R.id.nav_community:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CommFragment()).commit();
                break;
            case R.id.nav_logout:
                signOut();
                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext() , LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }


    private void removeColor(NavigationView view){
        for(int i=0; i<view.getMenu().size(); i++)
        {
            MenuItem item=view.getMenu().getItem(i);
            item.setChecked(false);
        }
    }

    private void profilePic() {

        navigationView = findViewById(R.id.nav_view);
        View headerView=navigationView.getHeaderView(0);
        ImageView profilepic = headerView.findViewById(R.id.profilepic);
        TextView userName = headerView.findViewById(R.id.userName);

        //Getting profile photo URL of google authenticated user
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

        if(googleSignInAccount != null) {
            String personName = googleSignInAccount.getDisplayName();
            Uri personPhotoUrl = googleSignInAccount.getPhotoUrl();

            Log.i("NAME", personName);
            Log.i("URL", personPhotoUrl.toString());

            //Calling Async Task to set Image view with profile photo of user
            new DownLoadImageTask(profilepic).execute(personPhotoUrl.toString());
            userName.setText(personName);

        }
    }

    private class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        public DownLoadImageTask (ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlOfImage = urls[0];
            Bitmap profilePhoto = null;

            try {
                InputStream inputStream = new URL(urlOfImage).openStream();
                profilePhoto = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return profilePhoto;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
        }
    }
}