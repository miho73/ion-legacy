const TOKEN = process.env.REACT_APP_CAPTCHA_SITEKEY;

function ready(action, and) {
    grecaptcha.enterprise.ready(async () => {
        const token = await grecaptcha.enterprise.execute(TOKEN, {action: action});
        and(token);
    });
}

export {ready}