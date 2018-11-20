package com.alessandrocosma.picopiimx7dtemperature_v1;

import android.app.Activity;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.things.contrib.driver.bmx280.Bmx280;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;

/**
 * A simple application for AndroidThings platform - PICO-PI-IMX7 with RainbowHat.
 * The RainbowHat BMP280 sensor reports the current temperature every 2 seconds
 * and displays it in the segment display.
 * If temperature >= 38°C the red led is turned on and the device plays an alarm.
 * If 34 <= temperature < 38 the green led is turned on.
 * Otherwise (temperature < 34) blue led is turned on.
 * N.B. Temperature readings are affected by heat radiated from your Pi’s CPU and the onboard LEDs;
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Bmx280 tempSensor;
    private Handler handler;
    private AlphanumericDisplay segment;
    private Speaker buzzer;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**Turn off the led lights, opened by default*/
        try {
            BoardDefaults.turnOffLedR();
            BoardDefaults.turnOffLedG();
            BoardDefaults.turnOffLedB();
        }
        catch (IOException e){
            Log.e(TAG, "Unable to turn off the leds");
        }

        /** Open temp sensor */
        try {
            tempSensor = BoardDefaults.openTempSensor();
        }
        catch (IOException e){
            Log.e(TAG, "Unable to open temperature sensor");
        }

        /** Open segment display */
        try {
            segment = BoardDefaults.openSegmentDisplay();
        }
        catch (IOException | InterruptedException e){
            Log.e(TAG, "Unable to open segment display");
        }

        /** Open speaker */
        try {
            buzzer = BoardDefaults.openSpeaker();
        }
        catch (IOException e){
            Log.e(TAG, "Unable to open the speaker");
        }

        /** Initialize the button driver for close the programm.
         *  we choose the button C - GPIO2_IO07
         */
        initPIO();

        /** Init the handler */
        handler = new Handler(Looper.getMainLooper());
    }



    @Override
    protected void onStart() {
        super.onStart();

        //avvio letture della temperatura
        handler.post(reportTemperature);
    }

    private void initPIO() {

        try {
            button = RainbowHat.openButtonC();
            button.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    Log.d(TAG, "button C pressed");
                    MainActivity.this.finish();
                }
            });
        }
        catch (IOException e){
            Log.e(TAG, "Unable to open Button C");
        }
    }



    private final Runnable reportTemperature = new Runnable() {

        float temperature;
        @Override
        public void run() {
            try {
                temperature = tempSensor.readTemperature();
                BigDecimal tempBG = new BigDecimal(temperature);
                tempBG = tempBG.setScale(2, BigDecimal.ROUND_HALF_UP);
                temperature = (tempBG.floatValue());
            }
            catch (IOException e){
                Log.e(TAG, "Unable to read temperature");
                temperature = Float.valueOf(null);
            }
            catch (IllegalStateException e){
                Log.e(TAG, "Unable to read temperature");
                temperature = Float.valueOf(null);
            }

            /**Print temperature value, turn on the correct light*/
            try {
                if(temperature < 30.0f){
                    BoardDefaults.turnOffLedG();
                    BoardDefaults.turnOffLedR();
                    BoardDefaults.turnOnLedB();
                }

                else if(temperature >= 30.0f && temperature < 31.0f){
                    BoardDefaults.turnOffLedR();
                    BoardDefaults.turnOffLedB();
                    BoardDefaults.turnOnLedG();
                }
                else {
                    BoardDefaults.turnOffLedG();
                    BoardDefaults.turnOffLedB();
                    BoardDefaults.turnOnLedR();

                    /** Play a sound with the piezo speaker */
                    try {
                        BoardDefaults.playSound(buzzer, 2000, 1800);
                    }
                    catch (IOException | InterruptedException e){
                        Log.e(TAG, "Unable to play a sound with the piezo buzzer");
                    }


                }

                BoardDefaults.writeText(String.valueOf(temperature), segment);

                Log.d(TAG, "temperatura: " + temperature + "°C");
            }
            catch (IOException e){
                Log.e(TAG, "Unable to write in segment display");
            }
            catch (InterruptedException e){
                Log.e(TAG, "Unable to write in segment display");
            }


            handler.postDelayed(reportTemperature, TimeUnit.SECONDS.toMillis(2));
        }
    };

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called.");

        handler.removeCallbacks(reportTemperature);

        try {
            button.close();
            BoardDefaults.closeSegmentDisplay(segment);
            BoardDefaults.closeTempSensor(tempSensor);
            BoardDefaults.closeSpeaker(buzzer);
            BoardDefaults.turnOffLedR();
            BoardDefaults.turnOffLedB();
            BoardDefaults.turnOffLedG();
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Unable to close all the BoardDefaults resources");
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
