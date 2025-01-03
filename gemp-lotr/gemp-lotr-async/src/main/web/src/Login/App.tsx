import Info from "./Info";
import "./App.css"

function App() {
  return (
    <>
      <div className="dim"></div>
      <div className="centerContainer">
        <h1 className="banner">GEMP: Play the Lord of the Rings TCG</h1>
        <div className="content">
          <div className="login">
            <div className="status"></div>
            <div className="error"></div>
            <div className="interaction"></div>
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
