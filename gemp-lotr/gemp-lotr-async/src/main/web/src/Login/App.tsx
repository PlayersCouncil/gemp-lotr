import Info from "./Info";
import Login from "./Login.tsx";
import "./App.css"

function App() {
  return (
    <>
      <div className="dim"></div>
      <div className="centerContainer">
        <h1 className="banner">GEMP: Play the Lord of the Rings TCG</h1>
        <div className="content">
          <div className="login">
            <Login />
          </div>

          <div className="info">
            <Info/>
          </div>

        </div>
      </div>
    </>
  )
}

export default App
