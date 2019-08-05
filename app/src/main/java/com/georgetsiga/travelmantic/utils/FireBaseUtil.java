package com.georgetsiga.travelmantic.utils;

import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.georgetsiga.travelmantic.R;
import com.georgetsiga.travelmantic.models.TravelDeal;
import com.georgetsiga.travelmantic.ui.UserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FireBaseUtil {
    public static FirebaseDatabase mFireBaseDatabase;
    public static DatabaseReference mDatabaseReference;
    private static FireBaseUtil firebaseUtil;
    public static FirebaseAuth mFireBaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthListener;
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageRef;
    public static ArrayList<TravelDeal> mDeals;
    private static final int RC_SIGN_IN = 123;
    private static UserActivity caller;
    public static boolean isAdmin;
    public static String username;

    private FireBaseUtil() {
    }

    public static void openFbReference(String ref, final UserActivity callerActivity) {
        if (firebaseUtil == null) {
            firebaseUtil = new FireBaseUtil();
            mFireBaseDatabase = FirebaseDatabase.getInstance();
            mFireBaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;

            mAuthListener = firebaseAuth -> {
                if (firebaseAuth.getCurrentUser() == null) {
                    FireBaseUtil.signIn();
                } else {
                    String userId = firebaseAuth.getUid();
                    checkAdmin(userId);
                    username = firebaseAuth.getCurrentUser().getDisplayName();
                    caller.setName(username);
                }

            };
            connectStorage();
        }

        mDeals = new ArrayList<>();
        mDatabaseReference = mFireBaseDatabase.getReference().child(ref);
    }

    private static void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        caller.startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).setTheme(R.style.LoginTheme)
                .setIsSmartLockEnabled(false).build(), RC_SIGN_IN);
    }

    private static void checkAdmin(String uid) {
        FireBaseUtil.isAdmin = false;
        DatabaseReference ref = mFireBaseDatabase.getReference().child("administrators").child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FireBaseUtil.isAdmin = true;
                caller.showMenu();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addChildEventListener(listener);
    }

    public static void attachListener() {
        mFireBaseAuth.addAuthStateListener(mAuthListener);
    }

    public static void detachListener() {
        mFireBaseAuth.removeAuthStateListener(mAuthListener);
    }

    public static void connectStorage() {
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference().child("deals_pictures");
    }
}
