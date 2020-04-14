package cau.injiyong.biking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Calendar;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int RC_SIGN_IN = 10;
    //    private SessionCallback sessionCallback;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    private boolean flag_BackKey = false;                //BACK KEY가 눌려졌는지에 대한 FLAG 로 사용됩니다.
    private long currentTime = 0;                          // Time(Millis) Interval을 계산합니다.
    private static final int MSG_TIMER = 1;              // Switch문에서 사용하게 되는 Key값입니다.
    private static final int BACKKEY_TIMEOUT = 2;    // Interval을 정의합니다.
    private static final int IN_MILLISEC = 1000;        // Millis를 정의합니다.
    SignInButton button;
    ProgressDialog progressDialog;

    @Override

    public void onStart() {
        super.onStart();
        // 활동을 초기화할 때 사용자가 현재 로그인되어 있는지 확인합니다.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("로그인 로딩");
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
        mAuth = FirebaseAuth.getInstance();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        button = (SignInButton) findViewById(R.id.login_button);
        Button loginbtn=(Button)findViewById(R.id.custom_login);
        loginbtn.setOnClickListener(this);



    }

    @Override
    public void onClick(View view) {
        progressDialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onBackPressed(){            //BACK키가 눌렸을 때의 함수로 Override하여 사용합니다.

        if ( flag_BackKey == false ){

            // 첫 번째로 클릭했을 경우로, false에서 true로 바꿔줍니다.

            flag_BackKey = true;



            currentTime = Calendar.getInstance().getTimeInMillis();       //Java의 Calendar를 import하여 사용합니다.

            Toast.makeText(this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();

            startTimer();

        }else{            //만약 BACKKey의 플래그가 현재 false가 아닌, true 라면 아래를 수행합니다.

            // second click : 2초 이내면 종료! 아니면 아무것도 안한다.

            flag_BackKey = false;

            if ( Calendar.getInstance().getTimeInMillis() <= (currentTime + (BACKKEY_TIMEOUT * IN_MILLISEC )) ) {

                finish();                //currentTime + 2000 (2초) 이므로, 2초 안에 클릭 했을 때, MainActivity를 종료해주는 부분입니다.

            }

        }

    }

    private void startTimer() {                                    //2초의 시간적 여유를 가지도록 Delay 시킵니다.

        backTimerHandler.sendEmptyMessageDelayed(MSG_TIMER , BACKKEY_TIMEOUT * IN_MILLISEC );

    }



    private Handler backTimerHandler = new Handler(){

        public void handleMessage(Message msg){

            switch( msg.what ){                //msg의 값이 무엇인지 파악하여 만약 MSG_TIMER라면, flag를 다시 false로 해줍니다.

                case MSG_TIMER :{

                    flag_BackKey = false;

                }

                break;

            }

        }

    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                            progressDialog.dismiss();

                        } else {

                        }

                    }
                });
    }




}