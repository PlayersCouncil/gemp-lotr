import { useEffect, useState } from 'react';
import { Button, FormControl, FormLabel, Input, Typography } from '@mui/joy';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import useAxios from 'axios-hooks';
import styles from './index.module.css';
import { LoginRequestInterface } from '../../types/login-request-body.interface';
import { getLoginConfiguration } from '../../types/register-api.functions';
import { getLoginErrorMessage, loginValidation } from './types';

// axios.defaults.baseURL = `http://${import.meta.env.VITE_API}:17001/gemp-lotr-server`;
// axios.defaults.baseURL = `http://${import.meta.env.VITE_API}:17001/`;
// axios.defaults.baseURL = `http://0.0.0.0:17001/gemp-lotr-server`;
// axios.defaults.baseURL = `http://${import.meta.env.VITE_API}:17001/gemp-lotr-server`;
// axios.defaults.baseURL = `http://localhost:17001/gemp-lotr-server`;
// axios.defaults.headers['Origin'] = `http://localhost:17001/`;


function Login() {
  let errorMessage = '';
  const navigate = useNavigate();
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [
    { response: loginResponse, loading: isLoginLoading, error: loginError },
    executeLogin,
  ] = useAxios<string, LoginRequestInterface, Error>(
    `/gemp-lotr-server/login`,
    { manual: true }
  );

  useEffect(() => {
    if (loginResponse) {
      navigate('/hall');
    }
  }, [isLoginLoading, loginResponse, navigate]);

  if (loginError?.response) {
    errorMessage = getLoginErrorMessage(loginError.response.status);
  }

  const loginAttempt = (): void => {
    executeLogin(getLoginConfiguration(login, password));
  };

  const onLoginUpdate = (e: React.ChangeEvent<HTMLInputElement>) =>
    setLogin(e.target.value);
  const onPasswordUpdate = (e: React.ChangeEvent<HTMLInputElement>) =>
    setPassword(e.target.value);

  const isLoginDisabled = !loginValidation(login, password).result;

  return (
    <>
    <span>FOOBAR3</span>
      <FormControl>
        <FormLabel>Username</FormLabel>
        <Input value={login} onChange={onLoginUpdate} />
      </FormControl>
      <FormControl>
        <FormLabel>Password</FormLabel>
        <Input type="password" value={password} onChange={onPasswordUpdate} />
      </FormControl>
      <Typography sx={{ mt: 1 }} color="danger">
        {errorMessage}
      </Typography>
      <span className={styles.actionRow}>
        <Button variant="soft">
          <Link to="/register">Create new Account</Link>
        </Button>
        <Button
          disabled={isLoginDisabled || isLoginLoading}
          onClick={loginAttempt}
        >
          Login
        </Button>
      </span>
    </>
  );
}

export default Login;
