package uk.ac.wlv.devwrite;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

public class ChooseImageDialogFragment extends DialogFragment {
    public final static String EXTRA_OPTION = "uk.ac.wlv.devwrite.option";

    public static ChooseImageDialogFragment newInstance() {
        return new ChooseImageDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] items = {getString(R.string.take_photo), getString(R.string.select_from_gallery)};
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.choose_image)
                .setItems(items, (dialogInterface, i) -> {
                    sendResult(Activity.RESULT_OK, items[i]);
                });


        return builder.create();
    }

    private void sendResult(int resultCode, String item) {
        if (getTargetFragment() == null) return;

        Intent intent = new Intent();
        intent.putExtra(EXTRA_OPTION, item);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
