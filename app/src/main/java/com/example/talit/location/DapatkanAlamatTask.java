package com.example.talit.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DapatkanAlamatTask extends AsyncTask<Location, Void, String> {

    private Context mContext;
    private onTaskSelesai mListener;

    DapatkanAlamatTask(Context applicationContext, onTaskSelesai listener) {
        mContext = applicationContext;
        mListener = listener;
    }

    @Override
    protected String doInBackground(Location... locations) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        Location location = locations[0];
        List<Address> alamat = null;
        String resultMessage = "";

        try {//try catch u/mendapatkan data list alamat dr objek location yg ngambil
            alamat = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) { //untu menangkap eror apabila long dan latitude tidak valid.
            resultMessage = "service tidak tersedia";
            Log.e("Lokasi eror", resultMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            resultMessage = "koordinat invalid";
            Log.e("LokasiEror", resultMessage + "." +
                            "Latitude = " + location.getLatitude() +
                            ",Longtitude = " + location.getLongitude(),
                    illegalArgumentException);
        }

        // jika alamt tida di temukan, maka eror.
        if (alamat == null || alamat.size() == 0) {
            if (resultMessage.isEmpty()) {
                resultMessage = "alamat tidak ditemukan";
                Log.e("lokasierror", resultMessage);
            }
        } else {
            Address address = alamat.get(0);
            ArrayList<String> barisAlamat = new ArrayList<>();

            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                barisAlamat.add(address.getAddressLine(i));
            }


            resultMessage = TextUtils.join("\n", barisAlamat);
        }

        return resultMessage;
    }


    @Override
    protected void onPostExecute(String alamat) {
        super.onPostExecute(alamat);
        mListener.onTaskCompleted(alamat);
    }

    interface onTaskSelesai {
        void onTaskCompleted(String result);
    }
}
