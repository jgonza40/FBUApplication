package com.memrecap.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.memrecap.activities.ComposeActivity;
import com.memrecap.activities.LocationRecapActivity;
import com.memrecap.R;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.memrecap.models.MarkerPoint;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import org.json.JSONArray;
import org.json.JSONException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class MapFragment extends Fragment implements GoogleMap.OnMapLongClickListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    public static final String TAG = "MapFragment";

    private static final String MARKERS_ARRAY = "markers";
    private final static String KEY_LOCATION = "location";
    private static final String PASS_LAT = "markerClickedLat";
    private static final String PASS_LONG = "markerClickedLong";
    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    private LocationRequest mLocationRequest;
    private Context mContext;
    private GoogleMap mGoogleMap;
    private Location mCurrentLocation;
    private SupportMapFragment mSupportMapFragment;

    public MapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        FragmentManager fm = getChildFragmentManager();
        mSupportMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (mSupportMapFragment == null) {
            mSupportMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mSupportMapFragment).commit();
        }
        mSupportMapFragment.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Display the connection status
        if (mCurrentLocation != null) {
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            mGoogleMap.animateCamera(cameraUpdate);
        }
        MapFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            loadUserMarkers();
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnMapLongClickListener(this);
        mGoogleMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onMapLongClick(final LatLng point) {
        showAlertDialogForPoint(point);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        View messageView = LayoutInflater.from(getActivity()).inflate(R.layout.add_view_window, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(messageView);
        ((Button) messageView.findViewById(R.id.btnAdd)).setText("add");
        ((Button) messageView.findViewById(R.id.btnView)).setText("recap");
        Log.i(TAG, String.valueOf(marker.getPosition().latitude));
        final AlertDialog alertDialog = builder.create();
        messageView.findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ComposeActivity.class);
                i.putExtra(PASS_LAT, String.valueOf(marker.getPosition().latitude));
                i.putExtra(PASS_LONG, String.valueOf(marker.getPosition().longitude));
                startActivity(i);
            }
        });
        messageView.findViewById(R.id.btnView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), LocationRecapActivity.class);
                i.putExtra(PASS_LAT, String.valueOf(marker.getPosition().latitude));
                i.putExtra(PASS_LONG, String.valueOf(marker.getPosition().longitude));
                startActivity(i);
            }
        });
        builder.setCancelable(true);
        alertDialog.show();
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void getMyLocation() {
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getActivity());
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        settingsClient.checkLocationSettings(locationSettingsRequest);

        getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.myLooper());
    }

    private void showAlertDialogForPoint(final LatLng point) {
        View messageView = LayoutInflater.from(getActivity()).
                inflate(R.layout.map_window_item, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(messageView);

        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BitmapDescriptor defaultMarker =
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                        String title = ((EditText) alertDialog.findViewById(R.id.etMemoryTitle)).
                                getText().toString();
                        // Creates and adds marker to the map
                        Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title(title)
                                .icon(defaultMarker));
                        dropPinEffect(marker);

                        // Saving to Parse Database
                        saveParseMarker(marker, title);
                    }
                });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void saveParseMarker(Marker marker, String title) {
        ParseUser user = ParseUser.getCurrentUser();

        MarkerPoint newMarker = new MarkerPoint();
        newMarker.setMarkerLat(Double.toString(marker.getPosition().latitude));
        newMarker.setMarkerLong(Double.toString((marker.getPosition().longitude)));
        newMarker.setMarkerUser(user);
        newMarker.setMarkerTitle(title);
        newMarker.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving marker", e);
                    Toast.makeText(getContext(), "error while saving!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
        user.add(MARKERS_ARRAY, newMarker);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving marker to user", e);
                    Toast.makeText(getContext(), "error while saving marker to user!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;
        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();
        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float bounce = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 14 * bounce);
                if (bounce > 0.0) {
                    handler.postDelayed(this, 15);
                } else {
                    marker.showInfoWindow();
                }
            }
        });
    }

    private void loadUserMarkers() throws ParseException, JSONException {
        JSONArray userMarkers = ParseUser.getCurrentUser().getJSONArray(MARKERS_ARRAY);
        for (int i = 0; i < userMarkers.length(); i++) {
            String marker = userMarkers.getJSONObject(i).getString("objectId");
            ParseQuery<MarkerPoint> query = ParseQuery.getQuery(MarkerPoint.class);
            MarkerPoint currMarker = query.get(marker);
            BitmapDescriptor defaultMarker =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            LatLng point = new LatLng(new Double(currMarker.getMarkerLat()), new Double(currMarker.getMarkerLong()));
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(currMarker.getMarkerTitle())
                    .icon(defaultMarker));
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (errorDialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getActivity().getSupportFragmentManager(), "Location Updates");
            }
            return false;
        }
    }

    private void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return;
        }
        // Report to the UI that the location was updated
        mCurrentLocation = location;
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
