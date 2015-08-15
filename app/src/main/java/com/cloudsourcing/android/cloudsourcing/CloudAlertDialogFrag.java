// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by angelmaredia on 7/16/15.
 */
public class CloudAlertDialogFrag extends DialogFragment {

    String mMessage;
    String mPositive;
    String mNegative;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

        builder.setMessage(mMessage).
                setPositiveButton(mPositive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mPositive != "") {
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                        } else {
                            dismiss();
                        }
                    }
                }).
                setNegativeButton(mNegative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    public void setDialogText(String messsage, String positive, String negative) {
        mMessage = messsage;
        mPositive = positive;
        mNegative = negative;
    }

}

