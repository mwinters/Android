package com.nymi.nymireferenceapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.nymi.api.NymiAdapter;
import com.nymi.api.NymiDevice;
import com.nymi.api.NymiRandomNumber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.BitSet;


public class MainActivity extends ActionBarActivity {

    private static final String NEA_NAME = "AndroidExampleNEA"; // must be <= 18 characters

    private static final int LEDS_NUMBER = 5;

    private NymiAdapter mNymiAdapter;

    private AdapterProvisions mAdapterProvisions;
    private ListView mListViewProvisions;

    private RadioButton mLeds[];

    private Button mButtonAccept;
    private Button mButtonDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNymiAdapter = NymiAdapter.getInstance(); // get singleton
        String nymulatorHost = loadBuildhostFromResources();
            // in typical dev environments, the nymulator will be run on the
            // same machine as your build machine, so this is a sensible default.
            // If that's not the case for you, update this host field, e.g.
            // nymulatorHost = "10.0.1.11"
        mNymiAdapter.setNymulator(nymulatorHost);

        // Initialize the NymiAdapter. We'll get a callback sometime in the future
        // when initialization can finish. Initialization can fail, but that's only if
        // you've specified a nymulator host that the backend is unable to talk to.
        mNymiAdapter.init(this, NEA_NAME, new NymiAdapter.NymiInitCallback() {
            @Override
            public void onNymiInitResult(int status) {
                if (status == NymiAdapter.NymiInitCallback.INIT_SUCCESS) {

                    // All callbacks from the NymiAdapter run on the UI thread,
                    // so you may safely update your UI (and the usual caveats apply)
                    Toast.makeText(MainActivity.this, "Initialized", Toast.LENGTH_SHORT).show();
                    mAdapterProvisions.setDevices(NymiAdapter.getInstance().getDevices());

                    // on success we immediately start provisioning.
                    // Starting provisioning in the callback avoids having to poll
                    // for init to continue before kicking off the method.
                    startProvision();
                } else {
                    // The only failure is if we couldn't contact a nymulator
                    // at the nymulator host set on line 48.
                    Toast.makeText(MainActivity.this, "Failed to initialize", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Adapter for displaying all the provisions.
        mAdapterProvisions = new AdapterProvisions(this);

        // Here we set up our UI for displaying agreement patterns.
        // Confirming the LED hash against what the user sees on their band
        // is a crucial step during provisioning. Without this, you may
        // unintentionally provision the wrong band and are susceptible to
        // man-in-the-middle attacks
        mLeds = new RadioButton[LEDS_NUMBER];

        // UI to display patterns
        mLeds[0] = (RadioButton) findViewById(R.id.layout_main_led0);
        mLeds[1] = (RadioButton) findViewById(R.id.layout_main_led1);
        mLeds[2] = (RadioButton) findViewById(R.id.layout_main_led2);
        mLeds[3] = (RadioButton) findViewById(R.id.layout_main_led3);
        mLeds[4] = (RadioButton) findViewById(R.id.layout_main_led4);

        // Buttons to accept User's communication of whether to accept or
        // reject the candidate provision.
        mButtonAccept = (Button) findViewById(R.id.layout_main_button_accept);
        mButtonDecline = (Button) findViewById(R.id.layout_main_button_decline);

        mButtonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitSet bitSet = new BitSet(LEDS_NUMBER);
                bitSet.clear();
                for (int i = 0; i < LEDS_NUMBER; i++) {
                    if (mLeds[i].isChecked()) {
                        bitSet.set(i);
                    }
                    mLeds[i].setChecked(false);
                    mLeds[i].setEnabled(false);
                }

                // Calling setPattern is your application's way of saying
                // "Yes, I really want to provision with this device."
                // After setPattern, you'll get a callback with a NymiDevice
                // instance with which your application can interact.
                mNymiAdapter.setPattern(bitSet);
                mButtonAccept.setEnabled(false);
                mButtonDecline.setEnabled(false);
            }
        });

        mButtonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < LEDS_NUMBER; i++) {
                    mLeds[i].setChecked(false);
                    mLeds[i].setEnabled(false);
                }
                mButtonAccept.setEnabled(false);
                mButtonDecline.setEnabled(false);
                Toast.makeText(MainActivity.this, "Device declined", Toast.LENGTH_SHORT).show();
            }
        });


        // Of course the main thing your application will want to do is interact
        // with NymiDevice objects themselves. Here are the main facilities of
        // the UI. All operations have the model of an operation being
        // dispatched with a callback, and that callback eventually being
        // called. Note that operations can fail if the target band is absent
        // or unavailable (e.g. busy communicating with another NEA)
        mListViewProvisions = (ListView) findViewById(R.id.layout_main_provision_list);
        mListViewProvisions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_menu_notify_positive:
                                ((NymiDevice) mAdapterProvisions.getItem(position)).sendNotification(NymiDevice.NymiDeviceNotification.POSITIVE, new NymiDevice.NymiNotificationCallback() {
                                    @Override
                                    public void onNymiNotificationResult(int status, NymiDevice.NymiDeviceNotification nymiDeviceNotification) {
                                        if (status == NymiDevice.NymiNotificationCallback.NOTIFICATION_SUCCESS) {
                                            Toast.makeText(MainActivity.this, nymiDeviceNotification.toString() + " notification completed", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Notification failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                            case R.id.popup_menu_notify_negative:
                                ((NymiDevice) mAdapterProvisions.getItem(position)).sendNotification(NymiDevice.NymiDeviceNotification.NEGATIVE, new NymiDevice.NymiNotificationCallback() {
                                    @Override
                                    public void onNymiNotificationResult(int status, NymiDevice.NymiDeviceNotification nymiDeviceNotification) {
                                        if (status == NymiDevice.NymiNotificationCallback.NOTIFICATION_SUCCESS) {
                                            Toast.makeText(MainActivity.this, nymiDeviceNotification.toString() + " notification completed", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Notification failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                            case R.id.popup_menu_get_random:
                                ((NymiDevice) mAdapterProvisions.getItem(position)).getRandom(new NymiDevice.NymiRandomCallback() {
                                    @Override
                                    public void onNymiRandomResult(int status, NymiRandomNumber nymiRandomNumber) {
                                        if (status == NymiDevice.NymiRandomCallback.RANDOM_SUCCESS) {
                                            // Of course a real application will want to make use of this value otherwise,
                                            // but this is to demonstrate flow.
                                            Toast.makeText(MainActivity.this, "Obtained random: " + nymiRandomNumber.toString(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Random failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                            case R.id.popup_menu_sign:

                                if (null != mAdapterProvisions.getItem(position) &&
                                    null != ((NymiDevice) mAdapterProvisions.getItem(position)).getKeys() &&
                                    !((NymiDevice) mAdapterProvisions.getItem(position)).getKeys().isEmpty()) {
                                    ((NymiDevice) mAdapterProvisions.getItem(position)).sign("Message to be signed",
                                            ((NymiDevice) mAdapterProvisions.getItem(position)).getKeys().get(0), new NymiDevice.NymiSignCallback() {
                                                @Override
                                                public void onMessageSigned(int status, String signature) {
                                                    if (status == NymiDevice.NymiSignCallback.SIGN_LOCAL_SUCCESS) {
                                                        // Of course your code will want to make use of this value otherwise.
                                                        // This code intends to demonstrate flow.
                                                        Toast.makeText(MainActivity.this, "Sign (on a dummy message) returned: " + signature, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Sign failed", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                } else {
                                    Toast.makeText(MainActivity.this, "Error retrieving keys", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        mListViewProvisions.setAdapter(mAdapterProvisions);
    }

    
    private void startProvision() {
        boolean status = mNymiAdapter.startProvision(new NymiAdapter.NymiProvisionCallback() {
            @Override
            public void onDeviceProvisioned(int status, NymiDevice nymiDevice, BitSet bitSet) {
                if (status == NymiAdapter.NymiProvisionCallback.PROVISION_SUCCESS) {
                    mAdapterProvisions.addDevice(nymiDevice);
                } else {
                    // Provisioning can fail due to connectivity problems.
                    // Unfortunately, your applications only recovery is to
                    // start the provisioning process over. You'll need to
                    // instruct the user to put their band back into provisioning mode.
                    Toast.makeText(MainActivity.this, "Error completing provision.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNymiAgreement(BitSet bitSet) {
                for (int i = 0; i < LEDS_NUMBER; i++) {
                    mLeds[i].setChecked(bitSet.get(i));
                    mLeds[i].setEnabled(true);
                }
                mButtonAccept.setEnabled(true);
                mButtonDecline.setEnabled(true);
                return;
            }
        });

        if (status == true) {
            Toast.makeText(MainActivity.this, "Provision started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Error starting provision", Toast.LENGTH_SHORT).show();
        }
    }

    // This build is configured so that the IP address of the host building
    // the apk is included among the resources and retrieved here. This is a
    // commmon trick, as most app development needs some server to talk to,
    // and in development settings, that server host will typically be your
    // build machine.
    private String loadBuildhostFromResources() {
        try {
            InputStream buildhost_stream = getResources().openRawResource(R.raw.buildhost);
            return new BufferedReader(new InputStreamReader(buildhost_stream)).readLine();
        } catch (IOException e) {
            // shouldn't happen. The build ensures this resource will be present.
            return "";
        }
    }
}