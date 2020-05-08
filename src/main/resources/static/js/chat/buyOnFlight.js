function buySmile(id, element) {
    mySendRequest("/shop/buy/smile/" + id, null, function (request) {
        if (request.status === 200) {
            let parentNode = element.parentNode.parentNode;

            let img1 = parentNode.children[0];
            let img2 = parentNode.children[1];
            let tooltip = parentNode.children[2];

            img1.remove();

            img2.classList.add('smileFromWebsite');

            tooltip.innerHTML = tooltip.children[0].innerHTML;

            addSmileFromWebsiteListener(img2);

            // document.getElementById('myTest').getBoundingClientRect()
            // DOMRect {x: -14.765625, y: 766.5, width: 77.53125, height: 30, top: 766.5, …}
            // document.getElementById('myTest').style="display:block; margin-left: 15px;"
            // "display:block; margin-left: 15px;"
        }
    });
}

function buyStyle(id, element) {
    mySendRequest("/shop/buy/style/" + id, null, function (request) {
        if (request.status === 200) {
            element.setAttribute("onclick", "changeStyle(this)");
            element.children[0].remove();
        }
    });
}