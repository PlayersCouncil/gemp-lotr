import { Button, FormControl, FormLabel, Input, Typography } from '@mui/joy';
import useAxios from 'axios-hooks';
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { LoginRequestInterface } from '../../types/login-request-body.interface';
import { getLoginConfiguration } from '../../types/register-api.functions';
import {
  defaultRegisrationErrorState,
  isValidAccount,
  RegistrationErrorState,
} from './types/client-validation';
import { getRegistrationErrorMessages } from './types/response-handlers';
import styles from './index.module.css';

function Registration() {
  const navigate = useNavigate(); // yes this isn't a best practice, should probably have an auth context

  let serverErrorMessage = '';
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirmation, setPasswordConfirmation] = useState('');
  const [errors, setErrors] = useState<RegistrationErrorState>(
    defaultRegisrationErrorState()
  );

  useEffect(() => {
    const validationErrors = isValidAccount(
      login,
      password,
      passwordConfirmation
    );

    setErrors(validationErrors);
  }, [login, password, passwordConfirmation]);

  const [
    { data: registrationResponse, error: registrationError },
    executeRegister,
  ] = useAxios<string, LoginRequestInterface, Error>(
    '/gemp-lotr-server/register',
    { manual: true }
  );

  if (registrationResponse === 'OK') {
    navigate('/hall');
  }

  if (registrationError?.response) {
    serverErrorMessage = getRegistrationErrorMessages(
      registrationError.response.status
    );
  }

  const disableCreateButton = errors.global !== '';

  const registerAttempt = (): void => {
    executeRegister(getLoginConfiguration(login, password));
  };

  const onLoginUpdate = (e: React.ChangeEvent<HTMLInputElement>) =>
    setLogin(e.target.value);
  const onPasswordUpdate = (e: React.ChangeEvent<HTMLInputElement>) =>
    setPassword(e.target.value);
  const onPasswordConfirmationUpdate = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => setPasswordConfirmation(e.target.value);

  return (
    <>
      <FormControl>
        <FormLabel>Username</FormLabel>
        <Input value={login} onChange={onLoginUpdate} />
        <Typography color="danger">{errors.login}</Typography>
      </FormControl>
      <FormControl>
        <FormLabel>Password</FormLabel>
        <Input type="password" value={password} onChange={onPasswordUpdate} />
        <Typography color="danger">{errors.password}</Typography>
      </FormControl>
      <FormControl>
        <FormLabel>Confirm Password</FormLabel>
        <Input
          type="password"
          value={passwordConfirmation}
          onChange={onPasswordConfirmationUpdate}
        />
      </FormControl>
      <Typography sx={{ mt: 1 }} color="danger">
        {serverErrorMessage}
      </Typography>
      <span className={styles.actionRow}>
        <Button variant="soft">
          <Link to="/">Return to Login</Link>
        </Button>
        <Button
          className={styles.submit}
          type="submit"
          disabled={disableCreateButton}
          onClick={registerAttempt}
        >
          Create Account
        </Button>
      </span>
    </>
  );
}

export default Registration;
