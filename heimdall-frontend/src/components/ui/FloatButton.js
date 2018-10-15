import React from 'react'
import PropTypes from 'prop-types'
import { Button, Tooltip } from 'antd'
import { Link } from 'react-router-dom'

const buttonStyle = {
    position: 'fixed',
    bottom: '30px',
    right: '30px',
    zIndex: 9
}

const FloatButton = ({label, to, idButton}) => (
    <Tooltip placement="left" title={label}>
        <Link to={to}><Button id={idButton} style={buttonStyle} className="floatButton" type="primary" icon="plus" size="large" shape="circle" /></Link>
    </Tooltip>
)

FloatButton.propTypes = {
    label: PropTypes.string,
    to: PropTypes.string,
    idButton: PropTypes.string.isRequired
}

export default FloatButton