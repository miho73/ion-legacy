import React, { useEffect, useState } from 'react';
import { isLogin } from '../../service/auth';
import CannotAuthorize from '../auth/cannotAuth';
import { useNavigate } from 'react-router-dom';
import { LoginPage } from '../auth/login';

function LoggedInIndex() {
    return (
        <main className='container mt-4'>
            <h1>인덱스에는 뭐가 있어야 할까</h1>
        </main>
    );
}

function Index() {
    const [loginState, setLoginState] = useState(-1);

    const navigate = useNavigate();
    
    useEffect(() => {
        isLogin(setLoginState);
    }, []);

    if(loginState === -1) {
        return <></>;
    }
    else if(loginState === 0) {
        return (
            <LoggedInIndex/>
        );
    }
    else if(loginState === 1) {
        return <LoginPage/>;
    }
    else {
        return (
            <CannotAuthorize/>
        );
    }
}

export default Index;