package com.jetec.pdfmakerexample;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName() + "My";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
        EditText edTitle = findViewById(R.id.editText_title);
        EditText edInput = findViewById(R.id.editText_input);
        edInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edInput.setGravity(Gravity.TOP);
        edInput.setSingleLine(false);
        edInput.setHorizontallyScrolling(false);

        Button buttonEx = findViewById(R.id.button_Export);
        Button buttonClear = findViewById(R.id.buttonClear);

        buttonClear.setOnClickListener((v -> {
            edInput.setText("");
            edTitle.setText("");
        }));

        buttonEx.setOnClickListener((v) -> {
            String title = edTitle.getText().toString();
            String input = edInput.getText().toString();
            PDFExport(title, input);

        });

    }//onCreate
    /**@param title 設置文件名稱與抬頭
     * @param input 設置文件內容*/

    private void PDFExport(String title, String input) {
        ProgressDialog dialog = ProgressDialog.show(this, "處理中", "請稍候", true);
        new Thread(() -> {
            try {
                String fileName = "/" +title + ".pdf";
                String mFilePath = Environment.getExternalStorageDirectory() + fileName;
                Document document = new Document(PageSize.A4, 40, 40, 40, 40);
                PdfWriter.getInstance(document, new FileOutputStream(mFilePath));

                //這裏開始寫內容
                document.open();
                LineSeparator line = new LineSeparator(2f, 300, BaseColor.BLACK, Element.ALIGN_CENTER, 20f);
                BaseFont chinese = BaseFont.createFont("assets/kaiu.ttf"
                        , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font titleFont = new Font(chinese, 32);//這是大~標題的
                Font inputFont = new Font(chinese, 16);
                document.add(new Paragraph(new Phrase(20f
                        , title, titleFont)));
                document.add(new Paragraph(" "));
                document.add(new Paragraph(" "));
                document.add(line);
                document.add(new Paragraph(" "));
                document.add(new Paragraph(new Phrase(20f
                        , input, inputFont)));
                document.close();
                //這裏結束寫內容

                runOnUiThread(() -> {
                    dialog.dismiss();
                    output(fileName);
                });
            } catch (Exception e) {
                Log.d(TAG, "PDFExport: " + e);
            }


        }).start();
    }//PDFExport

    private void output(String fileName) {
        //->遇上exposed beyond app through ClipData.Item.getUri() 錯誤時在onCreate加上這行
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        //->遇上exposed beyond app through ClipData.Item.getUri() 錯誤時在onCreate加上這行
        File filelocation = new File(Environment.
                getExternalStorageDirectory(), fileName);
        Uri path = Uri.fromFile(filelocation);

        Intent fileIntent = new Intent(Intent.ACTION_SEND);
        fileIntent.setType("text/plain");
        fileIntent.putExtra(Intent.EXTRA_SUBJECT, "我的資料");
        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
        startActivity(Intent.createChooser(fileIntent, "Send Mail"));
    }
}
