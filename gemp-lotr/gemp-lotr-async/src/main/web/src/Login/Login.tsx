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
            <WelcomeInteraction /> :
          mode == InteractionMode.Login ?
            <LoginInteraction /> :
            <RegistrationInteraction />
        }
      </div>
    </>
  )
}

function WelcomeInteraction() {
  return (
    <>
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
    </>
  )
}

export default Login
