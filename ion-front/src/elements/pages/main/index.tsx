import React, { useEffect, useState, useRef } from 'react';
import { isLogin } from '../../service/auth';
import CannotAuthorize from '../auth/cannotAuth';
import { LoginPage } from '../auth/login';
import { Link } from 'react-router-dom';
import ErrorPage from '../etc/error';
import axios from 'axios';

function LoggedInIndex() {
    const [user, setUser] = useState({name: '', id: '', priv: 0});
    const [workState, setWorkState] = useState(-1);
    const [picture, setPicture] = useState({url: 'https://apod.nasa.gov/apod/image/1708/PerseidsoverPyreneesGraffand1024.jpg', type: 'image', title: 'Perseids over the Pyrénées'});
    const [apodSet, setApodSet] = useState(false);

    useEffect(() => {
        axios.get('/user/api/idx-iden')
        .then(res => {
            setUser(res.data['result']);
        })
        .catch(err => {
            setWorkState(1);
        });

        axios.get('/idx/apod')
        .then(res => {
            if(res.data['result']['type'] === 'image') {
                setPicture(res.data['result']);
            }
        })
        .catch(err => {
            console.error(err);
        }).finally(() => {
            setApodSet(true);
        });
        
    }, []);

    if(workState === 1) {
        return <ErrorPage exp='사용자 정보를 받아오지 못했어요.'/>
    }

    return (
        <>
            <main className='d-flex flex-column justify-content-center align-items-center h-100 text-center'>
                <div className='bdf'>
                    <h1 className='mb-3 text-white'>Hi {user.name}</h1>
                    <ul className="nav col-md-auto justify-content-center mb-md-0 gap-2">
                        <li><Link to="/ns" className="nav-link px-2 rounded fs-5">면불 신청</Link></li>
                        {user.priv > 1 &&
                            <li><Link to="/manage" className="nav-link px-2 rounded fs-5">관리자</Link></li>
                        }
                        <li><Link to="/auth/signout" className="nav-link px-2 rounded fs-5">로그아웃</Link></li>
                    </ul>
                </div>
            </main>
            {picture.type === 'image' && apodSet &&
                <div className='w-100 h-100 pict' style={{backgroundImage: ('url('+picture.url+')')}}>
                    <p className='m-0 fs-6'>{picture.title}</p>
                </div>
            }
            { !apodSet &&
                <div className='w-100 h-100 pict'/>
            }
        </>
    );
}

function Index() {
    const [loginState, setLoginState] = useState(-1);
    
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
