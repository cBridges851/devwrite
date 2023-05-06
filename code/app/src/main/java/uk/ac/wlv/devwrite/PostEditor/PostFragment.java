package uk.ac.wlv.devwrite.PostEditor;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.MimeTypeFilter;
import androidx.core.graphics.BitmapCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import uk.ac.wlv.devwrite.ChooseImageDialogFragment;
import uk.ac.wlv.devwrite.DatabaseManager;
import uk.ac.wlv.devwrite.Images.PictureUtils;
import uk.ac.wlv.devwrite.Models.Post;
import uk.ac.wlv.devwrite.R;
import uk.ac.wlv.devwrite.Sharing.Sharer;

public class PostFragment extends Fragment {
    private static final String ARG_POST_ID = "post_id";
    private static final String DIALOG_CHOOSE_IMAGE = "DialogChooseImage";
    private static final int REQUEST_CHOOSE_IMAGE_OPTION = 1;
    private static final int REQUEST_PHOTO_FROM_CAMERA = 2;
    private static final int REQUEST_PHOTO_FROM_GALLERY = 3;
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

        if (item.getItemId() == R.id.option_publish) {
            new Sharer().sharePostToLinkedIn(this, mPost.getContent());
        }

        if (item.getItemId() == R.id.option_share_via_email) {
            new Sharer().sharePostToEmail(this, mPost.getTitle(), mPost.getContent());
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

        mChooseImageButton.setOnClickListener(event -> {
            DialogFragment chooseImageFragment = ChooseImageDialogFragment.newInstance();
            chooseImageFragment.setTargetFragment(PostFragment.this, REQUEST_CHOOSE_IMAGE_OPTION);
            chooseImageFragment.show(getFragmentManager(), DIALOG_CHOOSE_IMAGE);
        });
        updatePhotoView();
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
                Uri uri = FileProvider.getUriForFile(
                        getActivity(),
                        "uk.ac.wlv.devwrite.fileprovider",
                        mPhotoFile
                );

                captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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

                startActivityForResult(captureImage, REQUEST_PHOTO_FROM_CAMERA);
            }

            if (Objects.equals(selectedItem, getString(R.string.select_from_gallery))) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(
                        Intent.createChooser(intent, "Select Photo"),
                        REQUEST_PHOTO_FROM_GALLERY
                );
            }
        }

        if (requestCode == REQUEST_PHOTO_FROM_CAMERA) {
            Uri uri = FileProvider.getUriForFile(
                    requireActivity(),
                    "uk.ac.wlv.devwrite.fileprovider",
                    mPhotoFile
            );

            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView();
        }

        if (requestCode == REQUEST_PHOTO_FROM_GALLERY) {
            if (data == null) {
                return;
            }

            try {
                Uri uri = data.getData();
                String documentId = DocumentsContract.getDocumentId(uri);
                String[] parts = documentId.split(":");
                String id = parts[1];

                String[] projection = { MediaStore.Images.Media.DATA };
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = { id };
                Cursor cursor = getActivity().getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null
                );
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                Bitmap scaledBitmap = PictureUtils.getScaledBitmap(
                        filePath,
                        requireActivity()
                );
                mPhotoView.setImageBitmap(scaledBitmap);
                FileOutputStream fileOutputStream = new FileOutputStream(this.mPhotoFile);
                Bitmap.CompressFormat compressFormat = getCompressFormat(uri);
                scaledBitmap.compress(compressFormat, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException exception) {
                Toast.makeText(getActivity(), "Cannot Display Image - File not found", Toast.LENGTH_SHORT).show();
                Log.println(Log.ERROR, "DisplayingGalleryImage", exception.getMessage());
            } catch (IOException exception) {
                Toast.makeText(getActivity(), "Cannot Display Image - Unable to flush or close stream", Toast.LENGTH_SHORT).show();
                Log.println(Log.ERROR, "DisplayingGalleryImage", exception.getMessage());
            } catch (Exception exception) {
                Toast.makeText(getActivity(), "Cannot Display Image", Toast.LENGTH_SHORT).show();
                Log.println(Log.ERROR, "DisplayingGalleryImage", exception.getMessage());
            }
        }
    }

    private Bitmap.CompressFormat getCompressFormat(Uri uri) {
        String mimeType;

        if (Objects.equals(ContentResolver.SCHEME_CONTENT, uri.getScheme())) {
            ContentResolver contentResolver = getActivity()
                    .getApplicationContext()
                    .getContentResolver();
            mimeType = contentResolver.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }

        if (Objects.equals(mimeType, "image/jpeg")) {
            return Bitmap.CompressFormat.JPEG;
        }

        if (Objects.equals(mimeType, "image/png")) {
            return Bitmap.CompressFormat.PNG;
        }

        return null;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), requireActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }
}
