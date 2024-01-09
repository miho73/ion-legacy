import axios from "axios";

const PREFIX = process.env.REACT_APP_API_PREFIX;


axios.defaults.withCredentials = true;

export const API_PREFIX = PREFIX;
