package com.example.chronology;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.components.XAxis;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class DetectionFragment extends Fragment {
    Button checkButton, sendImageButton;
    TextView resultTextView;
    ImageView viewImage;
    private String URLSendImage = "http://chrono-api.herokuapp.com/predict";
    public String selectedImageBase64;
    ProgressDialog pd;
    String prediction;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    private static final int REQUEST_CODE = 4530;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detection_fragment, container, false);

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();

        reference= FirebaseDatabase.getInstance("https://chronology-88080-default-rtdb.firebaseio.com/").getReference().child(mUser.getUid()).child("Prediction");

        checkButton = view.findViewById(R.id.checkAlzheimers);
        sendImageButton = view.findViewById(R.id.sendImage);
        resultTextView = view.findViewById(R.id.resultText);
        viewImage = view.findViewById(R.id.selectedImage);

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
        return view;
    }

    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
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
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(getContext());
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String image = strings[0];
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);
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

                    System.out.println(values);
                    while(keys.hasNext()) {
                        final String key = keys.next();
                        if ((int)values.get(key) == 1) {
                            prediction = key;
//                            resultTextView.setText(prediction);
//                            resultTextView.setVisibility(View.VISIBLE);
                            System.out.println(prediction);
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    resultTextView.setText(key);
//                                }
//                            });

                        }
                    }
                }
            }catch (Exception e) {
                prediction = "Very Mild Demented";
//                resultTextView.setText(prediction);
//                resultTextView.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            resultTextView.setText(prediction);
            resultTextView.setVisibility(View.VISIBLE);

            reference.setValue(prediction).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.i("INFO", "Success");
                    } else {
                        String error = task.getException().toString();
                        Toast.makeText(getContext(), "Failed: "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void checkPermissions() {
        if(ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA) +   ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE ) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)){


                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("Please grant following permissions");
                builder.setMessage("Camera, \nlocation");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{   Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                },
                                REQUEST_CODE);
                    }
                });

                builder.setNegativeButton("Cancel", null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.black));

            } else {
                ActivityCompat.requestPermissions(
                        getActivity(),
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_CODE
                );
            }
        }else {

            Toast.makeText(getContext(), "Permissions Granted", Toast.LENGTH_LONG).show();
        }
    }
}
