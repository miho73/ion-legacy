import React from 'react';
import Dactivation from './archive/d_activation';
import Deula from './archive/d_eula';
import Dadmin from './archive/d_admin';
import Dcredit from './archive/d_credit';

const DOC_REGISTRY = {
    'activation': ['IonID 활성화', (<Dactivation/>)],
    'eula': ["이용약관", (<Deula/>)],
    'manage': ["Management 도구", (<Dadmin/>)],
    'credit': ["Ion Project", <Dcredit/>]
};

function Docs() {
    const curl = window.location.href;
    const ptr = curl.split('/');
    const docCode = ptr[ptr.length - 1]

    return (
        <>
            <h1>{DOC_REGISTRY[docCode][0]}</h1>
            <hr/>
            {DOC_REGISTRY[docCode][1]}
        </>
    )
}

export default Docs;