package com.sobriety.sobriety.commons;

import android.app.NotificationManager;

import com.firebase.client.Firebase;
import com.google.firebase.messaging.RemoteMessage;
import com.sobriety.sobriety.models.Message;
import com.sobriety.sobriety.models.User;

import java.util.ArrayList;

/**
 * Created by andre on 8/14/2018.
 */

public class Commons {
    public static boolean isMyGroup = false;
    public static boolean isMyStory = false;
    public static User thisUser = new User();
    public static User user = new User();
    public static ArrayList<Integer> selectedUserIds = new ArrayList<>();
    public static ArrayList<Integer> selectedNotiIds = new ArrayList<>();
    public static Firebase reference = null;
    public static int myNetworkId = 1;
    public static boolean available = true;
    public static Message message = new Message();
    public static ArrayList<Message> messages = new ArrayList<>();
    public static boolean event = false;
    public static NotificationManager notificationManager = null;
}
