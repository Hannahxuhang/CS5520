package hangxu.finalproject.cs5520.hikerplus.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import hangxu.finalproject.cs5520.hikerplus.MainActivity;
import hangxu.finalproject.cs5520.hikerplus.R;
import hangxu.finalproject.cs5520.hikerplus.StatusActivity;
import id.zelory.compressor.Compressor;

/**
 * Fragment class used for user settings.
 */

public class SettingFragment extends Fragment {

    private static final String TAG = "SETTING_FRAGMENT";
    private static final int GALLERY_ID = 1;
    private static final String TOAST_SIGN_OUT = "Signed Out";
    private static final String TOAST_PROFILE_SAVED = "Profile Image Saved";
    private static final String TOAST_PROFILE_NOT_SAVED = "Profile Image Not Saved";

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    private Button changePicButton;
    private Button changeStatusButton;
    private Button signOutButton;
    private TextView displayNameTv;
    private TextView statusTv;
    private CircleImageView profileImage;

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        String uid = mCurrentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("MUsers")
                .child(uid);

        changePicButton = (Button) view.findViewById(R.id.settingChangeImgButton);
        changeStatusButton = (Button) view.findViewById(R.id.settingChangeStatusButton);
        signOutButton = (Button) view.findViewById(R.id.signOutButton);
        displayNameTv = (TextView) view.findViewById(R.id.settingDisplayName);
        statusTv = (TextView) view.findViewById(R.id.settingsStatusText);
        profileImage = (CircleImageView) view.findViewById(R.id.settingProfile);

        // add value event listener on mDatabase
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("username").getValue().toString();
                String profile = dataSnapshot.child("profile").getValue().toString();
                String userStatus = dataSnapshot.child("status").getValue().toString();

                displayNameTv.setText(userName);
                statusTv.setText(userStatus);

                if (!profile.equals("default")) {
                    Picasso.with(getActivity())
                            .load(profile)
                            .placeholder(R.drawable.profile_img1)
                            .into(profileImage);

                    Log.d(TAG, "Uploading image successfully");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value", databaseError.toException());
            }
        });

        // wire up changePicButton
        changePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT_IMAGE"), GALLERY_ID);
            }
        });

        // wire up changeStatusButton
        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StatusActivity.class);
                intent.putExtra("status", statusTv.getText().toString().trim());
                startActivity(intent);
            }
        });

        // wire up signOutButton
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentUser != null && mAuth != null) {
                    mAuth.signOut();

                    Toast.makeText(getActivity(), TOAST_SIGN_OUT, Toast.LENGTH_LONG)
                            .show();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GALLERY_ID && resultCode == Activity.RESULT_OK) {
            Uri image = data.getData();

            // Use getContext() instead of getActivity() in Fragment (According to documentation in Github)
            CropImage.activity(image)
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "resultCode == RESULT_OK");

                String uid = mCurrentUser.getUid();

                Uri resultUri = result.getUri();

                File thumbnailFile = new File(resultUri.getPath());
                try {
                    Bitmap thumbnailBitmap = new Compressor(getActivity())
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(65)
                            .compressToBitmap(thumbnailFile);

                    // Upload thumbnailBitmap to firebase
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                    thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
                    final byte[] thumbnailByteArray = byteArray.toByteArray();

                    StorageReference filePath = mStorageRef.child("profile_images")
                            .child(uid + ".jpg");

                    // Create another directory for thumbnail images (smaller, compressed images)
                    final StorageReference thumbnailFilePath = mStorageRef.child("profile_images")
                            .child("thumbnails")
                            .child(uid + ".jpg");

                    filePath.putFile(resultUri)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        // Get the picture url
                                        // Add the annotation for removing the warning caused by firebase bug
                                        @SuppressWarnings("VisibleForTests") final
                                        String downloadUrl = task.getResult().getDownloadUrl().toString();

                                        // Upload task
                                        UploadTask uploadTask = thumbnailFilePath.putBytes(thumbnailByteArray);

                                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                @SuppressWarnings("VisibleForTests")
                                                String thumbnailUrl = task.getResult().getDownloadUrl().toString();

                                                if (task.isSuccessful()) {
                                                    HashMap<String, Object> updateObj = new HashMap<>();
                                                    updateObj.put("profile", downloadUrl);
                                                    updateObj.put("small_image", thumbnailUrl);

                                                    // Save profile and thumbnail images to firebase database
                                                    mDatabase.updateChildren(updateObj)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(getActivity(), TOAST_PROFILE_SAVED, Toast.LENGTH_LONG)
                                                                                .show();
                                                                    } else {
                                                                        Log.w(TAG, "Profile image not saved");
                                                                        Toast.makeText(getActivity(), TOAST_PROFILE_NOT_SAVED, Toast.LENGTH_LONG)
                                                                                .show();
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    Log.w(TAG, "Upload image task failed");
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                } catch (IOException e) {
                    Log.w(TAG, "IOException in compress thumbnail file to bitmap");
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, error.toString());
            }
        }
    }
}
