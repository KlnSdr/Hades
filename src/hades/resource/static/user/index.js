function updateMail() {
    const mail = document.getElementById('email').value;

    fetch(`{{CONTEXT}}/rest/users/id/${userId}/update/mail`, {
        method: 'PUT', headers: {
            'Content-Type': 'application/json',
        }, body: JSON.stringify({mail}),
    }).then((response) => {
        if (response.ok) {
            alert('Mail updated');
        } else {
            alert('Error updating mail');
        }
    });
}

function updateName() {
    const displayName = document.getElementById('username').value;

    fetch(`{{CONTEXT}}/rest/users/id/${userId}/update/name`, {
        method: 'PUT', headers: {
            'Content-Type': 'application/json',
        }, body: JSON.stringify({displayName}),
    }).then((response) => {
        if (response.ok) {
            alert('Displayname updated');
        } else {
            alert('Error updating displayname');
        }
    });
}

function updatePassword() {
    const oldPassword = document.getElementById('password').value;
    const password = document.getElementById('newPassword').value;
    const passwordRepeat = document.getElementById('confirmPassword').value;

    fetch(`{{CONTEXT}}/rest/users/id/${userId}/update/password`, {
        method: 'PUT', headers: {
            'Content-Type': 'application/json',
        }, body: JSON.stringify({oldPassword, password, passwordRepeat}),
    }).then((response) => {
        if (response.ok) {
            alert('Password updated');
        } else {
            alert('Error updating password');
        }
    });
}

function copyToken() {
    navigator.clipboard.writeText(document.getElementById("inputLoginToken").value).then(r => {
        document.getElementById("bttnCopyToken").classList.add("pulseGreen");
        setTimeout(() => {
            document.getElementById("bttnCopyToken").classList.remove("pulseGreen");
        }, 750);
    }).catch(e => {
        alert("Error: Could not copy token to clipboard");
    });
}

function regenToken() {
    document.getElementById("bttnRegenToken").innerText = "generating...";
    fetch(`{{CONTEXT}}/rest/users/id/${userId}/update/token`, {
        method: 'PUT',
    }).then((response) => {
        if (response.ok) {
            return response.json();
        } else {
            alert('Error regenerating token');
        }
    }).then(resJson => {
        document.getElementById("inputLoginToken").value = resJson["token"];
        document.getElementById("bttnRegenToken").innerText = "regenerate";
    });
}