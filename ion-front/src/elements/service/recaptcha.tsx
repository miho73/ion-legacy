function ready(action, and) {
    grecaptcha.enterprise.ready(async () => {
        const token = await grecaptcha.enterprise.execute('6LdsNjInAAAAAJByk1stdHinRzoNZqcN4wl_CFXR', {action: action});
        and(token);
    });
}

export {ready}