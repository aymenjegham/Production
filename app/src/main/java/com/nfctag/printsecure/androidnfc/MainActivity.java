package com.nfctag.printsecure.androidnfc;

/**
 * Created by Print Secure on 4/12/2016.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import static android.R.attr.id;
import static android.R.attr.tag;
import static com.nfctag.printsecure.androidnfc.Common.setTag;

public class MainActivity extends Activity {
    private static final String PREFER_NAME = "Reg";
    private SharedPreferences sharedPreferences;

    private TextView mInfo;
    private StringBuilder mStringBuilder;

    private Intent mOldIntent = null;
    private AlertDialog mEnableNfc;
    private boolean mResume = true;
    public int i=0;
    public String Sak;
    public String atqa;
    public int count=0;


    // list of NFC technologies detected:
    private final String[][] techList = new String[][] {
            new String[] {
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };
    byte[] uid;
    TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mInfo = (TextView) findViewById(R.id.textView2);
        tv=(TextView)findViewById(R.id.textView14);
        final String a[]={"hello","world"};
        final ArrayAdapter<String> at=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,a);

          Common.setNfcAdapter(NfcAdapter.getDefaultAdapter(this));
        sharedPreferences = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.contains("Name"))
        {
            String uName = sharedPreferences.getString("tag"+i, "couldn't retrieve data");
            Log.e("tag","ag"+uName);
           // tv.setText(uName);


        }
        i++;


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
                protected void onResume() {
                    super.onResume();
                    checkNfc();
                    if (mResume) {

        }
       /* // creating pending intent:
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // creating intent receiver for NFC events:
        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        // enabling foreground dispatch for getting intent from NFC event:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        Common.disableNfcForegroundDispatch(this);
    }
    public void onShowReadTag(View view) {
        if(Common.getTag()!=null) {
            Intent intent = new Intent(this, ReadTag.class);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, R.string.info_tag_removed_while_reading,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onLoginClick(View view) {

            Intent intent = new Intent(this, loginActivity.class);
            startActivity(intent);

    }
    public static int treatAsNewTag(Intent intent, Context context) {
        // Check if Intent has a NFC Tag.
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            setTag(tag);

            // Show Toast message with UID.
            String id = context.getResources().getString(
                    R.string.info_new_tag_found) + " (UID: ";
            byte[] uid =tag.getId();
         //   id += byte2HexString(tag.getId());
            id += ")";
            Toast.makeText(context, id, Toast.LENGTH_LONG).show();
          //  return checkMifareClassicSupport(tag, context);
        }
        return -4;
    }

    @Override
    public void onNewIntent(Intent intent) {
        sharedPreferences = getApplicationContext().getSharedPreferences("Reg", 0);
// get editor to edit in file
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int typeCheck = Common.treatAsNewTag(intent, this);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NfcA nfca = NfcA.get(tag);
        try {
            nfca.connect();
            int s = nfca.getSak();
            byte[] a = nfca.getAtqa();
            //String atqa = new String(a, Charset.forName("US-ASCII"));
            Log.e("sak"+s,":atqa"+byte2HexString(a));
            nfca.close();
            Sak=(String.valueOf(s));
            atqa=(byte2HexString(a));



        } catch (IOException e) {
            e.printStackTrace();
            Log.e("fdsd","dvdvd");

        }


        Log.e("fdsd","dvdvd"+byte2HexString(tag.getId()));
        String uid =byte2HexString(tag.getId());
        final ListView sp=(ListView)findViewById(R.id.lv);




        //editor.putString("tag"+i, uid);
           // Log.e("num","count"+i);
            //editor.commit();
         tv.setText(" | "+"id: "+uid+"  Sak: "+Sak+"  atqa: "+atqa );
       // String newItem = (count+" | "+"id: "+uid+"  Sak: "+Sak+"  atqa: "+atqa );
       // count++;
        int k=sp.getCount();
        String a1[]=new String[k+1];

            for (int i = 0; i < k; i++) {
                a1[i] = sp.getItemAtPosition(i).toString();
                Log.e("test", "test" + a1[i].toString());
                Log.e("test", "orig" + " | " + "id: " + uid + "  Sak: " + Sak + "  atqa: " + atqa);

                String foo = (" | " + "id: " + uid + "  Sak: " + Sak + "  atqa: " + atqa);
                if (foo.equals(a1[i].toString())) {
                    Log.e("eqal", "equal");
                    return;


                }
            }

        a1[k]=tv.getText().toString();
        ArrayAdapter<String> ats=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,a1);
        Log.e("test","test"+ats.getItem(k));


        sp.setAdapter(ats);





        //editor.putString("tag", uid);
        //editor.putString("Email",email);
        //editor.putString("txtPassword",pass);
        //editor.commit();

    if (typeCheck == -1 || typeCheck == -2) {
            // Device or tag does not support Mifare Classic.
            // Run the only thing that is possible: The tag info tool.
            Intent i = new Intent(this, TagInfoTool.class);
            startActivity(i);

        }
    }
    public static String byte2HexString(byte[] bytes) {
        String ret = "";
        if (bytes != null) {
            for (Byte b : bytes) {
                ret += String.format("%02X", b.intValue() & 0xFF);
            }
        }
        return ret;
    }
    /*
       @Override
      protected void onNewIntent(Intent intent) {

           if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
               ((TextView)findViewById(R.id.textView)).setText(
                       "NFC Tag\n" +
                               ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)));
               Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
               MCReader read = new MCReader(tagFromIntent);
               try {
                   read.connect();
                   for(int j=0;j<18;j++ ) {
                       String [] tmp;
                       String  tmpt="";
                       tmp = read.readSector(j, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,}, true);
                       // for(int i =0 ; i<a.length;i++)
                       if (tmp == null)
                           tmp = read.readSector(j, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,}, false);
                       for (int i = 0; i < tmp.length-1; i++) {
                           Log.e("String", " A== " + tmp[i]);
                           tmpt+=read.hex2ascii(tmp[i]);

                       }
                       displayText("S== "+j,tmpt);
                   }
               } catch (TagLostException e) {
                   e.printStackTrace();
               } catch (IOException e) {
                   e.printStackTrace();
               }

           }
       }*/
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

        mInfo.setText(mStringBuilder.toString());
    }
    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    /**
     * Check if NFC adapter is enabled. If not, show the user a dialog and let
     * him choose between "Goto NFC Setting", "Use Editor Only" and "Exit App".
     * Also enable NFC foreground dispatch system.
     * @see Common#enableNfcForegroundDispatch(Activity)
     */
    private void checkNfc() {
        // Check if the NFC hardware is enabled.
        if (Common.getNfcAdapter() != null
                && !Common.getNfcAdapter().isEnabled()) {
            // NFC is disabled.
            // Use as editor only?


        } else {
            // NFC is enabled. Hide dialog and enable NFC
            // foreground dispatch.
            if (mOldIntent != getIntent()) {
                int typeCheck = Common.treatAsNewTag(getIntent(), this);
                if (typeCheck == -1 || typeCheck == -2) {
                    // Device or tag does not support Mifare Classic.
                    // Run the only thing that is possible: The tag info tool.
                    Intent i = new Intent(this, TagInfoTool.class);
                    startActivity(i);
                }
                mOldIntent = getIntent();
            }
            Common.enableNfcForegroundDispatch(this);

            if (mEnableNfc == null) {
                createNfcEnableDialog();
            }
            mEnableNfc.hide();
            if (Common.hasMifareClassicSupport() ) {

            }
        }
    }

    /**
     * Create a dialog that send user to NFC settings if NFC is off (and save
     * the dialog in {@link #mEnableNfc}). Alternatively the user can choos to
     * use the App in editor only mode or exit the App.
     */
    private void createNfcEnableDialog() {
        mEnableNfc = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_nfc_not_enabled_title)
                .setMessage(R.string.dialog_nfc_not_enabled)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(R.string.action_nfc,
                        new DialogInterface.OnClickListener() {
                            @Override
                            @SuppressLint("InlinedApi")
                            public void onClick(DialogInterface dialog, int which) {
                                // Goto NFC Settings.
                                if (Build.VERSION.SDK_INT >= 16) {
                                    startActivity(new Intent(
                                            Settings.ACTION_NFC_SETTINGS));
                                } else {
                                    startActivity(new Intent(
                                            Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }
                        })
                .setNeutralButton(R.string.action_editor_only,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Only use Editor.

                            }
                        })
                .setNegativeButton(R.string.action_exit_app,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Exit the App.
                                finish();
                            }
                        }).create();
    }

}