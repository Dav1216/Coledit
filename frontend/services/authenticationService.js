const sendLogInCredentials = async (data) => {
    // Set up the URL and data for the login request
    const url = `https://${process.env.HOSTNAME}/api/auth/login`;

    try {
        // Making the fetch request to the login API
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data),
            credentials: 'include'  // Ensures cookies are sent and received with the request
        });

        // Check if the response is successful
        // if the response is unsuccessful, throw an error
        if (!response.ok) {
            throw new Error(`You don't have the right credentials!`);
        }
    } catch (error) {
        throw error;
    }
}

const sendSignUpCredentials = async (data) => {
    // Set up the URL and data for the login request
    const url = `https://${process.env.HOSTNAME}/api/auth/signup`;

    try {
        // Making the fetch request to the login API
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data),
            credentials: 'include'  // Ensures cookies are sent and received with the request
        });

        return response;
    } catch (error) {
        throw error;
    }
}


const sendLogOutRequest = async () => {
    sessionStorage.removeItem('reloadCount');
    // Set up the URL and data for the login request
    const url = `https://${process.env.HOSTNAME}/api/auth/logout`;

    try {
        // Making the fetch request to the login API
        const response = await fetch(url, {
            method: 'POST',
            credentials: 'include' 
        });

        return response;
    } catch (error) {
        throw error;
    }
}



export default { sendLogInCredentials, sendSignUpCredentials, sendLogOutRequest }