import { useState } from "react"
import "./Login.css"

function Login() {
  enum InteractionMode {
    Welcome,
    Registration,
    Login,
  }

  const [mode, setMode] = useState(InteractionMode.Welcome)

  return (
    <>
      <div className="status"></div>
      <div className="error"></div>
      <div className="interaction">
        {
          mode == InteractionMode.Welcome ?
            <WelcomeInteraction onRegister={() => setMode(InteractionMode.Registration)} /> :
          mode == InteractionMode.Login ?
            <LoginInteraction /> :
            <RegistrationInteraction />
        }
      </div>
    </>
  )
}

interface WelcomeInteractionProps {
  onRegister: () => void,
}

function WelcomeInteraction(props: WelcomeInteractionProps) {
  return (
    <>
      Login below, or <div
        className="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"
        role="button"
        onClick={props.onRegister}
      >
        <span className="ui-button-text">Register</span>
      </div>
    </>
  )
}

function LoginInteraction() {
  return (
    <>
    </>
  )
}

function RegistrationInteraction() {
  return (
    <>
      Login: <input id='login' type='text'/><br/>
      Password: <input id='password' type='password'/><br/>
      Password repeated: <input id='password2' type='password'/><br/>
      <button id='registerButton'>Register</button>
   </>
  )
}

export default Login
