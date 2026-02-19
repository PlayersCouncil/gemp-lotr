import { useEffect, useState } from "react"
import RegistrationScreen from "./RegistrationScreen"
import LoginScreen from "./LoginScreen"
import "./Login.css"

function Login() {
  enum InteractionMode {
    Register,
    Login,
    Banned,
  }

  enum LoginError {
    None,
    MismatchedPasswords,
    PasswordReset,
    SuccessfulReset,
    InvalidLogin,
    InvalidAuthentication,
    PermanentBan,
    TemporaryBan,
    ExistingUser,
    ServerDown,
  }

  const [status, setStatus] = useState("")
  const [error, setError] = useState(LoginError.None)
  const [mode, setMode] = useState(InteractionMode.Login)
  const [comm, setComm] = useState(undefined as any)
  const [login, setLogin] = useState("")
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
      <div className="status" dangerouslySetInnerHTML={{ __html: status}}></div>
      <div className="error">{
        error == LoginError.MismatchedPasswords ?
          "Password and Password repeated are different! Try again" :
        error == LoginError.PasswordReset ?
          "Your password has been reset.  Please enter a new password." :
        error == LoginError.SuccessfulReset ?
          "Your password has successfully been reset!  Please refresh the page and log in." :
        error == LoginError.InvalidLogin ?
          <>
            Login is invalid. Login must be between 2-10 characters long, and contain only<br/>
            english letters, numbers or _ (underscore) and - (dash) characters.
          </> :
        error == LoginError.InvalidAuthentication ?
          "Invalid username or password. Try again." :
        error == LoginError.PermanentBan ?
          <>
            You have been permanently banned. If you think it was a mistake please appeal with dmaz or ketura on <a href='https://lotrtcgpc.net/discord'>the PC Discord</a>.
          </> :
        error == LoginError.TemporaryBan ?
          <>
            You have been temporarily banned. You can try logging in at a later time. If you think it was a mistake please appeal with dmaz or ketura on <a href='https://lotrtcgpc.net/discord'>the PC Discord</a>.
          </> :
        error == LoginError.ExistingUser ?
          "User with this login already exists in the system. Try a different one." :
        error == LoginError.ServerDown ?
          "Server is down for maintenance. Please come at a later time." :
          ""
      }</div>
      <div className="interaction">
        {
          mode == InteractionMode.Register ?
            <RegistrationScreen
              login={login}
              setLogin={setLogin}
              registerButton={registerButton}
              onRegister={(login, password, password2) => {
                if (password != password2) {
                  setError(LoginError.MismatchedPasswords);
                } else {
                  comm.register(login, password, function (_: any, status: any) {
                    if(status == "202") {
                      setError(LoginError.SuccessfulReset);
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
                      setError(LoginError.InvalidLogin);
                    },
                    "409": function () {
                      setError(LoginError.ExistingUser);
                    },
                    "503": function () {
                      setError(LoginError.ServerDown);
                    }
                  });
                }
              }} /> :
          mode == InteractionMode.Login ?
            <LoginScreen
              login={login}
              setLogin={setLogin}
              onRegister={() => {
                setError(LoginError.None)
                setMode(InteractionMode.Register)
              }}
              onLogin={(login, password) => {
                comm && comm.login(login, password, function (_: any, status: any) {
                  if(status == "202") {
                      setMode(InteractionMode.Register)
                      setRegisterButton("Update Password")
                      setError(LoginError.PasswordReset)
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
                      setError(LoginError.InvalidAuthentication)
                      setMode(InteractionMode.Login)
                  },
                  "403": function () {
                      setError(LoginError.PermanentBan);
                      setMode(InteractionMode.Banned)
                  },
                  "409": function () {
                      setError(LoginError.TemporaryBan);
                      setMode(InteractionMode.Banned)
                  },
                  "503": function () {
                      setError(LoginError.ServerDown);
                  }
                });
              }}
            /> :
            <></>
        }
      </div>
    </>
  )
}

export default Login
