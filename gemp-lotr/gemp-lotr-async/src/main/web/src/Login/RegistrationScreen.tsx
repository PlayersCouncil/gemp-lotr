import { Button } from "./Button"

export interface RegistrationScreenProps {
  login: string,
  setLogin: (value: string) => void,
  password: string,
  setPassword: (value: string) => void,
  password2: string,
  setPassword2: (value: string) => void,
  registerButton: string,
  onRegister: () => void,
}

function RegistrationScreen(props: RegistrationScreenProps) {
  return (
    <>
      Login: <input type='text' value={props.login} onChange={e => props.setLogin(e.target.value)}/><br/>
      Password: <input type='password' value={props.password} onChange={e => props.setPassword(e.target.value)}/><br/>
      Password repeated: <input type='password' value={props.password2} onChange={e => props.setPassword2(e.target.value)}/><br/>
      <Button onClick={props.onRegister} text={props.registerButton} />
   </>
  )
}

export default RegistrationScreen
