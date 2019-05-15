package com.mteam.eabaya;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {
    private EditText Cname,Cphone,Cemail,pass,pass2;
    private Button Cregbtn;
    private ImageButton viewPass,viewPass2;
    private ProgressBar regProgress;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mData;
    String user_id;

    int flag=0;
    int flag2=0;
    int flag3=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Cname=(EditText)findViewById(R.id.owner_name);
        Cphone=(EditText)findViewById(R.id.owner_phone);
        Cemail=(EditText)findViewById(R.id.owner_email);
        pass=(EditText)findViewById(R.id.owner_pass);
        pass2=(EditText)findViewById(R.id.owner_confirm);
        Cregbtn=(Button)findViewById(R.id.reg_btn);
        regProgress=(ProgressBar)findViewById(R.id.progressBar1);

        mAuth=FirebaseAuth.getInstance();

        
        viewPass=(ImageButton)findViewById(R.id.view_pass);
        viewPass2=(ImageButton)findViewById(R.id.view_pass2);

        viewPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag==0) {

                    flag = 1;
                    pass.setTransformationMethod(null);
                }
                    else if(flag==1){
                        flag=0;
                    pass.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });
        viewPass2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag2==0) {

                    flag2 = 1;
                    pass2.setTransformationMethod(null);
                }
                else if(flag2==1){
                    flag2=0;
                    pass2.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });
        
        Cregbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            regProgress.setVisibility(View.VISIBLE);


                final String name=Cname.getText().toString().trim();
                final String phone=Cphone.getText().toString().trim();
                final String email=Cemail.getText().toString().trim();
                final String password=pass.getText().toString().trim();
                String confirm=pass2.getText().toString().trim();
                
                if (TextUtils.isEmpty(name) ||TextUtils.isEmpty(phone) ||TextUtils.isEmpty(email) ||
                        TextUtils.isEmpty(password) ||TextUtils.isEmpty(confirm)){
                    Toast toast = Toast.makeText(RegistrationActivity.this,"Please Fill All Fields", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    flag3=1;
                    regProgress.setVisibility(View.INVISIBLE);

                }
                else if (!password.equals(confirm)){
                    regProgress.setVisibility(View.INVISIBLE);

                    Toast toast = Toast.makeText(RegistrationActivity.this,"Password Not Match", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    flag3=1;
                }

                else{
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                user_id = mAuth.getCurrentUser().getUid();
                                mData= FirebaseDatabase.getInstance().getReference("Customers").child(user_id);

                                regProgress.setVisibility(View.INVISIBLE);

                                Toast toast = Toast.makeText(RegistrationActivity.this,
                                        "Success", Toast.LENGTH_LONG);


                                Customers customers=new Customers(name,email,phone,password);
                                mData.setValue(customers);



                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                startActivity(new Intent(RegistrationActivity.this,
                                        SideMenu.class));


                            }
                            regProgress.setVisibility(View.INVISIBLE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String error=e.getMessage();
                            Toast toast = Toast.makeText(RegistrationActivity.this,
                                    "Error"+error, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            regProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }



            }
        });


    }
}
