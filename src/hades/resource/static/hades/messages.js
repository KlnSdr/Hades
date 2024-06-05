function loadMessages() {
    fetch("{{CONTEXT}}/rest/messages/unread").then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error("Failed to load messages");
        }
    })
    .then(data => {
        displayMessageCount(data["messages"]);
        attacheOpenUnreadMessages(data["messages"]);
    })
    .catch(error => {
        alert(error);
    });
}

function displayMessageCount(messages) {
    document.getElementById("bttnMessages").innerText = "Messages (" + messages.length + ")";
}

function attacheOpenUnreadMessages(messages) {
    document.getElementById("bttnMessages").onclick = () => {
        openUnreadMessages(messages);
    }
}

function openUnreadMessages(messages) {
    const unreadMessagesContainer = document.createElement("table");
    unreadMessagesContainer.classList.add("tblUnreadMessages");

    messages.forEach(message => {
        const row = document.createElement("tr");

        [["from", i => i], ["dateSent", timestamp => new Date(parseInt(timestamp)).toLocaleDateString("de-DE")], ["message", content => content.substring(0, 6) + "..."]].forEach(pair => {
            const cell = document.createElement("td");
            cell.innerText = pair[1](message[pair[0]]);
            row.appendChild(cell);
        });

        const tdOpen = document.createElement("td");
        const bttnOpen = document.createElement("button");
        bttnOpen.innerText = "Open";
        bttnOpen.onclick = () => {
            const divContent = document.createElement("div");

            const pFrom = document.createElement("p");
            pFrom.innerText = "From: " + message["from"];
            divContent.appendChild(pFrom);

            const pDateSent = document.createElement("p");
            pDateSent.innerText = "Date sent: " + new Date(parseInt(message["dateSent"])).toLocaleDateString("de-DE");
            divContent.appendChild(pDateSent);

            const hr = document.createElement("hr");
            divContent.appendChild(hr);

            const pMessage = document.createElement("p");
            pMessage.innerText = message["message"];
            divContent.appendChild(pMessage);

            openPopup(divContent);
        }
        tdOpen.appendChild(bttnOpen);
        row.appendChild(tdOpen);

        const tdMarkAsRead = document.createElement("td");
        const bttnMarkAsRead = document.createElement("button");
        bttnMarkAsRead.innerText = "Mark as read";
        bttnMarkAsRead.onclick = () => {
            markMessageAsRead(message["id"], bttnMarkAsRead);
        }
        tdMarkAsRead.appendChild(bttnMarkAsRead);
        row.appendChild(tdMarkAsRead);

        unreadMessagesContainer.appendChild(row);
    });

    openPopup(unreadMessagesContainer);
}

function markMessageAsRead(messageId, sender) {
    fetch(`{{CONTEXT}}/rest/messages/read/${messageId}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        }
    }).then(response => {
        if (response.ok) {
            loadMessages();
            removeParentTableRow(sender);
        } else {
            throw new Error("Failed to mark message as read");
        }
    }).catch(error => {
        alert(error);
    });
}

function removeParentTableRow(sender) {
    if (sender.tagName === "BODY") {
        console.error("No parent table row found");
        return;
    }

    if (sender.tagName === "TR") {
        sender.remove();
    } else {
        removeParentTableRow(sender.parentElement);
    }
}