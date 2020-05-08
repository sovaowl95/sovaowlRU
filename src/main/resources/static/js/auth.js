function authGoogle() {
    let url = "https://accounts.google.com/o/oauth2/v2/auth" +
        "?" +
        "client_id=" + googleClientId +
        "&" +
        "response_type=code" +
        "&" +
        "access_type=offline" +
        "&" +
        "scope=openid profile email " +
        "https://www.googleapis.com/auth/youtube.force-ssl " +
        "https://www.googleapis.com/auth/youtube " +
        "https://www.googleapis.com/auth/youtube.readonly " +
        "https://www.googleapis.com/auth/youtube.upload " +
        "https://www.googleapis.com/auth/youtubepartner-channel-audit" +
        "&" +
        "redirect_uri=https://sovaowl.ru/api/auth/google";
    let token = document.getElementsByName("secTokenState")[0].getAttribute('content');
    url = url + "&state=" + token;
    window.location = url;
}

function authTwitch() {
    let url = "https://id.twitch.tv/oauth2/authorize" +
        "?" +
        "client_id=" + twitchClientId +
        "&" +
        "redirect_uri=https://sovaowl.ru/api/auth/twitch" +
        "&" +
        "response_type=code" +
        "&" +
        "scope=" +
        "openid" +
        "+" +
        "analytics:read:extensions" +
        "+" +
        "analytics:read:games" +
        "+" +
        "bits:read" +
        "+" +
        "channel:read:subscriptions" +
        "+" +
        "clips:edit" +
        "+" +
        "user:edit" +
        "+" +
        "user:edit:broadcast" +
        "+" +
        "user:read:broadcast" +
        "+" +
        "user:read:email" +
        "+" +
        "channel:moderate" +
        "+" +
        "chat:edit" +
        "+" +
        "chat:read" +
        "+" +
        "whispers:read" +
        "+" +
        "whispers:edit" +
        "&" +
        'claims={"id_token":{"email":null,"email_verified":null,"preferred_username":null}}'
    ;
    let token = document.getElementsByName("secTokenState")[0].getAttribute('content');
    url = url + "&state=" + token;
    window.location = url;
}

function authGG() {
    let url = "https://api2.goodgame.ru/oauth/authorize" +
        "?" +
        "client_id=" + ggClientId +
        "&" +
        "redirect_uri=https://sovaowl.ru/api/auth/gg" +
        "&" +
        "response_type=code" +
        "&" +
        "scope=chat.token"
    ;
    let token = document.getElementsByName("secTokenState")[0].getAttribute('content');
    url = url + "&state=" + token;
    window.location = url;
}

function authVK() {
    let url = "https://oauth.vk.com/authorize" +
        "?" +
        "client_id=" + vkClientId +
        "&" +
        "display=page" +
        "&" +
        "response_type=code" +
        "&" +
        "redirect_uri=https://sovaowl.ru/api/auth/vk" +
        "&" +
        "scope=notify,email"
    ;
    let token = document.getElementsByName("secTokenState")[0].getAttribute('content');
    url = url + "&state=" + token;
    window.location = url;
}