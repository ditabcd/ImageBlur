package android.jplas.imageblur;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ResultActivity extends AppCompatActivity {

    ImageView imgResult;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imgResult = findViewById(R.id.imgResult);
        btnBack = findViewById(R.id.btnLogoutResult);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            Bitmap img = (Bitmap)extras.get("image");
            if(imgResult != null){
                imgResult.setImageBitmap(img);
            }
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}
