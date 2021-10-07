package com.example.caneat;

import android.accounts.Account;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.firebase.firestore.auth.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IntroActivity  extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private SignInButton google_login; //구글 로그인버
    private FirebaseAuth auth; //파이어 베이스 인증 객체
    private GoogleApiClient googleApiClient; //구글 api 클라이언트 객체
    private static final int REQ_SIGN_GOOGLE = 100; //구글 로그인 결과 코




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro); // xml파일과 자바파일 연결튼

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();
        auth = FirebaseAuth.getInstance(); //파이어 베이스 인증 객체 초기화

        google_login = findViewById(R.id.google_login);
        google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,REQ_SIGN_GOOGLE);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //구글 로그인 인증을 요청 했을떄 결과 값을 되돌려 받는곳
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==REQ_SIGN_GOOGLE){
            GoogleSignInResult result =Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess() ==true){ //인증결과가 성공일
                GoogleSignInAccount account = result.getSignInAccount();// account 라는 데이터는 구글로그인 정보를 담고있음 (닉네임,프로필,주소등)
                resultLogin(account); //로그인 결과 값 출력 수행 메소드

            }
        }
    }

    private void resultLogin(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){//로그인 성공
                            Toast.makeText(IntroActivity.this,"로그인 성공",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),SelectActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("nickname",account.getDisplayName());
                            intent.putExtra("email",account.getEmail());

                            startActivity(intent);




                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                for(UserInfo profile : user.getProviderData()) {
                                    // Id of the provider (ex: google.com)
                                    String providerId = profile.getProviderId();

                                    // UID specific to the provider
                                    String uid = profile.getUid();

                                    // Name, email address, and profile photo Url
                                    String name = profile.getDisplayName();
                                    String email = profile.getEmail();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("user-uid :"+user.getUid());
                                    myRef.child("user-email").setValue(email);
                                    myRef.child("user-name").setValue(name);
                                }


                            }

                        } else{
                            Toast.makeText(IntroActivity.this,"로그인 실",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}