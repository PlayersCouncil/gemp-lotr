import { useState } from "react"
import { Button } from "./Button"

export interface RegistrationScreenProps {
  login: string,
  setLogin: (value: string) => void,
  registerButton: string,
  onRegister: (login: string, password: string, password2: string) => void,
}

function RegistrationScreen(props: RegistrationScreenProps) {
  const [password, setPassword] = useState("")
  const [password2, setPassword2] = useState("")

  return (
    <>
      Login: <input type='text' value={props.login} onChange={e => props.setLogin(e.target.value)}/><br/>
      Password: <input type='password' value={password} onChange={e => setPassword(e.target.value)}/><br/>
      Password repeated: <input type='password' value={password2} onChange={e => setPassword2(e.target.value)}/><br/>
      <Button onClick={() => props.onRegister(props.login, password, password2)} text={props.registerButton} />
   </>
  )
}

export default RegistrationScreen
