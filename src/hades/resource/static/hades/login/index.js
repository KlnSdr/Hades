function doLogin() {
    const username = document.getElementById('inputUsername').value;
    const password = document.getElementById('inputPassword').value;

    fetch("{{CONTEXT}}/rest/users/login", {
        method: "POST", headers: {
            "Content-Type": "application/json"
        }, body: JSON.stringify({
            displayName: username, password: password
        })
    })
        .then(response => {
            if (response.status === 200) {
                return response.json();
            }
            throw new Error("Login failed");
        })
        .then(data => {
            location.assign(data["redirectTo"]);
        })
        .catch(error => {
            alert("Login failed. Please try again.");
        });
}
