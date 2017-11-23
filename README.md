# Android example app

Android example to build an app that will access your data on pryv.io using the [Pryv Java library](https://github.com/pryv/lib-java)

This sample app contains the code that provides the login to your pryv account on the platform that you will have defined [here](https://github.com/pryv/app-android-example/blob/master/app/src/main/java/com/pryv/appAndroidExample/LoginActivity.java#L35).
For example, when using the pryv.me demo platform:

```java
public final static String DOMAIN = "pryv.me";
```

After signing in your account the app is able to fetch all Events from a Stream and create a text note on the push of a button.

## Usage

Get the code `git clone https://github.com/pryv/android-app-example`

### Android studio

To use the example app in Android Studio, go to `File>open` and select the folder that was generated by the `git clone`. Now run in on your emulated device or Android phone.

### Eclipse

[Request it](mailto:tech@pryv.com)

## Quick integration with another existing Android app

If you already developed your own Android app and you want to integrate Pryv into it, here is a concise procedure to setup the only elements that you will need and in the shortest time.

### Prerequisites

First of all, you need to include the [Pryv Java library](https://github.com/pryv/lib-java) in your project.

Moreover, do not forget to add the ***Internet permission*** in your ***AndroidManifest.xml*** as follow:

```android
<uses-permission android:name="android.permission.INTERNET" />
```

### Login to your Pryv

You will need to copy the following classes in your project:
[***LoginActivity***](https://github.com/pryv/app-android-example/blob/master/app/src/main/java/com/pryv/appAndroidExample/LoginActivity.java) and
[***Credentials***](https://github.com/pryv/app-android-example/blob/master/app/src/main/java/com/pryv/appAndroidExample/Credentials.java).
Do not forget to declare the ***LoginActivity*** in your manifest and also copy the corresponding xml file.

***LoginActivity*** will handle all the process of account creation and login through a WebView.
As soon as a login is successful, a pair of username and token will be stored in the Android SharedPreferences using a ***Credentials*** object.

You can load the **LoginActivity** on the push of a login button for example and check if the user is log by calling the function ***hasCredentials()*** from your ***Credentials*** object.

Note that you can modify **LoginActivity** to adapt the domain and app ID to be used:

```java
public final static String DOMAIN = "pryv.me";
public final static String APPID = "app-android-example";
```

### Interacting with your Pryv

There is still few necessary additions to be made in your ***MainActivity***.

First of all, you need to initialize the connection by providing the credentials previously stored during the login phase:
```java
Credentials credentials = new Credentials(MainActivity.this);
Connection connection = new Connection(credentials.getUsername(), credentials.getToken(), LoginActivity.DOMAIN);
```

You can then follow the end of the [Getting-started:java guide](http://pryv.github.io/getting-started/java/#manage-events) in order to manage your Pryv resources.

Note that Android enforces HTTP calls to the Pryv API (Get, Create, Update, Delete) to be executed in a different thread. Thus, you could encapsulate them in an AsyncTask or use the following basic alternative:

```java
new Thread() {
	public void run() {
	    // Do you HTTP calls to the Pryv API here (Get, Create, Update, Delete)
	}
}.start();
```

In the same spirit, Android also verify that only the thread that created a View can modify it. Thus, if we want our **updateStatusText** function to update the UI, we have to force it to run on the UI thread in the following way:

```java
private void updateStatusText(final String text) {
	runOnUiThread(new Runnable() {
	    @Override
	    public void run() {
		progressView.setText(text);
	    }
	});
}
```

### Further explanations

If you still have misunderstandings when integrating Pryv into your app or if you want to see more concrete examples, do not hesitate to take a look at the sample [MainActivity](https://github.com/pryv/app-android-example/blob/master/app/src/main/java/com/pryv/appAndroidExample/MainActivity.java).

## License

[Revised BSD license](https://github.com/pryv/documents/blob/master/license-bsd-revised.md)
