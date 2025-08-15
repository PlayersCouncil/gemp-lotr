import { useState } from "react"

export interface ButtonProps {
  text: string,
  onClick: () => void,
}


export function Button(props: ButtonProps) {
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

export function DivButton(props: ButtonProps) {
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
