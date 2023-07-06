import React from 'react';
import { Link } from 'react-router-dom';

function Header(props) {
    const iden = props.iden;

    return (
        <>
            <header className='d-flex flex-wrap align-items-center justify-content-center justify-content-md-between p-4 border-bottom'>
                <a href='/' className='d-flex align-items-center col-md-3 mb-2 mb-md-0 text-dark text-decoration-none'>
                    <h1 className='fw-bolder'>Ion</h1>
                </a>

                <ul className='nav col-12 col-md-auto mb-2 justify-content-center mb-md-0'>
                    <li><Link to='/' className='nav-link px-2 link-secondary'>Ion</Link></li>
                    <li><Link to='/ns' className='nav-link px-2 link-dark'>면불신청</Link></li>
                </ul>

                <ul className='nav col-12 col-md-auto mb-2 justify-content-center mb-md-0'>
                    {iden === null &&
                        <>
                            <li><Link to='/auth/login' className='nav-link px-2 link-secondary'>Login</Link></li>
                            <li><Link to='/auth/signup' className='nav-link px-2 link-secondary'>Sign-up</Link></li>
                        </>
                    }
                    {iden !== null &&
                        <>
                            <li><Link to='/user/profile' className='nav-link px-2 link-dark'>{iden.name}</Link></li>
                            <li><Link to='/auth/signout' className='nav-link px-2 link-dark'>로그아웃</Link></li>
                        </>
                    }
                </ul>
            </header>
        </>
    )
}

export default Header;