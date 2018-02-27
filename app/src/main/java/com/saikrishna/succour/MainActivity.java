package com.saikrishna.succour;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements LocationListener {

    Boolean tookPic = false;
    ImageButton takePicture;

    Spinner question0, question1, question2;
    EditText description, number;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    Uri filePath;
    LocationManager locationManager;
    String longitude = "", latitude = "", address = "", city = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }

        if (isReadStoragePermissionGranted() && isWriteStoragePermissionGranted()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 0);
        }

        description = findViewById(R.id.description);
        number = findViewById(R.id.number);

        takePicture = findViewById(R.id.takePicture);

        question0 = findViewById(R.id.question0);

        String[] answer0 = new String[]{"Low", "Normal", "High", "Critical"};
        ArrayAdapter<String> adapter0 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, answer0);
        question0.setAdapter(adapter0);

        question1 = findViewById(R.id.question1);

        String[] answer1 = new String[]{"Road/Vehicle accident", "Fire accident", "Electric Shock accident", "Slip or Trip", "Other"};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, answer1);
        question1.setAdapter(adapter1);

        question2 = findViewById(R.id.question2);

        String[] answer2 = new String[]{"Injured and Bleeding", "Injured but not bleeding", "Minor Fracture(s)", "Major Fracture(s)", "Severe Burns", "Minor Burns", "Unconscious", "Unfamiliar"};

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, answer2);
        question2.setAdapter(adapter2);
    }

    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("zxc","Permission is granted1");
                return true;
            } else {

                Log.v("zxc","Permission is revoked1");
                Toast.makeText(MainActivity.this, "Allow us to take Photos", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Zxc","Permission is granted1");
            return true;
        }
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("zxc","Permission is granted2");
                return true;
            } else {

                Toast.makeText(MainActivity.this, "Allow us to take Photos", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("zxc","Permission is granted2");
            return true;
        }
    }

    public void process(View view) {
        if (isReadStoragePermissionGranted() && isWriteStoragePermissionGranted()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 0);
        }
    }

    public void submit(View view) {
        if (tookPic) {
            getLocation();
            if (!city.equals("")) {
                if (!number.getText().toString().equals("")) {
                    uploadImage();
                } else {
                    Toast.makeText(MainActivity.this, "Enter number of victims", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(MainActivity.this, "Take a Photo", Toast.LENGTH_SHORT).show();
        }
    }

    String rand = "";

    public void uploadImage() {
        if(filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            rand = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("images/"+ rand);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            imageUploaded(taskSnapshot.getDownloadUrl().toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    public void imageUploaded(String url) {
        Log.e("zxc", number.getText().toString());
        FirebaseDatabase.getInstance()
                .getReference(city.toLowerCase())
                .push()
                .setValue(new Succour(url, Integer.parseInt(number.getText().toString()), question0.getSelectedItemPosition() + 1, question1.getSelectedItemPosition() + 1, question2.getSelectedItemPosition() + 1, description.getText().toString(), address, latitude, longitude));

        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                Log.d("zxc", "External storage2");
                if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v("zxc","Permission: "+permissions[0]+ "was "+grantResults[0]);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 0);
                } else{
//                    finish();
                }
                break;

            case 3:
                Log.d("zxc", "External storage1");
                if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v("zxc","Permission: "+permissions[0]+ "was "+grantResults[0]);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 0);
                }else{
//                    finish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && isReadStoragePermissionGranted() && isWriteStoragePermissionGranted()) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            takePicture.setBackgroundDrawable(bitmapDrawable);
            tookPic = true;

            filePath = getImageUri(this, bitmap);
            getLocation();
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("zxc", "Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());

        longitude = String.valueOf(location.getLatitude());
        latitude = String.valueOf(location.getLongitude());

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0)+", "+
                    addresses.get(0).getAddressLine(1)+", "+addresses.get(0).getAddressLine(2);
            city = addresses.get(0).getLocality();
            Log.e("zxc", addresses.get(0).getLocality());
            Log.e("zxc", addresses.get(0).getAddressLine(0)+", "+
                    addresses.get(0).getAddressLine(1)+", "+addresses.get(0).getAddressLine(2));
        } catch(Exception e) {

        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

}

