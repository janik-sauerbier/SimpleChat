package de.ignis.simplechat.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.ignis.simplechat.R;
import de.ignis.simplechat.home.HomeActivity;

/**
 * Created by Andreas on 13.10.2016.
 */

public class LoginActivity extends LoginHelperActivity implements View.OnClickListener{

    private EditText email_et, pasword_et,reg_email_et,reg_password_et,reg_password2_et,username_et;
    private View     login,username,register;
    private RelativeLayout baseView;
    public static boolean inRegistration = false;

    protected void onCreate(Bundle savedInstanceState) {

        setUpLoginHelperActivity(this);
        super.onCreate(savedInstanceState);
        setUpVievs();
        setContentView(R.layout.activity_login);
        setUpReferences();
        setView(login);

    }

    private void setUpReferences(){
        email_et         = (EditText) login.   findViewById(R.id.email_et        );
        pasword_et       = (EditText) login.   findViewById(R.id.password_et     );
        reg_password_et  = (EditText) register.findViewById(R.id.reg_password_et );
        reg_password2_et = (EditText) register.findViewById(R.id.reg_password2_et);
        reg_email_et     = (EditText) register.findViewById(R.id.reg_email_et    );
        baseView         = (RelativeLayout)    findViewById(R.id.login_base_view );
        username_et      = (EditText) username.findViewById(R.id.username_et     );

    }

    private void setUpVievs(){
        login    = View.inflate(this,R.layout.login_view   ,null);
        register = View.inflate(this,R.layout.register_view,null);
        username = View.inflate(this,R.layout.username_view,null);
    }

    private void setView(View v){
        baseView.removeAllViews();
        baseView.addView(v);
    }


    @Override
    public void showUsernameView(){
        setView(username);
    }

    @Override
    public void usernameExists() {
        toast("Dieser Name ist leider schon vergeben");
    }

    @Override
    public void usernameDoesnotExist(String username) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");

        users_ref.child(username).child("UUID").setValue(user.getUid());
        DatabaseReference user_ref = users_ref.child(username);
        user_ref.child("email").setValue(user.getEmail());
        user_ref.child("creation_date").setValue(df.format(c.getTime()));
        user_ref.child("creation_time").setValue(c.HOUR+" : "+c.MINUTE+" : "+c.SECOND);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username).build();
        user.updateProfile(profileUpdates);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.sign_in_btn:

                String email        = reg_email_et    .getText().toString().trim();
                String password     = reg_password_et .getText().toString().trim();
                String passwordzert = reg_password2_et.getText().toString().trim();

                Boolean Cemail,Cpasswordlength,Cpassword;
                Cemail = Cpassword = Cpasswordlength = false;

                if(email.   length()>0   ){Cemail          = true;}
                if(password.length()>5   ){Cpasswordlength = true;}
                if(password.equals(passwordzert)){Cpassword= true;}

                if(Cemail&&Cpassword&&Cpasswordlength){

                    createUser(email,password);

                }
                else{
                    if(!Cemail&&Cpassword){toast("Überprüfe deine Email Adresse"             );}
                    if(Cpassword&&!Cpasswordlength){toast("Passwort zu kurz"                 );}
                    if(!Cpassword&&Cpasswordlength){toast("Passwörter stimmen nicht überein" );}
                    if(!Cpassword&&!Cpasswordlength&&!Cemail){toast("Überprüfe deine Angaben");}
                }

                break;

            case R.id.google_sign_in_btn:
                signInGoogle();
                break;
            
            case R.id.log_in_btn:
                String email2    = email_et.  getText().toString().trim();
                String password2 = pasword_et.getText().toString().trim();

                if(email2.length()>0&&password2.length()>0){

                    loginUser(email2,password2);
                    showProgressDialog(this,"Loading. Please wait...");

                }
                else{

                    toast("Check your entries");

                }
                break;
            
            case R.id.skip_btn:
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                break;
            
            case R.id.register_btn:
                setView(register);
                break;
            
            case R.id.tologin_btn:
                setView(login);
                break;
            case R.id.checkusername_btn:
                doesUsernameExists(username_et.getText().toString().trim());
                break;
        }
    }
}
