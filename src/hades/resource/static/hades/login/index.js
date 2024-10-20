function doLogin() {
    const username = document.getElementById('inputUsername').value;
    const password = document.getElementById('inputPassword').value;

    const urlParams = new URLSearchParams(window.location.search);
    const src = urlParams.get('src');
    let redirectUrl = null;

    if (src) {
        try {
            redirectUrl = atob(src);  // Decode base64-encoded path
        } catch (e) {
            console.error("Invalid base64 string for src parameter");
        }
    }

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
            if (redirectUrl) {
                location.assign(redirectUrl);
            } else {
                location.assign(data["redirectTo"]);
            }
        })
        .catch(error => {
            alert("Login failed. Please try again later.");
        });
}
