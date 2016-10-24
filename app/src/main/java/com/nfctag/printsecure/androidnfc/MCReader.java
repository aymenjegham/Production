package com.nfctag.printsecure.androidnfc;

import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Print Secure on 4/13/2016.
 */
public class MCReader {
    private static final String LOG_TAG = MCReader.class.getSimpleName();
    /**
     * Placeholder for not found keys.
     */
    public static final String NO_KEY = "------------";
    /**
     * Placeholder for unreadable blocks.
     */
    public static final String NO_DATA = "--------------------------------";

    private final MifareClassic mMFC;
    MCReader(Tag tag) {
        MifareClassic tmpMFC = null;
        try {
            tmpMFC = MifareClassic.get(tag);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not create Mifare Classic reader for the"
                    + "provided tag (even after patching it).");

        }
        mMFC = tmpMFC;
    }
    private boolean authenticate(int sectorIndex, byte[] key,
                                 boolean useAsKeyB) {
        try {
            if (!useAsKeyB) {
                // Key A.
                return mMFC.authenticateSectorWithKeyA(sectorIndex, key);
            } else {
                // Key B.
                return mMFC.authenticateSectorWithKeyB(sectorIndex, key);
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error authenticating with tag.");
        }
        return false;
    }

    public String[] readSector(int sectorIndex, byte[] key,
                               boolean useAsKeyB) throws TagLostException {
        boolean auth = authenticate(sectorIndex, key, useAsKeyB);
        String[] ret = null;
        Log.e(LOG_TAG, useAsKeyB+ " block key "
                + byte2HexString(key)+ " index "+sectorIndex );
        // Read sector.
        if (auth) {
            Log.e(LOG_TAG, "block Text "
                    + hex2ascii(byte2HexString(key)) );
            // Read all blocks.
            ArrayList<String> blocks = new ArrayList<String>();
            int firstBlock = mMFC.sectorToBlock(sectorIndex);
            int lastBlock = firstBlock + 4;
            if (mMFC.getSize() == MifareClassic.SIZE_4K
                    && sectorIndex > 31) {
                lastBlock = firstBlock + 15;
            }
            for (int i = firstBlock; i < lastBlock; i++) {
                try {
                    byte blockBytes[] = mMFC.readBlock(i);
                    // mMFC.readBlock(i) must return 16 bytes or throw an error.
                    // At least this is what the documentation says.
                    // On Samsung's Galaxy S5 and Sony's Xperia Z2 however, it
                    // sometimes returns < 16 bytes for unknown reasons.
                    // Update: Aaand sometimes it returns more than 16 bytes...
                    // The appended byte(s) are 0x00.
                    if (blockBytes.length < 16) {
                        throw new IOException();
                    }
                    if (blockBytes.length > 16) {
                        byte[] blockBytesTmp = Arrays.copyOf(blockBytes, 16);
                        blockBytes = blockBytesTmp;
                    }

                    blocks.add(byte2HexString(blockBytes));
                    Log.e(LOG_TAG, "block Text "
                            + byte2HexString(blockBytes));
                    Log.e(LOG_TAG, "block Text "
                            + hex2ascii(byte2HexString(blockBytes)) );
                } catch (TagLostException e) {
                    throw e;
                } catch (IOException e) {
                    // Could not read block.
                    // (Maybe due to key/authentication method.)
                    Log.d(LOG_TAG, "(Recoverable) Error while reading block "
                            + i + " from tag.");
                    blocks.add(NO_DATA);

                    if (!mMFC.isConnected()) {
                        throw new TagLostException(
                                "Tag removed during readSector(...)");
                    }
                    // After an error, a re-authentication is needed.
                    authenticate(sectorIndex, key, useAsKeyB);
                }
            }
            ret = blocks.toArray(new String[blocks.size()]);
            int last = ret.length -1;

            // Merge key in last block (sector trailer).
            if (!useAsKeyB) {
                if (isKeyBReadable(hexStringToByteArray(
                        ret[last].substring(12, 20)))) {
                    ret[last] = byte2HexString(key)
                            + ret[last].substring(12, 32);
                } else {
                    ret[last] = byte2HexString(key)
                            + ret[last].substring(12, 20) + NO_KEY;
                }
            } else {
                if (ret[0].equals(NO_DATA)) {
                    // If Key B may be read in the corresponding Sector Trailer,
                    // it cannot serve for authentication (according to NXP).
                    // What they mean is that you can authenticate successfully,
                    // but can not read data. In this case the
                    // readBlock() result is 0 for each block.
                    ret = null;
                } else {
                    ret[last] = NO_KEY + ret[last].substring(12, 20)
                            + byte2HexString(key);
                }
            }
        }

        return ret;
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

    public  String hex2ascii(String hex){
        String str="";
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< hex.length(); i+=2){
            str=hex.substring(i, i+2);

            if(str.compareToIgnoreCase("00")==0) return sb.toString();
            sb.append((char)Integer.parseInt(str,16));
        }
        return sb.toString();
    }
    private boolean isKeyBReadable(byte[] ac) {
        byte c1 = (byte) ((ac[1] & 0x80) >>> 7);
        byte c2 = (byte) ((ac[2] & 0x08) >>> 3);
        byte c3 = (byte) ((ac[2] & 0x80) >>> 7);
        return c1 == 0
                && (c2 == 0 && c3 == 0)
                || (c2 == 1 && c3 == 0)
                || (c2 == 0 && c3 == 1);
    }
    public void connect() throws IOException {
        try {
            mMFC.connect();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error while connecting to tag.");
            throw e;
        }
    }
    public void close() {
        try {
            mMFC.close();
        }
        catch (IOException e) {
            Log.d(LOG_TAG, "Error on closing tag.");
        }
    }

    public String [] ReadCard(){

        String [] arrStr = new String[18];
        try {

            //reader.connect();

            for(int j=0;j<15;j++ ) {
                String [] tmp;
                String  tmpt="";
                //  tmp = reader.readSector(j, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,}, true);
                // for(int i =0 ; i<a.length;i++)
                //  if (tmp == null)
                tmp = this.readSector(j, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,}, false);
                if(tmp==null )
                    return null;
                for (int i = 0; i < tmp.length-1; i++) {
                    if(tmp[i].compareTo(NO_DATA)==0)return null;
                    Log.e("String", j+" A== " + tmp[i]);
                    tmpt+=this.hex2ascii(tmp[i]);
                    //tmpt+="\n";
                }
                arrStr[j]=tmpt;
                // displayText("S== "+j,tmpt);
            }
            return arrStr;
        } catch (TagLostException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

    }
    /**
     * Check if the reader is connected to the tag.
     * @return True if the reader is connected. False otherwise.
     */
    public boolean isConnected() {
        return mMFC.isConnected();
    }

    /**
     * Get new instance of {@link MCReader}.
     * If the tag is "null" or if it is not a Mifare Classic tag, "null"
     * will be returned.
     * @param tag The tag to operate on.
     * @return {@link MCReader} object or "null" if tag is "null" or tag is
     * not Mifare Classic.
     */
    public static MCReader get(Tag tag) {
        MCReader mcr = null;
        if (tag != null) {
            mcr = new MCReader(tag);
            if (!mcr.isMifareClassic()) {
                return null;
            }
        }
        return mcr;
    }

    public boolean isMifareClassic() {
        return mMFC != null;
    }
}
