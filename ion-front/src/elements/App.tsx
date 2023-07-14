import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Index from './pages/main';
import ErrorPage from './pages/etc/error';
import Ns from './pages/ns/ns';

import '../css/univ.scss';
import { SignoutPage } from './pages/auth/login';
import SignupPage from './pages/auth/signup';
import Docs from './pages/docs/docs';
import Header from './pages/fragments/header';
import ManagementPage from './pages/admin/admin';

function App() {
    return (
        <Router>
            <div className='d-flex h-100 sct'>
                <Header/>
                <div className='px-4 py-3 w-100 h-100 mct'>
                    <Routes>
                        <Route index path='/' element={<Index/>}/>

                        <Route path='/auth/signup' element={<SignupPage/>}/>
                        <Route path='/auth/signout' element={<SignoutPage/>}/>

                        <Route path='/docs/*' element={<Docs/>}/>

                        <Route path='/ns' element={<Ns/>}/>

                        <Route path='/manage' element={<ManagementPage/>}/>

                        <Route path='*' element={<ErrorPage errorTitle='찾으시는 페이지가 없어요.' exp='입력하신 주소가 정확한지 다시 한 번 확인해주세요.'/>}/>
                    </Routes>
                </div>
            </div>
        </Router>
    );
}

export default App;
