// jshint esversion: 6
const { Given, When } = require("cucumber");
const axios = require('axios').default;

const { payloads } = require('../support/payloads.js');

Given(/I generate a json payload called \'(.*)\'/, function (payloadIdentifier) {
  if(payloads[payloadIdentifier]) {
    this.world.request.body = payloads[payloadIdentifier];
  } else {
    throw new Error(`the required payload ${payloadIdentifier} is not defined in features/support/payloads.js`);
  }
});


When(/I make a (\w+) to the function at \'(.*)\'/, function (httpMethod, path) {
  timeout: 100
  this.world.response = axios.request({
    url: path,
    baseURL: this.world.getFunctionBaseUri(),
    data: this.world.request.body,
    method: httpMethod,
    validateStatus: function (status) {
      return status >= 200 && status < 503; // default
    },
    headers: this.world.request.headers
  });
});

