import { useEffect, useState } from "react"
import "./Login.css"

function Login() {
  enum InteractionMode {
    Welcome,
    Registration,
    Login,
    Banned,
  }

  const [status, setStatus] = useState("")
  const [error, setError] = useState("")
  const [mode, setMode] = useState(InteractionMode.Welcome)
  const [comm, setComm] = useState(undefined as any)
  const [login, setLogin] = useState("")
  const [password, setPassword] = useState("")
  const [password2, setPassword2] = useState("")
  const [registerButton, setRegisterButton] = useState("Register")

  useEffect(() => {
    var comm = new GempLotrCommunication("/gemp-lotr-server", function () {
      alert("Unable to contact the server");
    })
    setComm(comm)

    comm.getStatus(
      function (html: any) {
          setStatus(html)
      })
  }, [])

  return (
    <>
      <div className="status">{status}</div>
      <div className="error">{error}</div>
      <div className="interaction">
        {
          mode == InteractionMode.Welcome ?
            <WelcomeInteraction
              login={login}
              setLogin={setLogin}
              password={password}
              setPassword={setPassword}
              onRegister={() => setMode(InteractionMode.Registration)}
              onLogin={() => {
                comm && comm.login(login, password, function (_: any, status: any) {
                  if(status == "202") {
                      setMode(InteractionMode.Registration)
                      setRegisterButton("Update Password")
                      setError("Your password has been reset.  Please enter a new password.")
                      setLogin(login)
                  }
                  else {
                      location.href = "/gemp-lotr/hall.html";
                  }
              },
              {
                  "0": function () {
                      alert("Unable to connect to server, either server is down or there is a problem" +
                          " with your internet connection");
                  },
                  "401": function () {
                      setError("Invalid username or password. Try again.")
                      setMode(InteractionMode.Login)
                  },
                  "403": function () {
                      setError("You have been permanently banned. If you think it was a mistake please appeal with dmaz or ketura on <a href='https://lotrtcgpc.net/discord>the PC Discord</a>.");
                      setMode(InteractionMode.Banned)
                  },
                  "409": function () {
                      setError("You have been temporarily banned. You can try logging in at a later time. If you think it was a mistake please appeal with dmaz or ketura on <a href='https://lotrtcgpc.net/discord>the PC Discord</a>.");
                      setMode(InteractionMode.Banned)
                  },
                  "503": function () {
                      setError("Server is down for maintenance. Please come at a later time.");
                  }
              });
              }}
            /> :
          mode == InteractionMode.Login ?
            <LoginInteraction /> :
            <RegistrationInteraction
              login={login}
              setLogin={setLogin}
              password={password}
              setPassword={setPassword}
              password2={password2}
              setPassword2={setPassword2}
              registerButton={registerButton}
              onRegister={() => {
                if (password != password2) {
                  setError("Password and Password repeated are different! Try again");
                } else {
                  comm.register(login, password, function (_: any, status: any) {
                    if(status == "202") {
                      setError("Your password has successfully been reset!  Please refresh the page and log in.");
                    }
                    else {
                      location.href = "/gemp-lotr/hall.html";
                    }
                  },
                  {
                    "0": function () {
                      alert("Unable to connect to server, either server is down or there is a problem" +
                        " with your internet connection");
                    },
                    "400": function () {
                      setError("Login is invalid. Login must be between 2-10 characters long, and contain only<br/>" +
                        " english letters, numbers or _ (underscore) and - (dash) characters.");
                    },
                    "409": function () {
                      setError("User with this login already exists in the system. Try a different one.");
                    },
                    "503": function () {
                      setError("Server is down for maintenance. Please come at a later time.");
                    }
                  });
                }
              }} />
        }
      </div>
    </>
  )
}

interface WelcomeInteractionProps {
  login: string,
  setLogin: (value: string) => void,
  password: string,
  setPassword: (value: string) => void,
  onRegister: () => void,
  onLogin: () => void,
}

function WelcomeInteraction(props: WelcomeInteractionProps) {
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

function LoginInteraction() {
  return (
    <>
    </>
  )
}

interface RegistrationInteractionProps {
  login: string,
  setLogin: (value: string) => void,
  password: string,
  setPassword: (value: string) => void,
  password2: string,
  setPassword2: (value: string) => void,
  registerButton: string,
  onRegister: () => void,
}

function RegistrationInteraction(props: RegistrationInteractionProps) {
  return (
    <>
      Login: <input type='text' value={props.login} onChange={e => props.setLogin(e.target.value)}/><br/>
      Password: <input type='password' value={props.password} onChange={e => props.setPassword(e.target.value)}/><br/>
      Password repeated: <input type='password' value={props.password2} onChange={e => props.setPassword2(e.target.value)}/><br/>
      <Button onClick={props.onRegister} text={props.registerButton} />
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
