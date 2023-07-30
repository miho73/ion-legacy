import React, {useState} from 'react'
import {Link} from 'react-router-dom'
import CaptchaNotice from '../../fragments/captchaNotice'
import Credit from '../../fragments/credit'
import {inRange} from '../../../service/checker';
import {changeBit, getBit} from '../../../service/bitmask';
import {ready} from '../../../service/recaptcha';
import axios from 'axios';

function LoginSection(props) {
    const [id, setId] = useState('');
    const [pwd, setPwd] = useState('');
    const [block, setBlock] = useState(false);
    const [formState, setFormState] = useState(0);
    const [loginError, setLoginError] = useState(0);

    function submit() {
        let state = 0;
        if(!inRange(1, 30, id.length)) state = changeBit(state, 0);
        if(!inRange(1, 30, pwd.length)) state = changeBit(state, 1);
        setFormState(state);

        if(state !== 0) return;

        setBlock(true);
        ready('login', token => {
            axios.post('/auth/api/authenticate', {
                id: id,
                pwd: pwd,
                ctoken: token
            }).then(res => {
                let re = res.data['result'];
                if(re === 0) {
                    window.location.reload();
                }
                else if(re === 7) {
                    props.setChangeFlag(true);
                }
                else setLoginError(re);
            }).catch(err => {
                setLoginError(-1);
            }).finally(() => {
                setBlock(false);
            });
        });
    }

    function enterDown(e) {
        if(e.key === 'Enter') {
            e.preventDefault();
            submit();
        }
    }

    return (
        <form className='vstack gap-3 d-flex justify-content-center align-items-center text-center form-signin'>
            <h1 className='h3 my-3 fw-normal'>IonID</h1>
            <div className='form-floating'>
                <input type='text' className={'pe-5 form-control fs-6 form-control-lg'+(getBit(formState, 0) ? ' is-invalid' : '')} disabled={block} id='ionid' placeholder='IonID' autoComplete='username' aria-label='IonID' value={id} onChange={e => setId(e.target.value)} onKeyDown={enterDown}/>
                <label htmlFor='ionid'>IonID</label>
            </div>
            <div className='form-floating'>
                <input type='password' className={'pe-5 form-control fs-6 form-control-lg'+(getBit(formState, 1) ? ' is-invalid' : '')} disabled={block} id='pwd' placeholder='암호' autoComplete='current-password' aria-label='암호' value={pwd} onChange={e => setPwd(e.target.value)} onKeyDown={enterDown}/>
                <label htmlFor='pwd'>Password</label>
            </div>
            <button className='btn btn-lg btn-primary fs-6' type='button' onClick={submit} disabled={block}>Sign in</button>
            {loginError !== 0 &&
                <div className='alert alert-danger mt-2'>
                    {loginError === -1 &&
                        <p className='mb-0'>로그인하지 못했습니다.</p>
                    }
                    {(loginError === 1 || loginError === 4) &&
                        <p className='mb-0'>IonID 또는 암호가 잘못되었습니다.</p>
                    }
                    {loginError === 2 &&
                        <p className='mb-0'>IonID가 비활성화 상태입니다.</p>
                    }
                    {loginError === 3 &&
                        <p className='mb-0'>이 IonID로 로그인 할 수 없습니다.</p>
                    }
                    {loginError === 6 &&
                        <p className='mb-0'>reCAPTCHA를 검증하지 못했습니다.</p>
                    }
                    {loginError === 7 &&
                        <p className='mb-0'>사용자 보호를 위해 지금은 로그인할 수 없습니다.</p>
                    }
                </div>
            }
            <Link to={'/auth/signup'} className='text-muted text-decoration-none'>IonID 만들기</Link>
            <CaptchaNotice/>
            <Credit/>
        </form>
    )
}

export default LoginSection;