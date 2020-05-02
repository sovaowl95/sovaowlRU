function replenish() {
    let request = new XMLHttpRequest();
    request.open("POST", "/qiwi/createBill", true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            // window.location=request.response;
            window.open(request.response, '_blank');
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let text = document.getElementById('inputText').value;
    let forWhom;
    if (document.getElementById('inputFor') === null) {
        forWhom = null;
    } else {
        forWhom =  document.getElementById('inputFor').value;
    }

    let value = document.getElementById('inputValue').value;
    let currency = document.getElementById('inputCurrency').value;
    let anonymously = document.getElementById('inputAnonymously').checked;

    let json = {
        'text': text,
        'forWhom': forWhom,
        'value': value,
        'currency': currency,
        'anonymously': anonymously
    };
    let myJson = JSON.stringify(json);
    request.send(myJson);
}