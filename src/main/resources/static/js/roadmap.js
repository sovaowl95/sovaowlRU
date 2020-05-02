var id = 'uniqIdForModalShow';
var modal;

/*
*/
document.addEventListener('DOMContentLoaded', function () {
    if (true) {
        let instance = OverlayScrollbars(document.getElementsByClassName('roadmapPageWrap')[0], {
            className: "os-theme-light",
            resize: "none",
            sizeAutoCapable: true,
            clipAlways: true,
            normalizeRTL: true,
            paddingAbsolute: false,
            autoUpdate: null,
            autoUpdateInterval: 33,
            nativeScrollbarsOverlaid: {
                showNativeScrollbars: false,
                initialize: true
            },
            overflowBehavior: {
                x: "scroll",
                y: "hidden"
            },
            scrollbars: {
                visibility: "auto",
                autoHide: "move",
                autoHideDelay: 800,
                dragScrolling: true,
                clickScrolling: false,
                touchSupport: true,
                snapHandle: false
            },
            textarea: {
                dynWidth: false,
                dynHeight: false,
                inheritedAttrs: ["style", "class"]
            },
            callbacks: {
                onInitialized: null,
                onInitializationWithdrawn: null,
                onDestroyed: null,
                onScrollStart: null,
                onScroll: null,
                onScrollStop: null,
                onOverflowChanged: null,
                onOverflowAmountChanged: null,
                onDirectionChanged: null,
                onContentSizeChanged: null,
                onHostSizeChanged: null,
                onUpdated: null
            }
        });

        let arr = document.getElementsByClassName("roadmapBlock2Wrap");
        // let inst2 = OverlayScrollbars(arr, {
        //     className: "os-theme-light",
        //     resize: "none",
        //     sizeAutoCapable: true,
        //     clipAlways: true,
        //     normalizeRTL: true,
        //     paddingAbsolute: false,
        //     autoUpdate: null,
        //     autoUpdateInterval: 33,
        //     scrollbars: {
        //         visibility: "auto",
        //         autoHide: "move",
        //         autoHideDelay: 800,
        //         dragScrolling: true,
        //         clickScrolling: false,
        //         touchSupport: true,
        //         snapHandle: false
        //     }
        // });
        // let inst3 = OverlayScrollbars(document.getElementsByClassName("roadmapBlock2Wrap_ADV"), {
        //     className: "os-theme-light",
        //     resize: "none",
        //     sizeAutoCapable: true,
        //     clipAlways: true,
        //     normalizeRTL: true,
        //     paddingAbsolute: false,
        //     autoUpdate: null,
        //     autoUpdateInterval: 33,
        //     scrollbars: {
        //         visibility: "auto",
        //         autoHide: "move",
        //         autoHideDelay: 800,
        //         dragScrolling: true,
        //         clickScrolling: false,
        //         touchSupport: true,
        //         snapHandle: false
        //     }
        // });
    }
});

function show(idEl) {
    el = document.getElementById(idEl);
    modal = document.createElement('div');
    modal.setAttribute('id', id);
    modal.setAttribute('class', 'modal');

    modal.addEventListener('click', modalListener);

    modalBody = document.createElement('div');
    modal.appendChild(modalBody);
    modalBody.setAttribute('class', 'modalBody');

    add('rbStatus', el, modalBody);
    add('rbId', el, modalBody);
    add('rbTitle', el, modalBody);
    add('rbDescription', el, modalBody);


    hr = document.createElement('hr');
    modalBody.appendChild(hr);

    modalBodySub = document.createElement('div');
    modalBodySub.classList.add('modalBodySub');
    modalBody.appendChild(modalBodySub);

    try {
        ratingBlock = document.createElement('div');
        ratingBlock.classList.add('popRating');
        if(el.getElementsByTagName('img').length !== 0){
            add('minus', el, ratingBlock);
            add('rating', el, ratingBlock);
            add('plus', el, ratingBlock);
        }
    } catch (e) {

    }

    add('rbNick', el, modalBodySub);
    try {
        modalBodySub.appendChild(ratingBlock);
    } catch (e) {
    }
    add('rbDate', el, modalBodySub);

    document.getElementsByTagName('body')[0].appendChild(modal);
}

function modalListener(ev) {
    ev.stopPropagation();
    document.getElementById(id).removeEventListener('click', modalListener);
    document.getElementById(id).remove();
}

function add(name, el, modalBody) {
    let node;
    try {
        node = el.getElementsByClassName(name)[0].cloneNode(true);
    } catch (e) {
        node = document.createElement('img');
        node.classList.add('roadmapBlock2Img');
        node.classList.add('ok');
        node.setAttribute('src', '/img/success.png');
    }
    node.style.display = "block";
    if (name === 'rbStatus') {
        node.style.display = "flex";
    } else if (name === 'rbId' || name === 'rbTitle' || name === 'rbNick' || name === 'rbDate') {
        node.style.display = 'inline-block';
    }
    modalBody.appendChild(node);
}

function plus(num) {
    event.stopPropagation();
    let request = new XMLHttpRequest();
    let link = "/roadmap/" + num + "/plus";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            //increase rating
            def = document.getElementById(num);
            rating = def.getElementsByClassName('rating')[0];
            rating.innerHTML++;

            let status = def.getElementsByClassName('minus')[0] === undefined;
            if (status) {
                //repair minus button
                def = def.getElementsByClassName('ok')[0];
                node = document.createElement('img');
                node.classList.add('roadmapBlock2Img');
                node.classList.add('minus');
                node.setAttribute('src', '/img/minus.png');
                node.setAttribute('onclick', 'minus(' + num + ')');
                def.parentNode.replaceChild(node, def);
                try{
                    def = document.getElementsByClassName('popRating')[0];
                    rating = def.getElementsByClassName('rating')[0];
                    rating.innerHTML++;
                    if(status){
                        def = def.getElementsByClassName('ok')[0];
                        node = document.createElement('img');
                        node.classList.add('roadmapBlock2Img');
                        node.classList.add('minus');
                        node.setAttribute('src', '/img/minus.png');
                        node.setAttribute('onclick', 'minus(' + num + ')');
                        def.parentNode.replaceChild(node, def);
                    }
                }catch (e) {

                }
            } else {
                //just replace + to V
                img = def.getElementsByClassName('plus')[0];
                node = document.createElement('img');
                node.classList.add('roadmapBlock2Img');
                node.classList.add('ok');
                node.setAttribute('src', '/img/success.png');
                img.parentNode.replaceChild(node, img);
                try{
                    def = document.getElementsByClassName('popRating')[0];
                    rating = def.getElementsByClassName('rating')[0];
                    rating.innerHTML++;

                    // status = def.getElementsByClassName('minus')[0] === undefined;
                    img = def.getElementsByClassName('plus')[0];
                    node = document.createElement('img');
                    node.classList.add('roadmapBlock2Img');
                    node.classList.add('ok');
                    node.setAttribute('src', '/img/success.png');
                    img.parentNode.replaceChild(node, img);
                }catch (e) {

                }
            }
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function minus(num) {
    event.stopPropagation();
    let request = new XMLHttpRequest();
    let link = "/roadmap/" + num + "/minus";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            //increase rating
            def = document.getElementById(num);
            rating = def.getElementsByClassName('rating')[0];
            rating.innerHTML--;

            let status = def.getElementsByClassName('plus')[0] === undefined;
            if (status) {
                //repair minus button
                def = def.getElementsByClassName('ok')[0];
                node = document.createElement('img');
                node.classList.add('roadmapBlock2Img');
                node.classList.add('plus');
                node.setAttribute('src', '/img/plus.png');
                node.setAttribute('onclick', 'plus(' + num + ')');
                def.parentNode.replaceChild(node, def);
                try{
                    def = document.getElementsByClassName('popRating')[0];
                    rating = def.getElementsByClassName('rating')[0];
                    rating.innerHTML--;
                    if(status){
                        def = def.getElementsByClassName('ok')[0];
                        node = document.createElement('img');
                        node.classList.add('roadmapBlock2Img');
                        node.classList.add('plus');
                        node.setAttribute('src', '/img/minus.png');
                        node.setAttribute('onclick', 'plus(' + num + ')');
                        def.parentNode.replaceChild(node, def);
                    }
                }catch (e) {

                }
            } else {
                //just replace + to V
                img = def.getElementsByClassName('minus')[0];
                node = document.createElement('img');
                node.classList.add('roadmapBlock2Img');
                node.classList.add('ok');
                node.setAttribute('src', '/img/success.png');
                img.parentNode.replaceChild(node, img);
                try{
                    def = document.getElementsByClassName('popRating')[0];
                    rating = def.getElementsByClassName('rating')[0];
                    rating.innerHTML--;

                    // status = def.getElementsByClassName('minus')[0] === undefined;
                    img = def.getElementsByClassName('minus')[0];
                    node = document.createElement('img');
                    node.classList.add('roadmapBlock2Img');
                    node.classList.add('ok');
                    node.setAttribute('src', '/img/success.png');
                    img.parentNode.replaceChild(node, img);
                }catch (e) {

                }
            }
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}
