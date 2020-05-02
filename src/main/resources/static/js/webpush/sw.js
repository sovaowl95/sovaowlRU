importScripts('https://www.gstatic.com/firebasejs/5.9.2/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/5.9.2/firebase-messaging.js');

firebase.initializeApp({
    messagingSenderId: "185675376558"
});


self.addEventListener('push', async event => {
    const db = await getDb();
    const tx = this.db.transaction('jokes', 'readwrite');
    const store = tx.objectStore('jokes');

    const data = event.data.json().data;
    data.id = parseInt(data.id);
    store.put(data);

    tx.oncomplete = async e => {
        const allClients = await clients.matchAll({ includeUncontrolled: true });
        for (const client of allClients) {
            client.postMessage('newData');
        }
    };
});