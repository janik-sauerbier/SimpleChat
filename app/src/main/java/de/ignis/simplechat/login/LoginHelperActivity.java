package de.ignis.simplechat.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.ignis.simplechat.R;
import de.ignis.simplechat.chat.ChatActivity;
import de.ignis.simplechat.home.HomeActivity;

/**
 * Created by Andreas on 14.10.2016.
 */

public class LoginHelperActivity extends AppCompatActivity implements

        GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    public FirebaseDatabase firebaseDatabase;
    public DatabaseReference root_ref;
    public DatabaseReference users_ref;

    public FirebaseUser user;

    private GoogleApiClient googleApiClient;
    private String TAG  = "Login Activity";
    private Activity activity;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        setUpAuthStateListener();
        setUpGoogleLogin();

        firebaseDatabase = FirebaseDatabase.getInstance();
        root_ref = firebaseDatabase.getReference();
        users_ref = root_ref.child("users");

    }


    public void setUpGoogleLogin(){
        GoogleSignInOptions googlesigninoptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,googlesigninoptions)
                .build();

    }

    public void setUpLoginHelperActivity(Activity activity){
        this.activity = activity;
    }

    public void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void showUsernameView(){}
    public void usernameExists(){}
    public void usernameDoesnotExist(String username){}


    public void showProgressDialog(Context c,String message){
        ProgressDialog prog = ProgressDialog.show(c,"",message,true);
    }

    public void doesUsernameExists(final String username){
        users_ref.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    usernameExists();
                }
                else{
                    usernameDoesnotExist(username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }}) ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.d(TAG, "Google sign in failed ");
                Toast.makeText(activity, "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean inSigninFlow = false;
    public void setUpAuthStateListener(){
        inSigninFlow = false;
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    logData();
                    Log.d(TAG, "User is signed in");
                    Toast.makeText(activity, "Signed in", Toast.LENGTH_SHORT).show();

                        if(user.getDisplayName()==null){
                          showUsernameView();
                        }

                         if(user.getDisplayName()!=null&&!inSigninFlow) {
                             inSigninFlow = true;
                             Log.d(TAG, "Start Activity Called");
                             HomeActivity.user_name = user.getDisplayName();
                             activity.startActivity(new Intent(activity, HomeActivity.class));
                             activity.finish();
                    }

                    }
                else{
                    Log.d(TAG, "User is signed out");
                }
            }
        };
    }

    public void createUser(String email,String password){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(activity, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void loginUser(String email,String password){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(activity, "login failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signOut(){
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.d(TAG, "Google Sign out succesfully");
                    }
                });
    }


    // Konnte noch nicht getestet werden da die App signiert sein muss.

    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(activity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Nur zum Testen
    private void logData(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        Log.d(TAG, "Displayname :" + user.getDisplayName());
        Log.d(TAG, "Email :" + user.getEmail());
        Log.d(TAG, "Profile Photo :" + user.getPhotoUrl());
    }



    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public void toast(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }
}
