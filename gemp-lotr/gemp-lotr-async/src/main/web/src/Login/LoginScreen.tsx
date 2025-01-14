import { DivButton } from "./Button"

export interface LoginScreenProps {
  login: string,
  setLogin: (value: string) => void,
  password: string,
  setPassword: (value: string) => void,
  onRegister: () => void,
  onLogin: () => void,
}

function LoginScreen(props: LoginScreenProps) {
  return (
    <>
      Login below, or <DivButton onClick={props.onRegister} text="Register" />
      <br/>
      Login: <input type='text' value={props.login} onChange={e => props.setLogin(e.target.value)}/>
      <br/>
      Password: <input type='password' value={props.password} onChange={e => props.setPassword(e.target.value)}/>
      <br/>
      <DivButton onClick={props.onLogin} text="Login" />
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
