import React from 'react'
import PropTypes from 'prop-types'
import { Icon } from 'antd'
import { Link } from 'react-router-dom';

const SidebarLink = ({label, to, icon, id}) => (
    <Link to={to}>
        {icon ? (<Icon type={icon} />) : null}
        <span id={id} className="nav-text">{label}</span>
    </Link>
    // <a onClick={() => history.push(to)} href={to}>
    //     {icon ? (<Icon type={icon} />) : null}
    //     <span id={id} className="nav-text">{label}</span>
    // </a>
)

SidebarLink.propTypes = {
    label: PropTypes.string,
    to: PropTypes.string,
    icon: PropTypes.string
}

export default SidebarLink