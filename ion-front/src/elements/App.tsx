import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Header from './pages/fragments/header';
import Footer from './pages/fragments/footer';

import Index from './pages/main';
import ErrorPage from './pages/etc/error';
import Ns from './pages/absent_study/absent_study';

import '../css/univ.scss';
import 'bootstrap/dist/css/bootstrap.min.css';
import { LoginPage, SignoutPage } from './pages/auth/login';
import SignupPage from './pages/auth/signup';
import Docs from './pages/docs/docs';
import { getIden } from './service/auth/getIden';


function App() {
    const [iden, setIden] = useState(getIden());

    function updateIden() {
        setIden(getIden);
    }

    return (
        <>
            <Router>
                <Header iden={iden}/>
                <main className='container mt-4'>
                    <Routes>
                        <Route index path='/' element={<Index/>}/>

                        <Route path='/auth/login' element={<LoginPage iden={iden} uIden={updateIden}/>}/>
                        <Route path='/auth/signup' element={<SignupPage/>}/>
                        <Route path='/auth/signout' element={<SignoutPage uIden={updateIden}/>}/>

                        <Route path='/docs/*' element={<Docs/>}/>

                        <Route path='/ns' element={<Ns iden={iden}/>}/>
                        <Route path='*' element={<ErrorPage errorTitle='찾으시는 페이지가 없어요.' explain='입력하신 주소가 정확한지 다시 한 번 확인해주세요.'/>}/>
                    </Routes>
                </main>
            </Router>
        </>
    );
}

export default App;
