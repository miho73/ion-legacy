import axios from 'axios';

/**
 * check user login status
 * @returns 0: logged in. 1: not logged in. 2: error
 */
function isLogin(setLogin) {
    axios.get("/auth/api/authorize")
    .then(res => {
        if(res.data['result']) {
            setLogin(0);
        }
        else setLogin(1);
    })
    .catch(err => {
        console.error(err);
        setLogin(2);
    });
}

function checkPrivilege(setLogin) {
    axios.get("/auth/api/authorize-e", {
        params: {"priv": 3}
    })
    .then(res => {
        if(res.data['result']) {
            setLogin(0);
        }
        else setLogin(1);
    })
    .catch(err => {
        console.error(err);
        setLogin(2);
    });
}

export { isLogin, checkPrivilege };