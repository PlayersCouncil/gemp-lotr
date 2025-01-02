import './App.css'

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
                    <p><a href="https://wiki.lotrtcgpc.net/wiki/The_Lord_of_the_Rings_TCG">The Lord of the Rings TCG</a> was published by Decipher, Inc from 2001-2007.<br />GEMP is a platform for playing this excellent game for free in your browser.<br />Maintained by the unofficial <a href="https://lotrtcgpc.net/">Player's Council</a>.</p>
                    
                    <div className="sw-shoutout">
                        <img src="https://res.starwarsccg.org/gemp/lightForce-42.png" height="20"/>
                        <p>(Also check out the <a href="https://gemp.starwarsccg.org/gemp-swccg/">Star Wars CCG GEMP</a> ran by the <a href="https://www.starwarsccg.org/">Star Wars Players Committee</a>.)</p>
                        <img src="https://res.starwarsccg.org/gemp/darkForce-42.png" height="20"/>
                    </div>
                </div>
            </div>
        </div>
    </>
  )
}

export default App
