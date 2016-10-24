package com.nfctag.printsecure.androidnfc;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Print Secure on 4/15/2016.
 */
public class loginActivity extends Activity {
    private TextView Nom,Prenom,datDenaissance,Email,Tel,Adress,ID,solde,CIN;
    private EditText usernameField;
    private TextView status,role,method;
    ScrollView scrollView;
    Button bun;
    LinearLayout lay;
    RelativeLayout relativelayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameField = (EditText)findViewById(R.id.editCode);
        relativelayout=(RelativeLayout) findViewById(R.id.relativelogin);
        scrollView = (ScrollView) findViewById(R.id.scrolllogin);
        lay=(LinearLayout) findViewById(R.id.layLog);
        //bun = (Button) findViewById(R.id.button1);
        //bun.setVisibility(Button.INVISIBLE);
        relativelayout.setVisibility(relativelayout.GONE);
        scrollView.setVisibility(relativelayout.GONE);
        status = (TextView)findViewById(R.id.msg);
        Nom = (TextView) findViewById(R.id.editNom);
        Prenom= (TextView) findViewById(R.id.editPrenom);
        datDenaissance= (TextView) findViewById(R.id.EditOganisation);
        Adress= (TextView) findViewById(R.id.EditAdress);
        ID= (TextView) findViewById(R.id.EditSkype);
        Email= (TextView) findViewById(R.id.EditEmail);
        Tel= (TextView) findViewById(R.id.EditTel);
      //  position= (TextView) findViewById(R.id.EditPosition);
        CIN= (TextView) findViewById(R.id.EditFixe);

        solde= (TextView) findViewById(R.id.EditFax);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    public void login(View view){
        String username = usernameField.getText().toString();
        new SigninActivity(this,status,0).execute(username);

        // ech

       // while(username!=null)
       // {

      //  }

        String[] rawDump = username.split("#");
        //Log.e("eee",rawDump);
        relativelayout.setVisibility(relativelayout.VISIBLE);
        scrollView.setVisibility(relativelayout.VISIBLE);
       //bun.setVisibility(Button.);
        //bun.setVisibility(Button.INVISIBLE);
        lay.setVisibility(LinearLayout.GONE);
       /* Nom .setText(rawDump[1]);
        Prenom.setText(rawDump[2]);
        Tel.setText(rawDump[3]);
        Email.setText(rawDump[4]);
        Adress.setText(rawDump[5] );
        Organisation.setText(rawDump[6]);
        fixe.setText(rawDump[7]);
        Skype.setText(rawDump[8]);
        fax.setText(rawDump[9]);
        position.setText(rawDump[11]);*/
    }


}
