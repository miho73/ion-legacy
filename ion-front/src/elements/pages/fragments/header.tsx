import React from "react";
import { Link } from "react-router-dom";

function Header() {
    return (
        <>
            <nav className="d-flex flex-shrink-0 bg-light nav">
                <Link to="/">
                    <img className="w-100" src="https://images.squarespace-cdn.com/content/v1/51cdafc4e4b09eb676a64e68/77771c78-82f6-43bb-bc3d-bfbf7c410b5a/logo.jpg?format=1500w" alt="home"/>
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