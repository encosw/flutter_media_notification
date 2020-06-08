package com.tarafdari.flutter_media_notification;


import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import android.content.Intent;

import androidx.core.content.ContextCompat;

/** FlutterMediaNotificationPlugin */
public class FlutterMediaNotificationPlugin implements MethodCallHandler {
  private static Registrar registrar;
  private static MethodChannel channel;

  private FlutterMediaNotificationPlugin(Registrar r) {
    registrar = r;
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    FlutterMediaNotificationPlugin.channel = new MethodChannel(registrar.messenger(), "flutter_media_notification");
    FlutterMediaNotificationPlugin.channel.setMethodCallHandler(new FlutterMediaNotificationPlugin(registrar));
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    switch (call.method) {
      case "showNotification":
        final String title = call.argument("title");
        final String author = call.argument("author");
        final boolean isPlaying = call.argument("isPlaying");
        final String imageUrl = call.argument("imageUrl");
        final boolean hasNext = call.argument("hasNext");
        final boolean hasPrev = call.argument("hasPrev");
        showNotification(title, author, isPlaying, imageUrl, hasNext, hasPrev);
        result.success(null);
        break;
      case "hideNotification":
        hideNotification();
        result.success(null);
        break;
      default:
        result.notImplemented();
    }
  }

  static void callEvent(String event) {

    FlutterMediaNotificationPlugin.channel.invokeMethod(event, null, new Result() {
      @Override
      public void success(Object o) {
        // this will be called with o = "some string"
      }

      @Override
      public void error(String s, String s1, Object o) {}

      @Override
      public void notImplemented() {}
    });
  }

  static void showNotification(String title, String author, boolean play, String imageUrl, boolean hasNext, boolean hasPrev) {

    Intent serviceIntent = new Intent(registrar.context(), NotificationPanel.class);
    serviceIntent.putExtra("title", title);
    serviceIntent.putExtra("author", author);
    serviceIntent.putExtra("isPlaying", play);
    serviceIntent.putExtra("imageUrl", imageUrl);
    serviceIntent.putExtra("hasNext", hasNext);
    serviceIntent.putExtra("hasPrev", hasPrev);

    registrar.context().startService(serviceIntent);
  }

  private void hideNotification() {
    Intent serviceIntent = new Intent(registrar.context(), NotificationPanel.class);
    registrar.context().stopService(serviceIntent);
  }
}