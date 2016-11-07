package com.rfachrur.gmbr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private CustomView customView;
    private ImageButton currPaint;
    private String comment;
    //these three values are used to define the brush size
    private int smallBrush, mediumBrush, largeBrush;
    private static int TAKING_PHOTO = 0;
    private static int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customView = (CustomView) findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);



        subscribeDrawButtonClickEvent();
        subscribeEraseButtonClickEvent();
        subscribeNewButtonClickEvent();
        subscribeSaveButtonClickEvent();
        subscribeTakingPhotoButtonClickEvent();
        subscribeAddCommentButtonClickEvent();
        subscribeSelectImageButtonClickEvent();

    }

    private void subscribeSelectImageButtonClickEvent(){
        Button selectImgBtn = (Button) findViewById(R.id.select_img);

        selectImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
// Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

    }

    private void subscribeAddCommentButtonClickEvent(){
        ImageButton addCommentBtn = (ImageButton) findViewById(R.id.text_btn);

        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater li = LayoutInflater.from(v.getContext());
                View promptsView = li.inflate(R.layout.edit_text, null);
                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                AlertDialog.Builder EditDialog = new AlertDialog.Builder(v.getContext());
                EditDialog.setTitle("Add Comment");
                EditDialog.setView(promptsView);
                EditDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        comment = userInput.getText().toString();
                        customView.setComment(comment);
                        //Toast.makeText(getApplicationContext(), comment, Toast.LENGTH_SHORT).show();
                    }
                });
                EditDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                EditDialog.show();
            }
        });
    }

    private void subscribeTakingPhotoButtonClickEvent() {
        ImageButton takingPhoto_btn = (ImageButton) findViewById(R.id.takingPhoto_btn);

        takingPhoto_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, TAKING_PHOTO);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == TAKING_PHOTO && resultCode == RESULT_OK) {
            final Bitmap photo = (Bitmap) data.getExtras().get("data");
            if(photo == null)
            {
                Toast.makeText(getApplicationContext(), "no image", Toast.LENGTH_SHORT).show();
            }

            // Execute some code after 2 seconds have passed
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    customView.setImageBitmap(photo);
                }
            }, 1000);
            //drawView.setImageBitmap(photo);
        }
        else if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                customView.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void subscribeSaveButtonClickEvent(){
        ImageButton saveBtn = (ImageButton) findViewById(R.id.save_btn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder saveDialog = new AlertDialog.Builder(v.getContext());
                saveDialog.setTitle("Save drawing");
                saveDialog.setMessage("Save drawing to device Gallery?");
                saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    public void onClick(DialogInterface dialog, int which) {
                        //save drawing
                        customView.setDrawingCacheEnabled(true);
 /*                      String imgSaved = MediaStore.Images.Media.insertImage(
                                getContentResolver(), drawView.getDrawingCache(),
                                UUID.randomUUID().toString() + ".png", "drawing");
 */




                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "image");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "something");
                        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        //values.put(MediaStore.MediaColumns.DATA, drawView.getDrawingCache());
                        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
                        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

                        Uri url = null;
                        String stringUrl = null;    /* value to be returned */
                        Bitmap bmp = customView.getDrawingCache();
                        ContentResolver cr = getContentResolver();
                        try {
                            url =cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                            if (bmp != null) {
                                assert url != null;
                                try (OutputStream imageOut = cr.openOutputStream(url)) {
                                    bmp.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                                }

                                long id = ContentUris.parseId(url);
                                // Wait until MINI_KIND thumbnail is generated.
                                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                                // This is for backward compatibility.
                                storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
                            } else {
                                assert url != null;
                                cr.delete(url, null, null);
                                url = null;
                            }
                        } catch (Exception e) {
                            if (url != null) {
                                cr.delete(url, null, null);
                                url = null;
                            }
                        }

                        customView.destroyDrawingCache();
                    }
                });
                saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                saveDialog.show();
            }
        });
    }

    private static Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND,kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID,(int)id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT,thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH,thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            assert url != null;
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            assert thumbOut != null;
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }


    private void subscribeNewButtonClickEvent(){
        ImageButton newBtn = (ImageButton) findViewById(R.id.new_btn);

        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder newDialog = new AlertDialog.Builder(v.getContext());
                newDialog.setTitle("New drawing");
                newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
                newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        customView.startNew();
                        dialog.dismiss();
                    }
                });
                newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        dialog.cancel();
                    }
                });
                newDialog.show();
            }
        });
    }



    private void subscribeEraseButtonClickEvent(){
        ImageButton eraseBtn = (ImageButton) findViewById(R.id.erase_btn);

        eraseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog brushDialog = new Dialog(v.getContext());
                brushDialog.setTitle("Eraser size:");
                brushDialog.setContentView(R.layout.brush_chooser);

                brushDialog.show();

                ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customView.setErase(true);
                        customView.setBrushSize(smallBrush);
                        brushDialog.dismiss();
                    }
                });

                ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customView.setErase(true);
                        customView.setBrushSize(mediumBrush);
                        brushDialog.dismiss();
                    }
                });

                ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customView.setErase(true);
                        customView.setBrushSize(largeBrush);
                        brushDialog.dismiss();
                    }
                });
            }
        });
    }

    private void subscribeDrawButtonClickEvent(){
        ImageButton drawBtn = (ImageButton) findViewById(R.id.draw_btn);

        drawBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Dialog brushDialog;
                brushDialog = new Dialog(v.getContext());
                brushDialog.setTitle("Brush size:");
                brushDialog.setContentView(R.layout.brush_chooser);
                brushDialog.show();

                ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customView.setBrushSize(smallBrush);
                        customView.setLastBrushSize(smallBrush);
                        brushDialog.dismiss();
                    }
                });

                ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customView.setBrushSize(mediumBrush);
                        customView.setLastBrushSize(mediumBrush);
                        brushDialog.dismiss();
                    }
                });

                ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        customView.setBrushSize(largeBrush);
                        customView.setLastBrushSize(largeBrush);
                        brushDialog.dismiss();
                    }
                });

                customView.setErase(false);
            }
        });
    }

    public void paintClicked(View view){
        if(view != currPaint)
        {
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();

            customView.setColor(color);

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;

            customView.setErase(false);
            customView.setBrushSize(customView.getLastBrushSize());
        }



    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
