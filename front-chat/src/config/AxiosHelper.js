import axios from "axios";

export const baseURL = "";

export const httpClient = axios.create({
  baseURL: baseURL,
});