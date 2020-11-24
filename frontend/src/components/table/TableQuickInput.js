import PropTypes from 'prop-types';
import React, { Component } from 'react';
import cx from 'classnames';
import { connect } from 'react-redux';

import { getLayout, patchRequest } from '../../api';
import { completeRequest, createInstance } from '../../actions/GenericActions';
import { allowShortcut, disableShortcut } from '../../actions/WindowActions';

import { parseToDisplay } from '../../utils/documentListHelper';

import RawWidget from '../widget/RawWidget';

class TableQuickInput extends Component {
  // promise with patching for queuing form submission after patch is done
  patchPromise;

  constructor(props) {
    super(props);

    this.state = {
      layout: null,
      data: null,
      id: null,
      editedField: 0,
      inProgress: false,
    };
  }

  componentDidMount() {
    this.initQuickInput();
  }

  componentDidUpdate() {
    const { data, layout, editedField } = this.state;

    if (data && layout) {
      for (let i = 0; i < layout.length; i++) {
        const item = layout[i].fields.map((elem) => data[elem.field] || -1);

        if (!item[0].value) {
          if (editedField !== i) {
            this.setState(
              {
                editedField: i,
              },
              () => {
                if (this.rawWidgets) {
                  let curWidget = this.rawWidgets[i];
                  if (curWidget && curWidget.focus) {
                    curWidget.focus();
                  }
                }
              }
            );
          }

          break;
        }
      }
    }
  }

  initQuickInput = () => {
    const {
      addNotification,
      docType,
      docId,
      tabId,
      closeBatchEntry,
    } = this.props;
    const { layout } = this.state;

    this.setState(
      {
        data: null,
      },
      () => {
        createInstance('window', docType, docId, tabId, 'quickInput')
          .then((instance) => {
            this.setState({
              data: parseToDisplay(instance.data.fieldsByName),
              id: instance.data.id,
              editedField: 0,
            });
          })
          .catch((err) => {
            if (err.response.status === 404) {
              addNotification(
                'Batch entry error',
                'Batch entry is not available.',
                5000,
                'error'
              );
              closeBatchEntry();
            }
          });

        !layout &&
          getLayout('window', docType, tabId, 'quickInput', docId)
            .then((layout) => {
              this.setState({
                layout: layout.data.elements,
              });
            })
            .catch((err) => {
              // eslint-disable-next-line no-console
              console.error(err);
            });
      }
    );
  };

  handleChange = (field, value) => {
    this.setState((prevState) => ({
      data: Object.assign({}, prevState.data, {
        [field]: Object.assign({}, prevState.data[field], {
          value,
        }),
      }),
    }));
  };

  handlePatch = (prop, value, callback) => {
    const { docType, docId, tabId } = this.props;
    const { id } = this.state;

    this.setState(
      {
        inProgress: true,
      },
      () => {
        this.patchPromise = new Promise((resolve) => {
          patchRequest({
            entity: 'window',
            docType,
            docId,
            tabId,
            property: prop,
            value,
            subentity: 'quickInput',
            subentityId: id,
          }).then((response) => {
            const fields = response.data[0] && response.data[0].fieldsByName;

            fields &&
              Object.keys(fields).map((fieldName) => {
                this.setState(
                  (prevState) => ({
                    data: Object.assign({}, prevState.data, {
                      [fieldName]: Object.assign(
                        {},
                        prevState.data[fieldName],
                        fields[fieldName]
                      ),
                    }),
                    inProgress: false,
                  }),
                  () => {
                    if (callback) {
                      callback();
                    }
                    resolve();
                  }
                );
              });
          });
        });
      }
    );
  };

  renderFields = (layout, data, dataId, attributeType, quickInputId) => {
    const {
      tabId,
      docType,
      forceHeight,
      modalVisible,
      timeZone,
      allowShortcut,
      disableShortcut,
    } = this.props;
    const { inProgress } = this.state;

    this.rawWidgets = [];

    const layoutFieldsAmt = layout ? layout.length : 2;
    const stylingLayout = [
      {
        formGroup: cx(`col-12`, {
          'col-lg-5': layoutFieldsAmt === 2,
          'col-xl-6': layoutFieldsAmt === 2,
          'col-lg-4': layoutFieldsAmt === 3,
          'col-xl-5': layoutFieldsAmt === 3,
        }),
        label: `col-12 col-lg-3 quickInput-label`,
        field: `col-12 col-lg-9`,
      },
      {
        formGroup: `col-12 col-lg-3 col-xl-3`,
        label: `col-12 col-sm-4 col-lg-5 col-xl-4`,
        field: `col-12 col-sm-8 col-lg-7 col-xl-8`,
      },
      {
        formGroup: `col-12 col-lg-3 col-xl-2`,
        label: `col-12 col-sm-9 col-lg-7`,
        field: `col-12 col-sm-3 col-lg-5`,
      },
    ];

    if (data && layout) {
      return layout.map((item, idx) => {
        const widgetData = item.fields.map((elem) => data[elem.field] || -1);
        const lastFormField = idx === layout.length - 1;

        return (
          <RawWidget
            ref={this.setWidgetsRef}
            fieldFormGroupClass={stylingLayout[idx].formGroup}
            fieldLabelClass={stylingLayout[idx].label}
            fieldInputClass={stylingLayout[idx].field}
            inProgress={inProgress}
            entity={attributeType}
            subentity="quickInput"
            subentityId={quickInputId}
            tabId={tabId}
            windowType={docType}
            widgetType={item.widgetType}
            fields={item.fields}
            dataId={dataId}
            widgetData={widgetData}
            gridAlign={item.gridAlign}
            forceFullWidth={widgetData.length > 1}
            forceHeight={forceHeight}
            key={idx}
            lastFormField={lastFormField}
            caption={item.caption}
            handlePatch={this.handlePatch}
            handleChange={this.handleChange}
            type="secondary"
            autoFocus={idx === 0}
            initialFocus={idx === 0}
            {...{
              modalVisible,
              timeZone,
              allowShortcut,
              disableShortcut,
            }}
          />
        );
      });
    }
  };

  onSubmit = (e) => {
    const { addNotification, docType, docId, tabId } = this.props;
    const { id, data } = this.state;
    e.preventDefault();

    document.activeElement.blur();

    if (!this.validateForm(data)) {
      return addNotification(
        'Error',
        'Mandatory fields are not filled!',
        5000,
        'error'
      );
    }

    return this.patchPromise
      .then(() => {
        return completeRequest(
          'window',
          docType,
          docId,
          tabId,
          null,
          'quickInput',
          id
        );
      })
      .then(this.initQuickInput);
  };

  validateForm = (data) => {
    return !Object.keys(data).filter(
      (key) => data[key].mandatory && !data[key].value
    ).length;
  };

  setRef = (ref) => {
    this.form = ref;
  };

  setWidgetsRef = (ref) => {
    this.rawWidgets.push(ref);
  };

  render() {
    const { docId } = this.props;
    const { data, layout, id } = this.state;

    return (
      <form
        onSubmit={this.onSubmit}
        className="row quick-input-container"
        ref={this.setRef}
      >
        {this.renderFields(layout, data, docId, 'window', id)}
        <div className="col-sm-12 col-md-3 col-lg-2 hint">
          {`(Press 'Enter' to add)`}
        </div>
        <button type="submit" className="hidden-up" />
      </form>
    );
  }
}

const mapStateToProps = (state) => {
  const { appHandler, windowHandler } = state;

  return {
    modalVisible: windowHandler.modal.visible,
    timeZone: appHandler.me.timeZone,
  };
};

TableQuickInput.propTypes = {
  addNotification: PropTypes.func.isRequired,
  closeBatchEntry: PropTypes.func,
  forceHeight: PropTypes.number,
  docType: PropTypes.any,
  docId: PropTypes.string,
  tabId: PropTypes.string,
  allowShortcut: PropTypes.func.isRequired,
  disableShortcut: PropTypes.func.isRequired,
  modalVisible: PropTypes.bool.isRequired,
  timeZone: PropTypes.string.isRequired,
};

export default connect(
  mapStateToProps,
  {
    allowShortcut,
    disableShortcut,
  }
)(TableQuickInput);

export { TableQuickInput };
