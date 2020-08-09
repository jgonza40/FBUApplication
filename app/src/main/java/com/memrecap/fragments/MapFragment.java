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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import com.memrecap.activities.LocationSharedRecapActivity;
import com.memrecap.activities.SettingsActivity;
import com.memrecap.models.Friends;
import com.memrecap.models.MarkerPoint;
import com.memrecap.models.SharedMarker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class MapFragment extends Fragment implements GoogleMap.OnMapLongClickListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    public static final String TAG = "MapFragment";

    private static final String MARKERS_ARRAY = "markers";
    private static final String SHARED_MARKERS_ARRAY = "sharedMarkers";
    private final static String KEY_LOCATION = "location";
    private static final String PASS_LAT = "markerClickedLat";
    private static final String PASS_LONG = "markerClickedLong";
    private static final String MARKER_TYPE = "markerType";
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
    private Marker sendPersonalMarker;
    private Marker sendSharedMarker;
    private JSONObject friendsMap;

    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
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
        mGoogleMap.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater()));
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            loadUserMarkers();
            loadSharedMarkers();
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
        ((Button) messageView.findViewById(R.id.btnAdd)).setText("Add");
        ((Button) messageView.findViewById(R.id.btnView)).setText("Recap");
        ImageView exit = messageView.findViewById(R.id.ivExitButton);
        final AlertDialog alertDialog = builder.create();
        messageView.findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ComposeActivity.class);
                i.putExtra(PASS_LAT, String.valueOf(marker.getPosition().latitude));
                i.putExtra(PASS_LONG, String.valueOf(marker.getPosition().longitude));
                i.putExtra(MARKER_TYPE, String.valueOf(marker.getPosition().longitude));
                startActivity(i);
            }
        });
        messageView.findViewById(R.id.btnView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseQuery<SharedMarker> query = ParseQuery.getQuery(SharedMarker.class);
                query.include(SharedMarker.KEY_MARKER_LAT);
                query.whereEqualTo(SharedMarker.KEY_MARKER_LAT, String.valueOf(marker.getPosition().latitude));
                query.findInBackground(new FindCallback<SharedMarker>() {
                    @Override
                    public void done(List<SharedMarker> sharedMarkers, ParseException e) {
                        if (sharedMarkers.size() != 0) {
                            for (int i = 0; i < sharedMarkers.size(); i++) {
                                if (sharedMarkers.get(i).getMarkerLong().equals(String.valueOf(marker.getPosition().longitude))) {
                                    Intent intent = new Intent(getActivity(), LocationSharedRecapActivity.class);
                                    intent.putExtra(PASS_LAT, String.valueOf(marker.getPosition().latitude));
                                    intent.putExtra(PASS_LONG, String.valueOf(marker.getPosition().longitude));
                                    startActivity(intent);
                                }
                            }
                        } else {
                            Intent i = new Intent(getActivity(), LocationRecapActivity.class);
                            i.putExtra(PASS_LAT, String.valueOf(marker.getPosition().latitude));
                            i.putExtra(PASS_LONG, String.valueOf(marker.getPosition().longitude));
                            startActivity(i);
                        }
                    }
                });
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
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
        ((Button) messageView.findViewById(R.id.btnSharedMarker)).setText("Shared Marker");
        ((Button) messageView.findViewById(R.id.btnPersonalMarker)).setText("Personal Marker");
        ImageView ivExit = messageView.findViewById(R.id.ivExitMarkerWindow);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        messageView.findViewById(R.id.btnSharedMarker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDescriptor defaultMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                String title = ((EditText) alertDialog.findViewById(R.id.etMemoryTitle)).
                        getText().toString();
                String snippet = ParseUser.getCurrentUser().getUsername();
                // Creates and adds marker to the map
                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(title)
                        .snippet(snippet)
                        .icon(defaultMarker));
                dropPinEffect(marker);

                // Saving to Parse Database
                saveSharedMarker(marker, title);
                alertDialog.dismiss();
            }
        });
        messageView.findViewById(R.id.btnPersonalMarker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDescriptor defaultMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                String title = ((EditText) alertDialog.findViewById(R.id.etMemoryTitle)).
                        getText().toString();
                String snippet = ParseUser.getCurrentUser().getUsername();
                // Creates and adds marker to the map
                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(title)
                        .snippet(snippet)
                        .icon(defaultMarker));
                dropPinEffect(marker);

                // Saving to Parse Database
                savePersonalParseMarker(marker, title);
                alertDialog.dismiss();
            }
        });
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialogBuilder.setCancelable(true);
        alertDialog.show();
    }

    private void savePersonalParseMarker(Marker marker, String title) {
        sendPersonalMarker = marker;
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

        // Sends user to create a memory post after drop pin animation is complete
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getContext(), ComposeActivity.class);
                i.putExtra(PASS_LAT, String.valueOf(sendPersonalMarker.getPosition().latitude));
                i.putExtra(PASS_LONG, String.valueOf(sendPersonalMarker.getPosition().longitude));
                startActivity(i);
                getActivity().finish();
            }
        }, 2500);
    }

    private void saveSharedMarker(Marker marker, String title) {
        sendSharedMarker = marker;
        ParseUser user = ParseUser.getCurrentUser();
        SharedMarker newMarker = new SharedMarker();
        newMarker.setMarkerLat(Double.toString(marker.getPosition().latitude));
        newMarker.setMarkerLong(Double.toString((marker.getPosition().longitude)));
        newMarker.setMarkerCreator(user);
        newMarker.setMarkerTitle(title);
        newMarker.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving shared marker", e);
                    Toast.makeText(getContext(), "error while saving shared marker!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
        user.add(SHARED_MARKERS_ARRAY, newMarker);
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

        // Sends user to create a memory post after drop pin animation is complete
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getContext(), ComposeActivity.class);
                i.putExtra(PASS_LAT, String.valueOf(sendSharedMarker.getPosition().latitude));
                i.putExtra(PASS_LONG, String.valueOf(sendSharedMarker.getPosition().longitude));
                startActivity(i);
                getActivity().finish();
            }
        }, 2500);
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
        if (userMarkers != null) {
            for (int i = 0; i < userMarkers.length(); i++) {
                String marker = userMarkers.getJSONObject(i).getString("objectId");
                ParseQuery<MarkerPoint> query = ParseQuery.getQuery(MarkerPoint.class);
                MarkerPoint currMarker = query.get(marker);
                BitmapDescriptor defaultMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                LatLng point = new LatLng(new Double(currMarker.getMarkerLat()), new Double(currMarker.getMarkerLong()));
                String friendName = "null";
                try {
                    friendName = currMarker.getMarkerUser().fetchIfNeeded().getUsername();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(point)
                        .snippet(friendName)
                        .title(currMarker.getMarkerTitle())
                        .icon(defaultMarker));
            }
        }
    }

    private void loadSharedMarkers() throws ParseException, JSONException {
        final ParseUser currUser = ParseUser.getCurrentUser();
        ParseQuery<Friends> query = ParseQuery.getQuery(Friends.class);
        query.include(Friends.KEY_USER);
        query.whereEqualTo(Friends.KEY_USER, currUser);
        query.findInBackground(new FindCallback<Friends>() {
            @Override
            public void done(List<Friends> friends, ParseException e) {
                Friends currFriendModel = friends.get(0);
                friendsMap = currFriendModel.getFriendsMap();
                ParseQuery<SharedMarker> query = ParseQuery.getQuery(SharedMarker.class);
                query.findInBackground(new FindCallback<SharedMarker>() {
                    @Override
                    public void done(List<SharedMarker> sMarkers, ParseException e) {
                        for (int i = 0; i < sMarkers.size(); i++) {
                            SharedMarker curr = sMarkers.get(i);
                            if (currUser.getObjectId().equals(curr.getMarkerCreator().getObjectId())) {
                                BitmapDescriptor defaultMarker =
                                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                                LatLng point = new LatLng(new Double(curr.getMarkerLat()), new Double(curr.getMarkerLong()));
                                String name = "null";
                                try {
                                    name = curr.getMarkerCreator().fetchIfNeeded().getUsername();
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                                mGoogleMap.addMarker(new MarkerOptions()
                                        .position(point)
                                        .snippet(name)
                                        .title(curr.getMarkerTitle())
                                        .icon(defaultMarker));
                            } else {
                                if (friendsMap != null) {
                                    if (friendsMap.has(curr.getMarkerCreator().getObjectId())) {
                                        BitmapDescriptor defaultMarker =
                                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                                        LatLng point = new LatLng(new Double(curr.getMarkerLat()), new Double(curr.getMarkerLong()));
                                        String friendName = "null";
                                        try {
                                            friendName = curr.getMarkerCreator().fetchIfNeeded().getUsername();
                                        } catch (ParseException ex) {
                                            ex.printStackTrace();
                                        }
                                        mGoogleMap.addMarker(new MarkerOptions()
                                                .position(point)
                                                .snippet(friendName)
                                                .title(curr.getMarkerTitle())
                                                .icon(defaultMarker));
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
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

    private class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater mInflater;

        public CustomWindowAdapter(LayoutInflater i) {
            mInflater = i;
        }

        // This defines the contents within the info window based on the marker
        @Override
        public View getInfoContents(Marker marker) {
            // Getting view from the layout file
            View v = mInflater.inflate(R.layout.custom_info_window, null);
            // Populate fields
            TextView title = (TextView) v.findViewById(R.id.tv_info_window_title);
            title.setText(marker.getTitle());

            TextView description = (TextView) v.findViewById(R.id.tv_info_window_description);
            description.setText(marker.getSnippet());
            // Return info window contents
            return v;
        }

        // This changes the frame of the info window; returning null uses the default frame.
        // This is just the border and arrow surrounding the contents specified above
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }
    }
}
