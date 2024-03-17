function doSignup() {
    const username = document.getElementById('inputUsername').value;
    const mail = document.getElementById('inputMail').value;
    const password = document.getElementById('inputPassword').value;
    const passwordRepeat = document.getElementById('inputPasswordRepeat').value;

    fetch("/rest/users", {
        method: "POST", headers: {
            "Content-Type": "application/json"
        }, body: JSON.stringify({
            displayName: username, mail: mail, password: password, passwordRepeat: passwordRepeat
        })
    })
        .then(response => {
            if (response.status === 201) {
                return response.json();
            }
            throw new Error("Signup failed");
        })
        .then(data => {
            location.assign("/hades/");
        })
        .catch(error => {
            alert("Signup failed. Please try again.");
        });
}
