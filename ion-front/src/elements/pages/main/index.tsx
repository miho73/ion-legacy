import React, {useEffect, useState} from 'react';
import {isLogin} from '../../service/auth';
import CannotAuthorize from '../auth/cannotAuth';
import {LoginPage} from '../auth/login/login';
import {Link} from 'react-router-dom';
import ErrorPage from '../etc/error';
import axios from 'axios';
import {API_PREFIX} from "../../service/apiUrl";
import {Container} from "react-bootstrap";

function LnsStatusFrame(props) {
    return (
        <div className={'border border-0 p-2 rounded-4 d-flex justify-content-center align-items-center flex-column gap-0'}>
            <div className={'d-flex justify-content-center align-items-end gap-2'}>
                <p className={'display-4 mr-2 number'}>{props.cnt}</p>
                <p className={'number mb-2'}>/ 36</p>
            </div>
            <p className={'my-2'}>{props.nth}면 예약</p>
        </div>
    );
}

function LoggedInIndex() {
    const [workState, setWorkState] = useState(-1);

    const [user, setUser] = useState({name: '', id: '', priv: 0});
    const [picture, setPicture] = useState({
        url: 'https://apod.nasa.gov/apod/image/1708/PerseidsoverPyreneesGraffand1024.jpg',
        type: 'image',
        title: 'Perseids over the Pyrénées',
        exp: `This mountain and night skyscape stretches across the French Pyrenees National Park on August 12, near the peak of the annual Perseid meteor shower. The multi-exposure panoramic view was composed from the Col d'Aubisque, a mountain pass, about an hour before the bright gibbous moon rose. Centered is a misty valley and lights from the region's Gourette ski station toward the south. Taken over the following hour, frames capturing some of the night's long bright perseid meteors were aligned against the backdrop of stars and Milky Way.`,
        cpy: 'Jean-Francois\nGraffand'
    });
    const [apodSet, setApodSet] = useState(false);

    const [lns, setLns] = useState([]);
    const [lnsSet, setLnsSet] = useState(false);

    useEffect(() => {
        axios.get(API_PREFIX+'/user/api/idx-iden')
            .then(res => {
                setUser(res.data['result']);
            })
            .catch(err => {
                setWorkState(1);
            });

        axios.get(API_PREFIX+'/idx/apod')
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

        axios.get(API_PREFIX+'/ns/api/lns-idx')
            .then(res => {
                setLns(res.data['result']);
            })
            .catch(err => {
                console.error(err);
            }).finally(() => {
                setLnsSet(true);
            });
    }, []);

    if (workState === 1) {
        return <ErrorPage exp='사용자 정보를 받아오지 못했어요.'/>
    }

    return (
        <>
            {picture.type === 'image' && apodSet &&
                <div className='w-100 pict' style={{backgroundImage: ('url(' + picture.url + ')')}}></div>
            }
            {!apodSet &&
                <div className='w-100 pict'/>
            }
            <Container className={'index'}>
                {apodSet &&
                    <div className={'text'}>
                        <h1 className={'display-3 text-center'}>{picture.title}</h1>
                        <p className={'fw-light mb-5'}>{picture.exp}</p>
                    </div>
                }
                {!apodSet &&
                    <div className={'text'}>
                        <h1 className={'display-3 text-center'}></h1>
                    </div>
                }
                <div className={'d-flex justify-content-center info'}>
                    {lnsSet &&
                        <>
                            <LnsStatusFrame cnt={lns[0]} nth={8}/>
                            <LnsStatusFrame cnt={lns[1]} nth={1}/>
                            <LnsStatusFrame cnt={lns[2]} nth={2}/>
                        </>
                    }
                    <div className={'border border-0 px-2 py-2 rounded-4 d-flex flex-column justify-content-center gap-0 profile-href'}>
                        <Link className={'px-5 py-3 text-center'} to={'/ns'}>면불</Link>
                        <hr/>
                        { user.priv > 1 &&
                            <>
                                <Link className={'px-4 py-3 text-center'} to={'/manage'}>관리</Link>
                                <hr/>
                            </>
                        }
                        <Link className={'px-4 py-3 text-center'} to={'/auth/signout'}>로그아웃</Link>
                    </div>
                </div>
            </Container>
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
