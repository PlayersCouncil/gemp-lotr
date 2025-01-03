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
            <RegistrationInteraction onRegister={() => {}} />
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
      Login below, or <DivButton onClick={props.onRegister} text="Register" />
    </>
  )
}

function LoginInteraction() {
  return (
    <>
    </>
  )
}

interface RegistrationInteractionProps {
  onRegister: () => void,
}

function RegistrationInteraction(props: RegistrationInteractionProps) {
  return (
    <>
      Login: <input id='login' type='text'/><br/>
      Password: <input id='password' type='password'/><br/>
      Password repeated: <input id='password2' type='password'/><br/>
      <Button onClick={props.onRegister} text="Register" />
   </>
  )
}

interface ButtonProps {
  text: string,
  onClick: () => void,
}


function Button(props: ButtonProps) {
  const [mouseEntered, setMouseEntered] = useState(false)
  const onMouseBoundary = (entered: boolean, buttons: number) => {
    setMouseEntered(entered)
    setMouseDown(buttons != 0)
  }

  const [mouseDown, setMouseDown] = useState(false)
  const onMouseDown = () => setMouseDown(true)
  const onMouseUp = () => setMouseDown(false)

  const baseClassName = "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"
  const hoverClassName = mouseEntered ? " ui-state-hover" : ""
  const activeClassName = mouseEntered && mouseDown ? " ui-state-active" : ""

  return (
    <button
      className={baseClassName + hoverClassName + activeClassName}
      role="button"
      onClick={props.onClick}
      onMouseEnter={e => onMouseBoundary(true, e.buttons)}
      onMouseLeave={e => onMouseBoundary(false, e.buttons)}
      onMouseDown={onMouseDown}
      onMouseUp={onMouseUp}
    >
      <span className="ui-button-text">{props.text}</span>
    </button>
  )
}

function DivButton(props: ButtonProps) {
  const [mouseEntered, setMouseEntered] = useState(false)
  const onMouseBoundary = (entered: boolean, buttons: number) => {
    setMouseEntered(entered)
    setMouseDown(buttons != 0)
  }

  const [mouseDown, setMouseDown] = useState(false)
  const onMouseDown = () => setMouseDown(true)
  const onMouseUp = () => setMouseDown(false)

  const baseClassName = "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"
  const hoverClassName = mouseEntered ? " ui-state-hover" : ""
  const activeClassName = mouseEntered && mouseDown ? " ui-state-active" : ""

  return (
    <div
      className={baseClassName + hoverClassName + activeClassName}
      role="button"
      onClick={props.onClick}
      onMouseEnter={e => onMouseBoundary(true, e.buttons)}
      onMouseLeave={e => onMouseBoundary(false, e.buttons)}
      onMouseDown={onMouseDown}
      onMouseUp={onMouseUp}
    >
      <span className="ui-button-text">{props.text}</span>
    </div>
  )
}

export default Login
