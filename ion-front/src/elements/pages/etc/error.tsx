import React from 'react';

function ErrorPage(props) {
    return (
        <>
            <h2>{props.errorTitle}</h2>
            <p>{props.explain}</p>
        </>
    )
}

export default ErrorPage;