package android.jplas.imageblur;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmailLogin, edtPasswordLogin;
    Button btnLogin, btnLoginGoogle, btnLoginFacebook;
    TextView txtRegister, txtForgotPass;
    private FirebaseAuth mAuth;
    String TAG = "tag";
    private int RC_SIGN_IN = 7;
    private GoogleSignInClient mGoogleSignClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmailLogin = findViewById(R.id.edtEmail);
        edtPasswordLogin = findViewById(R.id.edtPass);
        btnLogin = findViewById(R.id.btnLogIn);
        btnLoginFacebook = findViewById(R.id.btnLogInFacebook);
        btnLoginGoogle = findViewById(R.id.btnLogInGoogle);
        txtRegister = findViewById(R.id.txtSignUp);
        txtForgotPass = findViewById(R.id.txtForgotPass);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignClient = GoogleSignIn.getClient(this, gso);
        mCallbackManager = CallbackManager.Factory.create();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = edtEmailLogin.getText().toString();
                String password = edtPasswordLogin.getText().toString();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                    i.putExtra("email", email);
                                    startActivity(i);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Login Failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        btnLoginFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                    }
                });
            }
        });

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });

        txtForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(i);
            }
        });
    }

        private void signInWithGoogle(){
            Intent i = mGoogleSignClient.getSignInIntent();
            startActivityForResult(i, RC_SIGN_IN);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            mCallbackManager.onActivityResult(requestCode, resultCode, data);

            if (requestCode == RC_SIGN_IN){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                }
                catch(ApiException e){
                    Log.w(TAG, "Google sign in failed", e);
                }
            }
        }

        private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
            Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Login with google success", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        i.putExtra("email", user.getEmail());
                        startActivity(i);
                    }
                    else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Login with google failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void handleFacebookAccessToken(AccessToken token){
            Log.d(TAG, "handleFacebookAccessToken:" + token);

            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
            mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Login with facebook success", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        i.putExtra("email", user.getEmail());
                        startActivity(i);
                    }
                    else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Login with facebook failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

}