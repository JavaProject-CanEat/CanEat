package com.example.caneat;


import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class IntroActivity  extends AppCompatActivity  {
    public DatabaseReference CanEatDatabase;
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth auth; //파이어 베이스 인증 객체
    private GoogleSignInClient googleApiClient; //구글 api 클라이언트 객체

    private SignInButton signInButton;



    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference maindb= database.getReference();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro); // xml파일과 자바파일 연결튼

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = GoogleSignIn.getClient(this,googleSignInOptions);
        auth = FirebaseAuth.getInstance(); //파이어 베이스 인증 객체 초기화

        signInButton =findViewById(R.id.google_login);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =googleApiClient.getSignInIntent();
                startActivityForResult(intent,RC_SIGN_IN);

            }
        });

        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplication(), AfterLogin_introactivity.class);
            startActivity(intent);
            finish();
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) { //구글 로그인 인증을 요청 했을떄 결과 값을 되돌려 받는곳
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sigin in failed", e);
            }
        }
    }


    private  void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG,"signInWithCredential:success");
                    FirebaseUser user =auth.getCurrentUser();
                    updateUI(user);
                    getProviderData();


                }else{
                    Log.w(TAG,"signInWithCredential:failure", task.getException());
                    updateUI(null);
                }
            }
        });
    }

    private void signIn(){
        Intent signInIntent = googleApiClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void updateUI(FirebaseUser user)
    {
        if(user !=null){
            Intent intent =new Intent(this,SelectActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void getProviderData() {
        // [START get_provider_data]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();

                // UID specific to the provider
                String uid = user.getUid();

                // Name, email address, and profile photo Url
                String name = profile.getDisplayName();
                String email = profile.getEmail();
                Uri photoUrl = profile.getPhotoUrl();

                User user_info =new User(email,name);

                maindb.child("user").child("user uid: "+uid).setValue(user_info);




            }
        }
        // [END get_provider_data]

    }
}