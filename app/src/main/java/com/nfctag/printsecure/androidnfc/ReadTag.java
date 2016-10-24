package com.nfctag.printsecure.androidnfc;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.nfc.TagLostException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Print Secure on 4/13/2016.
 */
public class ReadTag extends BasicActivity {
    private ScrollView scrollView;

    private TextView Nom,Prenom,dateNaissance,Email,Tel,Adress,ID,CIN,solde;
    ProgressBar progressBar;
    RelativeLayout relativelayout;
    private StringBuilder mStringBuilder;
    private final Handler mHandler = new Handler();
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readtag);

        progressBar = (ProgressBar) findViewById(R.id.progressBarReadTag);
        relativelayout=(RelativeLayout) findViewById(R.id.relativeLayoutReadTag);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        relativelayout.setVisibility(relativelayout.VISIBLE);

        scrollView.setVisibility(ScrollView.GONE);
        //findViewById(R.id.LinearLayoutbutton).setVisibility(LinearLayout.GONE);
        Nom = (TextView) findViewById(R.id.editNom);
        Prenom= (TextView) findViewById(R.id.editPrenom);
        dateNaissance= (TextView) findViewById(R.id.EditOganisation);
        Adress= (TextView) findViewById(R.id.EditAdress);
        ID= (TextView) findViewById(R.id.EditSkype);
        Email= (TextView) findViewById(R.id.EditEmail);
        Tel= (TextView) findViewById(R.id.EditTel);
       // position= (TextView) findViewById(R.id.EditPosition);
       CIN= (TextView) findViewById(R.id.EditFixe);

        solde= (TextView) findViewById(R.id.EditFax);

        readTag();



    }

    /**"
     * Triggered by {@link #onActivityResult(int, int, Intent)}
     * this method starts a worker thread that first reads the tag and then
     * calls {@link #readTag()}.
     */
    private void readTag() {
        final MCReader reader = Common.checkForTagAndCreateReader(this);
        if (reader == null) {
            finish();
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {



               // read.close();

               final String [] finalTmpt = reader.ReadCard();
                //reader.close();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        createTagDump(finalTmpt);

                    }
                });
            }
        }).start();
    }

    private void createTagDump(String [] rawDump) {
        if(rawDump == null){
            Toast.makeText(this, R.string.info_tag_removed_while_reading,
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.e("String NNNUUULLL", rawDump.toString());
        relativelayout.setVisibility(relativelayout.GONE);
        //displayText("S== ", rawDump);

        Nom .setText(rawDump[1]);
        Prenom.setText(rawDump[2]);
        Tel.setText(rawDump[3]);
        Email.setText(rawDump[4]);
        Adress.setText(rawDump[5] );
        dateNaissance.setText(rawDump[6]);
       //CIN.setText(rawDump[10]);
        ID.setText(rawDump[8]);
        //solde.setText(rawDump[9]);
        solde.setText("3000,000 DT");
      //  position.setText(rawDump[11]);


        scrollView.setVisibility(ScrollView.VISIBLE);
        //findViewById(R.id.LinearLayoutbutton).setVisibility(LinearLayout.VISIBLE);
    }

    private void displayText(String label, String text){
        if (mStringBuilder == null){
            mStringBuilder = new StringBuilder();
        }
        if (label != null){
            mStringBuilder.append(label);
            mStringBuilder.append(":");
        }
        mStringBuilder.append(text);
        mStringBuilder.append("\n");

       // mInfo.setText(mStringBuilder.toString());
    }

    public void AddContact(View view){
       // Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, Uri.parse("tel:" + Tel.getText())); //currentNum is my TextView, you can replace it with the number directly such as Uri.parse("tel:1293827")
       // intent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true); //skips the dialog box that asks the user to confirm creation of contacts
      //  startActivity(intent);
       Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        ArrayList<ContentValues> data = new ArrayList<ContentValues>();

        ContentValues row1 = new ContentValues();
        row1.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        row1.put(ContactsContract.CommonDataKinds.Organization.COMPANY, dateNaissance.getText().toString());
        //row1.put(ContactsContract.CommonDataKinds.Organization.TITLE, position.getText().toString());
        data.add(row1);
// Just two examples of information you can send to pre-fill out data for the
// user.  See android.provider.ContactsContract.Intents.Insert for the complete
// list.
       // ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
        intent.putExtra(ContactsContract.Intents.Insert.NAME, Nom.getText() + " " + Prenom.getText());
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Tel.getText());
        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, CIN.getText());
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL,Email.getText());
        intent.putExtra(ContactsContract.Intents.Insert.POSTAL, Adress.getText());
// Send with it a unique request code, so when you get called back, you can
// check to make sure it is from the intent you launched (ideally should be
// some public static final so receiver can check against it)
        int PICK_CONTACT = 100;
        startActivityForResult(intent, PICK_CONTACT);
    }
}


