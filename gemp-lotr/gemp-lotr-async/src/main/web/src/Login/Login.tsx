import { useEffect, useState } from "react"
import "./Login.css"

declare const window: any;

function Login() {
  enum InteractionMode {
    Welcome,
    Registration,
    Login,
  }

  const [mode, setMode] = useState(InteractionMode.Welcome)
  const [comm, setComm] = useState(undefined as any)
  useEffect(() => {
    var comm = new window.GempLotrCommunication("/gemp-lotr-server", function () {
      alert("Unable to contact the server");
    })
    setComm(comm)
  }, [])

  return (
    <>
      <div className="status"></div>
      <div className="error"></div>
      <div className="interaction">
        {
          mode == InteractionMode.Welcome ?
            <WelcomeInteraction
              onRegister={() => setMode(InteractionMode.Registration)}
              onLogin={() => {
                comm && comm.login("bondolin", "password", function (_: any, status: any) {
                  if(status == "202") {
                      setMode(InteractionMode.Registration)
                      // $("#registerButton").html("Update Password");
                      // $(".error").html("Your password has been reset.  Please enter a new password.");
                      // $("#login").val(login);
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
                      // $(".error").html("Invalid username or password. Try again.");
                      // loginScreen();
                  },
                  "403": function () {
                      // $(".error").html("You have been permanently banned. If you think it was a mistake please appeal with dmaz or ketura on <a href='https://lotrtcgpc.net/discord>the PC Discord</a>.");
                      // $(".interaction").html("");
                  },
                  "409": function () {
                      // $(".error").html("You have been temporarily banned. You can try logging in at a later time. If you think it was a mistake please appeal with dmaz or ketura on <a href='https://lotrtcgpc.net/discord>the PC Discord</a>.");
                      // $(".interaction").html("");
                  },
                  "503": function () {
                      // $(".error").html("Server is down for maintenance. Please come at a later time.");
                  }
              });
              }}
            /> :
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
  onLogin: () => void,
}

function WelcomeInteraction(props: WelcomeInteractionProps) {
  return (
    <>
      Login below, or <DivButton onClick={props.onRegister} text="Register" />
      <br/>
      Login: <input id='login' type='text'/>
      <br/>
      Password: <input id='password' type='password'/>
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
