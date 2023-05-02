package uk.ac.wlv.devwrite.PostEditor;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import uk.ac.wlv.devwrite.ChooseImageDialogFragment;
import uk.ac.wlv.devwrite.DatabaseManager;
import uk.ac.wlv.devwrite.Images.PictureUtils;
import uk.ac.wlv.devwrite.Models.Post;
import uk.ac.wlv.devwrite.R;

public class PostFragment extends Fragment {
    private static final String ARG_POST_ID = "post_id";
    private static final String DIALOG_CHOOSE_IMAGE = "DialogChooseImage";
    private static final int REQUEST_CHOOSE_IMAGE_OPTION = 1;
    private static final int REQUEST_CAMERA_PHOTO = 2;
    private static final int REQUEST_GALLERY_PHOTO = 3;
    private static final int PERMISSION_READ_EXTERNAL_STORAGE = 4;
    private static final int REQUEST_OPEN_PHOTO = 5;
    private Post mPost;
    private TextInputEditText mTitleField;
    private TextInputEditText mContentField;
    private MaterialToolbar mToolbar;
    private MaterialButton mChooseImageButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private Intent captureImage;

    public static PostFragment newInstance(UUID postId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST_ID, postId);
        PostFragment fragment = new PostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_post, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_save) {
            DatabaseManager.get(getActivity()).updatePost(mPost);
            Toast.makeText(getActivity(), mPost.getTitle() + " Saved", Toast.LENGTH_SHORT).show();
        }

        if (item.getItemId() == R.id.option_delete) {
            DatabaseManager.get(getActivity()).deletePost(mPost);
            requireActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID postId = (UUID) getArguments().getSerializable(ARG_POST_ID);
        mPost = DatabaseManager.get(getActivity()).getPost(postId);
        mPhotoFile = DatabaseManager.get(getActivity()).getPhotoFile(mPost);

        if (getActivity().getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ_EXTERNAL_STORAGE
            );
        }

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        mTitleField = view.findViewById(R.id.title_field_input);
        mTitleField.setText(mPost.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mPost.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mContentField = view.findViewById(R.id.content_field_input);
        mContentField.setText(mPost.getContent());
        mContentField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mPost.setContent(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mPhotoView = view.findViewById(R.id.post_photo);

        mChooseImageButton = view.findViewById(R.id.choose_image_button);
        PackageManager packageManager = requireActivity().getPackageManager();
        captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mChooseImageButton.setEnabled(canTakePhoto);
        mChooseImageButton.setOnClickListener(event -> {
            DialogFragment chooseImageFragment = ChooseImageDialogFragment.newInstance();
            chooseImageFragment.setTargetFragment(PostFragment.this, REQUEST_CHOOSE_IMAGE_OPTION);
            chooseImageFragment.show(getFragmentManager(), DIALOG_CHOOSE_IMAGE);
        });

//        updatePhotoView();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CHOOSE_IMAGE_OPTION) {
            assert data != null;
            String selectedItem = (String) data.getSerializableExtra(ChooseImageDialogFragment.EXTRA_OPTION);
            Toast.makeText(getActivity(), selectedItem, Toast.LENGTH_SHORT).show();

            if (Objects.equals(selectedItem, getString(R.string.take_photo))) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "uk.ac.wlv.devwrite.fileprovider", mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager()
                        .queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(
                            activity.activityInfo.packageName,
                            uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    );
                }

                startActivityForResult(captureImage, REQUEST_CAMERA_PHOTO);
            }

            if (Objects.equals(selectedItem, getString(R.string.select_from_gallery))) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY_PHOTO);
            }
        }

        if (requestCode == REQUEST_CAMERA_PHOTO) {
            Uri uri = FileProvider.getUriForFile(
                    getActivity(),
                    "uk.ac.wlv.devwrite.fileprovider",
                    mPhotoFile
            );

            mPost.setUri(uri);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            updatePhotoView();
        }

        if (requestCode == REQUEST_GALLERY_PHOTO) {
            Uri selectedImageUri = data.getData();
            mPost.setUri(selectedImageUri);
            mPhotoView.setImageURI(selectedImageUri);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                updatePhotoView();
            }
        }
    }

//    private void updatePhotoView() {
//        if (mPost.getUri().toString().equals("")) {
//            mPhotoView.setImageDrawable(null);
//        } else {
//            try {
//                getActivity().getContentResolver()
//                        .takePersistableUriPermission(mPost.getUri(),
//                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                InputStream inputStream = getActivity().getContentResolver().openInputStream(mPost.getUri());
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                mPhotoView.setImageBitmap(bitmap);
//            } catch (FileNotFoundException exception) {
//                Toast.makeText(getActivity(), "File Not Found", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}
