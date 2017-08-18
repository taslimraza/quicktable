package com.app.mobi.quicktabledemo.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.app.mobi.quicktabledemo.utils.SpecificMenuSingleton;
import com.google.gson.Gson;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.IOException;

/**
 * Created by mobi11 on 26/1/16.
 */
public class ChatXMPP {

    public static boolean connected = false;
    public boolean loggedIn = false;
    public static boolean isConnecting = false;
    public static boolean isToasted = true;
    private boolean chat_created = false;
    private String serverAddress;
    public static String userName;
    public static String password;
    public static XMPPTCPConnection connection;
    Gson gson;
    ChatService chatService;
    public static ChatXMPP instance = null;
    public static boolean instanceCreated = false;
    private MultiUserChat multiUserChat;

    public ChatXMPP(ChatService chatService, String serverAddress, String userName, String password){

        this.chatService = chatService;
        this.serverAddress = serverAddress;
        this.userName = userName;
        this.password = password;
        initialiseConnection();

    }

    public static ChatXMPP getInstance(ChatService chatService, String serverAddress, String userName, String password){

        if(instance == null){
            instance = new ChatXMPP(chatService, serverAddress, userName, password);
        }
        return instance;
    }

//    public Chat myChat;
//    ChatManagerListenerImpl mChatManagerListener;
//    MMessageListener mMessageListener;

//    public void init() {
//        gson = new Gson();
//        mMessageListener = new MMessageListener(chatService);
//        mChatManagerListener = new ChatManagerListenerImpl();
//        initialiseConnection();
//    }

    private void initialiseConnection() {

        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration
                .builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setServiceName(serverAddress);
        config.setHost(serverAddress);
        config.setPort(5222);
        config.setDebuggerEnabled(true);
        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        connection = new XMPPTCPConnection(config.build());
        XMPPConnectionListener connectionListener = new XMPPConnectionListener();
        connection.addConnectionListener(connectionListener);
//        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
//        multiUserChat = manager.getMultiUserChat("0123456789@conference.159.203.88.161");
//        try {
//            multiUserChat.join("917569550492");
//            Log.i("ChatRoom", "chatroom created");
//        } catch (XMPPException.XMPPErrorException e) {
//            e.printStackTrace();
//        } catch (SmackException e) {
//            e.printStackTrace();
//        }
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.disconnect();
            }
        }).start();
    }

    public void connect(final String caller) {

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected synchronized Boolean doInBackground(Void... arg0) {
                if (connection.isConnected())
                    return false;
                isConnecting = true;
                if (isToasted)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(chatService,
                                    caller + "=>connecting....",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                Log.d("Connect() Function", caller + "=>connecting....");

                try {
                    connection.connect();
                    DeliveryReceiptManager dm = DeliveryReceiptManager
                            .getInstanceFor(connection);
                    dm.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
                    dm.addReceiptReceivedListener(new ReceiptReceivedListener() {

                        @Override
                        public void onReceiptReceived(final String fromid,
                                                      final String toid, final String msgid,
                                                      final Stanza packet) {

                        }
                    });
                    connected = true;

                } catch (IOException e) {
                    if (isToasted)
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(
                                                chatService,
                                                "(" + caller + ")"
                                                        + "IOException: ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    Log.e("(" + caller + ")", "IOException: " + e.getMessage());
                } catch (SmackException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(chatService,
                                    "(" + caller + ")" + "SMACKException: ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("(" + caller + ")",
                            "SMACKException: " + e.getMessage());
                } catch (XMPPException e) {
                    if (isToasted)

                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(
                                                chatService,
                                                "(" + caller + ")"
                                                        + "XMPPException: ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    Log.e("connect(" + caller + ")",
                            "XMPPException: " + e.getMessage());

                }
                return isConnecting = false;
            }
        };
        connectionThread.execute();
    }

    public void login() {

        try {
            connection.login(userName, password);

            SpecificMenuSingleton menuSingleton = SpecificMenuSingleton.getInstance();
            String placeId = menuSingleton.getChatPlaceID();
            MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
            multiUserChat = manager.getMultiUserChat(placeId + "@conference.159.203.88.161");
            try {
                multiUserChat.join("917569550492");
                Log.i("ChatRoom", "chatroom created");
                multiUserChat.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Message message) {
                        Log.i("Group message", message.getBody());
//                        ChatActivity.chatMessages.add(chatMessage);
//                        ChatMessage chatMessage = new ChatMessage(message.getBody());
//                        String from = message.getFrom();
//                        if (message.getFrom().contains("917569550492")){
//                            chatMessage.setMine(true);
////                            ChatActivity.chatAdapter.add(chatMessage);
//                        }else {
//                            chatMessage.setMine(false);
////                            ChatActivity.chatAdapter.add(chatMessage);
//                        }
//                        ChatActivity.listView.setSelection(ChatActivity.chatAdapter.getCount() - 1);
//                        ChatActivity.chatAdapter.notifyDataSetChanged();
//                        final ChatMessage chatMessage = gson.fromJson(
//                                message.getBody(), ChatMessage.class);

                    }
                });
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            }
            Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");

        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

    }

    private class ChatManagerListenerImpl implements ChatManagerListener {
        @Override
        public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
                                final boolean createdLocally) {
//            if (!createdLocally)
//                chat.addMessageListener(mMessageListener);

        }

    }

    public void sendMessage(ChatMessage chatMessage) {
        String body = gson.toJson(chatMessage);

        if (!chat_created) {
//            myChat = ChatManager.getInstanceFor(connection).createChat(
//                    chatMessage.receiver + "@"
//                            + chatService.getString(R.string.server),
//                    mMessageListener);
//            try {
//                multiUserChat.sendMessage("Hi there");
//            } catch (XMPPException e) {
//                e.printStackTrace();
//            } catch (SmackException.NotConnectedException e) {
//                e.printStackTrace();
//            }
            chat_created = true;
        }
        final Message message = new Message();
        message.setBody(body);
        message.setStanzaId(chatMessage.msgid);
        message.setType(Message.Type.chat);

        try {
            if (connection.isAuthenticated()) {

//                myChat.sendMessage(message);
                multiUserChat.sendMessage(body);

            } else {

                login();
            }
        } catch (Exception e) {
//            Log.e("xmpp.SendMessage()-Exception",
//                    "msg Not sent!" + e.getMessage());
        }

    }

    public void sendMessage(String msg){
//        ChatMessage chatMessage = new ChatMessage(msg);
//        ChatActivity.chatMessages.add(chatMessage);
//        ChatActivity.chatAdapter.notifyDataSetChanged();
        try {
            multiUserChat.sendMessage(msg);
        }catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection) {

            Log.d("xmpp", "Connected!");
            connected = true;
            if (!connection.isAuthenticated()) {
                login();
            }
        }

        @Override
        public void connectionClosed() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(chatService, "ConnectionCLosed!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ConnectionCLosed!");
            connected = false;
            chat_created = false;
            loggedIn = false;
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(chatService, "ConnectionClosedOn Error!!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ConnectionClosedOn Error!");
            connected = false;

            chat_created = false;
            loggedIn = false;
        }

        @Override
        public void reconnectingIn(int arg0) {

            Log.d("xmpp", "Reconnectingin " + arg0);

            loggedIn = false;
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {

                        Toast.makeText(chatService, "ReconnectionFailed!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ReconnectionFailed!");
            connected = false;

            chat_created = false;
            loggedIn = false;
        }

        @Override
        public void reconnectionSuccessful() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(chatService, "REConnected!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ReconnectionSuccessful");
            connected = true;

            chat_created = false;
            loggedIn = false;
        }

        @Override
        public void authenticated(XMPPConnection arg0, boolean arg1) {
            Log.d("xmpp", "Authenticated!");
            loggedIn = true;

//            ChatManager.getInstanceFor(connection).addChatListener(
//                    mChatManagerListener);

            chat_created = false;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }).start();
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(chatService, "Connected!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
        }
    }

    private class MMessageListener implements ChatMessageListener {

        public MMessageListener(Context context) {
        }

        @Override
        public void processMessage(final org.jivesoftware.smack.chat.Chat chat,
                                   final Message message) {
            Log.i("MyXMPP_MESSAGE_LISTENER", "Xmpp message received: '"
                    + message);

            if (message.getType() == Message.Type.chat
                    && message.getBody() != null) {
                final ChatMessage chatMessage = gson.fromJson(
                        message.getBody(), ChatMessage.class);

                processMessage(chatMessage);
            }
        }

        private void processMessage(final ChatMessage chatMessage) {

            chatMessage.isMine = false;
//            ChatActivity.chatMessages.add(chatMessage);
            try {
                multiUserChat.sendMessage(chatMessage.body);
            } catch (XMPPException e) {
                e.printStackTrace();
            }catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
//                    ChatActivity.chatAdapter.notifyDataSetChanged();
                }
            });
        }

    }


}
