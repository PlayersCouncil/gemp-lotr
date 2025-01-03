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
      Login below, or <Button onClick={props.onRegister} text="Register" />
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

interface ButtonProps {
  text: string,
  onClick: () => void,
}

function Button(props: ButtonProps) {
  const [hovered, setHovered] = useState(false)
  const toggleHovered = () => setHovered(!hovered)
  return (
    <div
      className={"ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" + (hovered ? " ui-state-hover" : "")}
      role="button"
      onClick={props.onClick}
      onMouseEnter={toggleHovered}
      onMouseLeave={toggleHovered}
    >
      <span className="ui-button-text">{props.text}</span>
    </div>
  )
}

export default Login
