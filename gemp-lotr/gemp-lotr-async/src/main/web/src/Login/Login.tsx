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

  const [status, setStatus] = useState("")
  const [error, setError] = useState("")
  const [mode, setMode] = useState(InteractionMode.Login)
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
          mode == InteractionMode.Register ?
            <RegistrationScreen
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
              }} /> :
          mode == InteractionMode.Login ?
            <LoginScreen
              login={login}
              setLogin={setLogin}
              password={password}
              setPassword={setPassword}
              onRegister={() => setMode(InteractionMode.Register)}
              onLogin={() => {
                comm && comm.login(login, password, function (_: any, status: any) {
                  if(status == "202") {
                      setMode(InteractionMode.Register)
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
            <></>
        }
      </div>
    </>
  )
}

export default Login
