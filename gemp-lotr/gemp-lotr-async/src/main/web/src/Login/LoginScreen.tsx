import { useState } from "react"
import { DivButton } from "./Button"

export interface LoginScreenProps {
  login: string,
  setLogin: (value: string) => void,
  onRegister: () => void,
  onLogin: (login: string, password: string) => void,
}

function LoginScreen(props: LoginScreenProps) {
  const [password, setPassword] = useState("")

  return (
    <>
      Login below, or <DivButton onClick={props.onRegister} text="Register" />
      <br/>
      Login: <input type='text' value={props.login} onChange={e => props.setLogin(e.target.value)}/>
      <br/>
      Password: <input
        type='password'
        value={password}
        onChange={e => setPassword(e.target.value)}
        onKeyDown={e => {
          if (e.key == "Enter") {
            props.onLogin(props.login, password)
          }
        }}/>
      <br/>
      <DivButton onClick={() => props.onLogin(props.login, password)} text="Login" />
      <br/>
      <div style={{
        textAlign: "center",
        overflowWrap: "break-word",
        display: "inline-block",
        maxWidth: "300px",
      }}>
        <a href='https://lotrtcgpc.net/discord'>
          Forgot your password?  Contact <span style={{color:"orange"}}>ketura</span> on the PC Discord.
        </a>
      </div>
    </>
  )
}

export default LoginScreen
