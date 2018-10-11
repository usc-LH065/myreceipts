package myreceipts.lee.com.myreceipt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class ReceiptFragment extends Fragment {

    private static final String RECEIPT_FRAGMENT = "ReceiptFragment";
    private static final String ARG_RECEIPT_ID = "receipt_id";

    // Dialog tags
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_IMAGE = "DialogImage";

    // Activity extras
    public static final String EXTRA_DATE = "date";

    // request codes
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_PHOTO = 2;
    public static final int ACTIVITY_REQUEST_DATE = 3;
    public static final int REQUEST_CONTACT = 5;

    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 6;

    // Formatting date/time
    private static final int DATE_FORMAT = DateFormat.FULL;

    // Widgets
    private Receipt mReceipt;
    private File mPhotoFile;
    private EditText mTitleField;
    private EditText mCommentField;
    private EditText mShopnameField;
    private TextView mLatText;
    private TextView mLonText;
    private Button mDateButton;
    private Button mReportButton;
    private Button mMapButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private ViewTreeObserver mPhotoTreeObserver;

    private Callbacks mCallbacks;

    private GpsTracker gpsTracker;

    public interface Callbacks {
        void onReceiptUpdated(Receipt receipt);
    }

    private Point mPhotoViewSize;

    public static ReceiptFragment newInstance(UUID receiptId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_RECEIPT_ID, receiptId);

        ReceiptFragment fragment = new ReceiptFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID receiptId = (UUID) getArguments().getSerializable(ARG_RECEIPT_ID);
        mReceipt = ReceiptLab.get(getActivity()).getReceipt(receiptId);
        mPhotoFile = ReceiptLab.get(getActivity()).getPhotoFile(mReceipt);
    }

    @Override
    public void onPause() {
        super.onPause();

        ReceiptLab.get(getActivity()).updateReceipt(mReceipt);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_receipt, container, false);

        setHasOptionsMenu(true);
        gpsTracker = new GpsTracker(getActivity());

        try {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        // Setup Title EditText
        mTitleField = (EditText) v.findViewById(R.id.receipt_title);
        mTitleField.setText(mReceipt.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mReceipt.setTitle(charSequence.toString());
                updateReceipt();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Also intentionally left blank
            }
        });

        // Setup Comment EditText
        mCommentField = v.findViewById(R.id.receipt_comment);
        mCommentField.setText(mReceipt.getComment());
        mCommentField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReceipt.setComment(s.toString());
                updateReceipt();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Setup Shop Name TextEdit
        mShopnameField = v.findViewById(R.id.receipt_shop);
        mShopnameField.setText(mReceipt.getShopName());
        mShopnameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReceipt.setShopName(s.toString());
                updateReceipt();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Setup latitude TextView
        mLatText = v.findViewById(R.id.receipt_lat);


        // Setup longitude TextView
        mLonText = v.findViewById(R.id.receipt_lon);
        Log.e("lat1 : ", String.format("%.3f",mReceipt.getLat()));
        Log.e("long1 : ", String.format("%.3f",mReceipt.getLon()));
        if(mReceipt.getLat() != 1000 && mReceipt.getLon() != 1000) {
            mLatText.setText("latitude: " + String.format("%.3f", mReceipt.getLat()));
            mLonText.setText("  longitude: " +  String.format("%.3f", mReceipt.getLon()));
        }
        else {
            mLatText.setText("latitude: " + String.format("%.2f",gpsTracker.getLatitude()));
            mReceipt.setLat(gpsTracker.getLatitude());
            Log.e("lat : " ,String.format("%.3f",(float)gpsTracker.getLatitude()));
            Log.e("long : " ,String.format("%.3f",(float)gpsTracker.getLongitude()));
            mLonText.setText(" longitude: " + String.format("%.3f", gpsTracker.getLongitude()));
            mReceipt.setLon(gpsTracker.getLongitude());
            updateReceipt();
        }


        // Setup date button
        mDateButton = v.findViewById(R.id.receipt_date);
        final Date currentDate = mReceipt.getDate();

        String formattedDate = DateFormatter.formatDateAsString(DATE_FORMAT, currentDate);
        mDateButton.setText(formattedDate);

        // Button on click listeners
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTablet(getContext())) {
                    FragmentManager manager = getFragmentManager();
                    DatePickerFragment dialog = DatePickerFragment.newInstance(mReceipt.getDate());
                    dialog.setTargetFragment(ReceiptFragment.this, REQUEST_DATE);
                    dialog.show(manager, DIALOG_DATE);
                } else {
                    // Screen is smaller - display dialog as full screen activity
                    Intent intent = new Intent(getContext(), DatePickerActivity.class);
                    intent.putExtra(EXTRA_DATE, mReceipt.getDate());
                    startActivityForResult(intent, ACTIVITY_REQUEST_DATE);
                }
            }
        });

        mMapButton = v.findViewById(R.id.show_map);
        mMapButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("Lat", mReceipt.getLat());
                intent.putExtra("Lon", mReceipt.getLon());
                startActivity(intent);
            }
        });


        mReportButton = v.findViewById(R.id.receipt_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use intent builder to create a send action
                // that opens a populated email
                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getReceiptReport())
                        .setSubject(getString(R.string.receipt_report_subject))
                        .setChooserTitle(getString(R.string.send_report))
                        .startChooser();
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);


        // Disable the choose suspect button to prevent crash
        // when no contacts app is available
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
        }

        // Setup photo taking abilities
        mPhotoButton =  v.findViewById(R.id.receipt_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "myreceipts.lee.com.myreceipt.fileprovider",
                        mPhotoFile);

                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager()
                        .queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.toString(),
                            uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    startActivityForResult(captureImage, REQUEST_PHOTO);
                }
            }
        });

        mPhotoView =  v.findViewById(R.id.receipt_photo);

        // On image click, open zoomed image dialog
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                ImageDisplayFragment fragment = ImageDisplayFragment.newInstance(mPhotoFile);
                fragment.show(fragmentManager, DIALOG_IMAGE);
            }
        });

        mPhotoTreeObserver = mPhotoView.getViewTreeObserver();
        mPhotoTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mPhotoViewSize = new Point();
                mPhotoViewSize.set(mPhotoView.getWidth(), mPhotoView.getHeight());

                updatePhotoView();
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE || requestCode == ACTIVITY_REQUEST_DATE) {
            final Date date;
            date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mReceipt.setDate(date);
            mDateButton.setText(DateFormatter.formatDateAsString(DATE_FORMAT, date));
            updateReceipt();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
            };
            // Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);

            try {
                // Double-check that results were actually received
                if (c != null && c.getCount() == 0) {
                    return;
                }

                // Pull out the first column of the first row of data -
                // that is your suspect's name.
                c.moveToFirst();


                updateReceipt();
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.example.android.criminalintent2.fileprovider", mPhotoFile);
            // Remove temporary write access to file from camera
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            mPhotoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Announce to screenreader
                    mPhotoView.announceForAccessibility(getString(R.string.photo_announcement));
                }
            }, 500);

            updateReceipt();
            updatePhotoView();
        }
    }

    // For sending a formatted email of the report
    private String getReceiptReport() {

        Date date = mReceipt.getDate();
        String dateString = DateFormatter.formatDateAsString(DATE_FORMAT, date);



        // Return the report
        return getString(R.string.receipt_report,
                mReceipt.getTitle(), dateString, mReceipt.getComment(), mReceipt.getShopName(), mReceipt.getPhotoFilename());
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
            mPhotoView.setClickable(false);
            mPhotoView.setContentDescription(getString(R.string.receipt_photo_no_image_description));
        } else {
            mPhotoView.setClickable(true);
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoViewSize.x, mPhotoViewSize.y);
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setContentDescription(getString(R.string.receipt_photo_image_description));
        }
    }

    // For determining dialog as dialog or fragment
    // from http://www.androidcodesnippets.com/2016/02/check-if-device-is-tablet-or-phone/
    private boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putSerializable(ARG_RECEIPT_ID, mReceipt.getId());

        super.onSaveInstanceState(outState);
    }

    private void deleteReceipt() {
        ReceiptLab receiptLab = ReceiptLab.get(getActivity());
        receiptLab.deleteReceipt(mReceipt);
        mCallbacks.onReceiptUpdated(mReceipt);

        // Delete the image file associated with receipt if exists

        File file = new File(mPhotoFile.getPath());
        if (file.exists()) {
            file.delete();
            Log.i("ReceiptFragment", "deleteReceipt photo called");
        }

        Toast.makeText(getActivity(), R.string.toast_delete_receipt, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_receipt, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_receipt:
                if (getActivity().findViewById(R.id.detail_fragment_container) == null) {
                    deleteReceipt();
                    getActivity().finish();
                } else {
                    // Using tablet with two pane view (Clear out fragment
                    deleteReceipt();
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .remove(this)
                            .commit();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void checkReadContactsPermission() {// Here, thisActivity is the current activity
        Log.i(RECEIPT_FRAGMENT, "checkReadContactsPermission: start");
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Log.i(RECEIPT_FRAGMENT, "checkReadContactsPermission: called");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                Toast.makeText(getActivity(), "Dial with phone successful", Toast.LENGTH_SHORT).show();
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void updateReceipt() {
        ReceiptLab.get(getActivity()).updateReceipt(mReceipt);
        mCallbacks.onReceiptUpdated(mReceipt);
    }


}
