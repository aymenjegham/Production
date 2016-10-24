package com.nfctag.printsecure.androidnfc;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Print Secure on 4/13/2016.
 */
public class Common extends Application {

    private static final String LOG_TAG = Common.class.getSimpleName();
    private static Tag mTag = null;
    private static byte[] mUID = null;
    private static int mHasMifareClassicSupport = 0;
    private static String mVersionCode;
    private static NfcAdapter mNfcAdapter;
    private static Context mAppContext;
    public void onCreate() {
        super.onCreate();
        mAppContext = getApplicationContext();
        try {
            mVersionCode = getPackageManager().getPackageInfo(
                    getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, "Version not found.");
        }
    }
    public static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mAppContext);
    }



    /**
     * Create a connected {@link MCReader} if there is a present Mifare Classic
     * tag. If there is no Mifare Classic tag an error
     * message will be displayed to the user.
     * @param context The Context in which the error Toast will be shown.
     * @return A connected {@link MCReader} or "null" if no tag was present.
     */
    public static MCReader checkForTagAndCreateReader(Context context) {

        MCReader reader;
        boolean tagLost = false;
        // Check for tag.
        if (mTag != null && (reader = MCReader.get(mTag)) != null) {
            try {
                reader.connect();
            } catch (Exception e) {
                tagLost = true;
            }
            if (!tagLost && !reader.isConnected()) {
                reader.close();
                tagLost = true;
            }
            if (!tagLost) {
                return reader;
            }
        }

        // Error. The tag is gone.
        Toast.makeText(context, R.string.info_no_tag_found,
                Toast.LENGTH_LONG).show();

        return null;
    }


    /**
     * Enables the NFC foreground dispatch system for the given Activity.
     * @param targetActivity The Activity that is in foreground and wants to
     * have NFC Intents.
     * @see #disableNfcForegroundDispatch(Activity)
     */
    public static void enableNfcForegroundDispatch(Activity targetActivity) {
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {

            Intent intent = new Intent(targetActivity,
                    targetActivity.getClass()).addFlags(
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    targetActivity, 0, intent, 0);
            mNfcAdapter.enableForegroundDispatch(
                    targetActivity, pendingIntent, null, new String[][] {
                            new String[] { NfcA.class.getName() } });
        }
    }

    /**
     * Disable the NFC foreground dispatch system for the given Activity.
     * @param targetActivity An Activity that is in foreground and has
     * NFC foreground dispatch system enabled.
     * @see #enableNfcForegroundDispatch(Activity)
     */
    public static void disableNfcForegroundDispatch(Activity targetActivity) {
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mNfcAdapter.disableForegroundDispatch(targetActivity);
        }
    }

    /**
     * For Activities which want to treat new Intents as Intents with a new
     * Tag attached. If the given Intent has a Tag extra, it will be patched
     * by {@link MCReader#patchTag(Tag)} and  {@link #mTag} as well as
     * {@link #mUID} will be updated. A Toast message will be shown in the
     * Context of the calling Activity. This method will also check if the
     * device/tag supports Mifare Classic (see return values and
     * {@link #checkMifareClassicSupport(Tag, Context)}).
     * @param intent The Intent which should be checked for a new Tag.
     * @param context The Context in which the Toast will be shown.
     * @return
     * <ul>
     * <li>0 - The device/tag supports Mifare Classic</li>
     * <li>-1 - Device does not support Mifare Classic.</li>
     * <li>-2 - Tag does not support Mifare Classic.</li>
     * <li>-3 - Error (tag or context is null).</li>
     * <li>-4 - Wrong Intent (action is not "ACTION_TECH_DISCOVERED").</li>
     * </ul>
     * @see #mTag
     * @see #mUID
     * @see #checkMifareClassicSupport(Tag, Context)
     */
    public static int treatAsNewTag(Intent intent, Context context) {
        // Check if Intent has a NFC Tag.
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            setTag(tag);

            // Show Toast message with UID.
            String id = context.getResources().getString(
                    R.string.info_new_tag_found) + " (UID: ";
            id += byte2HexString(tag.getId());
            id += ")";
            Toast.makeText(context, id, Toast.LENGTH_LONG).show();
            return checkMifareClassicSupport(tag, context);
        }
        return -4;
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
    /**
     * Create a colored string.
     * @param data The text to be colored.
     * @param color The color for the text.
     * @return A colored string.
     */
    public static SpannableString colorString(String data, int color) {
        SpannableString ret = new SpannableString(data);
        ret.setSpan(new ForegroundColorSpan(color),
                0, data.length(), 0);
        return ret;
    }
    /**
     * Get the current active (last detected) Tag.
     * @return The current active Tag.
     * @see #mTag
     */
    public static Tag getTag() {
        return mTag;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i+1), 16));
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Argument(s) for hexStringToByteArray(String s)"
                    + "was not a hex string");
        }
        return data;
    }

    /**
     * Check if the tag and the device support the Mifare Classic technology.
     * @param tag The tag to check.
     * @param context The context of the package manager.
     * @return
     * <ul>
     * <li>0 - Device and tag support Mifare Classic.</li>
     * <li>-1 - Device does not support Mifare Classic.</li>
     * <li>-2 - Tag does not support Mifare Classic.</li>
     * <li>-3 - Error (tag or context is null).</li>
     * </ul>
     */
    public static int checkMifareClassicSupport(Tag tag, Context context) {
        if (tag == null || context == null) {
            // Error.
            return -3;
        }

        if (Arrays.asList(tag.getTechList()).contains(
                MifareClassic.class.getName())) {
            // Device and tag support Mifare Classic.
            return 0;

            // This is no longer valid. There are some devices (e.g. LG's F60)
            // that have this system feature but no Mifare Classic support.
            // (The F60 has a Broadcom NFC controller.)
        /*
        } else if (context.getPackageManager().hasSystemFeature(
                "com.nxp.mifare")){
            // Tag does not support Mifare Classic.
            return -2;
        */

        } else {
            // Check if device does not support Mifare Classic.
            // For doing so, check if the ATQA + SAK of the tag indicate that
            // it's a Mifare Classic tag.
            // See: http://www.nxp.com/documents/application_note/AN10833.pdf
            // (Table 5 and 6)
            NfcA nfca = NfcA.get(tag);
            byte[] atqa = nfca.getAtqa();
            if (atqa[1] == 0 &&
                    (atqa[0] == 4 || atqa[0] == (byte)0x44 ||
                            atqa[0] == 2 || atqa[0] == (byte)0x42)) {
                // ATQA says it is most likely a Mifare Classic tag.
                byte sak = (byte)nfca.getSak();
                if (sak == 8 || sak == 9 || sak == (byte)0x18 ||
                        sak == (byte)0x88) {
                    // SAK says it is most likely a Mifare Classic tag.
                    // --> Device does not support Mifare Classic.
                    return -1;
                }
            }
            // Nope, it's not the device (most likely).
            // The tag does not support Mifare Classic.
            return -2;
        }
    }

    /**
     * Get the App wide used NFC adapter.
     * @return NFC adapter.
     */
    public static NfcAdapter getNfcAdapter() {
        return mNfcAdapter;
    }

    public static void setNfcAdapter(NfcAdapter nfcAdapter) {
        mNfcAdapter = nfcAdapter;
    }

    public static void setTag(Tag tag) {
        mTag = tag;
        mUID = tag.getId();
    }
    public static boolean hasMifareClassicSupport() {
        if (mHasMifareClassicSupport != 0) {
            return mHasMifareClassicSupport == 1;
        }

        // Check for the MifareClassic class.
        // It is most likely there on all NFC enabled phones.
        // Therefore this check is not needed.
        /*
        try {
            Class.forName("android.nfc.tech.MifareClassic");
        } catch( ClassNotFoundException e ) {
            // Class not found. Devices does not support Mifare Classic.
            return false;
        }
        */

        // Check if there is the NFC device "bcm2079x-i2c".
        // Chips by Broadcom don't support Mifare Classic.
        // This could fail because on a lot of devices apps don't have
        // the sufficient permissions.
        File device = new File("/dev/bcm2079x-i2c");
        if (device.exists()) {
            mHasMifareClassicSupport = -1;
            return false;
        }

        // Check if there is the NFC device "pn544".
        // The PN544 NFC chip is manufactured by NXP.
        // Chips by NXP support Mifare Classic.
        device = new File("/dev/pn544");
        if (device.exists()) {
            mHasMifareClassicSupport = 1;
            return true;
        }

        // Check if there are NFC libs with "brcm" in their names.
        // "brcm" libs are for devices with Broadcom chips. Broadcom chips
        // don't support Mifare Classic.
        File libsFolder = new File("/system/lib");
        File[] libs = libsFolder.listFiles();
        for (File lib : libs) {
            if (lib.isFile()
                    && lib.getName().startsWith("libnfc")
                    && lib.getName().contains("brcm")
                // Add here other non NXP NFC libraries.
                    ) {
                mHasMifareClassicSupport = -1;
                return false;
            }
        }
        mHasMifareClassicSupport = 1;
        return true;
    }

}
