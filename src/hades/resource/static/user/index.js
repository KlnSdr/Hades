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
