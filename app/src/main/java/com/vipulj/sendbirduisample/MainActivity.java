package com.vipulj.sendbirduisample;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.sendbird.android.MessagingChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdEventHandler;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.handler.UserExistenceHandler;
import com.sendbird.android.model.BroadcastMessage;
import com.sendbird.android.model.Channel;
import com.sendbird.android.model.FileLink;
import com.sendbird.android.model.Message;
import com.sendbird.android.model.MessagingChannel;
import com.sendbird.android.model.ReadStatus;
import com.sendbird.android.model.SystemMessage;
import com.sendbird.android.model.TypeStatus;
import com.sendbird.android.model.User;
import com.vipulj.sendbirduisample.Constants.Credentials;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // SendBird
    private UserListQuery mUserListQuery;
    private ArrayAdapter  mAdapter;
    private MessagingChannelListQuery mChannelListQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
    }

    private void setup() {

        SendBird.init(Credentials.SEND_BIRD_APP_ID);

        SendBird.setEventHandler(new SendBirdEventHandler() {

            @Override
            public void onConnect(final Channel channel) {
                Log.v(TAG, "connected");
            }

            @Override
            public void onError(final int i) {
                Log.v(TAG, "error");
            }

            @Override
            public void onChannelLeft(final Channel channel) {
                Log.v(TAG, "channel left");
            }

            @Override
            public void onMessageReceived(final Message message) {
                Log.v(TAG, "message received");
            }

            @Override
            public void onMutedMessageReceived(final Message message) {
                Log.v(TAG, "muted message received");
            }

            @Override
            public void onSystemMessageReceived(final SystemMessage systemMessage) {
                Log.v(TAG, "system message received");
            }

            @Override
            public void onBroadcastMessageReceived(final BroadcastMessage broadcastMessage) {
                Log.v(TAG, "broadcast message received");
            }

            @Override
            public void onFileReceived(final FileLink fileLink) {
                Log.v(TAG, "file received");
            }

            @Override
            public void onMutedFileReceived(final FileLink fileLink) {
                Log.v(TAG, "muted file received");
            }

            @Override
            public void onReadReceived(final ReadStatus readStatus) {
                Log.v(TAG, "read received");
            }

            @Override
            public void onTypeStartReceived(final TypeStatus typeStatus) {
                Log.v(TAG, "type started received");
            }

            @Override
            public void onTypeEndReceived(final TypeStatus typeStatus) {
                Log.v(TAG, "type end received");
            }

            @Override
            public void onAllDataReceived(final SendBird.SendBirdDataType sendBirdDataType, final int i) {
                Log.v(TAG, "all data received");
            }

            @Override
            public void onMessageDelivery(final boolean b, final String s, final String s1, final String s2) {
                Log.v(TAG, "message delivery");
            }

            @Override
            public void onMessagingStarted(MessagingChannel channel) {
                SendBird.join(channel.getUrl());
                SendBird.connect();
            }

            @Override
            public void onMessagingUpdated(final MessagingChannel messagingChannel) {
                Log.v(TAG, "messaging updated");
            }

            @Override
            public void onMessagingEnded(final MessagingChannel messagingChannel) {
                Log.v(TAG, "messaging ended");
            }

            @Override
            public void onAllMessagingEnded() {
                Log.v(TAG, "all messaging enabled");
            }

            @Override
            public void onMessagingHidden(final MessagingChannel messagingChannel) {
                Log.v(TAG, "messaging hidden");
            }

            @Override
            public void onAllMessagingHidden() {
                Log.v(TAG, "all messaging hidden");
            }

        });

        String userId = generateDeviceUUID(this);

        SendBird.login(userId, "User " + userId.hashCode());

        SendBird.connect();
        SendBird.checkUserExistence(userId, new UserExistenceHandler() {
            @Override
            public void onError(final SendBirdException e) {
                Log.v(TAG, "error UEH");
            }

            @Override
            public void onSuccess(final Map<String, User> map) {
                Log.v(TAG, "success UEH");
                Log.v(TAG, "Connection state" + SendBird.getConnectionState().name());
            }
        });

        SendBird.startMessaging(userId);



    }

    private void loadUsers() {
        mUserListQuery = SendBird.queryUserList();
        mUserListQuery.setLimit(30);

        if(mUserListQuery != null && mUserListQuery.hasNext() && !mUserListQuery.isLoading()) {
            mUserListQuery.next(new UserListQuery.UserListQueryResult() {
                @Override
                public void onResult(List<User> users) {
                    mAdapter.addAll(users);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(SendBirdException e) {
                }
            });
        }
    }

    // Messaging
    private void startMessaging(String userId) {
        SendBird.startMessaging(userId); // Start a 1:1 messaging with given userId.
    }
    // OR
    private void startGroupMessaging(Collection<String> userIds) {
        SendBird.startMessaging(userIds); // Start a group messaging with given userIds.
    }



    private void queryMessagingChannels() {
        mChannelListQuery = SendBird.queryMessagingChannelList();
        if(mChannelListQuery.hasNext()) {
            mChannelListQuery.next(new MessagingChannelListQuery.MessagingChannelListQueryResult() {
                @Override
                public void onResult(final List<MessagingChannel> channels) {
                    mAdapter.addAll(channels);
                    if (channels.size() <= 0) {
                        Toast.makeText(MainActivity.this, "No messagings were found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(final int i) {

                }

            });
        }
    }

    private void inviteUser(String channelUrl, String userId) {
        SendBird.inviteMessaging(channelUrl, userId);
    }

    private void endMessaging(String channelUrl) {
        SendBird.endMessaging(channelUrl); // Remove a messaging from my list with given channelUrl.
    }


    // Generate UUID
    public static String generateDeviceUUID(Context context) {
        String serial = android.os.Build.SERIAL;
        String androidID = Settings.Secure.ANDROID_ID;
        String deviceUUID = serial + androidID;

        MessageDigest digest;
        byte[] result;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            result = digest.digest(deviceUUID.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }
}
