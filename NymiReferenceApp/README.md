# NymiReferenceApp
This is Android sample app for Nymi SDK 3.0 Beta for Android.

This sample app demonstrates initialization, provisioning, signing messages, and generating random numbers. It is also available in the download package for Nymi SDK 3.0 Beta for Android. The sample app is tested only on Android Studio. However, you can port the project to Eclipse and other IDEs along with the AAR file.

See the Nymi developer [documentation](https://www.nymi.com/dev/beta-documentation/) on more information about how to use the API. 

### Building the app

The Nymi Android API is provided in aar bundle which is a binary distribution of an Android Library Project. You can use the Nymi's Android API's sample app as a template that already has .aar file integrated within the app structure or you can integrate the aar bundle within your app structure yourself. Here's some helpful guidelines if you're integrating the aar file to your app:

1. Open the app in Android Studio.
2. Copy the aar file to the libs folder (create one if it's not there already) in your project directory. For example, <your_project>/app/libs/.
3. Open build.gradle (module) file and add a depedency to the aar files. Under `dependencies`, add the following line:

    `compile(name:'api-debug', ext:'aar')`

4. Open build.gradle (project file, and add a flat filesystem directory as a repository by adding the following under `repositories`:
    
    <code>flatDir {dirs 'libs'}</code>

5. Rebuild your project for changes to take effect.

Once you have the aar file integrated in your app, you can use the methods described in the API reference for initializing, provisioning, and interacting with the Nymi Band.

### Running the app

When you run the android sample app on a real Android device, it must be able to connect to the same network that the Nymulator can access. 

To set up the IP address of your machine in your application: 

1. Open `MainActivity.java` file.
2. Update `mNymiAdapter.setNymulator(localBuildhostResources());` line to include the IP address where the Nymulator is running.
3. The result should look like: `mNymiAdapter.setNymulator("10.0.1.99");`.


### Troubleshooting

#### Error when building sample app: Failed to resolve: :nymi-api-nymulator

* Workaround: Download JDK7 and point Android Studio to the correct location of the JDK.
