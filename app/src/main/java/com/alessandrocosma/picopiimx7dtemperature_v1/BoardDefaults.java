package com.alessandrocosma.picopiimx7dtemperature_v1;

import com.google.android.things.contrib.driver.bmx280.Bmx280;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;

import java.io.IOException;

public class BoardDefaults {
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_IMX7D_PICO = "imx7d_pico";
    public static final int HT16K33_BRIGHTNESS_MAX = 0b00001111;
    private static Gpio ledR;
    private static Gpio ledG;
    private static Gpio ledB;
    private static Bmx280 tempSensor;


    /**
     * Return the GPIO pin that the ButtonC is connected on.
     */
    public static String getGPIOForButton() throws IOException {
        return "GPIO2_IO07";
    }




    public static void turnOffLedR() throws IOException{
        // open RED led connection
        ledR = RainbowHat.openLedRed();
        //turn off the light
        ledR.setValue(false);
        //close the device when done
        ledR.close();

        ledR.getValue();
    }

    public static void turnOffLedG() throws IOException{

        ledG = RainbowHat.openLedGreen();
        ledG.setValue(false);
        ledG.close();
    }

    public static void turnOffLedB()throws IOException{

        ledB = RainbowHat.openLedBlue();
        ledB.setValue(false);
        ledB.close();

    }


    public static void turnOnLedR() throws IOException{
        // open RED led connection
        ledR = RainbowHat.openLedRed();
        //turn off the light
        ledR.setValue(true);
        //close the device when done
        ledR.close();
    }

    public static void turnOnLedG() throws IOException{

        ledG = RainbowHat.openLedGreen();
        ledG.setValue(true);
        ledG.close();
    }

    public static void turnOnLedB()throws IOException{

        ledB = RainbowHat.openLedBlue();
        ledB.setValue(true);
        ledB.close();

    }


    public static AlphanumericDisplay openSegmentDisplay()throws IOException, InterruptedException{
        AlphanumericDisplay segment = RainbowHat.openDisplay();
        segment.setBrightness(HT16K33_BRIGHTNESS_MAX);
        segment.setEnabled(true);

        return segment;
    }

    public static void writeText(String text, AlphanumericDisplay segment) throws IOException, InterruptedException{

        // Display a string on the segment display.
        segment.display(text);
        Thread.sleep(2000);
    }

    public static void closeSegmentDisplay(AlphanumericDisplay segment)throws IOException, InterruptedException{
        segment.clear();
        segment.setEnabled(false);
        segment.close();
    }


    public static Bmx280 openTempSensor()throws IOException{
        tempSensor = RainbowHat.openSensor();
        tempSensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
        tempSensor.setMode(Bmx280.MODE_NORMAL);

        return tempSensor;
    }



    public static void closeTempSensor(Bmx280 sensor) throws IOException{
        sensor.close();
    }


    public static Speaker openSpeaker() throws IOException{
        return RainbowHat.openPiezo();
    }

    public static void playSound(Speaker buzzer, int frequenza, int timeInMillis) throws IOException, InterruptedException{
        // Play a note on the buzzer.
        buzzer.play(frequenza);
        Thread.sleep(timeInMillis);
        // Stop the buzzer.
        buzzer.stop();

    }

    public static void closeSpeaker(Speaker buzzer) throws IOException{
        // Close the device when done.
        buzzer.stop();
        buzzer.close();
    }



}