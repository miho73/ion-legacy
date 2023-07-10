import React from "react";
import { Link } from "react-router-dom";

function Header() {
    return (
        <>
            <nav className="d-flex flex-shrink-0 bg-light nav">
                <Link to="/">
                    <img className="w-100" src="logo.png" alt="home"/>
                </Link>
                <hr/>
                <ul className="nav nav-pills">
                    <li className="nav-item">
                        <Link to="/" className="nav-link active" aria-current="page">Home</Link>
                    </li>
                    <li>
                        <Link to="/ns" className="nav-link link-dark">면불신청</Link>
                    </li>
                </ul>
            </nav>
        </>
    );
}

export default Header;