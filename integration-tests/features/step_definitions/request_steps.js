const { Given, When } = require("cucumber");
const axios = require('axios').default;

const { payloads } = require('../support/payloads.js');

Given(/I generate a json payload called \'(.*)\'/, function (payloadIdentifier) {
  this.world.request.body = payloads[payloadIdentifier];
});

When(/I make a (\w+) to the function at \'(.*)\'/, function (httpMethod, path) {
  this.world.response = axios.request({
    url: path,
    baseURL: this.world.getFunctionBaseUri(),
    data: this.world.request.body,
    method: httpMethod,
    validateStatus: function (status) {
      return status >= 200 && status < 503; // default
    },
  });
});

