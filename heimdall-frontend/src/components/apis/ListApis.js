import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import {
    Row,
    Col,
    Card,
    Tooltip,
    Icon,
} from 'antd'

class ListApis extends Component {

    redirect = (path) => (e) => {
        this.props.history.push(path)
    }

    render() {
        const { apis } = this.props
        return (
            <Row className="h-row api_list" gutter={20}>
                {apis && apis.length > 0
                    ? apis.map(api => {
                        return (
                            <Col sm={24} md={8} lg={6} style={{ marginBottom: 20 }} key={api.id}>
                                <Card
                                    className="heimdall-card"
                                    title={
                                        <div style={{paddingLeft: 24, paddingRight: 24, paddingTop: 16, paddingBottom: 16, cursor: 'pointer'}}>
                                            <Link to={'/apis/' + api.id}><span>{api.name}</span></Link>
                                        </div>
                                    }
                                    actions={[
                                        <Link to={'/apis/' + api.id}><Tooltip title="View"><Icon type="search" /></Tooltip></Link>
                                    ]}
                                >
                                    <div className="heimdall-card-body" onClick={this.redirect('/apis/' + api.id)}>
                                        <span className="api_description">{api.description}</span>
                                    </div>
                                </Card>
                            </Col>
                        )
                    })
                    : null
                }
            </Row>
        )
    }
}

ListApis.propTypes = {
    apis: PropTypes.array
}

export default ListApis