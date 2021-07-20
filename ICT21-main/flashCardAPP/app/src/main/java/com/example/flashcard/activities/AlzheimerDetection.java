package com.example.flashcard.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlzheimerDetection extends AppCompatActivity {

    Button checkButton, sendImageButton;
    TextView resultTextView;
    ImageView viewImage;
    private String URLSendImage = "http://192.168.1.104:5000/predict";
    public String selectedImageBase64;

    private static final int REQUEST_CODE = 4530;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alzheimer_detection);
        checkButton = findViewById(R.id.checkAlzheimers);
        sendImageButton = findViewById(R.id.sendImage);
        resultTextView = findViewById(R.id.resultText);
        viewImage = findViewById(R.id.selectedImage);

        checkPermissions();

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new sendImage().execute(selectedImageBase64);
            }
        });
    }

    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        viewImage.setImageBitmap(selectedImage);
                        sendImageButton.setVisibility(View.VISIBLE);
                        selectedImageBase64 = encodeTobase64(selectedImage);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                            viewImage.setImageBitmap(bitmap);
                            selectedImageBase64 = encodeTobase64(bitmap);
                            sendImageButton.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    public static String encodeTobase64(Bitmap bmp) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageBytes = baos.toByteArray();

            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            return encodedImage;
    }


    class  sendImage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String image = strings[0];
            OkHttpClient okHttpClient = new OkHttpClient();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("base64Image", image);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");

            RequestBody formBody = RequestBody.create(JSON, String.valueOf(jsonObject));
            Request request = new Request.Builder()
                    .url(URLSendImage)
                    .post(formBody)
                    .build();

            Response response = null;
                try{
                    response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        final JSONObject jsonResp = new JSONObject(result);
                        JSONObject values = jsonResp.getJSONObject("results");
                        Iterator<String> keys = values.keys();
                        resultTextView.setVisibility(View.VISIBLE);
                        System.out.println(values);
                        while(keys.hasNext()) {
                            String key = keys.next();
                            if ((int)values.get(key) == 1) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        resultTextView.setText(key);
                                    }
                                });

                            }
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
        }
    }



    private void checkPermissions() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) +   ContextCompat.checkSelfPermission(this,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE ) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)){


                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Please grant following permissions");
                builder.setMessage("Camera, \nlocation");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(AlzheimerDetection.this,
                                new String[]{   Manifest.permission.CAMERA,
                                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                },
                                REQUEST_CODE);
                    }
                });

                builder.setNegativeButton("Cancel", null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(AlzheimerDetection.this, R.color.black));
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(AlzheimerDetection.this, R.color.black));

            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_CODE
                );
            }
        }else {

            Toast.makeText(this, "Permissions Granted", Toast.LENGTH_LONG).show();
        }
    }
}