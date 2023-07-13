import React, { useEffect, useState, useRef } from 'react';
import { isLogin } from '../../service/auth';
import CannotAuthorize from '../auth/cannotAuth';
import { LoginPage } from '../auth/login';
import { Link } from 'react-router-dom';
import ErrorPage from '../etc/error';
import axios from 'axios';

function Graphic() {
    const canvasRef = useRef<HTMLCanvasElement>(null);

    const [canvas, setCanvas] = useState<HTMLCanvasElement>();
    const [ctx, setCtx] = useState<CanvasRenderingContext2D>();

    useEffect(() => {
        const canvas = canvasRef.current;

        const context = canvas?.getContext('2d');
        if(context === null || canvas === null) {
            console.error("ctx error");
            return;
        }

        setCanvas(canvas);
        setCtx(context);
    }, []);

    function render() {
        const P = 0.8;

        let pStack = [[0, 200]], rep = 60, cnt = 0;

        if(canvas !== undefined && ctx !== undefined) {
            canvas.width = 2000;
            canvas.height = 400;

            ctx.strokeStyle = 'black';

            ctx?.beginPath();
            ctx?.moveTo(0, 200);
            ctx?.stroke();
            ctx.lineWidth = 1;
            var reset = setInterval(() => {
                let cLst:any[] = [];
                ctx.beginPath();
                pStack.forEach(e => {
                    let angle1 = Math.random() - 0.5, angle2 = Math.random() - 0.5, len = Math.random() * 50;
                    let n1x = len * Math.cos(angle1) + e[0], n1y = len * Math.sin(angle1) + e[1];
                    let n2x = len * Math.cos(angle2) + e[0], n2y = len * Math.sin(angle2) + e[1];
                    ctx.moveTo(e[0], e[1]);
                    ctx?.lineTo(n1x, n1y);
                    ctx.moveTo(e[0], e[1]);
                    ctx?.lineTo(n2x, n2y);
                    cLst.push([n1x, n1y]);
                    cLst.push([n2x, n2y]);
                });
                pStack = [];
                if(cLst.length > 16) {
                    const shuffled = cLst.sort(() => 0.5 - Math.random());
                    cLst = shuffled.slice(0, 16);
                }
                cLst.forEach(e => {
                    if(Math.random() < P) pStack.push(e);
                });
                cnt ++;
                ctx.stroke();
                if(rep < cnt) {
                    clearInterval(reset);
                    render();
                }
            }, 150);
        }
    }

    useEffect(() => {
        render();
    }, [ ctx ]);

    return (
        <canvas style={{width: '100%', height: '400px'}} ref={canvasRef}></canvas>
    );
}

function LoggedInIndex() {
    const [user, setUser] = useState({name: '예지', id: 'yeji', priv: 3});
    const [workState, setWorkState] = useState(-1);
  
    useEffect(() => {
        axios.get('/user/api/idx-iden')
        .then(res => {
            setUser(res.data['result']);
        })
        .catch(err => {
            setWorkState(1);
        });
    }, []);

    if(workState === 1) {
        return <ErrorPage exp='사용자 정보를 받아오지 못했어요.'/>
    }

    return (
        <>
        <main className='mt-4 mx-4'>
            <div className='d-flex justify-content-between align-items-center'>
                <h1 className='mb-0'>Hi {user.name}!</h1>
                <ul className="nav col-md-auto justify-content-center mb-md-0">
                    <li><Link to="/" className="nav-link px-2 link-dark">Home</Link></li>
                    <li><Link to="/ns" className="nav-link px-2 link-dark">면불 신청</Link></li>
                    <li><Link to="/auth/signout" className="nav-link px-2 link-dark">로그아웃</Link></li>
                </ul>
            </div>
            <hr/>
            <div className='vstack'>
                <Graphic/>
                <div className='hstack gap-3'>
                    <Link to='/ns' className='text-decoration-none text-black p-3 border shadow-sm rounded home-lnk text-center d-flex justify-content-center align-items-center'>
                        <span className='fw-bolder'>면불 신청</span>
                    </Link>
                    {user.priv > 1 &&
                        <Link to='/manage' className='text-decoration-none text-black p-3 border shadow-sm rounded home-lnk text-center d-flex justify-content-center align-items-center'>
                            <span className='fw-bolder'>관리 페이지</span>
                        </Link>
                    }
                </div>
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