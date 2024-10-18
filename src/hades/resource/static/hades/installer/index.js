let adminPassword = null;

function openPanelAdminPassword() {
    const container = document.getElementById('centerContainer');
    container.innerHTML = "";

    const labelPassword = document.createElement('label');
    labelPassword.innerText = 'Password:';
    container.appendChild(labelPassword);

    const inputPassword = document.createElement('input');
    inputPassword.type = 'password';
    container.appendChild(inputPassword);

    const br = document.createElement('br');
    container.appendChild(br);

    const labelConfirmPassword = document.createElement('label');
    labelConfirmPassword.innerText = 'Confirm Password:';
    container.appendChild(labelConfirmPassword);

    const inputConfirmPassword = document.createElement('input');
    inputConfirmPassword.type = 'password';
    container.appendChild(inputConfirmPassword);

    const br2 = document.createElement('br');
    container.appendChild(br2);

    const buttonSubmit = document.createElement('button');
    buttonSubmit.innerText = 'install';
    buttonSubmit.onclick = () => {
        if (inputPassword.value === inputConfirmPassword.value && inputConfirmPassword.value.trim() !== "") {
            adminPassword = inputPassword.value;
            buttonSubmit.innerText = 'running...';
            runInstaller();
        } else {
            alert('Passwords do not match');
        }
    };
    container.appendChild(buttonSubmit);

}

function runInstaller() {
    fetch("{{CONTEXT}}/installer/run", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            adminPassword: adminPassword
        })
    }).then(response => {
        if (response.ok) {
            alert('Installer ran successfully');
            window.location.href = "{{CONTEXT}}";
        } else {
            throw new Error('Error running installer');
        }
    }).catch(error => {
        console.error('Error:', error);
        alert('Error running installer');
    })
}