package com.ajayvamsee.activenoise;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ajayvamsee.activenoise.ml.ModelQuant1;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    File saveDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AdoVoice");
    File originalDir = new File(saveDir.getAbsolutePath() + "/original");
    File cleanDir = new File(saveDir.getAbsolutePath() + "/clean");

    String WAV_FILE = "/storage/emulated/0/AdoVoice/ajay_lowbg_lowd.wav";
    String audioFilePathClean = null;

    MediaPlayer originalMP, cleanMP;
    TextView textView;
    byte[] audioBytes;

    int block_len=512;
    int block_shift=128;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.play);

        checkPermission(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);

        fileRead();


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modelRunning();
            }
        });

    }

    private void fileRead() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = null;
        try {

            in = new BufferedInputStream(new FileInputStream(WAV_FILE));
            int read;
            byte[] buff = new byte[1024];
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            out.flush();
            audioBytes = out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void modelRunning() {
        try {
            ModelQuant1 model = ModelQuant1.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 1, 257}, DataType.FLOAT32);
            inputFeature0.loadBuffer(ByteBuffer.wrap(audioBytes));
            TensorBuffer inputFeature1 = TensorBuffer.createFixedSize(new int[]{1, 2, 128, 2}, DataType.FLOAT32);
            inputFeature1.loadBuffer(ByteBuffer.wrap(audioBytes));

            // Runs model inference and gets result.
            ModelQuant1.Outputs outputs = model.process(inputFeature0, inputFeature1);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            TensorBuffer outputFeature1 = outputs.getOutputFeature1AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }

    // Function to check and request permission
    public void checkPermission(String[] permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, Arrays.toString(permission)) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, permission, requestCode);
        } else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

}