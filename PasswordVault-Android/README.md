# PasswordVault-Android
Password Vault app for Andoroid. It is a Nymi Enabled Application. This app was build using Nymi's Android SDK 2.0.

There are two libraries in the source code that you can choose to run the app and test it with either the Nymulator (simulator for the Nymi band) or with a Nymi band.

### To run the app on the Android simulator and test it with the Nymulator:
- Replace Password Vault/libs folder with Password Vault/lib/net folder.
- In Constants.java change `ISDEVICE` flag to `false`. 
- In Constants.java change the IP address to you network's IP address: `public static final String IP = "10.18.26.103";`.

### To run the app on a Android device and test it with a Nymi band:
- Replace Password Vault /libs folder with Password Vault /lib/native folder.
- In Constants.java change `ISDEVICE` flag to `true`.
