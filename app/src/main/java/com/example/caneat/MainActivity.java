package com.example.caneat;
import static android.Manifest.permission_group.CAMERA;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity {

    Button picture_button;
    FirebaseDatabase database;
    List<User> mUser;

    TextView vegan;
    TextView religion;
    TextView allergic;
    TextView ingredient;
    TextView txtResult;

    public String veg;
    public String rel;
    public String ing;
    public String adding;
    public String[] ingarray;

    static final int REQUEST_IMAGE_CAPTURE = 1; //????????? ??????
    Bitmap bitmap;
    InputImage image;
    TextView textview1;
    public String OCRTEXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        database = FirebaseDatabase.getInstance();

        txtResult = (TextView)findViewById(R.id.txtResult);
        Button change_allergic = findViewById(R.id.change_allergic);
        change_allergic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent change_allergic = new Intent(getApplicationContext(), Change_allergic_activity.class);
                startActivity(change_allergic);
            }
        });
        Button change_religion = (Button) findViewById(R.id.change_religion);
        change_religion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent change_religion = new Intent(getApplicationContext(), Change_religion_activity.class);
                startActivity(change_religion);
            }
        });

        Button vegan_change = findViewById(R.id.vegan_change);
        vegan_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vegan_change = new Intent(getApplicationContext(), Change_vegan_activity.class);
                startActivity(vegan_change);
            }
        });

        Button My_info = (Button) findViewById(R.id.myinfo);
        My_info.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent My_info = new Intent(getApplicationContext(), Myinfo_activity.class);
                startActivity(My_info);
            }
        });

        Button change_ingredient = findViewById(R.id.change_ingredient);
        change_ingredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent change_ingredient = new Intent(getApplicationContext(), Change_ingredient_activity.class);
                startActivity(change_ingredient);
            }
        });

        Button mypage=findViewById(R.id.mypage);
        mypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Mypage=new Intent(getApplicationContext(),Mypage_activity.class);
                startActivity(Mypage);
            }
        });

        vegan = findViewById(R.id.vegan_content);
        religion = findViewById(R.id.religion_content);
        allergic = findViewById(R.id.allergic_content);
        ingredient = findViewById(R.id.ingredient_content);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        DatabaseReference mydb = database.getReference("user").child(uid);
        mUser = new ArrayList<>();

        mydb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();
                User user = dataSnapshot.getValue(User.class);
                mUser.add(user);
                veg = mUser.get(0).getVegan();
                rel = mUser.get(0).getReligion();
                ing = mUser.get(0).getIngredient();
                adding = mUser.get(0).getAddingredient();

                String viewing = "";
                ingarray = ing.split("@");
                for (String s : ingarray) {
                    viewing = (viewing + s + " ");
                }
                vegan.setText("??? ??????: " + veg);
                religion.setText("??? ??????: " + rel);
                allergic.setText("??? ????????????:" + viewing);
                ingredient.setText("??? ??????: " + adding);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Log.e("MainActivity", String.valueOf(databaseError.toException())); // ????????? ??????
            }
        });


        picture_button = findViewById(R.id.picture_button);
        picture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int checkPermissions = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                if (checkPermissions == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
                } else {
                    showCamera();
                }
            }
        });

    }

    public void showCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == 0) {
                Toast.makeText(this, "????????? ??????", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "????????? ??????", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            getTextFromImage();
        }
    }

    private void getTextFromImage() {
        OCRTEXT = "";
        image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(@NonNull Text Text) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Text.TextBlock block : Text.getTextBlocks()) {
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect linRect = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            stringBuilder.append(elementText);
                        }
                    }
                    OCRTEXT = OCRTEXT + blockText;
                }
                Log.d("ocr??????",OCRTEXT);
                compareIngredient(OCRTEXT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void compareIngredient(String OCRTEXT) {
        ArrayList<String> messageText = new ArrayList<>();
        String message="";

        if (!(veg.equals("??????"))) {  //?????? ??????

            if (veg.equals("??????")) {
                String[] veganarray = {"??????", "????????????", "???", "?????????", "?????????", "?????????", "?????????", "??????", "????????????", "??????", "?????????", "??????", "??????", "?????????", "??????",
                        "?????????", "??????", "??????", "????????????", "??????", "??????", "????????????", "?????????", "?????????", "??????", "??????", "??????", "??????", "??????", "??????", "???", "??????",
                        "?????????", "E120", "E322", "E422", "E471", "E542", "E631", "E901", "E904"};
                for (String s : veganarray) {
                    if (OCRTEXT.contains(s)) {
                        if(!(messageText.contains(s))) messageText.add(s);
                    }
                }
            } else if (veg.equals("?????? ???????????????")) {
                String[] veganarray = {"??????", "????????????", "???", "?????????", "?????????", "?????????", "?????????", "??????", "????????????", "??????", "?????????", "??????", "??????", "?????????", "??????",
                        "?????????", "??????", "??????", "????????????", "??????", "??????", "????????????", "?????????", "?????????", "??????", "?????????"};
                for (String s : veganarray) {
                    if (OCRTEXT.contains(s)) {
                        if(!(messageText.contains(s))) messageText.add(s);
                    }
                }
            } else if (veg.equals("?????? ???????????????")) {
                String[] veganarray = {"??????", "????????????", "???", "?????????", "?????????", "?????????", "?????????", "??????", "????????????", "??????", "?????????", "??????", "??????", "?????????", "??????",
                        "?????????", "??????", "??????", "????????????", "??????", "??????", "??????", "??????", "??????", "?????????"};
                for (String s : veganarray) {
                    if (OCRTEXT.contains(s)) {
                        if(!(messageText.contains(s))) messageText.add(s);
                    }
                }
            } else if (veg.equals("?????? ?????? ???????????????")) {
                String[] veganarray = {"??????", "????????????", "???", "?????????", "?????????", "?????????", "?????????", "??????", "????????????", "??????", "?????????", "??????", "??????", "?????????", "??????",
                        "?????????", "??????", "??????", "????????????", "?????????"};
                for (String s : veganarray) {
                    if (OCRTEXT.contains(s)) {
                        if(!(messageText.contains(s))) messageText.add(s);
                    }
                }
            } else if (veg.equals("????????? ???????????????")) {
                String[] veganarray = {"??????", "????????????", "???", "?????????", "?????????", "?????????", "?????????", "??????", "????????????", "??????", "?????????"};
                for (String s : veganarray) {
                    if (OCRTEXT.contains(s)) {
                        if(!(messageText.contains(s))) messageText.add(s);
                    }
                }
            } else if (veg.equals("?????? ???????????????")) {
                String[] veganarray = {"??????", "????????????", "?????????", "?????????", "?????????"};
                for (String s : veganarray) {
                    if (OCRTEXT.contains(s)) {
                        if(!(messageText.contains(s))) messageText.add(s);
                    }
                }
            }

        }

        if (!(rel.equals("??????"))) {   //?????? ??????
            if (rel.equals("??????")) {
                String[] religionarray = {"??????", "????????????", "???", "?????????", "?????????", "?????????", "?????????", "??????", "????????????", "??????", "?????????", "??????", "??????", "?????????", "??????",
                        "?????????", "??????", "??????", "????????????", "??????", "??????", "????????????", "?????????", "?????????", "??????", "?????????"};
                for (String s : religionarray) {
                    if (OCRTEXT.contains(s)) {
                        if(!(messageText.contains(s))) messageText.add(s);
                    }
                }
            } else if (rel.equals("????????????")) {
                String[] religionarray = {"??????", "????????????"};
                for (String s : religionarray) {
                    if (OCRTEXT.contains(s)) {
                        if(!(messageText.contains(s))) messageText.add(s);
                    }
                }
            } else if (rel.equals("?????????")) {
                String[] religionarray = {"?????????", "?????????"};
                for (String s : religionarray) {
                    if (OCRTEXT.contains(s)) {
                        if(!(messageText.contains(s))) messageText.add(s);
                    }
                }
            }
        }


        if (!(ing.equals("??????"))) {   //???????????? ??????
            ingarray = ing.split("@");
            for (String s : ingarray) {
                if(!(s.equals(""))) {
                    if (OCRTEXT.contains(s)) {
                        if (!(messageText.contains(s))) messageText.add(s);
                    }
                }
            }
        }
        //messageText.get(0).equals("")

        if(messageText.isEmpty()) {
            message="??? ????????? ????????? ?????? ???????????????.";
        }
        else {
            for(int i=0; i<messageText.size(); i++){
                message+=messageText.get(i)+" ";
            }
            message+="????????? ???????????? ?????? ??? ????????????.";
        }

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("data", message);
        startActivityForResult(intent, 1);
        String result = intent.getStringExtra("result");
        txtResult.setText(result);

    }

}