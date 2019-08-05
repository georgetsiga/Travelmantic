package com.georgetsiga.travelmantic.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.georgetsiga.travelmantic.R;
import com.georgetsiga.travelmantic.models.TravelDeal;
import com.georgetsiga.travelmantic.utils.FireBaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Objects;
import java.util.UUID;

public class AdminActivity extends AppCompatActivity {

    public static final String EXTRA_DEAL = "Deal";
    private final String TAG = getClass().getSimpleName();
    private static final int PICTURE_RESULT = 42;
    private final String EMPTY_STRING = "";

    private TravelDeal mTravelDeal;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    @BindView(R.id.text_place) TextInputEditText tvPlace;
    @BindView(R.id.text_price) TextInputEditText tvPrice;
    @BindView(R.id.text_description) TextInputEditText tvDescription;
    @BindView(R.id.imageView) ImageView placeImage;
    @BindView(R.id.btn_image) Button btnImage;
    @BindView(R.id.btn_save) Button btnSave;

    public static Intent newInstance(Context context, TravelDeal travelDeal) {
        Intent intent = new Intent(context, AdminActivity.class);
        intent.putExtra(EXTRA_DEAL, travelDeal);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ButterKnife.bind(this);
        setUpScreen();
    }

    public void setUpScreen() {
        mFirebaseDatabase = FireBaseUtil.mFireBaseDatabase;
        mDatabaseReference = FireBaseUtil.mDatabaseReference;
        Bundle data = getIntent().getExtras();

        if (data != null){
            mTravelDeal = data.getParcelable(EXTRA_DEAL);
        }
        else {
            mTravelDeal = new TravelDeal();
        }

        tvPlace.setText(mTravelDeal.getTitle());
        tvDescription.setText(mTravelDeal.getDescription());
        tvPrice.setText(mTravelDeal.getPrice());
        showImage(mTravelDeal.getImageUrl());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.btn_image)
    public void saveImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
    }

    @OnClick(R.id.btn_save)
    public void saveDeal() {
        mTravelDeal.setTitle(Objects.requireNonNull(tvPlace.getText()).toString().trim());
        mTravelDeal.setDescription(Objects.requireNonNull(tvDescription.getText()).toString().trim());
        mTravelDeal.setPrice(Objects.requireNonNull(tvPrice.getText()).toString().trim());
        if (mTravelDeal.getId() == null) {
            mDatabaseReference.push().setValue(mTravelDeal);
        } else {
            mDatabaseReference.child(mTravelDeal.getId()).setValue(mTravelDeal);
        }

        Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
        clean();
        backToList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FireBaseUtil.isAdmin) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            btnSave.setVisibility(View.VISIBLE);
            btnImage.setVisibility(View.VISIBLE);
            enableEditTexts(true);
        } else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            btnSave.setVisibility(View.GONE);
            btnImage.setVisibility(View.GONE);
            enableEditTexts(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            upload(imageUri);
        }
    }

    private void upload(Uri filePath) {
        if (filePath != null && filePath.getLastPathSegment() != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.show();

            final StorageReference ref = FireBaseUtil.mStorageRef.child(new StringBuilder("images/").append(UUID.randomUUID().toString()).toString());
            UploadTask uploadTask = ref.putFile(filePath);

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    progressDialog.dismiss();

                    String pictureName = task.getResult().getPath();
                    mTravelDeal.setImageName(pictureName);
                    Log.d(TAG + " Picture name: ", pictureName);

                    String url = downloadUri.toString();
                    Log.d(TAG + " PictureUrl: ", url);
                    mTravelDeal.setImageUrl(url);
                    showImage(url);
                } else {
                    Toast.makeText(AdminActivity.this, "Fail UPLOAD", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    progressDialog.setMessage("Uploaded: ");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AdminActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteDeal() {
        if (mTravelDeal == null) {
            Toast.makeText(this, "Please save the mTravelDeal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(mTravelDeal.getId()).removeValue();
        Log.d(TAG, mTravelDeal.getImageName());
        if (mTravelDeal.getImageName() != null && mTravelDeal.getImageName().isEmpty()) {
            StorageReference picRef = FireBaseUtil.mStorage.getReference().child(mTravelDeal.getImageName());
            picRef.delete().addOnSuccessListener(aVoid -> Log.d(TAG, "Image Successfully Deleted"))
                    .addOnFailureListener(e -> Log.d("Delete Image", e.getMessage()));
        }
    }

    private void backToList() {
        startActivity(UserActivity.newInstance(this));
    }

    private void clean() {
        tvPlace.setText(EMPTY_STRING);
        tvPrice.setText(EMPTY_STRING);
        tvDescription.setText(EMPTY_STRING);
        tvPlace.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled) {
        tvPlace.setEnabled(isEnabled);
        tvDescription.setEnabled(isEnabled);
        tvPrice.setEnabled(isEnabled);
    }

    private void showImage(String url) {
        if (url != null && !url.isEmpty()) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Glide.with(this).load(url).apply(new RequestOptions().override(width, width * 2 / 3)).centerCrop()
                    .into(placeImage);
        }
    }
}
