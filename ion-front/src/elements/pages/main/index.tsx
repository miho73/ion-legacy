import React, {useEffect, useState} from 'react';
import {isLogin} from '../../service/auth';
import CannotAuthorize from '../auth/cannotAuth';
import {LoginPage} from '../auth/login/login';
import {Link} from 'react-router-dom';
import ErrorPage from '../etc/error';
import axios from 'axios';

function LoggedInIndex() {
    const [user, setUser] = useState({name: '', id: '', priv: 0});
    const [workState, setWorkState] = useState(-1);
    const [picture, setPicture] = useState({
        url: 'https://apod.nasa.gov/apod/image/1708/PerseidsoverPyreneesGraffand1024.jpg',
        type: 'image',
        title: 'Perseids over the Pyrénées',
        exp: `This mountain and night skyscape stretches across the French Pyrenees National Park on August 12, near the peak of the annual Perseid meteor shower. The multi-exposure panoramic view was composed from the Col d'Aubisque, a mountain pass, about an hour before the bright gibbous moon rose. Centered is a misty valley and lights from the region's Gourette ski station toward the south. Taken over the following hour, frames capturing some of the night's long bright perseid meteors were aligned against the backdrop of stars and Milky Way.`,
        cpy: 'Jean-Francois\nGraffand'
    });
    const [apodSet, setApodSet] = useState(false);
    const [apodDetail, setApodDetail] = useState(false);

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
                if (res.data['result']['type'] === 'image') {
                    setPicture(res.data['result']);
                }
            })
            .catch(err => {
                console.error(err);
            }).finally(() => {
            setApodSet(true);
        });

    }, []);

    if (workState === 1) {
        return <ErrorPage exp='사용자 정보를 받아오지 못했어요.'/>
    }

    return (
        <>
            {picture.type === 'image' && apodSet &&
                <div className='w-100 h-100 pict' style={{backgroundImage: ('url(' + picture.url + ')')}}>
                    <div onClick={() => setApodDetail(!apodDetail)} title='Details'>
                        <p className='m-0 tit'>{picture.title}</p>
                        {apodDetail &&
                            <>
                                <hr className='my-2'/>
                                <p className='m-0'>{picture.exp}</p>
                                {picture.cpy !== '' &&
                                    <p className='my-1'>Copyright: {picture.cpy}</p>
                                }
                            </>
                        }
                    </div>
                </div>
            }
            {!apodSet &&
                <div className='w-100 h-100 pict'/>
            }
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
        </>
    );
}

function Index() {
    const [loginState, setLoginState] = useState(-1);

    useEffect(() => {
        isLogin(setLoginState);
    }, []);

    if (loginState === -1) {
        return <></>;
    } else if (loginState === 0) {
        return (
            <LoggedInIndex/>
        );
    } else if (loginState === 1) {
        return <LoginPage/>;
    } else {
        return (
            <CannotAuthorize/>
        );
    }
}

export default Index;
