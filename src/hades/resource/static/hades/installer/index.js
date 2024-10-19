let adminPassword = null;

function openPanelAdminPassword() {
    const container = document.getElementById('centerContainer');
    container.innerHTML = "";

    const header = document.createElement('h1');
    header.innerText = 'Set Admin Password';
    container.appendChild(header);

    const containerInputs = document.createElement('div');
    containerInputs.style.display = 'flex';
    containerInputs.style.flexDirection = 'column';

    const labelPassword = document.createElement('label');
    labelPassword.innerText = 'Password:';
    containerInputs.appendChild(labelPassword);

    const inputPassword = document.createElement('input');
    inputPassword.type = 'password';
    containerInputs.appendChild(inputPassword);

    const br = document.createElement('br');
    containerInputs.appendChild(br);

    const labelConfirmPassword = document.createElement('label');
    labelConfirmPassword.innerText = 'Confirm Password:';
    containerInputs.appendChild(labelConfirmPassword);

    const inputConfirmPassword = document.createElement('input');
    inputConfirmPassword.type = 'password';
    containerInputs.appendChild(inputConfirmPassword);

    container.appendChild(containerInputs);

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