**PicoPiImx7dTemperature_v1**

**versione v1: God Activity**

PicoPiImx7dTemperature è un'app AndroidThings sviluppata per la piattaforma PICO-PI-IMX7-STARTKIT-RAINBOW-HAT 
(https://shop.technexion.com/pico-pi-imx7-startkit-rainbow-hat.html) che rileva la temperatura attraverso il sensore Bmx280 
presente nel RainbowHat, la mostra all'utente attraverso il display alfanumerico HT16K33 (sempre presente nel RainbowHat) e attiva 
i led blu, verde o rosso, a seconda se temperatura rilevata è minore di NORMAL_TEMPERATURE, compresa tra NORMAL_TEMPERATURE e 
MAX_TEMPERATURE oppure maggiore di MAX_TEMPERATURE.
Solo nel caso in cui il valore soglia MAX_TEMPERATURE venga superato, viene attivato un allarme sonoro attraverso il buzzer pwm Piezo Buzzer.
                  



Per importare il seguente progetto in AndroidStudio:
1) clonare il repo in locale
2) rinominare la directory del progetto da 'PicoPiTemperature_v1' a 'PicoPiImx7dTemperature'
3) fare l'import in AndoridStudio tramite l'apposita procedura

