import React from "react";
import { Link } from "react-router-dom";

function Credit(props) {
    return (
        <div className={'vstack credit '+props.className}>
            <p className='m-0 text-muted'>Ion by Changwoon Hyun</p>
            <p className='m-0 text-muted'>Seungwon Lee and Nakyung Lee</p>
            <p className='m-0 text-muted'>Look up <Link to='https://github.com/miho73/ion' target='_blank'>GitHub</Link> repository of project Ion</p>
        </div>
    )
}

export default Credit;