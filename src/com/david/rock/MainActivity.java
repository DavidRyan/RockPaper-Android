package com.david.rock;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.animation.*;
import android.view.animation.Animation.AnimationListener;
import android.content.Context;
import android.widget.*;
import android.widget.CompoundButton.*;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.os.Message;
import android.os.Handler;
import android.os.Messenger;
import android.view.View;
import android.location.Location;
import android.content.ComponentName; 
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.os.IBinder;
import android.util.Log;
import android.content.ServiceConnection;
import android.os.RemoteException; 

import java.util.*;
import java.math.*;

public class MainActivity extends Activity implements OnCheckedChangeListener, 
                                                      SensorEventListener, 
                                                      AnimationListener, 
                                                      View.OnClickListener
 
{

    CheckBox mRock;
    CheckBox mPaper;
    CheckBox mScissor; 
    ImageView mImage;
    String mCheckedButton = "";
    private SensorManager mSensorManager;
    private float mAccel; 
    private float mAccelCurrent; 
    private float mAccelLast; 
    private Messenger mLocationService;
    private Boolean mBound;
    Location mLocation;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mRock    = (CheckBox) findViewById(R.id.rock);        
        mPaper   = (CheckBox) findViewById(R.id.paper);        
        mScissor = (CheckBox) findViewById(R.id.scissor);        
        mImage   = (ImageView) findViewById(R.id.conf_image);
        mRock.setOnCheckedChangeListener(this);
        mPaper.setOnCheckedChangeListener(this);
        mScissor.setOnCheckedChangeListener(this);
        mImage.setOnClickListener(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this, 
                                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                             SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;


        //Bind GPS
        startServices();
        doBind();

    }

    @Override
    public void onPause() {
        doUnbind();
        stopServices();
        super.onPause();
    }

    private void stopServices() {
        stopService(new Intent(this, LocationService.class));
    }

    private void startServices() {
        startService(new Intent(this, LocationService.class));
    }

    private ServiceConnection mLocationServiceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName aName, IBinder aIBinder) {
                mLocationService = new Messenger(aIBinder);
                try {
                    Message msg = Message.obtain(null, LocationService.MSG_REGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mLocationService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            public void onServiceDisconnected(ComponentName aName) {
                mLocationService = null;
            }
    };

    Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message aMsg) {
            switch (aMsg.what) {
                case LocationService.MSG_LOCATION_RESPONSE:
                    handleLocationResponse(aMsg);
                    break;
                case LocationService.MSG_LOCATION_STATUS:
                    break;
                default:
                    super.handleMessage(aMsg);
            }
        }
    });

    private void handleLocationResponse(Message aMsg) {
        mLocation = (Location) aMsg.obj;
        Log.d("XXX", Double.toString(mLocation.getLatitude()));
        new SendMoveTask(MainActivity.this).execute();
    }


    private void doBind() {
        Intent i = new Intent(this, com.david.rock.LocationService.class);
        mBound = this.bindService(i, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void doUnbind() {
        if (mBound) {
            if (mLocationService != null) {
                try {
                    Message msg = Message.obtain(null, LocationService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mLocationService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            this.unbindService(mLocationServiceConnection);
            mBound = false;
        }
    }

    @Override
    public void onClick(View aView) {
        switch (aView.getId()) {
            case R.id.conf_image:
                mImage.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked == false) {
            mRock.setChecked(false);
            mPaper.setChecked(false);
            mScissor.setChecked(false);
        }
        else {
            mRock.setChecked(false);
            mPaper.setChecked(false);
            mScissor.setChecked(false);
            buttonView.setChecked(true);
        }

        if(mRock.isChecked()) {
            mCheckedButton = Constants.ROCK;
        } else if (mPaper.isChecked()) {
            mCheckedButton = Constants.PAPER;
        } else if (mScissor.isChecked()) {
            mCheckedButton = Constants.SCISSOR;
        }
        
    }

    private void sendMove() {

        try {
            Message msg = Message.obtain(null, LocationService.MSG_REQUEST_LOCATION);
            msg.replyTo = mMessenger;
            mLocationService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    } 

    private void animateConfirmation(String result) {

        if (result.equals("win")) {
            mImage.setImageResource(R.drawable.win);
        } else if (result.equals("lose")) {
            mImage.setImageResource(R.drawable.lose);
        } else if (result.equals("tie")) {
            mImage.setImageResource(R.drawable.tie);
        } else {
            Toast.makeText(getApplicationContext(), "No Game", Toast.LENGTH_LONG).show();
            return;
        }

        Animation upAnim = AnimationUtils.loadAnimation(this, R.anim.image_up);
        upAnim.setAnimationListener(this);
        mImage.setVisibility(View.VISIBLE);
        mImage.startAnimation(upAnim);
    }

	private class SendMoveTask extends AsyncTask<String, Void, String> {

        ProgressDialog mDialog;
        Activity mActivity;

       public SendMoveTask(MainActivity activity) {
            this.mActivity = activity;
            Context context = activity;
            mDialog = new ProgressDialog(context);
        }
    
        @Override
        protected void onPreExecute() {
            this.mDialog.setMessage("Progress start");
            this.mDialog.show();
            
        }
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
            double lat = round(mLocation.getLatitude(),4);
            double llong = round(mLocation.getLongitude(),4);
            HashMap<String, String> params = new HashMap<String, String>();

            params.put("loca", Double.toString(round((lat + llong),3)));
            params.put("move", mCheckedButton);
            try {
                response = Network.post(Network.URL, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
            this.mDialog.dismiss();
            animateConfirmation(result);
		}
	}

    public static double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    //Accelerometer Stuff

    @Override
    public void onSensorChanged(SensorEvent se) {
        float x = se.values[0];
        float y = se.values[1];
        float z = se.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta; // perform low-cut filter
        if(mAccel > 7.0f) {
            if(mCheckedButton != "") {
                sendMove();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @Override
    protected void onResume() {
        mSensorManager.registerListener(this, 
                                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
                                        SensorManager.SENSOR_DELAY_NORMAL);
        doBind();
        startServices();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(this);
        super.onStop();
    }


    //AnimationListener
    
    @Override
    public void onAnimationEnd(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onAnimationStart(Animation animation) {
    } 

}
