package de.jojii.matrixclientserver.Bot;

import de.jojii.matrixclientserver.Callbacks.DataCallback;
import de.jojii.matrixclientserver.Callbacks.LoginCallback;
import org.awaitility.Duration;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

public class PreprodIntegrationTest {

    String username="" ;
    String password= "";
    String preprodHost = "https://matrix.i.tchap.gouv.fr";
    String accessToken = "";
    String roomId = "";
    String message = "coucou";
    String olivier = "";
    String calev = "";



    @Test
    void onLoginSuccess_shouldReturn_AccessToken() throws IOException {
        Client client = new Client(preprodHost);
        AtomicBoolean loginDone = new AtomicBoolean();


        client.login(username, password, loginData -> {
            if (loginData.isSuccess()) {
                System.err.println("logging in ok");
                loginDone.set(true);
            } else {
                System.err.println("error logging in : "+ loginData.getHome_server() );
                loginDone.set(false);
            }
        });

        await()
                .atLeast(Duration.ONE_HUNDRED_MILLISECONDS)
                .atMost(Duration.FIVE_SECONDS)
                .with()
                .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(()->{
                    return loginDone.get();
                });

    }


    @Test
    void onTestApi_shouldReturnResult() throws IOException {
        Client client = new Client(preprodHost);
        AtomicBoolean loginDone = new AtomicBoolean();
        AtomicBoolean apiCallDone = new AtomicBoolean();


        client.login(accessToken, new LoginCallback() {
            @Override
            public void onResponse(LoginData loginData) throws IOException {
                if (loginData.isSuccess()) {
                    System.err.println("logging in ok");
                    loginDone.set(true);
                    client.sendText(roomId,message,new DataCallback() {
                        @Override
                        public void onData(Object data) throws IOException {
                            apiCallDone.set(true);
                        }
                    });
                }else {
                    System.err.println("error logging in : "+ loginData.getHome_server() );
                    loginDone.set(false);
                }
                loginDone.set(true);
            }
        });



        await()
                .atLeast(Duration.ONE_HUNDRED_MILLISECONDS)
                .atMost(Duration.TEN_MINUTES)
                .with()
                .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(()->{
                    return loginDone.get() && apiCallDone.get();
                });

    }


    @Test
    void onCreateRoom_shouldReturnResult() throws IOException {
        Client client = new Client(preprodHost);
        AtomicBoolean loginDone = new AtomicBoolean();
        AtomicBoolean apiCallDone = new AtomicBoolean();

        String preset = Client.Room.trusted_private_chat;
        String visibility = Client.Room.room_private;
        String name = "AudioConf";
        String alias = "AudioConf_with_"+new Date().getTime();
        String topic = "AudioConf topic";
        List<String> invitations = Arrays.asList(olivier);
        String roomVersion = null;

        client.login(accessToken, new LoginCallback() {
            @Override
            public void onResponse(LoginData loginData) throws IOException {
                if (loginData.isSuccess()) {
                    System.err.println("logging in ok");
                    loginDone.set(true);
                    client.createRoom(preset,visibility, alias,name,topic, invitations, roomVersion,new DataCallback() {
                        @Override
                        public void onData(Object responsedata) throws IOException {
                            System.err.println("create room :" + responsedata.toString());
                            JSONObject object =(JSONObject)responsedata;

                            if(object.has("room_id")){
                                System.err.println("send message to room :" + object.get("room_id").toString());

                                client.sendText(object.get("room_id").toString(),message,new DataCallback() {
                                    @Override
                                    public void onData(Object data) throws IOException {
                                        System.err.println("send msg :" + data.toString());
                                        apiCallDone.set(true);
                                    }
                            });
                            }else{
                                apiCallDone.set(true);

                            }

                        }
                    });
                }else {
                    System.err.println("error logging in : "+ loginData.getHome_server() );
                    loginDone.set(false);
                }
                loginDone.set(true);
            }
        });



        await()
                .atLeast(Duration.ONE_HUNDRED_MILLISECONDS)
                .atMost(Duration.TEN_MINUTES)
                .with()
                .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(()->{
                    return loginDone.get() && apiCallDone.get();
                });

    }
}
