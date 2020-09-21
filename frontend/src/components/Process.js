import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { connect } from 'react-redux';

import {
  openModal,
  patch,
  updatePropertyValue,
} from '../actions/WindowActions';

import MasterWidget from './widget/MasterWidget';
import Loader from './app/Loader';

/**
 * @file Class based component.
 * @module Process
 * @extends Component
 */
class Process extends Component {
  constructor(props) {
    super(props);
  }

  /**
   * @method renderElements
   * @summary ToDo: Describe the method
   * @param {*} layout
   * @param {*} data
   * @param {*} type
   * @todo Write the documentation
   */
  renderElements = (layout, data, type) => {
    const { disabled, openModal, patch, updatePropertyValue } = this.props;
    const elements = layout.elements;

    return elements.map((elem, id) => {
      const widgetData = elem.fields.map((item) => data[item.field] || -1);
      return (
        <div key={`${id}-${layout.pinstanceId}`}>
          <MasterWidget
            entity="process"
            key={'element' + id}
            windowId={type}
            dataId={layout.pinstanceId}
            widgetData={widgetData}
            isModal={true}
            disabled={disabled}
            autoFocus={id === 0}
            openModal={openModal}
            patch={patch}
            updatePropertyValue={updatePropertyValue}
            {...elem}
          />
        </div>
      );
    });
  };

  /**
   * @method render
   * @summary ToDo: Describe the method
   * @todo Write the documentation
   */
  render() {
    const { data, layout, type, disabled } = this.props;
    return (
      <div key="window" className="window-wrapper process-wrapper">
        {disabled && <Loader loaderType="bootstrap" />}
        {!disabled &&
          layout &&
          layout.elements &&
          this.renderElements(layout, data, type)}
      </div>
    );
  }
}

/**
 * @typedef {object} Props Component props
 * @prop {bool} [disabled]
 * @prop {*} data
 * @prop {*} layout
 * @prop {*} type
 * @todo Check title, buttons. Which proptype? Required or optional?
 */
Process.propTypes = {
  disabled: PropTypes.bool,
  data: PropTypes.any,
  layout: PropTypes.any,
  type: PropTypes.any,
  updatePropertyValue: PropTypes.func.isRequired,
  openModal: PropTypes.func.isRequired,
  patch: PropTypes.func.isRequired,
};

export default connect(
  null,
  {
    openModal,
    patch,
    updatePropertyValue,
  }
)(Process);
