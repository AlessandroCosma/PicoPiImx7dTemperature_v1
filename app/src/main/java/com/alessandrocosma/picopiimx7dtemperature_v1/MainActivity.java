package com.alessandrocosma.picopiimx7dtemperature_v1;

import android.app.Activity;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.things.contrib.driver.bmx280.Bmx280;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.pio.Gpio;
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
 * If temperature >= 28°C the red led is turned on and the device plays an alarm.
 * If 24 <= temperature < 28 the green led is turned on.
 * Otherwise (temperature < 24) blue led is turned on.
 * N.B. Temperature readings are affected by heat radiated from your Pi’s CPU and the onboard LEDs;
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Constants for different temperature threshold
    private static final float MAX_TEMPERATURE = 29.0f;
    private static final float NORMAL_TEMPERATURE = 22.0f;
    // Constant for brightness max value
    public static final int HT16K33_BRIGHTNESS_MAX = 0b00001111;


    private Bmx280 tempSensor;
    private AlphanumericDisplay mDisplay;
    private Speaker buzzer;
    private Button buttonC;
    private Gpio ledR, ledG, ledB;
    private Handler myHandler;
    private boolean playing = false;


    private void initButton() {

        try {
            buttonC = RainbowHat.openButtonC();
            buttonC.setOnButtonEventListener(new Button.OnButtonEventListener() {
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

    public void setLedLight(char led, boolean value){

        switch (led){
            case 'R':
                try {
                    ledR.setValue(value);
                    break;
                }
                catch (IOException e){
                    Log.e(TAG, "Unable to manage RED led");
                }

            case 'B':
                try {
                    ledB.setValue(value);
                    break;
                }
                catch (IOException e){
                    Log.e(TAG, "Unable to manage GREEN led");
                }

            case 'G':
                try {
                    ledG.setValue(value);
                    break;
                }
                catch (IOException e){
                    Log.e(TAG, "Unable to manage BLUE led");
                }

            default:
                Log.e(TAG, "Invalid led identifier. Allowed values: R,G,B");
        }
    }



    private final Runnable reportTemperature = new Runnable() {

        float temperature;

        @Override
        public void run() {
            if(playing) {
                try {
                    // Stop the buzzer.
                    buzzer.stop();
                    playing = false;
                }
                catch (IOException e){}
            }
            try {
                temperature = tempSensor.readTemperature();
                BigDecimal tempBG = new BigDecimal(temperature);
                tempBG = tempBG.setScale(2, BigDecimal.ROUND_HALF_UP);
                temperature = (tempBG.floatValue());
            }
            catch (IOException | IllegalStateException e){
                Log.e(TAG, "Unable to read temperature");
                temperature = Float.valueOf(null);
            }

            /** Display temperature value and turn on the correct light*/
            try {
                if(temperature < NORMAL_TEMPERATURE){
                    setLedLight('R',false);
                    setLedLight('G',false);
                    setLedLight('B',true);
                }

                else if(temperature >= NORMAL_TEMPERATURE && temperature < MAX_TEMPERATURE){
                    setLedLight('R',false);
                    setLedLight('G',true);
                    setLedLight('B',false);
                }
                else {
                    setLedLight('R',true);
                    setLedLight('G',false);
                    setLedLight('B',false);

                    /** Play a note on the piezo buzzer at 2000 Hz for 2 seconds*/
                    try {
                        buzzer.play(2000);
                        playing = true;
                    }
                    catch (IOException e){
                        Log.e(TAG, "Unable to play a sound with the piezo buzzer");
                    }
                }

                if (temperature != null)
                    temperature_string = String.valueOf(temperature);
                else
                    temperature_string = "--"

                mDisplay.display(temperature_string);
                Log.d(TAG, "temperatura: " + temperature + "°C");
            }
            catch (IOException e){
                Log.e(TAG, "Unable to write in mDisplay display");
            }
            catch (InterruptedException e){
                Log.e(TAG, "Unable to write in mDisplay display");
            }


            myHandler.postDelayed(reportTemperature, TimeUnit.SECONDS.toMillis(2));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        /**Open led connection*/
        try {
            ledR = RainbowHat.openLedRed();
            ledG = RainbowHat.openLedGreen();
            ledB = RainbowHat.openLedBlue();
        }
        catch (IOException e){
            Log.e(TAG, "Unable to open leds");
        }

        /**Turn off the led lights, opened by default*/

        setLedLight('R',false);
        setLedLight('G',false);
        setLedLight('B',false);


        /**Open temperature sensor*/
        try {
            tempSensor = RainbowHat.openSensor();
            tempSensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
            tempSensor.setMode(Bmx280.MODE_NORMAL);
        }
        catch (IOException e){
            Log.e(TAG, "Unable to open BMP280 sensor");
        }

        /** Open segment display */
        try {
            mDisplay = RainbowHat.openDisplay();
            mDisplay.setBrightness(HT16K33_BRIGHTNESS_MAX);
            mDisplay.setEnabled(true);
        }
        catch (IOException e){
            Log.e(TAG, "Unable to open segment display");
        }

        /** Open speaker */
        try {
            buzzer = RainbowHat.openPiezo();
        }
        catch (IOException e){
            Log.e(TAG, "Unable to open the speaker");
        }

        /** Initialize the button C for close the programm */
        initButton();

        /** Init the handler */
        myHandler = new Handler(Looper.getMainLooper());
    }



    @Override
    protected void onStart() {
        super.onStart();

        // Start temperature detection
        myHandler.post(reportTemperature);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called.");

        // Stop temperature detection
        myHandler.removeCallbacks(reportTemperature);

        try {
            buttonC.close();
        }
        catch (IOException e){
            Log.e(TAG, "Unable to close Button C");
        }


        /** Turn off the led lights, opened by default*/

        setLedLight('R',false);
        setLedLight('G',false);
        setLedLight('B',false);

        /** Close led connection*/
        try {
            ledR.close();
            ledG.close();
            ledB.close();
        }
        catch (IOException e){
            Log.e(TAG, "Unable to close leds");
        }



        /** Close temperature sensor*/
        try {
            tempSensor.close();
        }
        catch (IOException e){
            Log.e(TAG, "Unable to close BMP280 sensor");
        }

        /** Close segment display */
        try {
            mDisplay.clear();
            mDisplay.setEnabled(false);
            mDisplay.close();
        }
        catch (IOException e){
            Log.e(TAG, "Unable to open segment display");
        }

        /** Close speaker */
        try {
            if(playing){
                buzzer.stop();
            }
            buzzer.close();
        }
        catch (IOException e){
            Log.e(TAG, "Unable to open the speaker");
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

    }
}

/**
 * Analisi PicoPiImx7dTemperature_v1 [2018-11-22, 19:32:42]
 *      Programma utilizza la classe BoardDefaults per aprire e chiudere le componenti,
 *      sfruttando quindi gli user driver per RainbowHat.
 *
 * Analisi PicoPiImx7dTemperature_v1 _bis [2018-11-22, 19:40:30]
 *      Programma NON utilizza i metodi di BoardDefaults per aprire e chiudere il sensore di
 *      temperatura tempSensor, ma usa gli user drivers specifici del Bmp280:
 *
 *
 *         APERTURA
 *
 *          try {
 *                 tempSensor = new Bmx280("I2C1");
 *                 // Configure driver settings
 *                 tempSensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
 *                 // Ensure the driver is powered and not sleeping before trying to read from it
 *                 tempSensor.setMode(Bmx280.MODE_NORMAL);
 *             } catch (IOException e) {
 *                 Log.e(TAG, "Couldn't configure the device");
 *             }
 *
 *
 *        CHIUSURA
 *
 *         tempSensor.close();
 *
 *
 *
 * Aalisi PicoPiImx7dTemperature_v1 _bis_NoTempSensorClosure [2018-11-22, 20:20:38]
 *      Programma NON chiude il sensore di temperaruta tempSensor
 *      Codice per la chiusura commentato per non essere eseguito:
 *
 *             tempSensor.close();
 *
 *
 */
