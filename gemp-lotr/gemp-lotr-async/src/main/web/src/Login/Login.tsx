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
  const [hover, setHover] = useState(false)
  const toggleHover = () => setHover(!hover)

  const [active, setActive] = useState(false)
  const toggleActive = () => setActive(!active)

  const baseClassName = "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"
  const hoverClassName = hover ? " ui-state-hover" : ""
  const activeClassName = active ? " ui-state-active" : ""

  return (
    <div
      className={baseClassName + hoverClassName + activeClassName}
      role="button"
      onClick={props.onClick}
      onMouseEnter={toggleHover}
      onMouseLeave={toggleHover}
      onMouseDown={toggleActive}
      onMouseUp={toggleActive}
    >
      <span className="ui-button-text">{props.text}</span>
    </div>
  )
}

export default Login
