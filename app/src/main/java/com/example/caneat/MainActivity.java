package com.example.caneat;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    ImageView imageview1;//사진 미리보기
    TextView textview1;
    Bitmap bitmap;
    FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();// ocr 함수 초기화를 위한 변수 설정// 변수 mfunctions를 이용해 functions 초기화


    static final int REQUEST_IMAGE_CAPTURE = 1; //카메라 변수

    public MainActivity() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        ImageView img_test = findViewById(R.id.image);

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://javaproject-caneat.appspot.com/");
        StorageReference storageRef = storage.getReference();
        storageRef.child("imagefile/20211016_231825.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //이미지 로드 성공시

                Glide.with(getApplicationContext())
                        .load(uri)
                        .into(img_test);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //이미지 로드 실패시
                Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
            }
        });



        //imageview1 = findViewById(R.id.image);
        textview1 = findViewById(R.id.text1);



        Button My_info = (Button) findViewById(R.id.myinfo);
        My_info.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent My_info = new Intent(getApplicationContext(),Myinfo_activity.class);
                startActivity(My_info);
            }
        });



    }



    public void showCameraBtn(View view){ //카메라 버튼
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //이미지 bitmap로 가져오기  (android developer)
        //(android developer)bitmap으로 가져오기
        super.onActivityResult(requestCode, resultCode, data);
       // if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //bitmap = (Bitmap) extras.get("data");
           // imageview1.setImageBitmap(bitmap);

            //파이어베이스 cloud vision api 사용 코드
            // Scale down bitmap size
            bitmap = scaleBitmapDown(bitmap, 640);/////////////////bitmap 사이즈 설정
            // Convert bitmap to base64 encoded string
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            //json요청
            //Create json request to cloud vision
            JsonObject request = new JsonObject();
            //Add image to request
            JsonObject image = new JsonObject();
            image.add("content", new JsonPrimitive(base64encoded));
            request.add("image", image);
            //Add features to the request
            JsonObject feature = new JsonObject();
            feature.add("type", new JsonPrimitive("TEXT_DETECTION"));
            //Alternatively, for DOCUMENT_TEXT_DETECTION:
            //feature.add("type", new JsonPrimitive("DOCUMENT_TEXT_DETECTION"));
            JsonArray features = new JsonArray();
            features.add(feature);
            request.add("features", features);

            //언어힌트로 언어감지
            JsonObject imageContext = new JsonObject();
            JsonArray languageHints = new JsonArray();
            languageHints.add("ko");//한국어설정
            languageHints.add("en");//영어설정
            imageContext.add("languageHints", languageHints);
            request.add("imageContext", imageContext);

            //함수호출
            annotateImage(request.toString())
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast toast = Toast.makeText(MainActivity.this, "이미지 없음", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            //인식된 텍스트 문자열로 가져오기
                            Toast toast = Toast.makeText(MainActivity.this,"이미지 있음", Toast.LENGTH_SHORT);
                            toast.show();
                            JsonObject annotation = Objects.requireNonNull(task.getResult()).getAsJsonArray().get(0).getAsJsonObject().get("fullTextAnnotation").getAsJsonObject();
                            System.out.format("%nComplete annotation:%n");
                            String ak = String.format("%s%n", annotation.get("text").getAsString());
                            Log.d("로그확인", "ㅁㄴㅇㄹ");
                            textview1.setText(ak);
                        }

                    });
        }




    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) { // 이미지축소
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private Task<JsonElement> annotateImage(String requestJson) {
        return mFunctions
                .getHttpsCallable("annotateImage")
                .call(requestJson)
                .continueWith(new Continuation<HttpsCallableResult, JsonElement>() {
                    @Override
                    public JsonElement then(@NonNull Task<HttpsCallableResult> task) {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        return JsonParser.parseString(new Gson().toJson(task.getResult().getData()));
                    }
                });
    }


}