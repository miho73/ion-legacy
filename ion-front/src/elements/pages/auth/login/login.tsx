import React, {useEffect, useState} from 'react';
import axios from 'axios';
import {useNavigate} from 'react-router-dom';
import UpdateScode from './updateScode';
import LoginSection from './loginSection';
import {API_PREFIX} from "../../../service/apiUrl";

function LoginPage() {
    const [changeFlag, setChangeFlag] = useState(false);

    return (
        <main className='container mt-4'>
            {changeFlag &&
                <UpdateScode/>
            }
            {!changeFlag &&
                <LoginSection setChangeFlag={setChangeFlag}/>
            }
        </main>
    )
}

function SignoutPage(props) {
    const navigate = useNavigate();

    const [error, setError] = useState(0);

    useEffect(() => {
        axios.get(API_PREFIX+'/auth/api/signout')
            .then(res => {
                navigate('/');
            })
            .catch(err => {
                setError(1);
            });
    }, [])

    if (error) {
        return (
            <div className="alert alert-danger" role="alert">로그아웃하지 못했습니다.</div>
        )
    } else {
        return (
            <p>로그아웃중</p>
        );
    }
}

export {LoginPage, SignoutPage};