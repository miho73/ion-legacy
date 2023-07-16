import React from "react";
import { Link } from "react-router-dom";

function Header() {
    return (
        <>
            <nav className="d-flex flex-shrink-0 bg-light nav">
                <Link to="/">
                    <img src="/logo.png" alt="home"/>
                </Link>
                <hr/>
                <ul className="nav nav-pills gap-2">
                    <li>
                        <Link to="/" className="nav-link">Home</Link>
                    </li>
                    <li>
                        <Link to="/ns" className="nav-link">면불신청</Link>
                    </li>
                </ul>
            </nav>
        </>
    );
}

export default Header;